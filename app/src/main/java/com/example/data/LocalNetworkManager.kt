package com.example.data

import android.net.wifi.WifiManager
import com.example.network.ClassroomClient
import com.example.network.ClassroomServer
import com.example.network.DiscoveryService
import com.example.network.NetworkConstants.areSectionsEqual
import com.example.network.NetworkConstants.DEFAULT_TCP_PORT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Manager Global que actúa como Facade (puerta de enlace) para la red.
 * Mantiene el estado vivo de la sesión y delega el trabajo pesado a los servicios de red.
 */
object LocalNetworkManager {
    private const val TAG = "LocalNetworkManager"

    // Estados del Servidor (Lado Profesor)
    private var tcpServerJob: Job? = null
    private var udpServerJob: Job? = null
    private var activeServerSocket: java.net.ServerSocket? = null
    private var activeUdpSocket: java.net.DatagramSocket? = null
    private val _isServerRunning = MutableStateFlow(false)
    val isServerRunning = _isServerRunning.asStateFlow()

    // Configuración compartida de la sesión
    private var currentTeacherName = ""
    private var currentSectionName = ""
    private var activeUnitIndex = 1
    private var isReadingActive = true
    private var isExamActive = false
    private var enabledReadUnits = "1"
    private var enabledExamUnits = ""
    private var teacherMessage = ""
    private var sectionTotalClasses = 0
    private var currentSessionId = ""
    private var sessionStartTime = 0L
    private var sessionCheckpointCounter = 0
    
    private var _pendingCommand: String? = null
    private var databaseRef: AppDatabase? = null
    private var appContext: android.content.Context? = null

    // Archivo compartido
    private var sharedFileUri: android.net.Uri? = null
    private var sharedFileName = ""
    private var sharedFileSize = 0L
    private var sharedFileId = ""
    private var sharedFileIsSystem = false

    // Flujo de comandos para el alumno (Push simulado)
    private val _clientCommand = MutableStateFlow<String?>(null)
    val clientCommand = _clientCommand.asStateFlow()

    fun resetClientCommand() { _clientCommand.value = null }
    fun setClientCommand(cmd: String?) { _clientCommand.value = cmd }

    // Estados de descubrimiento (Lado Alumno)
    private val _discoveredTeacherIp = MutableStateFlow<String?>(null)
    val discoveredTeacherIp = _discoveredTeacherIp.asStateFlow()

    private var multicastLock: WifiManager.MulticastLock? = null

    fun areSectionsEqual(s1: String, s2: String) = com.example.network.NetworkConstants.areSectionsEqual(s1, s2)

    fun acquireMulticastLock(wifiManager: WifiManager) {
        if (multicastLock == null) {
            multicastLock = wifiManager.createMulticastLock("AulaLocalMulticastLock").apply {
                setReferenceCounted(true)
                acquire()
            }
        }
    }

    fun releaseMulticastLock() {
        multicastLock?.let { if (it.isHeld) it.release() }
        multicastLock = null
    }

    fun updateServerConfig(teacher: String, section: String, unit: Int, reading: Boolean, exam: Boolean, reads: String, exams: String, msg: String, total: Int = 0) {
        currentTeacherName = teacher
        currentSectionName = section
        activeUnitIndex = unit
        isReadingActive = reading
        isExamActive = exam
        enabledReadUnits = reads
        enabledExamUnits = exams
        teacherMessage = msg
        sectionTotalClasses = total
    }

    fun startTeacherServer(scope: CoroutineScope, teacher: String, section: String, unit: Int, reading: Boolean, exam: Boolean, reads: String, exams: String, msg: String, total: Int, database: AppDatabase, context: android.content.Context, tcpPort: Int = DEFAULT_TCP_PORT) {
        if (_isServerRunning.value) return
        databaseRef = database
        appContext = context.applicationContext
        sessionStartTime = System.currentTimeMillis()
        sessionCheckpointCounter = 0
        currentSessionId = java.util.UUID.randomUUID().toString()
        
        // Limpieza de sesión previa
        sharedFileUri = null
        sharedFileName = ""
        sharedFileSize = 0L
        sharedFileId = ""
        sharedFileIsSystem = false
        _pendingCommand = null
        
        updateServerConfig(teacher, section, unit, reading, exam, reads, exams, msg, total)
        _isServerRunning.value = true

        // Ticker de checkpoints
        scope.launch(Dispatchers.IO) {
            while (_isServerRunning.value) {
                kotlinx.coroutines.delay(5000)
                sessionCheckpointCounter++
            }
        }

        // Iniciar UDP vía DiscoveryService
        udpServerJob = scope.launch {
            DiscoveryService.runDiscoveryResponder(
                shouldContinue = { _isServerRunning.value },
                provideOffer = {
                    JSONObject().apply {
                        put("type", "AULA_OFFER")
                        put("teacherName", currentTeacherName)
                        put("sectionName", currentSectionName)
                        put("tcpPort", tcpPort)
                    }
                },
                onSocketCreated = { activeUdpSocket = it }
            )
        }

        // Iniciar TCP vía ClassroomServer
        tcpServerJob = scope.launch {
            ClassroomServer.runTcpServer(
                port = tcpPort,
                shouldContinue = { _isServerRunning.value },
                db = database,
                appContext = appContext!!,
                sessionStartTime = sessionStartTime,
                getState = {
                    JSONObject().apply {
                        put("sessionId", currentSessionId)
                        put("teacherName", currentTeacherName)
                        put("sectionName", currentSectionName)
                        put("activeUnitIndex", activeUnitIndex)
                        put("isReadingActive", isReadingActive)
                        put("isExamActive", isExamActive)
                        put("enabledReadUnits", enabledReadUnits)
                        put("enabledExamUnits", enabledExamUnits)
                        put("teacherMessage", teacherMessage)
                        put("totalClasses", sectionTotalClasses)
                    }
                },
                getPendingCommand = { _pendingCommand },
                getSharedFile = {
                    if (sharedFileUri == null) null 
                    else JSONObject().apply {
                        put("id", sharedFileId)
                        put("name", sharedFileName)
                        put("size", sharedFileSize)
                        put("isSystemUpdate", sharedFileIsSystem)
                    }
                },
                getSharedFileUri = { sharedFileUri },
                onDoubtReceived = { cardId, message ->
                    scope.launch(Dispatchers.IO) {
                        val dao = database.classroomDao()
                        dao.getStudentByCardId(cardId)?.let {
                            val isUnlock = message.contains("desbloqueo de perfil", ignoreCase = true)
                            dao.insertSyncedStudent(
                                it.copy(
                                    hasDoubt = true,
                                    doubtMessage = message,
                                    isProfileUnlockRequested = isUnlock || it.isProfileUnlockRequested
                                )
                            )
                        }
                    }
                },
                onServerSocketCreated = { activeServerSocket = it },
                getCurrentCheckpoint = { sessionCheckpointCounter }
            )
        }
    }

    fun stopTeacherServer() {
        currentSessionId = ""
        sharedFileUri = null
        _pendingCommand = com.example.network.NetworkConstants.CMD_FORCE_DISCONNECT
        
        CoroutineScope(Dispatchers.IO).launch {
            databaseRef?.let { db ->
                try {
                    db.classroomDao().resetAllStudentsLastSeen()
                } catch (e: Exception) {
                    AppLogger.e(TAG, "Error reseteando lastSeen al detener servidor", e)
                }
            }

            // Notificar a los sockets de que cerramos (esto rompe el bloqueo de accept/receive)
            try { activeServerSocket?.close() } catch (e: Exception) {}
            try { activeUdpSocket?.close() } catch (e: Exception) {}
            
            delay(1000)
            _isServerRunning.value = false
            udpServerJob?.cancel()
            tcpServerJob?.cancel()
            udpServerJob = null
            tcpServerJob = null
            activeServerSocket = null
            activeUdpSocket = null
            databaseRef = null
            releaseMulticastLock()
        }
    }

    fun setPendingCommand(command: String?) {
        _pendingCommand = command
        if (command != null) {
            CoroutineScope(Dispatchers.IO).launch {
                kotlinx.coroutines.delay(10000)
                _pendingCommand = null
            }
        }
    }

    fun setSharedFile(uri: android.net.Uri?, name: String, size: Long, isSystem: Boolean = false) {
        sharedFileUri = uri
        sharedFileName = name
        sharedFileSize = size
        sharedFileIsSystem = isSystem
        sharedFileId = if (uri != null) java.util.UUID.randomUUID().toString() else ""
    }

    fun getSessionStartTime() = sessionStartTime
    fun getSessionCheckpoints() = sessionCheckpointCounter
    fun getSharedFileUriInternal() = sharedFileUri

    // LADO ALUMNO: Delegación a ClassroomClient y DiscoveryService
    suspend fun discoverTeacher(onSuccess: (String, Int, String, String) -> Unit, onFailure: () -> Unit) {
        DiscoveryService.discoverTeacher(onSuccess, onFailure)
    }

    suspend fun fetchClassInfo(ip: String, port: Int, onSuccess: (String, String, Int, Boolean, Boolean, String, String, String, Int, String) -> Unit, onFailure: (String) -> Unit) {
        val res = ClassroomClient.fetchClassInfo(ip, port)
        if (res != null && res.optString("status") == "ok") {
            onSuccess(
                res.getString("teacherName"), res.getString("sectionName"), res.getInt("activeUnitIndex"),
                res.optBoolean("isReadingActive", true), res.getBoolean("isExamActive"),
                res.optString("enabledReadUnits", "1"), res.optString("enabledExamUnits", ""),
                res.optString("teacherMessage", ""), res.optInt("totalClasses", 0), res.optString("sessionId", "")
            )
        } else onFailure("No se pudo obtener info de clase")
    }

    suspend fun sendCheckIn(ip: String, port: Int, cardId: String, first: String, last: String, email: String, avatar: String, section: String, onSuccess: (Int, Boolean, Boolean, String, String, String, Int, String) -> Unit, onFailure: (String) -> Unit) {
        val data = JSONObject().apply {
            put("cardId", cardId); put("firstName", first); put("lastName", last)
            put("email", email); put("avatar", avatar); put("sectionName", section)
        }
        val res = ClassroomClient.checkIn(ip, port, data)
        if (res != null && res.optString("status") == "ok") {
            onSuccess(res.getInt("activeUnitIndex"), res.optBoolean("isReadingActive", true), res.getBoolean("isExamActive"), res.optString("enabledReadUnits", "1"), res.optString("enabledExamUnits", ""), res.optString("teacherMessage", ""), res.optInt("totalClasses", 0), res.optString("sessionId", ""))
        } else onFailure(res?.optString("message") ?: "Error en registro")
    }

    suspend fun syncWithTeacher(ip: String, port: Int, cardId: String, first: String, last: String, email: String, avatar: String, section: String, read: List<Int>, challenge: List<Int>, grades: Map<Int, Float>, onSuccess: (JSONObject) -> Unit, onExpelled: () -> Unit, onFailure: () -> Unit) {
        val data = JSONObject().apply {
            put("cardId", cardId); put("firstName", first); put("lastName", last)
            put("email", email); put("avatar", avatar); put("sectionName", section)
            put("readProgress", org.json.JSONArray(read)); put("challengeProgress", org.json.JSONArray(challenge))
            put("examGrades", JSONObject().apply { grades.forEach { (k, v) -> put(k.toString(), v.toDouble()) } })
        }
        val res = ClassroomClient.sync(ip, port, data)
        if (res == null) { onFailure(); return }
        if (res.optString("status") == "expelled") { onExpelled(); return }
        if (res.optString("status") == "ok") {
            if (res.has("command")) _clientCommand.value = res.getString("command")
            onSuccess(res)
        } else onFailure()
    }

    suspend fun fetchFileChunk(ip: String, port: Int, fileId: String, offset: Long, length: Int, cardId: String) : String? {
        val res = ClassroomClient.fetchChunk(ip, port, fileId, offset, length, cardId)
        return if (res != null && res.optString("status") == "ok") res.getString("data") else null
    }

    suspend fun sendCheckOut(ip: String, port: Int, cardId: String) {
        ClassroomClient.checkOut(ip, port, cardId)
    }

    suspend fun sendDoubt(ip: String, port: Int, cardId: String, message: String) {
        ClassroomClient.sync(ip, port, JSONObject().apply { put("action", com.example.network.NetworkConstants.ACTION_SEND_DOUBT); put("cardId", cardId); put("message", message) })
    }
}

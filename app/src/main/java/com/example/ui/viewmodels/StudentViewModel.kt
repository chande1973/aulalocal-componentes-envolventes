package com.example.ui.viewmodels

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import com.example.util.ZipUtils
import com.example.R

class StudentViewModel(application: Application) : AndroidViewModel(application) {

    private val db by lazy { AppDatabase.getDatabase(application) }
    private val dao by lazy { db.classroomDao() }

    private val _detectedTeacherIp = MutableStateFlow<String?>(null)
    val detectedTeacherIp = _detectedTeacherIp.asStateFlow()

    private val _detectedSectionName = MutableStateFlow<String?>(null)
    val detectedSectionName = _detectedSectionName.asStateFlow()

    private val _manualServerIp = MutableStateFlow("192.168.43.1")
    val manualServerIp = _manualServerIp.asStateFlow()

    private val _manualServerPort = MutableStateFlow("9999")
    val manualServerPort = _manualServerPort.asStateFlow()

    private val _activeUnitIndexStudent = MutableStateFlow(1)
    val activeUnitIndexStudent = _activeUnitIndexStudent.asStateFlow()

    private val _isExamActiveStudent = MutableStateFlow(false)
    val isExamActiveStudent = _isExamActiveStudent.asStateFlow()

    private val _isDiscovering = MutableStateFlow(false)
    val isDiscovering = _isDiscovering.asStateFlow()

    private val _enabledReadUnits = MutableStateFlow("1")
    val enabledReadUnits = _enabledReadUnits.asStateFlow()

    private val _enabledExamUnits = MutableStateFlow("")
    val enabledExamUnits = _enabledExamUnits.asStateFlow()

    private val _teacherMessage = MutableStateFlow("")
    val teacherMessageState = _teacherMessage.asStateFlow()

    private val _totalClassesFromTeacher = MutableStateFlow(0)
    val totalClassesFromTeacher = _totalClassesFromTeacher.asStateFlow()

    private val _pendingUnitUpdate = MutableStateFlow<Int?>(null)
    val pendingUnitUpdate = _pendingUnitUpdate.asStateFlow()

    private val _pendingExamUpdate = MutableStateFlow<Boolean?>(null)
    val pendingExamUpdate = _pendingExamUpdate.asStateFlow()

    private val _incomingPrivateMessage = MutableStateFlow<String?>(null)
    val incomingPrivateMessage = _incomingPrivateMessage.asStateFlow()

    private val _isProfileEditUnlocked = MutableStateFlow(false)
    val isProfileEditUnlocked = _isProfileEditUnlocked.asStateFlow()

    private val _classEndedNotice = MutableStateFlow<String?>(null)
    val classEndedNotice = _classEndedNotice.asStateFlow()

    fun dismissClassEndedNotice() {
        _classEndedNotice.value = null
    }

    fun unlockProfileEdit() {
        _isProfileEditUnlocked.value = true
    }

    fun lockProfileEdit() {
        _isProfileEditUnlocked.value = false
    }

    private val _incomingFile = MutableStateFlow<JSONObject?>(null)
    val incomingFile = _incomingFile.asStateFlow()

    private val _isListenerMode = MutableStateFlow(false)
    val isListenerMode = _isListenerMode.asStateFlow()
    
    private var lastSessionId: String = ""

    private var lastProcessedFileId: String = ""

    private var clientSyncJob: Job? = null
    private var downloadJob: Job? = null

    private val _uiEvents = MutableSharedFlow<String>()
    val uiEvents = _uiEvents.asSharedFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(0L)
    val lastSyncTime = _lastSyncTime.asStateFlow()

    // El modo estudio independiente está activo si NO estamos bloqueados por un profesor
    val isStudyModeActive: StateFlow<Boolean> by lazy {
        dao.getUserProfileFlow().map { it?.isLocked == false }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    }

    val inboxMessages: StateFlow<List<InboxMessage>> by lazy {
        dao.getInboxMessagesFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    init {
        // Auto-resume sync loop if student is already configured
        viewModelScope.launch(Dispatchers.IO) {
            dao.getUserProfileFlow().collect { profile ->
                if (profile != null && profile.role == "student" && profile.isLocked && profile.registeredTeacherIp.isNotEmpty() && clientSyncJob == null) {
                    startStudentSyncLoop(
                        ip = profile.registeredTeacherIp,
                        port = profile.registeredTeacherPort,
                        cardId = profile.cardId,
                        firstName = profile.firstName,
                        lastName = profile.lastName,
                        email = profile.email,
                        avatar = profile.avatar,
                        section = profile.activeSection
                    )
                }
            }
        }

        // Listen for network commands (teacher -> student push)
        viewModelScope.launch {
            LocalNetworkManager.clientCommand.collect { cmd ->
                if (cmd == null) return@collect
                
                when (cmd) {
                    "attendance_check" -> _uiEvents.emit(getApplication<Application>().getString(R.string.msg_attendance_registered))
                    "FORCE_DISCONNECT" -> {
                        _uiEvents.emit(getApplication<Application>().getString(R.string.msg_class_ended))
                        unlockAndDisconnectStudent()
                    }
                }
                // Limpiar el comando para no procesarlo dos veces
                LocalNetworkManager.resetClientCommand()
            }
        }
    }

    fun updateManualServerIp(ip: String) { _manualServerIp.value = ip }
    fun updateManualServerPort(port: String) { _manualServerPort.value = port }

    fun discoverActiveTeacher() {
        if (_isDiscovering.value) return
        _isDiscovering.value = true
        _detectedTeacherIp.value = null
        _detectedSectionName.value = null

        viewModelScope.launch {
            try {
                val wifiManager = getApplication<Application>().getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager
                LocalNetworkManager.acquireMulticastLock(wifiManager)
                
                LocalNetworkManager.discoverTeacher(
                    onSuccess = { ip, port, teacherName, sectionName ->
                        _detectedTeacherIp.value = ip
                        _manualServerIp.value = ip
                        _manualServerPort.value = port.toString()
                        _detectedSectionName.value = sectionName
                        _isDiscovering.value = false
                        viewModelScope.launch { _uiEvents.emit(getApplication<Application>().getString(R.string.msg_section_detected)) }
                    },
                    onFailure = {
                        _isDiscovering.value = false
                        viewModelScope.launch { _uiEvents.emit(getApplication<Application>().getString(R.string.msg_no_teacher_found)) }
                    }
                )
            } catch (e: Exception) {
                AppLogger.e("StudentViewModel", "Error durante el descubrimiento", e)
                _isDiscovering.value = false
                _uiEvents.emit("Error técnico al buscar: ${e.localizedMessage}")
            }
        }
    }

    fun connectManuallyToClass(ip: String, portStr: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val port = portStr.toIntOrNull() ?: 9999
        _isDiscovering.value = true
        viewModelScope.launch {
            LocalNetworkManager.fetchClassInfo(
                ip = ip,
                port = port,
                onSuccess = { _, section, activeUnit, readingActive, examActive, enabledReads, enabledExams, message, totalClasses, sessionId ->
                    _detectedTeacherIp.value = ip
                    _manualServerIp.value = ip
                    _manualServerPort.value = port.toString()
                    _detectedSectionName.value = section
                    _activeUnitIndexStudent.value = activeUnit
                    _isExamActiveStudent.value = examActive
                    _enabledReadUnits.value = enabledReads
                    _enabledExamUnits.value = enabledExams
                    _teacherMessage.value = message
                    _totalClassesFromTeacher.value = totalClasses
                    _isDiscovering.value = false
                    lastSessionId = sessionId
                    onSuccess()
                },
                onFailure = { error ->
                    _isDiscovering.value = false
                    onFailure(error)
                }
            )
        }
    }

    fun unlockAndDisconnectStudent() {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = dao.getUserProfile()
            if (profile != null && profile.registeredTeacherIp.isNotEmpty()) {
                // Notificar al profesor de la salida voluntaria (con tiempo de espera muy corto)
                try {
                    kotlinx.coroutines.withTimeout(1500) {
                        LocalNetworkManager.sendCheckOut(
                            profile.registeredTeacherIp,
                            profile.registeredTeacherPort,
                            profile.cardId
                        )
                    }
                } catch (e: Exception) {
                    // Si el profesor ya no está, ignoramos el error y seguimos con la desconexión local
                }
                
                val updated = profile.copy(
                    activeSection = profile.activeSection, // Mantener su sección oficial
                    registeredTeacherIp = "",
                    registeredTeacherPort = 9999,
                    isLocked = false
                )
                dao.insertProfile(updated)
            }
            
            withContext(Dispatchers.Main) {
                clientSyncJob?.cancel()
                clientSyncJob = null
                _detectedTeacherIp.value = null
                _detectedSectionName.value = null
                _activeUnitIndexStudent.value = 1
                _isExamActiveStudent.value = false
                _incomingFile.value = null
                lastProcessedFileId = ""
                lastSessionId = ""
                _isSyncing.value = false
            }
        }
    }

    fun performStudentCheckIn(onComplete: () -> Unit) {
        val ip = _detectedTeacherIp.value
        val section = _detectedSectionName.value
        val port = _manualServerPort.value.toIntOrNull() ?: 9999
        if (ip == null || section == null) return

        viewModelScope.launch {
            val profile = withContext(Dispatchers.IO) { dao.getUserProfile() } ?: return@launch
            
            // Si el perfil no tiene sección (legacy), usamos la detectada del profesor para completarlo la primera vez
            val officialSection = profile.activeSection.ifEmpty { section } 

            LocalNetworkManager.sendCheckIn(
                ip = ip,
                port = port,
                cardId = profile.cardId,
                first = profile.firstName,
                last = profile.lastName,
                email = profile.email,
                avatar = profile.avatar,
                section = officialSection, 
                onSuccess = { activeUnit, readingActive, examActive, enabledReads, enabledExams, message, totalClasses, sessionId ->
                    _activeUnitIndexStudent.value = activeUnit
                    _isExamActiveStudent.value = examActive
                    _enabledReadUnits.value = enabledReads
                    _enabledExamUnits.value = enabledExams
                    _teacherMessage.value = message
                    _totalClassesFromTeacher.value = totalClasses
                    lastSessionId = sessionId
                    
                    viewModelScope.launch(Dispatchers.IO) {
                        // Guardar la sección si estaba vacía en el perfil
                        dao.insertProfile(profile.copy(
                            activeSection = officialSection,
                            registeredTeacherIp = ip,
                            registeredTeacherPort = port,
                            isLocked = true
                        ))
                    }
                    startStudentSyncLoop(ip, port, profile.cardId, profile.firstName, profile.lastName, profile.email, profile.avatar, officialSection)
                    onComplete()
                },
                onFailure = { error ->
                    viewModelScope.launch { _uiEvents.emit("Error: $error") }
                }
            )
        }
    }

    fun startStudentSyncLoop(ip: String, port: Int, cardId: String, firstName: String, lastName: String, email: String, avatar: String, section: String) {
        clientSyncJob?.cancel()
        clientSyncJob = viewModelScope.launch(Dispatchers.IO) {
            var consecutiveFailures = 0
            while (true) {
                try {
                    val progressList = dao.getAllProgress()
                    val readProgress = progressList.filter { it.isRead }.map { it.unitIndex }
                    val challengeProgress = progressList.filter { it.isChallengeDone }.map { it.unitIndex }
                    val examGrades = mutableMapOf<Int, Float>()
                    progressList.forEach { if (it.examCompleted && it.examGrade != null) examGrades[it.unitIndex] = it.examGrade }

                    LocalNetworkManager.syncWithTeacher(
                        ip = ip,
                        port = port,
                        cardId = cardId,
                        first = firstName,
                        last = lastName,
                        email = email,
                        avatar = avatar,
                        section = section,
                        read = readProgress,
                        challenge = challengeProgress,
                        grades = examGrades,
                        onSuccess = { activeUnit, readingActive, examActive, enabledReads, enabledExams, message, totalClasses, personalAttendance, privateMsg, pendingFile, privateFile, isListener, sessionId ->
                            consecutiveFailures = 0
                            _isSyncing.value = true
                            _lastSyncTime.value = System.currentTimeMillis()
                            
                            val oldReads = _enabledReadUnits.value
                            val oldExams = _enabledExamUnits.value

                            _enabledReadUnits.value = enabledReads
                            _enabledExamUnits.value = enabledExams
                            _totalClassesFromTeacher.value = totalClasses
                            _isListenerMode.value = isListener

                            // NOTIFICAR SI SE HABILITÓ ALGO NUEVO
                            viewModelScope.launch(Dispatchers.Main) {
                                if (oldReads.isNotEmpty() && enabledReads != oldReads) {
                                    val newlyAdded = enabledReads.split(",").filter { it.isNotEmpty() && !oldReads.split(",").contains(it) }
                                    if (newlyAdded.isNotEmpty()) {
                                        _uiEvents.emit(getApplication<Application>().getString(R.string.msg_unit_enabled))
                                    }
                                }
                                if (oldExams != enabledExams && enabledExams.isNotEmpty()) {
                                    val newlyAddedExams = enabledExams.split(",").filter { it.isNotEmpty() && !oldExams.split(",").contains(it) }
                                    if (newlyAddedExams.isNotEmpty()) {
                                        _uiEvents.emit(getApplication<Application>().getString(R.string.msg_exam_enabled))
                                    }
                                }
                            }

                            // DETECTAR CAMBIO DE SESIÓN (NUEVA CLASE)
                            if (lastSessionId.isNotEmpty() && sessionId != lastSessionId) {
                                viewModelScope.launch(Dispatchers.Main) {
                                    _uiEvents.emit(getApplication<Application>().getString(R.string.msg_new_class_cleaning))
                                    // Resetear estados temporales
                                    _incomingFile.value = null
                                    lastProcessedFileId = ""
                                    _teacherMessage.value = ""
                                    _incomingPrivateMessage.value = null
                                }
                            }
                            lastSessionId = sessionId
                            
                            // Guardar asistencia personal en el perfil local
                            viewModelScope.launch(Dispatchers.IO) {
                                val currentProfile = dao.getUserProfile()
                                if (currentProfile != null && currentProfile.attendanceCount != personalAttendance) {
                                    dao.insertProfile(currentProfile.copy(attendanceCount = personalAttendance))
                                }
                            }

                            if (_teacherMessage.value != message && message.isNotEmpty()) {
                                _teacherMessage.value = message
                            }

                            if (privateMsg != null && _incomingPrivateMessage.value != privateMsg) {
                                _incomingPrivateMessage.value = privateMsg
                                if (privateMsg.contains("AUTORIZACION_PERFIL_DESBLOQUEADO", ignoreCase = true)) {
                                    _isProfileEditUnlocked.value = true
                                }
                                viewModelScope.launch(Dispatchers.IO) {
                                    dao.insertInboxMessage(InboxMessage(sender = "Profesor", content = privateMsg))
                                    if (privateMsg.contains("Recuperativo Habilitado", ignoreCase = true)) {
                                        val unitNum = privateMsg.substringAfter("Unidad ", "").takeWhile { it.isDigit() }.toIntOrNull()
                                        if (unitNum != null) {
                                            dao.resetExamProgress(unitNum)
                                        }
                                    }
                                }
                            }

                            // Procesar archivos entrantes (Prioridad al Privado sobre el Grupal/ZIP)
                            val fileToProcess = privateFile ?: pendingFile
                            
                            if (fileToProcess != null) {
                                val newId = fileToProcess.optString("id")
                                if (newId != lastProcessedFileId && _incomingFile.value?.optString("id") != newId) {
                                    val isSystem = fileToProcess.optBoolean("isSystemUpdate", false)
                                    val isPrivate = privateFile != null
                                    
                                    _incomingFile.value = fileToProcess
                                    
                                    viewModelScope.launch(Dispatchers.IO) {
                                        dao.insertInboxMessage(InboxMessage(
                                            sender = if (isPrivate) "Profesor (Privado)" else "Profesor", 
                                            content = if (isSystem) "Actualización de Curso Completa" else "Archivo compartido: ${fileToProcess.optString("name")}",
                                            type = "FILE",
                                            fileName = fileToProcess.optString("name"),
                                            fileSize = fileToProcess.optLong("size"),
                                            fileId = newId
                                        ))
                                        
                                        val notifyMsg = when {
                                            isPrivate -> getApplication<Application>().getString(R.string.msg_private_file_received)
                                            isSystem -> getApplication<Application>().getString(R.string.msg_course_update_available)
                                            else -> getApplication<Application>().getString(R.string.msg_new_material_available)
                                        }
                                        _uiEvents.emit(notifyMsg)
                                    }
                                }
                            } else {
                                // El profesor dejó de compartir o ya no hay archivos pendientes
                                // No limpiamos _incomingFile.value aquí para permitir que el alumno decida en el diálogo
                            }

                            // RESTAURADO: Disparador de diálogo de avance de unidad
                            if (_activeUnitIndexStudent.value != activeUnit) {
                                _pendingUnitUpdate.value = activeUnit
                            }

                            if (_isExamActiveStudent.value != examActive) {
                                if (examActive) _pendingExamUpdate.value = true else _isExamActiveStudent.value = false
                            }
                        },
                        onExpelled = { 
                            unlockAndDisconnectStudent()
                            _classEndedNotice.value = getApplication<Application>().getString(R.string.msg_class_ended)
                            viewModelScope.launch { _uiEvents.emit(getApplication<Application>().getString(R.string.msg_class_ended)) }
                        },
                        onFailure = { 
                            consecutiveFailures++
                            _isSyncing.value = false
                            
                            // Si fallamos por más de 30 segundos (5-6 intentos), asumimos que la clase terminó
                            if (consecutiveFailures >= 6) {
                                unlockAndDisconnectStudent()
                                viewModelScope.launch { _uiEvents.emit(getApplication<Application>().getString(R.string.msg_connection_lost)) }
                            } else if (consecutiveFailures >= 2) {
                                // Intentar redescubrir más rápido si hay fallos
                                viewModelScope.launch {
                                    LocalNetworkManager.discoverTeacher(
                                        onSuccess = { newIp, newPort, _, _ ->
                                            if (newIp != ip) {
                                                viewModelScope.launch(Dispatchers.IO) {
                                                    val currentProfile = dao.getUserProfile()
                                                    if (currentProfile != null) {
                                                        dao.insertProfile(currentProfile.copy(registeredTeacherIp = newIp, registeredTeacherPort = newPort))
                                                    }
                                                }
                                                startStudentSyncLoop(newIp, newPort, cardId, firstName, lastName, email, avatar, section)
                                            }
                                        },
                                        onFailure = { }
                                    )
                                }
                            }
                        }
                    )
                } catch (e: Exception) { }
                
                // Si estamos desconectados, bajamos la frecuencia de reintentos para no gastar batería
                val waitTime = if (_isSyncing.value) 3000L else 7000L
                delay(waitTime)
            }
        }
    }

    fun sendDoubtToTeacher(message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = dao.getUserProfile() ?: return@launch
            if (profile.registeredTeacherIp.isEmpty()) return@launch
            LocalNetworkManager.sendDoubt(
                ip = profile.registeredTeacherIp,
                port = profile.registeredTeacherPort,
                cardId = profile.cardId,
                message = message
            )
            withContext(Dispatchers.Main) {
                _uiEvents.emit("Duda enviada al profesor")
            }
        }
    }

    fun markPresenceInClass() {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = dao.getUserProfile() ?: return@launch
            if (profile.registeredTeacherIp.isEmpty()) return@launch
            
            // Reutilizamos el mecanismo de sincronización para mandar una acción especial de presencia
            val ip = profile.registeredTeacherIp
            val port = profile.registeredTeacherPort
            
            // Simplemente disparamos un Check-in rápido o una señal de duda especial
            // Pero lo más limpio es añadir ACTION_MARK_PRESENCE en NetworkConstants y usarlo aquí.
            // Por ahora, para no romper compatibilidad, mandamos un mensaje interno al profesor:
            LocalNetworkManager.sendDoubt(
                ip = ip,
                port = port,
                cardId = profile.cardId,
                message = "SISTEMA: El alumno ha marcado presencia manualmente."
            )
            
            withContext(Dispatchers.Main) {
                _uiEvents.emit("¡Asistencia confirmada para esta clase!")
            }
        }
    }

    fun markUnitAsRead(unitIndex: Int) {
        viewModelScope.launch(Dispatchers.IO) { 
            val existing = dao.getProgressForUnit(unitIndex)
            if (existing == null) {
                dao.insertUnitProgress(UnitProgress(unitIndex = unitIndex, isRead = true))
            } else {
                dao.updateReadProgress(unitIndex, true)
            }
        }
    }

    fun markChallengeAsDone(unitIndex: Int) {
        viewModelScope.launch(Dispatchers.IO) { dao.updateChallengeProgress(unitIndex, true) }
    }

    fun saveExamGrade(unitIndex: Int, grade: Float) {
        viewModelScope.launch(Dispatchers.IO) { 
            // Primero aseguramos que la fila existe, luego actualizamos
            val existing = dao.getProgressForUnit(unitIndex)
            if (existing == null) {
                dao.insertUnitProgress(UnitProgress(unitIndex = unitIndex, examGrade = grade, examCompleted = true))
            } else {
                dao.updateExamProgress(unitIndex, grade)
            }
        }
    }

    fun acceptUnitUpdate() {
        _pendingUnitUpdate.value?.let { _activeUnitIndexStudent.value = it; _pendingUnitUpdate.value = null }
    }

    fun dismissUnitUpdate() { _pendingUnitUpdate.value = null }

    fun acceptExamUpdate() { _isExamActiveStudent.value = true; _pendingExamUpdate.value = null }

    fun dismissExamUpdate() { _pendingExamUpdate.value = null }

    fun dismissPrivateMessage() { _incomingPrivateMessage.value = null }

    fun dismissIncomingFile() { 
        _incomingFile.value?.let { lastProcessedFileId = it.optString("id") }
        _incomingFile.value = null 
        cancelDownload()
    }

    fun cancelDownload() {
        downloadJob?.cancel()
        downloadJob = null
    }

    fun downloadSharedFile(fileId: String, fileName: String, fileSize: Long, onProgress: (Float) -> Unit, onComplete: (java.io.File?) -> Unit) {
        cancelDownload()
        downloadJob = viewModelScope.launch(Dispatchers.IO) {
            val profile = dao.getUserProfile() ?: return@launch
            val ip = profile.registeredTeacherIp
            val port = profile.registeredTeacherPort
            if (ip.isEmpty()) return@launch

            val isCourseUpdate = fileName == "course_update.zip"

            try {
                val context = getApplication<Application>()
                val resolver = context.contentResolver
                var savedFileUri: Uri? = null
                
                if (isCourseUpdate) {
                    val tempFile = File(context.cacheDir, "temp_update.zip")
                    FileOutputStream(tempFile).use { output ->
                        var offset = 0L
                        val chunkSize = 131072 // 128 KB
                        var lastReportedProgress = 0f
                        while (offset < fileSize) {
                            if (downloadJob?.isActive != true) break
                            val length = if (fileSize - offset < chunkSize) (fileSize - offset).toInt() else chunkSize
                            val chunkBase64 = LocalNetworkManager.fetchFileChunk(ip, port, fileId, offset, length, profile.cardId)
                            if (chunkBase64 != null) {
                                val bytes = android.util.Base64.decode(chunkBase64, android.util.Base64.DEFAULT)
                                output.write(bytes)
                                offset += bytes.size
                                val currentProgress = offset.toFloat() / fileSize
                                if (currentProgress - lastReportedProgress >= 0.01f || offset == fileSize) {
                                    lastReportedProgress = currentProgress
                                    withContext(Dispatchers.Main) { onProgress(currentProgress) }
                                }
                            } else throw Exception("Error en chunk")
                        }
                    }
                    if (downloadJob?.isActive != true) throw Exception("Cancelado")
                    savedFileUri = Uri.fromFile(tempFile)
                    processCourseUpdate(tempFile)
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val values = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                        put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
                        put(MediaStore.Downloads.IS_PENDING, 1)
                        put(MediaStore.Downloads.RELATIVE_PATH, "Download/AulaLocal")
                    }
                    val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    val uri = resolver.insert(collection, values)
                    
                    if (uri != null) {
                        resolver.openOutputStream(uri)?.use { output ->
                            var offset = 0L
                            val chunkSize = 131072 // 128 KB
                            var lastReportedProgress = 0f
                            while (offset < fileSize) {
                                if (downloadJob?.isActive != true) break
                                val length = if (fileSize - offset < chunkSize) (fileSize - offset).toInt() else chunkSize
                                val chunkBase64 = LocalNetworkManager.fetchFileChunk(ip, port, fileId, offset, length, profile.cardId)
                                if (chunkBase64 != null) {
                                    val bytes = android.util.Base64.decode(chunkBase64, android.util.Base64.DEFAULT)
                                    output.write(bytes)
                                    offset += bytes.size
                                    val currentProgress = offset.toFloat() / fileSize
                                    if (currentProgress - lastReportedProgress >= 0.01f || offset == fileSize) {
                                        lastReportedProgress = currentProgress
                                        withContext(Dispatchers.Main) { onProgress(currentProgress) }
                                    }
                                } else throw Exception("Error en chunk")
                            }
                        }
                        if (downloadJob?.isActive != true) {
                            resolver.delete(uri, null, null)
                            throw Exception("Cancelado")
                        }
                        values.clear()
                        values.put(MediaStore.Downloads.IS_PENDING, 0)
                        resolver.update(uri, values, null, null)
                        savedFileUri = uri
                    }
                } else {
                    val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                    val appDir = java.io.File(downloadsDir, "AulaLocal")
                    if (!appDir.exists()) appDir.mkdirs()
                    
                    val targetFile = java.io.File(appDir, fileName)
                    if (targetFile.exists()) targetFile.delete()
                    
                    java.io.FileOutputStream(targetFile).use { output ->
                        var offset = 0L
                        val chunkSize = 131072 // 128 KB
                        var lastReportedProgress = 0f
                        while (offset < fileSize) {
                            if (downloadJob?.isActive != true) break
                            val length = if (fileSize - offset < chunkSize) (fileSize - offset).toInt() else chunkSize
                            val chunkBase64 = LocalNetworkManager.fetchFileChunk(ip, port, fileId, offset, length, profile.cardId)
                            if (chunkBase64 != null) {
                                val bytes = android.util.Base64.decode(chunkBase64, android.util.Base64.DEFAULT)
                                output.write(bytes)
                                offset += bytes.size
                                val currentProgress = offset.toFloat() / fileSize
                                if (currentProgress - lastReportedProgress >= 0.01f || offset == fileSize) {
                                    lastReportedProgress = currentProgress
                                    withContext(Dispatchers.Main) { onProgress(currentProgress) }
                                }
                            } else throw Exception("Error en chunk")
                        }
                    }
                    if (downloadJob?.isActive != true) {
                        if (targetFile.exists()) targetFile.delete()
                        throw Exception("Cancelado")
                    }
                    savedFileUri = Uri.fromFile(targetFile)
                }

                if (savedFileUri != null) {
                    dao.updateMessageLocalUri(fileId, savedFileUri.toString())
                    withContext(Dispatchers.Main) { 
                        lastProcessedFileId = fileId
                        onComplete(java.io.File(fileName)) 
                    }
                } else {
                    throw Exception("No se pudo guardar el archivo")
                }
            } catch (e: Exception) {
                if (e.message != "Cancelado") {
                    AppLogger.e("StudentViewModel", "Download failed", e)
                }
                withContext(Dispatchers.Main) { onComplete(null) }
            }
        }
    }

    fun openFile(localUriString: String) {
        val context = getApplication<Application>()
        try {
            val uri = Uri.parse(localUriString)
            val extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(localUriString) ?: ""
            val mimeType = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase()) ?: "*/*"

            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            if (uri.scheme == "file") {
                val file = java.io.File(uri.path ?: "")
                val contentUri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                intent.setDataAndType(contentUri, mimeType)
            }
            
            context.startActivity(android.content.Intent.createChooser(intent, "Abrir archivo con...").apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            AppLogger.e("StudentViewModel", "Error al abrir archivo: ${e.localizedMessage}")
            viewModelScope.launch { _uiEvents.emit("No se pudo abrir el archivo") }
        }
    }

    private fun processCourseUpdate(zipFile: File) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val internalCourseDir = File(context.filesDir, "course_data")
            
            _uiEvents.emit("Actualizando materiales del curso...")
            
            val result = ZipUtils.unzip(zipFile, internalCourseDir)
            
            if (result is ZipUtils.UnzipResult.Success) {
                // Update profile to point to this internal folder
                val profile = dao.getUserProfile()
                if (profile != null) {
                    val uri = Uri.fromFile(internalCourseDir)
                    dao.insertProfile(profile.copy(materialsFolderUri = uri.toString()))
                    _uiEvents.emit("¡Curso actualizado con éxito!")
                }
            } else if (result is ZipUtils.UnzipResult.Error) {
                _uiEvents.emit("Error de actualización: ${result.message}")
            }
            
            // Delete the zip file after processing
            if (zipFile.exists()) zipFile.delete()
        }
    }

    fun clearBuzon() {
        viewModelScope.launch(Dispatchers.IO) { dao.clearInbox() }
    }

    fun setOfflineMode(enabled: Boolean) {
        // No longer needed as it's derived from isLocked
    }

    override fun onCleared() {
        clientSyncJob?.cancel()
        super.onCleared()
    }
}

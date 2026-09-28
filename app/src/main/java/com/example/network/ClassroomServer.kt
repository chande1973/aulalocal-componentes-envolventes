package com.example.network

import android.content.Context
import com.example.data.AppDatabase
import com.example.data.AppLogger
import com.example.data.SyncedStudent
import com.example.network.NetworkConstants.ACTION_CHECK_IN
import com.example.network.NetworkConstants.ACTION_CHECK_OUT
import com.example.network.NetworkConstants.ACTION_GET_CLASS_INFO
import com.example.network.NetworkConstants.ACTION_GET_FILE_CHUNK
import com.example.network.NetworkConstants.ACTION_SEND_DOUBT
import com.example.network.NetworkConstants.ACTION_SYNC
import com.example.network.NetworkConstants.CMD_ATTENDANCE_CHECK
import com.example.network.NetworkConstants.STATUS_EOF
import com.example.network.NetworkConstants.STATUS_ERROR
import com.example.network.NetworkConstants.STATUS_EXPELLED
import com.example.network.NetworkConstants.STATUS_NOT_FOUND
import com.example.network.NetworkConstants.STATUS_OK
import com.example.network.NetworkConstants.areSectionsEqual
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

object ClassroomServer {
    private const val TAG = "ClassroomServer"

    /**
     * Servidor TCP Principal para sincronización de clase (Profesor)
     */
    suspend fun runTcpServer(
        port: Int,
        shouldContinue: () -> Boolean,
        db: AppDatabase,
        appContext: Context,
        sessionStartTime: Long,
        getState: () -> JSONObject,
        getPendingCommand: () -> String?,
        getSharedFile: () -> JSONObject?,
        getSharedFileUri: () -> android.net.Uri?,
        onDoubtReceived: (cardId: String, message: String) -> Unit,
        onServerSocketCreated: (ServerSocket) -> Unit,
        getCurrentCheckpoint: () -> Int
    ) = withContext(Dispatchers.IO) {
        var serverSocket: ServerSocket? = null
        try {
            serverSocket = ServerSocket().apply {
                reuseAddress = true
                bind(java.net.InetSocketAddress(port))
            }
            onServerSocketCreated(serverSocket)
            AppLogger.d(TAG, "TCP Server started on port $port")

            while (shouldContinue()) {
                val clientSocket = try {
                    val s = serverSocket.accept()
                    AppLogger.d(TAG, "Client connected: ${s.inetAddress.hostAddress}")
                    s.apply {
                        tcpNoDelay = true
                        keepAlive = true
                        soTimeout = 30000
                    }
                } catch (e: Exception) { break }

                launch {
                    handleClient(clientSocket, db, appContext, sessionStartTime, getState, getPendingCommand, getSharedFile, getSharedFileUri, onDoubtReceived, getCurrentCheckpoint)
                }
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "TCP Server failed", e)
        } finally {
            serverSocket?.close()
        }
    }

    private suspend fun handleClient(
        socket: Socket,
        db: AppDatabase,
        appContext: Context,
        sessionStartTime: Long,
        getState: () -> JSONObject,
        getPendingCommand: () -> String?,
        getSharedFile: () -> JSONObject?,
        getSharedFileUri: () -> android.net.Uri?,
        onDoubt: (String, String) -> Unit,
        getCurrentCheckpoint: () -> Int
    ) = withContext(Dispatchers.IO) {
        socket.use { s ->
            val reader = BufferedReader(InputStreamReader(s.getInputStream()))
            val writer = PrintWriter(s.getOutputStream(), true)
            
            val requestLine = reader.readLine() ?: return@use
            val request = try { JSONObject(requestLine) } catch (e: Exception) { return@use }
            val action = request.optString("action")
            val response = JSONObject()
            val dao = db.classroomDao()

            when (action) {
                ACTION_GET_CLASS_INFO -> {
                    response.put("status", STATUS_OK)
                    val state = getState()
                    state.keys().forEach { response.put(it, state.get(it)) }
                }

                ACTION_CHECK_IN -> {
                    val cardId = request.optString("cardId")
                    val firstName = request.optString("firstName")
                    val sectionName = request.optString("sectionName")
                    
                    if (cardId.isEmpty() || firstName.isEmpty()) {
                        response.put("status", STATUS_ERROR)
                        response.put("message", "Datos incompletos")
                    } else {
                        val existing = dao.getStudentByCardId(cardId)
                        val student = SyncedStudent(
                            cardId = cardId,
                            firstName = firstName,
                            lastName = request.optString("lastName"),
                            email = request.optString("email"),
                            avatar = request.optString("avatar", "👤"),
                            sectionName = existing?.sectionName ?: sectionName,
                            lastSeen = System.currentTimeMillis(),
                            firstSeenInSession = if (existing == null || existing.lastSeen < sessionStartTime) System.currentTimeMillis() else existing.firstSeenInSession,
                            attendanceCount = existing?.attendanceCount ?: 0,
                            checkpointCount = 0,
                            lastCheckpointSeen = -1
                        )
                        dao.insertSyncedStudent(student)
                        response.put("status", STATUS_OK)
                        
                        val state = getState()
                        state.keys().forEach { response.put(it, state.get(it)) }
                        
                        val isOyente = !areSectionsEqual(student.sectionName, getState().optString("sectionName"))
                        response.put("isListenerMode", isOyente)
                        response.put("personalAttendance", student.attendanceCount)
                    }
                }

                ACTION_SYNC -> {
                    val cardId = request.optString("cardId")
                    val existing = dao.getStudentByCardId(cardId)
                    if (existing != null) {
                        val currentServerSection = getState().optString("sectionName")
                        
                        // Lógica de asistencia (Checkpoints)
                        val currentCP = getCurrentCheckpoint()
                        var cpCount = existing.checkpointCount
                        var lastCP = existing.lastCheckpointSeen
                        
                        if (currentCP > lastCP) {
                            cpCount++
                            lastCP = currentCP
                        }

                        val updated = existing.copy(
                            firstName = request.optString("firstName", existing.firstName),
                            lastName = request.optString("lastName", existing.lastName),
                            email = request.optString("email", existing.email),
                            avatar = request.optString("avatar", existing.avatar),
                            sectionName = request.optString("sectionName", existing.sectionName),
                            lastSeen = System.currentTimeMillis(),
                            checkpointCount = cpCount,
                            lastCheckpointSeen = lastCP,
                            readProgressString = request.optJSONArray("readProgress")?.toString() ?: "[]",
                            challengeProgressString = request.optJSONArray("challengeProgress")?.toString() ?: "[]",
                            examGradesString = request.optJSONObject("examGrades")?.toString() ?: "{}"
                        )
                        dao.insertSyncedStudent(updated)
                        
                        response.put("status", STATUS_OK)
                        response.put("personalAttendance", existing.attendanceCount) // Mandamos el acumulado real
                        response.put("isListenerMode", !areSectionsEqual(updated.sectionName, currentServerSection))
                        
                        // Enviar historial de asistencia del alumno
                        val history = dao.getAttendanceByStudentList(cardId)
                        val historyArray = JSONArray()
                        history.forEach { record ->
                            historyArray.put(JSONObject().apply {
                                put("date", record.date)
                                put("status", record.status)
                                put("classNumber", record.classNumber)
                            })
                        }
                        response.put("attendanceHistory", historyArray)
                        
                        // Mensajes y Archivos
                        if (updated.privateMessage.isNotEmpty()) {
                            response.put("privateMessage", updated.privateMessage)
                            dao.insertSyncedStudent(updated.copy(privateMessage = ""))
                        }
                        
                        getSharedFile()?.let { response.put("pendingFile", it) }
                        
                        if (updated.privateFileUri.isNotEmpty()) {
                            response.put("privateFile", JSONObject().apply {
                                put("id", updated.privateFileId)
                                put("name", updated.privateFileName)
                                put("size", updated.privateFileSize)
                            })
                        }
                        
                        val state = getState()
                        state.keys().forEach { if(it != "sectionName") response.put(it, state.get(it)) }
                        response.put("command", getPendingCommand())
                    } else {
                        response.put("status", STATUS_EXPELLED)
                    }
                }

                ACTION_CHECK_OUT -> {
                    val cardId = request.optString("cardId")
                    dao.getStudentByCardId(cardId)?.let {
                        dao.insertSyncedStudent(it.copy(lastSeen = 0L))
                        response.put("status", STATUS_OK)
                    }
                }

                ACTION_SEND_DOUBT -> {
                    onDoubt(request.getString("cardId"), request.optString("message"))
                    response.put("status", STATUS_OK)
                }

                ACTION_GET_FILE_CHUNK -> {
                    val fileId = request.getString("fileId")
                    val offsetVal = request.getLong("offset")
                    val lengthVal = request.getInt("length")
                    
                    val uri = findFileUri(fileId, request.optString("cardId"), db, getSharedFile(), getSharedFileUri())
                    if (uri != null) {
                        try {
                            appContext.contentResolver.openInputStream(uri)?.use { input ->
                                // Skip manual robusto usando resta para evitar error de Serializable
                                var currentPos = 0L
                                while (offsetVal - currentPos > 0) {
                                    val skipped = input.skip(offsetVal - currentPos)
                                    if (skipped <= 0) break
                                    currentPos += skipped
                                }
                                
                                val buffer = ByteArray(lengthVal)
                                val read = input.read(buffer)
                                if (read > 0) {
                                    response.put("status", STATUS_OK)
                                    response.put("data", android.util.Base64.encodeToString(buffer.copyOf(read), android.util.Base64.NO_WRAP))
                                } else response.put("status", STATUS_EOF)
                            }
                        } catch (e: Exception) { 
                            AppLogger.e(TAG, "Error leyendo chunk de $uri", e)
                            response.put("status", STATUS_ERROR) 
                        }
                    } else response.put("status", STATUS_NOT_FOUND)
                }
            }
            writer.println(response.toString())
        }
    }

    private suspend fun findFileUri(id: String, cardId: String, db: AppDatabase, sharedFile: JSONObject?, sharedFileUri: android.net.Uri?): android.net.Uri? {
        if (id == sharedFile?.optString("id")) return sharedFileUri
        if (cardId.isNotEmpty()) {
            val s = db.classroomDao().getStudentByCardId(cardId)
            if (s?.privateFileId == id) return android.net.Uri.parse(s.privateFileUri)
        }
        return null
    }
}

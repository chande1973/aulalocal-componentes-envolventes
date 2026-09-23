package com.example.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object AppLogger {
    private const val TAG = "AppLogger"
    private const val LOG_FILE_NAME = "app_debug_log.txt"
    private var logFile: File? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    private val loggerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Real-time flow for the UI
    private val _logFlow = MutableSharedFlow<String>(extraBufferCapacity = 100)
    val logFlow = _logFlow.asSharedFlow()

    // Cache in memory for immediate view (last 200 lines)
    private val memoryLogs = Collections.synchronizedList(mutableListOf<String>())

    // Canal para encolar logs y procesarlos uno por uno sin saturar el sistema
    private val logChannel = kotlinx.coroutines.channels.Channel<String>(capacity = 500)

    fun initialize(context: Context) {
        loggerScope.launch {
            val logsDir = File(context.filesDir, "logs")
            if (!logsDir.exists()) logsDir.mkdirs()
            logFile = File(logsDir, LOG_FILE_NAME)
            
            if (logFile!!.exists() && logFile!!.length() > 2 * 1024 * 1024) {
                logFile!!.delete()
            }
            
            // Iniciar el trabajador que consume el canal y escribe en disco
            launch(Dispatchers.IO) {
                for (entry in logChannel) {
                    val file = logFile ?: continue
                    try {
                        FileWriter(file, true).use { it.write("$entry\n") }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error en trabajador de log", e)
                    }
                }
            }
            
            d(TAG, "Logger profesional inicializado con Channel")
        }
    }

    fun getMemoryLogs(): List<String> = memoryLogs.toList()

    fun d(tag: String, message: String) {
        Log.d(tag, message)
        enqueueLog("DEBUG", tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
        enqueueLog("ERROR", tag, "$message ${throwable?.localizedMessage ?: ""}")
    }

    fun i(tag: String, message: String) {
        Log.i(tag, message)
        enqueueLog("INFO", tag, message)
    }

    fun w(tag: String, message: String) {
        Log.w(tag, message)
        enqueueLog("WARN", tag, message)
    }

    private fun enqueueLog(level: String, tag: String, message: String) {
        val timestamp = dateFormat.format(Date())
        val logEntry = "[$timestamp] [$level] [$tag]: $message"
        
        // 1. Emitir a la UI en tiempo real
        _logFlow.tryEmit(logEntry)
        
        // 2. Guardar en memoria (últimas 200 líneas)
        memoryLogs.add(logEntry)
        if (memoryLogs.size > 200) memoryLogs.removeAt(0)

        // 3. Encolar para escritura en disco (No bloqueante, sin crear corruteinas nuevas)
        logChannel.trySend(logEntry)
    }

    fun clearLogs() {
        memoryLogs.clear()
        logFile?.delete()
    }

    fun shareLogFile(context: Context) {
        val file = logFile ?: return
        if (!file.exists()) {
            d(TAG, "El archivo de log no existe para compartir")
            return
        }

        try {
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Log de Diagnóstico - Aula Local")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            context.startActivity(Intent.createChooser(shareIntent, "Enviar Log a Soporte"))
        } catch (e: Exception) {
            e(TAG, "Error compartiendo archivo de log", e)
        }
    }
}

package com.example.data

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.AppDatabase
import com.example.data.LocalNetworkManager
import com.example.data.UserProfile
import com.example.data.LocalSection
import kotlinx.coroutines.*

class ClassroomServerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val CHANNEL_ID = "classroom_server_channel"
    private val NOTIFICATION_ID = 1001

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        if (action == "STOP_SERVER") {
            stopServer()
            return START_NOT_STICKY
        }

        val teacherName = intent?.getStringExtra("teacherName") ?: "Profesor"
        val sectionName = intent?.getStringExtra("sectionName") ?: "Aula"
        val tcpPort = intent?.getIntExtra("tcpPort", 9999) ?: 9999
        val currentUnit = intent?.getIntExtra("activeUnitIndex", 1) ?: 1
        val isReading = intent?.getBooleanExtra("isReadingActive", true) ?: true
        val isExam = intent?.getBooleanExtra("isExamActive", false) ?: false

        startForegroundService(sectionName)
        
        val db = AppDatabase.getDatabase(this)
        
        // Cargar datos reales de la sección antes de iniciar el servidor
        serviceScope.launch(Dispatchers.IO) {
            val dao = db.classroomDao()
            val section = dao.getSectionByName(sectionName)
            val profile = dao.getUserProfile()
            
            val p = profile
            val teacherNameFull = if (p != null) "${p.firstName} ${p.lastName}" else "Profesor"
            val enabledReads = section?.enabledReadUnits ?: "1"
            val enabledExams = section?.enabledExamUnits ?: ""
            val message = section?.teacherMessage ?: ""

            // Resetear contadores de la sesión anterior para esta sección
            dao.resetSectionCheckpoints(sectionName)

            LocalNetworkManager.startTeacherServer(
                scope = serviceScope,
                teacher = teacherNameFull,
                section = sectionName,
                unit = currentUnit,
                reading = isReading,
                exam = isExam,
                reads = enabledReads,
                exams = enabledExams,
                msg = message,
                total = section?.totalClasses ?: 0,
                database = db,
                context = this@ClassroomServerService,
                tcpPort = tcpPort
            )
        }

        return START_STICKY
    }

    private fun startForegroundService(sectionName: String) {
        val notificationIntent = Intent()
        notificationIntent.setClassName(this, "com.example.MainActivity")
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, ClassroomServerService::class.java).apply {
            action = "STOP_SERVER"
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Aula Local - Clase en Vivo")
            .setContentText("Dando clase a: $sectionName")
            .setSmallIcon(android.R.drawable.ic_menu_share)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "FINALIZAR CLASE", stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun stopServer() {
        serviceScope.launch {
            // Dar 2 segundos para que el comando FORCE_DISCONNECT llegue a los alumnos
            LocalNetworkManager.stopTeacherServer()
            delay(2000)
            withContext(Dispatchers.Main) {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Servidor de Clase Aula Local",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Mantiene activa la conexión de los alumnos"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }
}

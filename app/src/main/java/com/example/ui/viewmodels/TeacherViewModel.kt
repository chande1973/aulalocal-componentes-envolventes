package com.example.ui.viewmodels

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.domain.ReportManager
import com.example.domain.AttendanceManager
import com.example.util.ZipUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import androidx.documentfile.provider.DocumentFile
import com.example.R
import java.io.File

class TeacherViewModel(application: Application) : AndroidViewModel(application) {

    private val db by lazy { AppDatabase.getDatabase(application) }
    private val dao by lazy { db.classroomDao() }

    val sections: StateFlow<List<LocalSection>> by lazy {
        dao.getAllSectionsFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    private val _liveActiveUnitIndex = MutableStateFlow(1)
    val liveActiveUnitIndex = _liveActiveUnitIndex.asStateFlow()

    private val _liveIsReadingActive = MutableStateFlow(true)
    val liveIsReadingActive = _liveIsReadingActive.asStateFlow()

    private val _liveIsExamActive = MutableStateFlow(false)
    val liveIsExamActive = _liveIsExamActive.asStateFlow()

    private val _liveSelectedSection = MutableStateFlow("")
    val liveSelectedSection = _liveSelectedSection.asStateFlow()

    private val _enabledReadUnits = MutableStateFlow("1")
    val enabledReadUnits = _enabledReadUnits.asStateFlow()

    private val _enabledExamUnits = MutableStateFlow("")
    val enabledExamUnits = _enabledExamUnits.asStateFlow()

    private val _teacherMessage = MutableStateFlow("")
    val teacherMessageState = _teacherMessage.asStateFlow()

    val isServerRunningState = LocalNetworkManager.isServerRunning

    private val _uiEvents = MutableSharedFlow<String>()
    val uiEvents = _uiEvents.asSharedFlow()

    init {
        // Recuperar la última sección activa al iniciar
        viewModelScope.launch(Dispatchers.IO) {
            val profile = dao.getUserProfile()
            if (profile != null && profile.activeSection.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    selectSection(profile.activeSection)
                }
            }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val syncedStudents: StateFlow<List<SyncedStudent>> by lazy {
        _liveSelectedSection.flatMapLatest { section ->
            if (section.isEmpty()) dao.getSyncedStudentsFlow()
            else dao.getSyncedStudentsBySectionFlow(section)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    // Alumnos de cualquier sección que han sido vistos recientemente (últimos 30 segundos)
    val allConnectedStudents: StateFlow<List<SyncedStudent>> by lazy {
        dao.getSyncedStudentsFlow().map { list ->
            val limit = System.currentTimeMillis() - 30000
            list.filter { it.lastSeen > limit }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun selectSection(name: String) {
        _liveSelectedSection.value = name
        viewModelScope.launch(Dispatchers.IO) {
            val section = dao.getSectionByName(name)
            if (section != null) {
                _enabledReadUnits.value = section.enabledReadUnits
                _enabledExamUnits.value = section.enabledExamUnits
                _teacherMessage.value = section.teacherMessage
                
                // Persistir la selección en el perfil para que la app la recuerde
                val profile = dao.getUserProfile()
                if (profile != null) {
                    dao.insertProfile(profile.copy(activeSection = name))
                }
                AppLogger.d("TeacherViewModel", "Sección seleccionada y persistida: $name")
            }
        }
    }

    fun startClassServer(sectionName: String, customPort: Int = 9999) {
        if (sectionName.isBlank()) {
            viewModelScope.launch { _uiEvents.emit(getApplication<Application>().getString(R.string.label_no_section_selected)) }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            // Si el servidor está en proceso de cierre, esperamos un poco
            if (LocalNetworkManager.isServerRunning.value) {
                LocalNetworkManager.stopTeacherServer()
                kotlinx.coroutines.delay(1500)
            }
            
            _liveSelectedSection.value = sectionName
            try {
                val profile = dao.getUserProfile()
                val section = dao.getSectionByName(sectionName)
                
                if (profile != null && section != null) {
                    val startTime = if (profile.lastSessionStartTime > 0) profile.lastSessionStartTime else System.currentTimeMillis()
                    if (profile.lastSessionStartTime == 0L) {
                        dao.insertProfile(profile.copy(lastSessionStartTime = startTime))
                    }
                    
                    // Iniciar vía Foreground Service para persistencia
                    val serviceIntent = Intent(getApplication(), ClassroomServerService::class.java).apply {
                        putExtra("teacherName", "${profile.firstName} ${profile.lastName}")
                        putExtra("sectionName", sectionName)
                        putExtra("tcpPort", customPort)
                        putExtra("activeUnitIndex", _liveActiveUnitIndex.value)
                        putExtra("isReadingActive", _liveIsReadingActive.value)
                        putExtra("isExamActive", _liveIsExamActive.value)
                    }
                    ContextCompat.startForegroundService(getApplication(), serviceIntent)
                    
                    LocalNetworkManager.updateServerConfig(
                        teacher = "${profile.firstName} ${profile.lastName}",
                        section = sectionName,
                        unit = _liveActiveUnitIndex.value,
                        reading = _liveIsReadingActive.value,
                        exam = _liveIsExamActive.value,
                        reads = section.enabledReadUnits,
                        exams = section.enabledExamUnits,
                        msg = section.teacherMessage,
                        total = section.totalClasses
                    )
                    _enabledReadUnits.value = section.enabledReadUnits
                    _enabledExamUnits.value = section.enabledExamUnits
                    _teacherMessage.value = section.teacherMessage
                    
                    _uiEvents.emit(getApplication<Application>().getString(R.string.msg_server_started, customPort))
                }
            } catch (e: Exception) {
                AppLogger.e("TeacherViewModel", "Failed to start server", e)
                _uiEvents.emit(getApplication<Application>().getString(R.string.error_server_failed))
            }
        }
    }

    fun stopClassServer() {
        val intent = Intent(getApplication(), ClassroomServerService::class.java).apply {
            action = "STOP_SERVER"
        }
        getApplication<Application>().startService(intent)
    }

    fun updateActiveUnit(unitIndex: Int) {
        _liveActiveUnitIndex.value = unitIndex
        viewModelScope.launch(Dispatchers.IO) {
            val sectionName = liveSelectedSection.value
            val section = dao.getSectionByName(sectionName) ?: return@launch
            val profile = dao.getUserProfile() ?: return@launch

            // Determinar si el examen está activo para esta nueva unidad
            val isExamNow = section.enabledExamUnits.split(",").filter { it.isNotEmpty() }.contains(unitIndex.toString())
            _liveIsExamActive.value = isExamNow

            LocalNetworkManager.updateServerConfig(
                teacher = "${profile.firstName} ${profile.lastName}",
                section = sectionName,
                unit = unitIndex,
                reading = _liveIsReadingActive.value,
                exam = isExamNow,
                reads = section.enabledReadUnits,
                exams = section.enabledExamUnits,
                msg = _teacherMessage.value,
                total = section.totalClasses
            )
            _uiEvents.emit("Unidad $unitIndex marcada como activa para los alumnos")
        }
    }

    fun updateTeacherUnitToggle(unitIndex: Int, isEnabled: Boolean, isExam: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val sectionName = liveSelectedSection.value
            val section = dao.getSectionByName(sectionName) ?: return@launch
            val profile = dao.getUserProfile() ?: return@launch

            var reads = section.enabledReadUnits.split(",").filter { it.isNotEmpty() }.toMutableSet()
            var exams = section.enabledExamUnits.split(",").filter { it.isNotEmpty() }.toMutableSet()

            if (isExam) {
                if (isEnabled) exams.add(unitIndex.toString()) else exams.remove(unitIndex.toString())
            } else {
                if (isEnabled) reads.add(unitIndex.toString()) else reads.remove(unitIndex.toString())
            }

            val newReads = reads.joinToString(",")
            val newExams = exams.joinToString(",")
            
            dao.insertSection(section.copy(enabledReadUnits = newReads, enabledExamUnits = newExams))
            
            _enabledReadUnits.value = newReads
            _enabledExamUnits.value = newExams

            // Si cambiamos el estado de examen de la unidad que está ACTUALMENTE activa,
            // actualizamos el flag global para que los alumnos reciban la alerta.
            if (isExam && unitIndex == _liveActiveUnitIndex.value) {
                _liveIsExamActive.value = isEnabled
            }

            LocalNetworkManager.updateServerConfig(
                teacher = "${profile.firstName} ${profile.lastName}",
                section = sectionName,
                unit = _liveActiveUnitIndex.value,
                reading = _liveIsReadingActive.value,
                exam = _liveIsExamActive.value,
                reads = newReads,
                exams = newExams,
                msg = section.teacherMessage,
                total = section.totalClasses
            )
        }
    }

    fun sendGlobalMessage(message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val sectionName = liveSelectedSection.value
            val section = dao.getSectionByName(sectionName) ?: return@launch
            val profile = dao.getUserProfile() ?: return@launch

            dao.insertSection(section.copy(teacherMessage = message))
            _teacherMessage.value = message
            LocalNetworkManager.updateServerConfig(
                teacher = "${profile.firstName} ${profile.lastName}",
                section = sectionName,
                unit = _liveActiveUnitIndex.value,
                reading = _liveIsReadingActive.value,
                exam = _liveIsExamActive.value,
                reads = section.enabledReadUnits,
                exams = section.enabledExamUnits,
                msg = message,
                total = section.totalClasses
            )
        }
    }

    fun toggleTeacherReading(isActive: Boolean) {
        _liveIsReadingActive.value = isActive
        viewModelScope.launch(Dispatchers.IO) {
            val section = dao.getSectionByName(liveSelectedSection.value) ?: return@launch
            val profile = dao.getUserProfile() ?: return@launch
            LocalNetworkManager.updateServerConfig(
                teacher = "${profile.firstName} ${profile.lastName}",
                section = liveSelectedSection.value,
                unit = _liveActiveUnitIndex.value,
                reading = isActive,
                exam = _liveIsExamActive.value,
                reads = section.enabledReadUnits,
                exams = section.enabledExamUnits,
                msg = section.teacherMessage,
                total = section.totalClasses
            )
        }
    }

    fun toggleTeacherExam(isActive: Boolean) {
        _liveIsExamActive.value = isActive
        viewModelScope.launch(Dispatchers.IO) {
            val section = dao.getSectionByName(liveSelectedSection.value) ?: return@launch
            val profile = dao.getUserProfile() ?: return@launch
            LocalNetworkManager.updateServerConfig(
                teacher = "${profile.firstName} ${profile.lastName}",
                section = liveSelectedSection.value,
                unit = _liveActiveUnitIndex.value,
                reading = _liveIsReadingActive.value,
                exam = isActive,
                reads = section.enabledReadUnits,
                exams = section.enabledExamUnits,
                msg = section.teacherMessage,
                total = section.totalClasses
            )
        }
    }

    fun incrementTotalClasses() {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = dao.getUserProfile()
            if (profile != null && profile.role == "teacher") {
                val sessionStart = LocalNetworkManager.getSessionStartTime()
                val sessionEnd = System.currentTimeMillis()
                val duration = sessionEnd - sessionStart
                
                if (duration < 60000) {
                    _uiEvents.emit(getApplication<Application>().getString(R.string.msg_class_min_duration))
                    return@launch
                }

                val sectionName = liveSelectedSection.value
                val students = dao.getSyncedStudentsBySectionList(sectionName)
                val section = dao.getSectionByName(sectionName) ?: return@launch
                
                AttendanceManager.finalizeAttendance(dao, section, students)

                val newTotal = section.totalClasses + 1
                dao.insertSection(section.copy(totalClasses = newTotal))
                dao.insertProfile(profile.copy(lastSessionStartTime = 0L))
                
                stopClassServer()
                _uiEvents.emit(getApplication<Application>().getString(R.string.msg_class_finished_attendance_saved))
            }
        }
    }

    fun updateManualAttendance(cardId: String, status: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val student = dao.getStudentByCardId(cardId)
            if (student != null) {
                dao.insertSyncedStudent(student.copy(manualAttendanceStatus = status))
            }
        }
    }

    fun generateAcademicReport(context: Context, totalUnits: Int = 11) {
        viewModelScope.launch(Dispatchers.IO) {
            val sectionName = liveSelectedSection.value
            val section = dao.getSectionByName(sectionName)
            val students = syncedStudents.value
            val profile = dao.getUserProfile()
            
            if (students.isEmpty() || section == null) {
                _uiEvents.emit(getApplication<Application>().getString(R.string.msg_no_data_for_report))
                return@launch
            }

            val teacherName = "${profile?.firstName} ${profile?.lastName}"
            val file = ReportManager.generateAcademicReport(context, teacherName, section, students, totalUnits)
            
            if (file != null) {
                _uiEvents.emit("✅ Acta guardada en: Documentos/AulaLocal_Reportes/${file.name}")
            } else {
                _uiEvents.emit(getApplication<Application>().getString(R.string.error_report_processing))
            }
        }
    }

    fun generateAttendanceReport(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val sectionName = liveSelectedSection.value
            val section = dao.getSectionByName(sectionName)
            val students = syncedStudents.value
            
            if (students.isEmpty() || section == null) {
                _uiEvents.emit(getApplication<Application>().getString(R.string.msg_no_data_for_report))
                return@launch
            }

            val file = ReportManager.generateAttendanceReport(context, section, students)
            if (file != null) {
                _uiEvents.emit("✅ Asistencia guardada en: Descargas/AulaLocal_Reportes/${file.name}")
            }
        }
    }

    fun generateAllAcademicReports(context: Context, totalUnits: Int = 11) {
        viewModelScope.launch(Dispatchers.IO) {
            val allSections = dao.getAllSections()
            if (allSections.isEmpty()) {
                _uiEvents.emit("No hay secciones para exportar")
                return@launch
            }
            
            val profile = dao.getUserProfile()
            val teacherName = "${profile?.firstName} ${profile?.lastName}"
            var successCount = 0
            
            allSections.forEach { section ->
                val students = dao.getSyncedStudentsBySectionList(section.name)
                if (students.isNotEmpty()) {
                    val file = ReportManager.generateAcademicReport(context, teacherName, section, students, totalUnits)
                    if (file != null) successCount++
                }
            }
            
            _uiEvents.emit("Se generaron $successCount actas de un total de ${allSections.size} aulas.")
        }
    }

    fun closeCurrentCorte(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = dao.getUserProfile()
            if (profile != null && profile.role == "teacher") {
                generateAcademicReport(context)
                val nextLapso = if (profile.currentLapso < 4) profile.currentLapso + 1 else 4
                dao.insertProfile(profile.copy(currentLapso = nextLapso))
                _uiEvents.emit(getApplication<Application>().getString(R.string.msg_corte_closed))
            }
        }
    }

    fun addSection(name: String, plannedClasses: Int = 36, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (name.isBlank()) {
            onError(getApplication<Application>().getString(R.string.error_empty_name))
            return
        }
        viewModelScope.launch {
            val currentCount = withContext(Dispatchers.IO) { dao.getSectionCount() }
            if (currentCount >= 20) {
                onError(getApplication<Application>().getString(R.string.error_max_sections))
            } else {
                withContext(Dispatchers.IO) {
                    dao.insertSection(LocalSection(name = name, plannedClasses = plannedClasses))
                }
                onSuccess()
            }
        }
    }

    fun updateSectionPlannedClasses(sectionName: String, planned: Int, offset: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val section = dao.getSectionByName(sectionName) ?: return@launch
            dao.insertSection(section.copy(plannedClasses = planned, currentWeekOffset = offset))
        }
    }

    fun deleteSection(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteSection(id)
        }
    }

    fun expelStudent(cardId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.removeStudentByCardId(cardId)
        }
    }

    fun clearStudentDoubt(cardId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val student = dao.getStudentByCardId(cardId)
            if (student != null) {
                dao.insertSyncedStudent(student.copy(hasDoubt = false, doubtMessage = ""))
            }
        }
    }

    fun sendPrivateMessage(cardId: String, message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val student = dao.getStudentByCardId(cardId)
            if (student != null) {
                dao.insertSyncedStudent(student.copy(privateMessage = message))
                _uiEvents.emit("Mensaje enviado a ${student.firstName}")
            }
        }
    }

    fun sharePrivateFile(cardId: String, uri: android.net.Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val student = dao.getStudentByCardId(cardId) ?: return@launch
            val context = getApplication<Application>()
            var fileName = "archivo"
            var fileSize = 0L

            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    fileName = cursor.getString(nameIndex)
                    fileSize = cursor.getLong(sizeIndex)
                }
            }

            // Persistir permiso de lectura para que el servidor pueda leerlo después
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                AppLogger.w("TeacherViewModel", "No se pudo persistir permiso URI: ${e.message}")
            }

            dao.insertSyncedStudent(student.copy(
                privateFileUri = uri.toString(),
                privateFileName = fileName,
                privateFileSize = fileSize,
                privateFileId = java.util.UUID.randomUUID().toString()
            ))
            _uiEvents.emit("Archivo privado enviado a ${student.firstName}: $fileName")
        }
    }

    fun shareFile(uri: android.net.Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            var fileName = "archivo"
            var fileSize = 0L
            
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    fileName = cursor.getString(nameIndex)
                    fileSize = cursor.getLong(sizeIndex)
                }
            }

            // Persistir permiso de lectura para que el servidor pueda leerlo después
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                AppLogger.w("TeacherViewModel", "No se pudo persistir permiso URI: ${e.message}")
            }

            LocalNetworkManager.setSharedFile(uri, fileName, fileSize, false)
            _uiEvents.emit("Archivo compartido: $fileName")
        }
    }

    fun stopSharingFile() {
        LocalNetworkManager.setSharedFile(null, "", 0)
        viewModelScope.launch { _uiEvents.emit(getApplication<Application>().getString(R.string.msg_offline_mode)) }
    }

    /**
     * Comprime la carpeta de materiales especificada (o la guardada) y la comparte con los alumnos como un ZIP.
     */
    fun shareFullCoursePack(customFolderUri: Uri? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = dao.getUserProfile() ?: return@launch
            
            val targetUri = customFolderUri ?: if (profile.materialsFolderUri.isNotEmpty()) {
                Uri.parse(profile.materialsFolderUri)
            } else null

            if (targetUri == null) {
                _uiEvents.emit(getApplication<Application>().getString(R.string.error_no_materials_folder))
                return@launch
            }

            val context = getApplication<Application>()
            
            val rootFolder = if (targetUri.scheme == "file") {
                val file = File(targetUri.path ?: "")
                DocumentFile.fromFile(file)
            } else {
                DocumentFile.fromTreeUri(context, targetUri)
            }

            if (rootFolder == null || !rootFolder.exists()) {
                _uiEvents.emit(getApplication<Application>().getString(R.string.error_invalid_folder))
                return@launch
            }

            // Si se seleccionó una carpeta personalizada, actualizamos el perfil
            if (customFolderUri != null) {
                dao.insertProfile(profile.copy(materialsFolderUri = customFolderUri.toString()))
            }

            _uiEvents.emit(getApplication<Application>().getString(R.string.msg_preparing_zip))
            
            val cacheFile = java.io.File(context.cacheDir, "course_update.zip")
            val success = com.example.util.ZipUtils.zipFolder(context, rootFolder, cacheFile)

            if (success) {
                val contentUri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    cacheFile
                )
                
                LocalNetworkManager.setSharedFile(contentUri, "course_update.zip", cacheFile.length(), true)
                _uiEvents.emit(getApplication<Application>().getString(R.string.msg_zip_ready))
            } else {
                _uiEvents.emit(getApplication<Application>().getString(R.string.error_zip_failed))
            }
        }
    }

    fun getStudentAttendanceHistory(cardId: String): Flow<List<AttendanceRecord>> {
        return dao.getAttendanceByStudentFlow(cardId)
    }

    /**
     * Habilita el Examen Recuperativo para un alumno específico en una unidad.
     * Elimina la nota anterior en la BD y envía un aviso privado al estudiante.
     */
    fun enableRecuperativoForStudent(cardId: String, unitIndex: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val student = dao.getStudentByCardId(cardId) ?: return@launch
            val gradesObj = try { JSONObject(student.examGradesString) } catch (e: Exception) { JSONObject() }
            
            gradesObj.remove(unitIndex.toString())
            
            val updated = student.copy(
                examGradesString = gradesObj.toString(),
                privateMessage = "🔄 ¡Recuperativo Habilitado! Puedes volver a presentar el examen de la Unidad $unitIndex."
            )
            dao.insertSyncedStudent(updated)
            _uiEvents.emit("✅ Recuperativo de Unidad $unitIndex habilitado para ${student.firstName}")
        }
    }

    /**
     * Autoriza la edición de perfil a un alumno remoto por red local.
     */
    fun authorizeStudentProfileEdit(cardId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val student = dao.getStudentByCardId(cardId) ?: return@launch
            val updated = student.copy(
                privateMessage = "AUTORIZACION_PERFIL_DESBLOQUEADO: Puedes editar tus datos de perfil ahora."
            )
            dao.insertSyncedStudent(updated)
            _uiEvents.emit("✅ Edición de perfil autorizada para ${student.firstName}")
        }
    }

    fun moveStudentToCurrentSection(student: SyncedStudent) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentSection = _liveSelectedSection.value
            if (currentSection.isNotEmpty()) {
                dao.insertSyncedStudent(student.copy(sectionName = currentSection))
                _uiEvents.emit("Alumno ${student.firstName} trasladado oficialmente a $currentSection")
            }
        }
    }

    fun injectTestData(weeks: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val sectionName = _liveSelectedSection.value
            if (sectionName.isEmpty()) {
                _uiEvents.emit("Selecciona una sección primero")
                return@launch
            }
            _uiEvents.emit("Inyectando datos para $weeks encuentros...")
            com.example.util.TestDataInjector.injectSimulationData(dao, sectionName, weeks)
            _uiEvents.emit("Simulación completada para $sectionName")
        }
    }
}

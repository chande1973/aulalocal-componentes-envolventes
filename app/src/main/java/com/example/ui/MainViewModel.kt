package com.example.ui

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.documentfile.provider.DocumentFile
import com.example.R
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.data.model.LoadResult
import com.example.data.model.UnitData
import com.example.domain.CourseManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val db by lazy { AppDatabase.getDatabase(application) }
    private val dao by lazy { db.classroomDao() }
    private val courseRepository by lazy { CourseRepository(application) }

    // Reactive StateFlows from Room database
    val userProfile: StateFlow<UserProfile?> by lazy {
        dao.getUserProfileFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }

    val studyProgress: StateFlow<List<UnitProgress>> by lazy {
        dao.getAllProgressFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    // UI State - Course Content (Loaded from Course Pack)
    private val _units = MutableStateFlow<List<UnitData>>(emptyList())
    val units: List<UnitData> get() = _units.value
    val unitsFlow = _units.asStateFlow()

    private val _courseName = MutableStateFlow("Aula Local")
    val courseName = _courseName.asStateFlow()

    private val _courseVersion = MutableStateFlow(1)
    val courseVersion = _courseVersion.asStateFlow()

    private val _courseVersionUpdateMessage = MutableStateFlow<String?>(null)
    val courseVersionUpdateMessage = _courseVersionUpdateMessage.asStateFlow()
    private var dismissedVersion: Int = 0

    fun dismissVersionNotification() {
        dismissedVersion = _courseVersion.value
        _courseVersionUpdateMessage.value = null
    }

    // Wizard Validation State
    private val _wizardValidationResult = MutableStateFlow<LoadResult<CoursePack>?>(null)
    val wizardValidationResult = _wizardValidationResult.asStateFlow()

    data class MaterialLoadingProgress(
        val step: String = "",
        val current: Int = 0,
        val total: Int = 0,
        val currentFile: String = "",
        val isLoading: Boolean = false
    )

    private val _loadingProgress = MutableStateFlow(MaterialLoadingProgress())
    val loadingProgress = _loadingProgress.asStateFlow()

    // Cached validated course pack for instant transition after wizard
    private var _validatedCoursePack: CoursePack? = null

    // TTS engine
    private var textToSpeech: TextToSpeech? = null
    private val _isTtsReady = MutableStateFlow(false)
    val isTtsReady = _isTtsReady.asStateFlow()

    private val _hotspotSsid = MutableStateFlow<String?>(null)
    val hotspotSsid = _hotspotSsid.asStateFlow()

    private val _hotspotPassword = MutableStateFlow<String?>(null)
    val hotspotPassword = _hotspotPassword.asStateFlow()

    private val _isHotspotActive = MutableStateFlow(false)
    val isHotspotActive = _isHotspotActive.asStateFlow()

    private val _localIpAddress = MutableStateFlow("192.168.43.1")
    val localIpAddress = _localIpAddress.asStateFlow()

    private var localOnlyHotspotReservation: WifiManager.LocalOnlyHotspotReservation? = null

    init {
        // Initialize TTS in background to avoid blocking main thread
        viewModelScope.launch(Dispatchers.Default) {
            textToSpeech = TextToSpeech(application, this@MainViewModel)
        }

        // Resolve initial local IP address
        updateLocalIpAddress()
        
        // Monitorear red periódicamente para actualizar el banner
        viewModelScope.launch {
            while(true) {
                delay(10000)
                updateLocalIpAddress()
            }
        }

        // React to materials folder changes (e.g. course updates)
        viewModelScope.launch {
            userProfile
                .map { it?.materialsFolderUri }
                .distinctUntilChanged()
                .collect { uriStr ->
                    if (!uriStr.isNullOrEmpty()) {
                        loadCourseFromUri(Uri.parse(uriStr))
                    }
                }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale("es", "ES"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("MainViewModel", "Spanish language not supported in TTS")
                textToSpeech?.language = Locale.getDefault()
            }
            _isTtsReady.value = true
        } else {
            Log.e("MainViewModel", "TTS Initialization failed")
        }
    }

    fun playAudioNarrator(text: String) {
        if (_isTtsReady.value) {
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "CLASS_AUDIO")
            Toast.makeText(getApplication(), getApplication<Application>().getString(R.string.msg_playing_audio), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(getApplication(), getApplication<Application>().getString(R.string.msg_tts_not_ready), Toast.LENGTH_SHORT).show()
        }
    }

    fun stopAudio() {
        textToSpeech?.stop()
    }

    // Clean up TTS
    override fun onCleared() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        LocalNetworkManager.stopTeacherServer()
        super.onCleared()
    }

    // ============================================================
    // COURSE PACK LOADING
    // ============================================================

    suspend fun validateAndCacheCoursePack(folderUri: Uri): LoadResult<CoursePack> {
        return withContext(Dispatchers.IO) {
            _loadingProgress.value = MaterialLoadingProgress(step = "Accediendo a materiales...", isLoading = true)
            val result = CourseManager.processCoursePack(courseRepository, folderUri) { step, current, total, file ->
                _loadingProgress.value = MaterialLoadingProgress(
                    step = step,
                    current = current,
                    total = total,
                    currentFile = file,
                    isLoading = true
                )
            }
            
            when (result) {
                is LoadResult.Success -> {
                    _validatedCoursePack = result.data
                    _wizardValidationResult.value = LoadResult.Success(result.data)
                }
                is LoadResult.PartialSuccess -> {
                    _validatedCoursePack = result.data
                    _wizardValidationResult.value = LoadResult.PartialSuccess(result.data, result.warnings)
                }
                is LoadResult.Failure -> {
                    _wizardValidationResult.value = LoadResult.Failure(result.errors)
                }
            }
            _loadingProgress.value = MaterialLoadingProgress(isLoading = false)
            result
        }
    }

    suspend fun confirmSetupAndSaveProfile(folderUri: Uri, profile: UserProfile) {
        dao.insertProfile(profile.copy(materialsFolderUri = folderUri.toString()))

        _validatedCoursePack?.let { pack ->
            _courseName.value = pack.index.courseMeta.title
            _units.value = CourseManager.mapPackToUnits(pack)
        }

        _wizardValidationResult.value = null
        _validatedCoursePack = null

        if (profile.role == "student") {
            initStudentProgress(_units.value.size)
        }
    }

    fun restoreValidatedCourseIfNeeded() {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = dao.getUserProfile()
            if (profile != null && profile.materialsFolderUri.isNotEmpty()) {
                val uri = Uri.parse(profile.materialsFolderUri)
                loadCourseFromUri(uri)
            }
        }
    }

    fun refreshCurrentCourse() {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = dao.getUserProfile()
            if (profile != null && profile.materialsFolderUri.isNotEmpty()) {
                val uri = Uri.parse(profile.materialsFolderUri)
                loadCourseFromUri(uri)
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), getApplication<Application>().getString(R.string.msg_course_refreshed), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun loadCourseFromUri(uri: Uri) {
        val result = CourseManager.processCoursePack(courseRepository, uri)
        val pack = when (result) {
            is LoadResult.Success -> result.data
            is LoadResult.PartialSuccess -> result.data
            else -> null
        }
        if (pack != null) {
            val newVersion = pack.index.courseMeta.version
            val previousVersion = _courseVersion.value
            withContext(Dispatchers.Main) {
                _courseName.value = pack.index.courseMeta.title
                if (newVersion > previousVersion && previousVersion > 0 && newVersion != dismissedVersion) {
                    _courseVersionUpdateMessage.value = "¡Nueva versión (v$newVersion) del curso disponible! Se actualizó el contenido."
                }
                _courseVersion.value = newVersion
                val mappedUnits = CourseManager.mapPackToUnits(pack)
                _units.value = mappedUnits
                initStudentProgress(mappedUnits.size)
            }
        }
    }

    fun updateUserProfile(profile: UserProfile) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertProfile(profile)
        }
    }

    fun resetApp(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentUnitsCount = _units.value.size
            dao.clearProfile()
            dao.clearSyncedStudents()
            dao.clearProgress()
            dao.clearAllSections()
            dao.clearAttendanceHistory()
            dao.clearInbox()
            if (currentUnitsCount > 0) initStudentProgress(currentUnitsCount)
            LocalNetworkManager.stopTeacherServer()
        }
    }

    private fun initStudentProgress(totalUnits: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val progress = dao.getAllProgress()
            if (progress.size != totalUnits) {
                // Si el curso cambió de tamaño, ajustamos respetando lo que ya existe
                for (i in 1..totalUnits) {
                    if (progress.none { it.unitIndex == i }) {
                        dao.insertProgress(UnitProgress(unitIndex = i))
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")
    fun startLocalOnlyHotspot() {
        val wifiManager = getApplication<Application>().getSystemService(Context.WIFI_SERVICE) as WifiManager
        try {
            wifiManager.startLocalOnlyHotspot(object : WifiManager.LocalOnlyHotspotCallback() {
                override fun onStarted(reservation: WifiManager.LocalOnlyHotspotReservation) {
                    super.onStarted(reservation)
                    localOnlyHotspotReservation = reservation
                    _hotspotSsid.value = reservation.wifiConfiguration?.SSID
                    _hotspotPassword.value = reservation.wifiConfiguration?.preSharedKey
                    _isHotspotActive.value = true
                    updateLocalIpAddress()
                }
                override fun onStopped() { super.onStopped(); _isHotspotActive.value = false }
                override fun onFailed(reason: Int) { super.onFailed(reason); _isHotspotActive.value = false }
            }, Handler(Looper.getMainLooper()))
        } catch (e: Exception) { }
    }

    @androidx.annotation.RequiresApi(android.os.Build.VERSION_CODES.O)
    fun stopLocalOnlyHotspot() {
        localOnlyHotspotReservation?.close()
        _isHotspotActive.value = false
    }

    fun updateLocalIpAddress() {
        viewModelScope.launch(Dispatchers.IO) {
            val candidates = mutableListOf<String>()
            try {
                val interfaces = java.util.Collections.list(java.net.NetworkInterface.getNetworkInterfaces())
                for (intf in interfaces) {
                    if (!intf.isUp || intf.isLoopback) continue
                    val name = intf.name.lowercase()
                    val addrs = java.util.Collections.list(intf.inetAddresses)
                        .filterIsInstance<java.net.Inet4Address>()
                        .mapNotNull { it.hostAddress }
                        .filter { it != "127.0.0.1" }

                    for (ip in addrs) {
                        // Scoring system from FileShareHotspot
                        val score = when {
                            ip == "192.168.43.1" || ip == "192.168.49.1" || ip == "192.168.137.1" -> 10
                            name.contains("ap") || name.contains("softap") || name.contains("p2p") -> 5
                            name.contains("wlan") || name.contains("swlan") -> 3
                            ip.startsWith("192.168.") -> 2
                            else -> 1
                        }
                        candidates += "$score|$ip"
                    }
                }
            } catch (e: Exception) {
                com.example.data.AppLogger.e("MainViewModel", "Error scanning interfaces", e)
            }

            val bestIp = candidates
                .sortedByDescending { it.substringBefore("|").toIntOrNull() ?: 0 }
                .map { it.substringAfter("|") }
                .firstOrNull() ?: "192.168.43.1"

            _localIpAddress.value = bestIp
        }
    }

    fun getMediaUri(mediaFileName: String?): Uri? {
        if (mediaFileName == null) return null
        val profile = userProfile.value ?: return null
        if (profile.materialsFolderUri.isEmpty()) return null

        return try {
            val rootUri = Uri.parse(profile.materialsFolderUri)
            val rootDoc = if (rootUri.scheme == "file") {
                val file = java.io.File(rootUri.path ?: "")
                DocumentFile.fromFile(file)
            } else {
                DocumentFile.fromTreeUri(getApplication(), rootUri)
            } ?: return null

            // 1. Intentar acceso directo o por búsqueda recursiva case-insensitive
            var targetFile = findFileRecursive(rootDoc, mediaFileName)

            // 2. Intentar buscar solo el nombre del archivo en videos_clase/ y en la raíz
            if (targetFile == null) {
                val fileNameOnly = mediaFileName.substringAfterLast("/")
                targetFile = findFileRecursive(rootDoc, "videos_clase/$fileNameOnly")
                    ?: findFileCaseInsensitive(rootDoc, fileNameOnly)
            }

            Log.d("MainViewModel", "getMediaUri: buscaba '$mediaFileName' -> ${if (targetFile != null) "ENCONTRADO: ${targetFile.name}" else "NO ENCONTRADO"}")
            targetFile?.uri
        } catch (e: Exception) {
            Log.e("MainViewModel", "Error en getMediaUri para '$mediaFileName'", e)
            null
        }
    }

    private fun findFileCaseInsensitive(dir: DocumentFile, targetName: String): DocumentFile? {
        val exact = dir.findFile(targetName)
        if (exact != null && exact.exists()) return exact

        return dir.listFiles().firstOrNull { file ->
            file.name?.equals(targetName, ignoreCase = true) == true
        }
    }

    private fun findFileRecursive(root: DocumentFile, relativePath: String): DocumentFile? {
        var current: DocumentFile? = root
        val parts = relativePath.split("/")
        for (part in parts) {
            current = current?.let { findFileCaseInsensitive(it, part) }
            if (current == null) {
                Log.d("MainViewModel", "findFileRecursive: no encontrado '$part' en '$relativePath'")
                return null
            }
        }
        return current
    }
}
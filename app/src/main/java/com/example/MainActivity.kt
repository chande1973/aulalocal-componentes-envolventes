package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.example.ui.ClassroomMainScreen
import com.example.ui.MainViewModel
import com.example.ui.viewmodels.StudentViewModel
import com.example.ui.viewmodels.TeacherViewModel

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private val teacherViewModel: TeacherViewModel by viewModels()
    private val studentViewModel: StudentViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val writeStorageGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: true
        val readStorageGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: true
        
        if (!fineLocationGranted) {
            Toast.makeText(this, "Se requiere permiso de ubicación para detectar el profesor en la red local.", Toast.LENGTH_LONG).show()
        }
        
        if (!writeStorageGranted || !readStorageGranted) {
            Toast.makeText(this, "Se requiere permiso de almacenamiento para guardar reportes y archivos.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicialización asíncrona de utilidades pesadas
        com.example.data.AppLogger.initialize(this)
        
        checkAndRequestPermissions()
        
        enableEdgeToEdge()
        setContent {
            ClassroomMainScreen(
                mainViewModel = mainViewModel,
                teacherViewModel = teacherViewModel,
                studentViewModel = studentViewModel
            )
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        // Petición universal de almacenamiento para todos los modelos
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}

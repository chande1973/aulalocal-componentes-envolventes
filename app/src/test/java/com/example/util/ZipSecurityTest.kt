package com.example.util

import org.junit.Test
import org.junit.Assert.*
import java.io.File

class ZipSecurityTest {

    @Test
    fun testZipSlipPreventionLogic() {
        // GIVEN: Un directorio de destino y una entrada maliciosa
        val targetDir = File("/data/user/0/com.example/files/course_data")
        val maliciousEntry = "../../bad_file.txt"
        
        // WHEN: Simulamos la lógica de seguridad de ZipUtils
        val file = File(targetDir, maliciousEntry)
        
        // THEN: La ruta absoluta no debería empezar con la ruta absoluta del directorio
        // Nota: En un test unitario real usamos canonicalPath para resolver los ".."
        val isInside = file.canonicalPath.startsWith(targetDir.canonicalPath)
        
        assertFalse("Vulnerabilidad Zip Slip detectada", isInside)
    }

    @Test
    fun testNormalZipEntryLogic() {
        // GIVEN: Una entrada normal
        val targetDir = File("/data/user/0/com.example/files/course_data")
        val normalEntry = "unidad1/leccion1.pdf"
        
        // WHEN: Simulamos la lógica
        val file = File(targetDir, normalEntry)
        
        // THEN: Debería estar dentro del directorio
        assertTrue(file.canonicalPath.startsWith(targetDir.canonicalPath))
    }
}

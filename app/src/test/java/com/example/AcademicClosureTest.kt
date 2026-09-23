package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.LocalSection
import com.example.data.SyncedStudent
import com.example.domain.ReportManager
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AcademicClosureTest {

    @Test
    fun `test report generation with 8 units`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val section = LocalSection(name = "8-Units-Class", totalClasses = 10, plannedClasses = 36)
        
        // Alumno con notas en 4 de las 8 unidades
        val students = listOf(
            SyncedStudent(
                cardId = "V-11111",
                firstName = "Luis",
                lastName = "Perez",
                email = "",
                avatar = "",
                sectionName = "8-Units-Class",
                attendanceCount = 10,
                examGradesString = "{\"1\":\"20\", \"2\":\"20\", \"3\":\"20\", \"4\":\"20\"}"
            )
        )

        // Generar reporte indicando que el curso tiene 8 unidades
        val reportFile = ReportManager.generateAcademicReport(context, "Profesor Adaptativo", section, students, totalUnits = 8)
        
        assertNotNull(reportFile)
        val content = reportFile!!.readText()
        
        // El promedio debería ser 20.00 (porque solo se cuentan las notas existentes)
        // Ojo: Si la lógica de negocio dice que se divide entre el TOTAL del curso (8), sería 10.00
        // Actualmente tu código hace: if (count > 0) sum / count (solo las presentadas)
        assertTrue("El reporte debe contener la nota 20.00", content.contains("20.00"))
        assertTrue("El alumno debe estar APROBADO", content.contains("APROBADO"))
    }

    @Test
    fun `test report generation with 12 units and low attendance`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val section = LocalSection(name = "12-Units-Class", totalClasses = 20, plannedClasses = 36)
        
        val students = listOf(
            SyncedStudent(
                cardId = "V-22222",
                firstName = "Ana",
                lastName = "Gomez",
                email = "",
                avatar = "",
                sectionName = "12-Units-Class",
                attendanceCount = 10, // 50% de asistencia (Meta es 75%)
                examGradesString = "{\"1\":\"20\"}"
            )
        )

        val reportFile = ReportManager.generateAcademicReport(context, "Profesor Test", section, students, totalUnits = 12)
        val content = reportFile!!.readText()
        
        assertTrue("Debe mostrar 50% de asistencia", content.contains("50%"))
        assertTrue("Debe estar REPROBADO por inasistencia", content.contains("REPROBADO"))
    }

    @Test
    fun `test average calculation logic for dynamic units`() {
        // Simulación manual de la lógica interna de ReportManager
        val totalUnits = 9
        val gradesJson = "{\"1\":\"10\", \"9\":\"20\"}"
        val gradesObj = org.json.JSONObject(gradesJson)
        
        var sum = 0f
        var count = 0
        for (i in 1..totalUnits) {
            val g = gradesObj.optString(i.toString(), "")
            if (g.isNotEmpty()) {
                sum += g.toFloatOrNull() ?: 0f
                count++
            }
        }
        
        val avg = if (count > 0) sum / count else 0f
        assertEquals("El promedio de 10 y 20 debe ser 15", 15f, avg, 0.01f)
        assertEquals("Se deben haber detectado 2 notas", 2, count)
    }
}

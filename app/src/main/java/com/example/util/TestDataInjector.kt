package com.example.util

import com.example.data.*
import org.json.JSONObject
import kotlin.random.Random

object TestDataInjector {

    suspend fun injectSimulationData(
        dao: ClassroomDao,
        sectionName: String,
        weeks: Int,
        studentsCount: Int = 15,
        totalUnits: Int = 12
    ) {
        // 1. Crear la sección si no existe
        val existingSection = dao.getSectionByName(sectionName)
        val section = existingSection ?: LocalSection(name = sectionName)
        val updatedSection = section.copy(
            totalClasses = weeks,
            plannedClasses = weeks,
            enabledReadUnits = (1..totalUnits).joinToString(","),
            enabledExamUnits = (1..totalUnits).joinToString(",")
        )
        dao.insertSection(updatedSection)
        AppLogger.d("TestDataInjector", "Sección '$sectionName' actualizada a $weeks encuentros.")

        // 2. Generar alumnos
        val firstNames = listOf("Juan", "Maria", "Pedro", "Ana", "Luis", "Elena", "Carlos", "Sofia", "Miguel", "Lucia", "Jorge", "Carmen", "Roberto", "Rosa", "Andres", "Isabel", "Jose", "Paula", "Francisco", "Adriana")
        val lastNames = listOf("Perez", "Garcia", "Rodriguez", "Lopez", "Martinez", "Gonzalez", "Hernandez", "Sanchez", "Ramirez", "Torres", "Diaz", "Vargas", "Castro", "Rojas", "Guzman", "Mendoza", "Medina", "Castillo", "Salazar", "Arias")

        // Crear una semilla basada en el nombre de la sección para evitar colisiones de IDs
        val sectionSeed = sectionName.hashCode().toLong()
        val random = Random(sectionSeed)

        val students = mutableListOf<SyncedStudent>()
        for (i in 1..studentsCount) {
            // Generar un ID único que combine un base con el hash de la sección y el índice
            val baseId = 20000000 + (sectionName.hashCode() % 100000).let { if (it < 0) -it else it }
            val cardId = "V-${baseId + i}"
            
            val firstName = firstNames.random(random)
            val lastName = lastNames.random(random)
            
            // Simular asistencia: entre 60% y 100%
            val attendanceCount = (weeks * (random.nextFloat() * 0.4f + 0.6f)).toInt()
            
            // Simular notas: algunas unidades completadas, otras no
            val grades = JSONObject()
            val nbGrades = JSONObject()
            val nbFeedbacks = JSONObject()
            for (u in 1..totalUnits) {
                if (random.nextFloat() > 0.1f) { 
                    val grade = random.nextInt(8, 21).toFloat() 
                    grades.put(u.toString(), grade.toString())
                }
                if (random.nextFloat() > 0.2f) {
                    val nbGrade = random.nextInt(10, 21).toFloat()
                    nbGrades.put(u.toString(), nbGrade.toString())
                    nbFeedbacks.put(u.toString(), "Contenido: 8/10 - Coherencia: 4/5 - Ortografía: 4/5")
                }
            }

            students.add(
                SyncedStudent(
                    cardId = cardId,
                    firstName = firstName,
                    lastName = lastName,
                    email = "${firstName.lowercase()}.${lastName.lowercase()}@example.com",
                    avatar = "Geek",
                    sectionName = sectionName,
                    attendanceCount = attendanceCount,
                    examGradesString = grades.toString(),
                    notebookGradesString = nbGrades.toString(),
                    notebookFeedbacksString = nbFeedbacks.toString()
                )
            )
        }

        // 3. Insertar alumnos e historial de asistencia
        students.forEach { student ->
            dao.insertSyncedStudent(student)
            
            // Insertar registros históricos de asistencia para los reportes detallados
            for (w in 1..weeks) {
                val isPresent = random.nextFloat() < (student.attendanceCount.toFloat() / weeks)
                dao.insertAttendanceRecord(
                    AttendanceRecord(
                        studentCardId = student.cardId,
                        studentName = "${student.lastName}, ${student.firstName}",
                        sectionName = sectionName,
                        classNumber = w,
                        date = System.currentTimeMillis() - (weeks - w) * 7 * 24 * 60 * 60 * 1000L,
                        status = if (isPresent) "Presente" else "Ausente"
                    )
                )
            }
        }
        AppLogger.i("TestDataInjector", "Inyectados exitosamente $studentsCount alumnos y $weeks encuentros de historial.")
    }
}

package com.example.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.example.data.CoursePack
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Repositorio encargado de cargar, parsear y validar el Course Pack externo
 * desde una carpeta SAF (Storage Access Framework).
 */
class CourseRepository(
    private val context: Context
) {

    private val moshi = CoursePackMoshi.moshi
    private val TAG = "CourseRepository"

    suspend fun loadCoursePack(rootFolderUri: Uri): LoadResult<CoursePack> = withContext(Dispatchers.IO) {
        try {
            val rootDoc = try {
                if (rootFolderUri.scheme == "file") {
                    val file = java.io.File(rootFolderUri.path ?: "")
                    DocumentFile.fromFile(file)
                } else {
                    DocumentFile.fromTreeUri(context, rootFolderUri)
                }
            } catch (e: Exception) {
                null
            } ?: return@withContext LoadResult.Failure(listOf(ValidationError(
                    file = "ROOT",
                    pointer = "",
                    message = "Error al intentar acceder a la carpeta. Verifica los permisos de Android."
                )))

            if (!rootDoc.exists() || !rootDoc.canRead()) {
                return@withContext LoadResult.Failure(listOf(ValidationError(
                    file = "ROOT",
                    pointer = "",
                    message = "La carpeta no existe o no tiene permisos de lectura."
                )))
            }

            val csvFile = rootDoc.findFile("clase.csv")
            if (csvFile != null) {
                return@withContext loadFromCsv(rootDoc, csvFile.uri)
            }

            val indexFile = rootDoc.findFile("course_index.json")
                ?: return@withContext LoadResult.Failure(listOf(ValidationError(
                    file = "ROOT",
                    pointer = "",
                    message = "No se encontró 'course_index.json' en la raíz de la carpeta."
                )))

            val indexJson = readFileToString(indexFile.uri)
                ?: return@withContext LoadResult.Failure(listOf(ValidationError(
                    file = "course_index.json",
                    pointer = "",
                    message = "El archivo course_index.json está vacío o no se pudo leer."
                )))

            val courseIndex: CourseIndex
            try {
                courseIndex = moshi.adapter(CourseIndex::class.java).fromJson(indexJson)
                    ?: throw IllegalStateException("Parsing devolvió null")
            } catch (e: Exception) {
                return@withContext LoadResult.Failure(listOf(ValidationError(
                    file = "course_index.json",
                    pointer = "",
                    message = "Error de formato JSON en course_index.json: ${e.message}"
                )))
            }

            val unitsMap = mutableMapOf<String, UnitPack>()
            val allErrors = mutableListOf<ValidationError>()
            val enabledUnits = courseIndex.units.filter { it.enabled }
            
            for (unitRef in enabledUnits) {
                val unitFile = findFileRecursive(rootDoc, unitRef.file)
                if (unitFile == null) {
                    allErrors.add(ValidationError(unitRef.file, "", "Archivo de unidad no encontrado: ${unitRef.file}"))
                    continue
                }

                val unitJson = readFileToString(unitFile.uri)
                if (unitJson == null) {
                    allErrors.add(ValidationError(unitRef.file, "", "No se pudo leer el archivo de unidad."))
                    continue
                }

                val unitPack: UnitPack
                try {
                    unitPack = moshi.adapter(UnitPack::class.java).fromJson(unitJson)
                        ?: throw IllegalStateException("Parsing UnitPack devolvió null")
                } catch (e: Exception) {
                    allErrors.add(ValidationError(unitRef.file, "", "Error de formato JSON en unidad: ${e.message}"))
                    continue
                }

                if (unitPack.id != unitRef.id) {
                    allErrors.add(ValidationError(unitRef.file, "/id", "ID en archivo (${unitPack.id}) no coincide con índice (${unitRef.id})", "ERROR"))
                }

                val mediaErrors = validateMediaFiles(rootDoc, unitPack, unitRef.id)
                allErrors.addAll(mediaErrors)
                unitsMap[unitRef.id] = unitPack
            }

            val crossErrors = CoursePackValidator.validateCoursePack(courseIndex, unitsMap)
            allErrors.addAll(crossErrors)

            val blockingErrors = allErrors.filter { it.severity == "ERROR" }
            val coursePack = CoursePack(courseIndex, unitsMap)

            if (blockingErrors.isNotEmpty()) {
                LoadResult.Failure(blockingErrors)
            } else if (allErrors.isNotEmpty()) {
                LoadResult.PartialSuccess(coursePack, allErrors)
            } else {
                LoadResult.Success(coursePack)
            }

        } catch (e: Exception) {
            AppLogger.e(TAG, "Error crítico cargando Course Pack", e)
            LoadResult.Failure(listOf(ValidationError("SYSTEM", "", "Error inesperado: ${e.message}")))
        }
    }

    private suspend fun loadFromCsv(rootDoc: DocumentFile, uri: Uri): LoadResult<CoursePack> = withContext(Dispatchers.IO) {
        try {
            val csvText = readFileToString(uri) ?: return@withContext LoadResult.Failure(listOf(ValidationError("clase.csv", "", "No se pudo leer el archivo CSV")))
            val lines = csvText.lines().map { it.trim() }.filter { it.isNotBlank() }
            if (lines.isEmpty()) return@withContext LoadResult.Failure(listOf(ValidationError("clase.csv", "", "El archivo CSV está vacío")))

            val unitsMap = mutableMapOf<String, UnitPack>()
            val unitRefs = mutableListOf<UnitRef>()
            val allErrors = mutableListOf<ValidationError>()

            var courseTitle = "Curso Aula Local (CSV)"
            var courseVersion = 1
            var startIndex = 0

            // 1. Detección de Fila de Metadatos (COURSE_INFO) y Encabezados
            val firstLineParts = lines[0].split("|").map { it.trim() }
            if (firstLineParts.isNotEmpty() && firstLineParts[0].equals("COURSE_INFO", ignoreCase = true)) {
                if (firstLineParts.size >= 2 && firstLineParts[1].isNotEmpty()) {
                    courseTitle = firstLineParts[1]
                }
                if (firstLineParts.size >= 3) {
                    courseVersion = firstLineParts[2].toIntOrNull() ?: 1
                }
                if (lines.size > 1) {
                    val secondLineParts = lines[1].split("|").map { it.trim() }
                    if (secondLineParts[0].equals("INDEX", ignoreCase = true) || 
                        secondLineParts[0].equals("ID", ignoreCase = true) || 
                        secondLineParts[0].toIntOrNull() == null) {
                        startIndex = 2 // Fila 0 = COURSE_INFO, Fila 1 = Encabezados
                    } else {
                        startIndex = 1 // Fila 0 = COURSE_INFO, Fila 1 = Datos
                    }
                }
            } else {
                if (firstLineParts[0].toIntOrNull() == null) {
                    startIndex = 1 // Fila 0 es Encabezado
                } else {
                    startIndex = 0 // Fila 0 es Datos
                }
            }

            // 2. Procesamiento y Validación Rápida de cada Línea
            for (i in startIndex until lines.size) {
                val line = lines[i]
                val lineNum = i + 1
                val parts = line.split("|")

                if (parts.size < 9) {
                    allErrors.add(ValidationError(
                        file = "clase.csv",
                        pointer = "Línea $lineNum",
                        message = "❌ Línea $lineNum incompletas (${parts.size}/9 columnas). Verifica que el separador sea '|'.",
                        severity = "ERROR"
                    ))
                    continue
                }

                val index = parts[0].trim().toIntOrNull() ?: (i - startIndex + 1)
                val title = parts[1].trim()
                val desc = parts[2].trim()
                val narrator = parts[3].trim().replace("\\n", "\n")
                val theory = parts[4].trim().replace("\\n", "\n")
                val video = parts[5].trim().takeIf { it.isNotEmpty() && it != "null" }
                val qText = parts[6].trim()
                val qOptions = parts[7].trim().split(";").map { it.trim() }.filter { it.isNotEmpty() }
                val qCorrect = parts[8].trim()

                if (title.isEmpty()) {
                    allErrors.add(ValidationError("clase.csv", "Línea $lineNum", "⚠️ El título de la unidad en la línea $lineNum está vacío.", "WARNING"))
                    continue
                }

                val unitId = "unit_$index"

                // Validación de Retos
                if (qText.isEmpty()) {
                    allErrors.add(ValidationError(unitId, "/challenge", "⚠️ Pregunta del reto vacía en unidad '$title'.", "WARNING"))
                }
                if (qOptions.isEmpty()) {
                    allErrors.add(ValidationError(unitId, "/challenge", "⚠️ No hay opciones de respuesta en la unidad '$title'.", "WARNING"))
                } else if (qCorrect.isNotEmpty() && !qOptions.contains(qCorrect)) {
                    allErrors.add(ValidationError(unitId, "/challenge", "⚠️ La opción correcta ('$qCorrect') no coincide con las opciones (${qOptions.joinToString(",")}) en '$title'.", "WARNING"))
                }

                // Validación de Examen (Sintaxis con * y ~)
                val examQuestions = mutableListOf<ExamQuestion>()
                if (parts.size >= 10) {
                    val examData = parts[9].trim()
                    if (examData.isNotEmpty() && examData != "null") {
                        val qBlocks = examData.split("*")
                        for ((qIdx, block) in qBlocks.withIndex()) {
                            val qParts = block.split("~") 
                            if (qParts.size >= 3) {
                                val text = qParts[0].trim()
                                val options = qParts[1].split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                val correctIdx = qParts[2].trim().toIntOrNull() ?: 0

                                if (text.isEmpty()) {
                                    allErrors.add(ValidationError(unitId, "/exam", "⚠️ La pregunta ${qIdx + 1} del examen en '$title' no tiene enunciado.", "WARNING"))
                                }
                                if (options.size < 2) {
                                    allErrors.add(ValidationError(unitId, "/exam", "⚠️ La pregunta ${qIdx + 1} del examen en '$title' requiere al menos 2 opciones.", "WARNING"))
                                }
                                if (correctIdx < 0 || correctIdx >= options.size) {
                                    allErrors.add(ValidationError(unitId, "/exam", "❌ Índice de respuesta ($correctIdx) fuera de rango en pregunta ${qIdx + 1} de '$title'.", "ERROR"))
                                }

                                examQuestions.add(ExamQuestion(
                                    id = java.util.UUID.randomUUID().toString(),
                                    text = text,
                                    type = "single_choice",
                                    options = options,
                                    correctIndex = correctIdx,
                                    weight = 1
                                ))
                            } else if (block.isNotBlank()) {
                                allErrors.add(ValidationError(unitId, "/exam", "❌ Formato de examen incorrecto en pregunta ${qIdx + 1} de '$title'. Usar: 'Pregunta~Opción1,Opción2~ÍndiceCorrecto'", "ERROR"))
                            }
                        }
                    }
                }

                val unitPack = UnitPack(
                    id = unitId,
                    index = index,
                    title = title,
                    description = desc,
                    narrator = NarratorBlock(text = narrator),
                    theory = TheoryBlock(fallbackText = theory),
                    challenge = ChallengeBlock(
                        title = "Reto de Unidad",
                        instructions = qText,
                        options = qOptions.map { ChallengeOption(it, it, it == qCorrect) },
                        feedback = ChallengeFeedback()
                    ),
                    exam = ExamBlock(questions = examQuestions),
                    extendedContent = if (video != null) listOf(ExtendedBlock(type = "VIDEO", content = video, mediaFile = video)) else emptyList()
                )

                // Validación de Archivo de Video (Búsqueda Case-Insensitive en Raíz y /videos_clase/)
                video?.let { vFile ->
                    val foundVideo = findFileRecursive(rootDoc, vFile) 
                        ?: findFileRecursive(rootDoc, "videos_clase/$vFile")
                        ?: findFileRecursive(rootDoc, "videos_clase/${vFile.substringAfterLast("/")}")
                    if (foundVideo == null) {
                        allErrors.add(ValidationError(unitId, "/video", "❌ Falta el video '$vFile' en la carpeta o en /videos_clase/.", "WARNING"))
                    }
                }

                unitsMap[unitId] = unitPack
                unitRefs.add(UnitRef(unitId, "clase.csv", index))
            }

            val courseMeta = CourseMeta(id = "csv_course", title = courseTitle, version = courseVersion)
            val courseIndex = CourseIndex(courseMeta, unitRefs)
            val coursePack = CoursePack(courseIndex, unitsMap)

            val blockingErrors = allErrors.filter { it.severity == "ERROR" }
            if (blockingErrors.isNotEmpty()) {
                LoadResult.Failure(allErrors)
            } else if (allErrors.isNotEmpty()) {
                LoadResult.PartialSuccess(coursePack, allErrors)
            } else {
                LoadResult.Success(coursePack)
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error procesando CSV", e)
            LoadResult.Failure(listOf(ValidationError("clase.csv", "", "Error de sistema al procesar CSV: ${e.localizedMessage}")))
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
            if (current == null) return null
        }
        return current
    }

    private fun readFileToString(uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader -> reader.readText() }
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error leyendo archivo $uri", e)
            null
        }
    }

    private fun validateMediaFiles(rootDoc: DocumentFile, unitPack: UnitPack, unitId: String): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        val assetsDir = findFileRecursive(rootDoc, "videos_clase")
        if (assetsDir == null) return errors
        
        unitPack.narrator.audioFile?.let { file ->
            if (findFileRecursive(rootDoc, file)?.exists() != true) {
                errors.add(ValidationError(unitId, "/narrator/audioFile", "Archivo de audio no encontrado: $file", "WARNING"))
            }
        }
        return errors
    }
}

data class CoursePack(
    val index: CourseIndex,
    val units: Map<String, UnitPack>
)

package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ============================================================
// CONFIGURACIÓN MOSHI (Singleton para usar en Repository)
// ============================================================
object CoursePackMoshi {
    // Lenient: permite comas finales, comillas simples, etc. Ideal para JSONs editados a mano.
    // KotlinJsonAdapterFactory: Soporte nativo para data classes, sealed classes, nullable types.
    val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
}

// ============================================================
// 1. ÍNDICE MAESTRO (course_index.json)
// ============================================================
@JsonClass(generateAdapter = true)
data class CourseIndex(
    @Json(name = "courseMeta") val courseMeta: CourseMeta,
    @Json(name = "units") val units: List<UnitRef>
)

@JsonClass(generateAdapter = true)
data class CourseMeta(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "version") val version: Int,
    @Json(name = "minAppVersion") val minAppVersion: String = "1.0.0",
    @Json(name = "author") val author: String = "Profesor",
    @Json(name = "description") val description: String = ""
)

@JsonClass(generateAdapter = true)
data class UnitRef(
    @Json(name = "id") val id: String,
    @Json(name = "file") val file: String,
    @Json(name = "order") val order: Int,
    @Json(name = "enabled") val enabled: Boolean = true
)

// ============================================================
// 2. UNIDAD COMPLETA (units/unit_XX.json)
// ============================================================
@JsonClass(generateAdapter = true)
data class UnitPack(
    @Json(name = "id") val id: String,
    @Json(name = "index") val index: Int,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String,
    @Json(name = "estimatedHours") val estimatedHours: Int = 2,

    @Json(name = "narrator") val narrator: NarratorBlock,
    @Json(name = "theory") val theory: TheoryBlock,
    @Json(name = "challenge") val challenge: ChallengeBlock,
    @Json(name = "exam") val exam: ExamBlock,

    @Json(name = "extendedContent") val extendedContent: List<ExtendedBlock> = emptyList(),
    @Json(name = "labs") val labs: List<LabBlock> = emptyList()
)

@JsonClass(generateAdapter = true)
data class NarratorBlock(
    @Json(name = "text") val text: String,
    @Json(name = "audioFile") val audioFile: String? = null
)

@JsonClass(generateAdapter = true)
data class TheoryBlock(
    @Json(name = "format") val format: String = "markdown",
    @Json(name = "contentFile") val contentFile: String? = null,
    @Json(name = "fallbackText") val fallbackText: String = ""
)

@JsonClass(generateAdapter = true)
data class ChallengeBlock(
    @Json(name = "title") val title: String,
    @Json(name = "instructions") val instructions: String,
    @Json(name = "type") val type: String = "single_choice",
    @Json(name = "options") val options: List<ChallengeOption>,
    @Json(name = "feedback") val feedback: ChallengeFeedback
)

@JsonClass(generateAdapter = true)
data class ChallengeOption(
    @Json(name = "id") val id: String,
    @Json(name = "text") val text: String,
    @Json(name = "isCorrect") val isCorrect: Boolean
)

@JsonClass(generateAdapter = true)
data class ChallengeFeedback(
    @Json(name = "correct") val correct: String = "¡Correcto! Bien hecho.",
    @Json(name = "incorrect") val incorrect: String = "Incorrecto. Revisa la teoría e inténtalo de nuevo."
)

@JsonClass(generateAdapter = true)
data class ExamBlock(
    @Json(name = "passingScore") val passingScore: Int = 11,
    @Json(name = "questions") val questions: List<ExamQuestion>
)

@JsonClass(generateAdapter = true)
data class ExamQuestion(
    @Json(name = "id") val id: String,
    @Json(name = "text") val text: String,
    @Json(name = "type") val type: String = "single_choice",
    @Json(name = "options") val options: List<String>,
    @Json(name = "correctIndex") val correctIndex: Int,
    @Json(name = "weight") val weight: Int = 1
)

@JsonClass(generateAdapter = true)
data class ExtendedBlock(
    @Json(name = "type") val type: String, // "TIP" | "VIDEO" | "IMAGE" | "PDF" | "LINK"
    @Json(name = "title") val title: String? = null,
    @Json(name = "content") val content: String,
    @Json(name = "mediaFile") val mediaFile: String? = null
)

@JsonClass(generateAdapter = true)
data class LabBlock(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "instructions") val instructions: String,
    @Json(name = "question") val question: String,
    @Json(name = "type") val type: String = "single_choice",
    @Json(name = "options") val options: List<String>,
    @Json(name = "correctIndex") val correctIndex: Int
)

// ============================================================
// 5. VALIDADOR DE REGLAS DE NEGOCIO (Puro Kotlin, sin I/O)
// ============================================================
object CoursePackValidator {

    fun validateCoursePack(index: CourseIndex, units: Map<String, UnitPack>): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()

        // 1. Consistencia Índice <-> Unidades
        val indexIds = index.units.map { it.id }.toSet()
        val loadedIds = units.keys.toSet()

        indexIds.minus(loadedIds).forEach { id ->
            errors.add(ValidationError(
                file = "course_index.json",
                pointer = "/units/${index.units.indexOfFirst { it.id == id }}",
                message = "Unidad referenciada en índice pero no encontrada o falló al cargar: $id"
            ))
        }

        loadedIds.minus(indexIds).forEach { id ->
            errors.add(ValidationError(
                file = "units/$id.json",
                pointer = "/id",
                message = "Unidad cargada pero no referenciada en course_index.json: $id",
                severity = "WARNING"
            ))
        }

        // 2. Validar cada unidad individual
        units.forEach { (id, unit) ->
            errors.addAll(validateUnit(unit, id))
        }

        // 3. Orden y duplicados
        val orders = index.units.map { it.order }
        if (orders.distinct().size != orders.size) {
            errors.add(ValidationError(
                file = "course_index.json",
                pointer = "/units",
                message = "Existen números de orden (order) duplicados en el índice."
            ))
        }

        return errors
    }

    private fun validateUnit(unit: UnitPack, unitId: String): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        val basePointer = "/id"

        // IDs consistentes
        if (unit.id != unitId) {
            errors.add(ValidationError(file = "units/$unitId.json", pointer = "/id", message = "ID del archivo ($unitId) no coincide con ID interno (${unit.id})"))
        }
        if (unit.index !in 1..100) {
            errors.add(ValidationError(file = "units/$unitId.json", pointer = "/index", message = "Índice de unidad fuera de rango (1-100): ${unit.index}"))
        }

        // Narrator
        if (unit.narrator.text.isBlank()) {
            errors.add(ValidationError(file = "units/$unitId.json", pointer = "/narrator/text", message = "El texto del narrador está vacío"))
        }

        // Theory
        if (unit.theory.contentFile.isNullOrBlank() && unit.theory.fallbackText.isBlank()) {
            errors.add(ValidationError(file = "units/$unitId.json", pointer = "/theory", message = "Teoría sin archivo de contenido (contentFile) ni texto de respaldo (fallbackText)"))
        }

        // Challenge
        if (unit.challenge.options.size < 2) {
            errors.add(ValidationError(file = "units/$unitId.json", pointer = "/challenge/options", message = "El reto debe tener al menos 2 opciones"))
        } else {
            val correctCount = unit.challenge.options.count { it.isCorrect }
            if (correctCount != 1) {
                errors.add(ValidationError(file = "units/$unitId.json", pointer = "/challenge/options", message = "El reto debe tener EXACTAMENTE 1 opción correcta (encontradas: $correctCount)"))
            }
        }

        // Exam
        if (unit.exam.questions.isEmpty()) {
            errors.add(ValidationError(file = "units/$unitId.json", pointer = "/exam/questions", message = "El examen debe tener al menos 1 pregunta"))
        } else {
            unit.exam.questions.forEachIndexed { qIdx, q ->
                val qPointer = "/exam/questions/$qIdx"
                if (q.options.size < 2) {
                    errors.add(ValidationError(file = "units/$unitId.json", pointer = "$qPointer/options", message = "Pregunta ${q.id} debe tener al menos 2 opciones"))
                }
                if (q.correctIndex < 0 || q.correctIndex >= q.options.size) {
                    errors.add(ValidationError(file = "units/$unitId.json", pointer = "$qPointer/correctIndex", message = "Índice correcto inválido en pregunta ${q.id}: ${q.correctIndex}"))
                }
            }
        }

        // Extended Content & Labs (Warnings mostly)
        unit.extendedContent.forEachIndexed { idx, block ->
            if (block.type in setOf("VIDEO", "IMAGE", "PDF")) {
                if (block.mediaFile.isNullOrBlank()) {
                    errors.add(ValidationError(file = "units/$unitId.json", pointer = "/extendedContent/$idx/mediaFile", message = "Bloque ${block.type} requiere 'mediaFile'", severity = "WARNING"))
                }
            }
        }

        unit.labs.forEachIndexed { idx, lab ->
            if (lab.correctIndex < 0 || lab.correctIndex >= lab.options.size) {
                errors.add(ValidationError(file = "units/$unitId.json", pointer = "/labs/$idx/correctIndex", message = "Laboratorio ${lab.id}: índice correcto inválido"))
            }
        }

        return errors
    }
}
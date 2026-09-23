package com.example.data

import android.content.Context
import java.util.Locale
import com.example.data.model.ContentType
import com.example.data.model.CourseIndex
import com.example.data.model.CourseMeta
import com.example.data.model.CoursePackMoshi
import com.example.data.model.UnitData
import com.example.data.model.UnitPack
import com.example.data.model.UnitRef
import java.io.File
import java.io.FileWriter

/**
 * Utilidad para exportar los datos hardcodeados actuales (defaultUnits de MainViewModel)
 * a la estructura de Course Pack externo (JSON + carpetas).
 * 
 * USO: Ejecutar una vez desde un test o script temporal para generar la carpeta 'starter_course_pack'
 * en assets/ o en almacenamiento interno para depuración.
 * 
 * NO ES PARA PRODUCCIÓN - Solo para migración inicial.
 */
@Suppress("UNUSED_PARAMETER")
object CoursePackExporter {

    private val moshi = CoursePackMoshi.moshi

    /**
     * Genera el Course Pack completo basado en la lista de unidades proporcionada.
     * @param units Lista de UnitData (los datos actuales hardcodeados en MainViewModel)
     * @param outputDir Directorio base donde crear la estructura (ej: context.filesDir o assets/)
     * @param courseId ID único del curso
     * @param courseTitle Título visible
     * @param version Versión del curso
     */
    @Suppress("UNUSED_PARAMETER")
    fun exportCoursePack(
        context: Context,
        units: List<UnitData>,
        outputDir: File,
        courseId: String = "curso_mantenimiento_pc_2025_v1",
        courseTitle: String = "Mantenimiento y Ensamblaje de PC 2025",
        version: Int = 1
    ): Boolean {
        try {
            // 1. Limpiar/Crear directorios
            val rootDir = File(outputDir, "starter_course_pack")
            val unitsDir = File(rootDir, "units")
            val assetsDir = File(rootDir, "assets")
            
            if (rootDir.exists()) {
                deleteRecursively(rootDir)
            }
            unitsDir.mkdirs()
            assetsDir.mkdirs()

            // 2. Crear CourseIndex
            val unitRefs = units.mapIndexed { index, unit ->
                UnitRef(
                    id = "unit_${String.format(Locale.US, "%02d", unit.index)}",
                    file = "units/unit_${String.format(Locale.US, "%02d", unit.index)}.json",
                    order = unit.index,
                    enabled = true
                )
            }

            val courseIndex = CourseIndex(
                courseMeta = CourseMeta(
                    id = courseId,
                    title = courseTitle,
                    version = version,
                    minAppVersion = "2.0.0",
                    author = "Equipo Aula Local",
                    description = "Curso completo de mantenimiento, ensamblaje y diagnóstico de PC. 11 Unidades."
                ),
                units = unitRefs
            )

            // 3. Escribir course_index.json
            writeJson(rootDir, "course_index.json", courseIndex, CourseIndex::class.java)

            // 4. Convertir y escribir cada unidad
            units.forEach { unitData ->
                val unitPack = convertToUnitPack(unitData)
                val fileName = "unit_${String.format(Locale.US, "%02d", unitData.index)}.json"
                writeJson(unitsDir, fileName, unitPack, UnitPack::class.java)
                
                // 5. Generar archivos de assets placeholder (teoría, audio, etc.)
                generatePlaceholderAssets(assetsDir, unitData)
            }

            // 6. Generar README de instrucciones
            generateReadme(rootDir)

            println("✅ Course Pack exportado exitosamente en: ${rootDir.absolutePath}")
            return true

        } catch (e: Exception) {
            println("❌ Error exportando Course Pack: ${e.message}")
            e.printStackTrace()
            return false
        }
    }

    private fun convertToUnitPack(u: UnitData): UnitPack {
        return UnitPack(
            id = "unit_${String.format(Locale.US, "%02d", u.index)}",
            index = u.index,
            title = u.title,
            description = u.description,
            estimatedHours = 2,
            narrator = com.example.data.model.NarratorBlock(
                text = u.audioNarratorText,
                audioFile = "assets/unit_${String.format(Locale.US, "%02d", u.index)}_narrator.mp3"
            ),
            theory = com.example.data.model.TheoryBlock(
                format = "markdown",
                contentFile = "assets/unit_${String.format(Locale.US, "%02d", u.index)}_theory.md",
                fallbackText = u.topicText
            ),
            challenge = com.example.data.model.ChallengeBlock(
                title = u.challengeTitle,
                instructions = u.challengeInstructions,
                type = "single_choice",
                options = u.challengeOptions.mapIndexed { idx, text ->
                    com.example.data.model.ChallengeOption(
                        id = "opt_${idx + 1}",
                        text = text,
                        isCorrect = text == u.challengeAnswer
                    )
                },
                feedback = com.example.data.model.ChallengeFeedback(
                    correct = "¡Correcto! Has comprendido el concepto clave.",
                    incorrect = "Incorrecto. Revisa la teoría de la Unidad ${u.index} e inténtalo de nuevo."
                )
            ),
            exam = com.example.data.model.ExamBlock(
                passingScore = 11,
                questions = u.examQuestions.mapIndexed { idx, q ->
                    com.example.data.model.ExamQuestion(
                        id = "q${idx + 1}",
                        text = q.questionText,
                        type = "single_choice",
                        options = q.options,
                        correctIndex = q.correctIndex,
                        weight = 1
                    )
                }
            ),
            extendedContent = u.extendedContent.map { ext ->
                com.example.data.model.ExtendedBlock(
                    type = ext.type.name,
                    title = ext.title,
                    content = ext.content,
                    mediaFile = ext.mediaResId?.let { "assets/$it" }
                )
            },
            labs = u.labs.map { lab ->
                com.example.data.model.LabBlock(
                    id = "lab_${u.index}_${lab.title.replace(" ", "_").lowercase()}",
                    title = lab.title,
                    instructions = lab.instructions,
                    question = lab.question,
                    type = "single_choice",
                    options = lab.options,
                    correctIndex = lab.options.indexOf(lab.correctAnswer)
                )
            }
        )
    }

    private fun <T : Any> writeJson(dir: File, fileName: String, data: T, clazz: Class<T>) {
        val json = moshi.adapter(clazz).toJson(data)
        val file = File(dir, fileName)
        FileWriter(file).use { it.write(json) }
    }

    private fun generatePlaceholderAssets(assetsDir: File, unit: UnitData) {
        val prefix = "unit_${String.format(Locale.US, "%02d", unit.index)}"
        
        // 1. Teoría Markdown (contenido real)
        val theoryMd = """# ${unit.title}

## Resumen
${unit.summary}

## Contenido Teórico

${unit.topicText}

---
*Generado automáticamente desde CoursePackExporter. Edita este archivo .md para actualizar la teoría.*
""".trimIndent()
        File(assetsDir, "${prefix}_theory.md").writeText(theoryMd)

        // 2. Narrador Texto (para TTS si no hay audio)
        File(assetsDir, "${prefix}_narrator.txt").writeText(unit.audioNarratorText)

        // 3. Placeholders para multimedia (archivos vacíos con extensión correcta)
        // El profesor debe reemplazar estos archivos reales.
        File(assetsDir, "${prefix}_narrator.mp3").createNewFile() // Audio narrador
        File(assetsDir, "${prefix}_video_intro.mp4").createNewFile() // Video principal
        
        // Placeholders para extendedContent
        unit.extendedContent.forEach { ext ->
            ext.mediaResId?.let { mediaName ->
                val extFile = File(assetsDir, mediaName)
                if (!extFile.exists()) {
                    when (ext.type) {
                        ContentType.VIDEO -> extFile.writeText("[PLACEHOLDER VIDEO: Reemplazar con archivo .mp4 real]")
                        ContentType.IMAGE -> extFile.writeText("[PLACEHOLDER IMAGEN: Reemplazar con archivo .png/.jpg real]")
                        ContentType.TIP -> extFile.writeText("[PLACEHOLDER TIP: Contenido de texto]")
                        else -> extFile.writeText("[PLACEHOLDER]")
                    }
                }
            }
        }
    }

    private fun generateReadme(rootDir: File) {
        val readme = """
# 📦 Aula Local - Starter Course Pack v1.0

## Estructura
\`\`\`
starter_course_pack/
├── course_index.json          # Manifiesto principal (metadatos + índice de unidades)
├── units/                     # Una JSON por unidad (contenido académico puro)
│   ├── unit_01.json
│   ├── unit_02.json
│   └── ...
└── assets/                    # Recursos multimedia referenciados
    ├── unit_01_theory.md      # Teoría en Markdown
    ├── unit_01_narrator.mp3   # Audio para TTS (opcional)
    ├── unit_01_video_intro.mp4 # Video (opcional)
    └── ...
\`\`\`

## Cómo usar en la App
1. Copia **TODA la carpeta \`starter_course_pack\`** al almacenamiento del teléfono del profesor
   (ej: \`Documentos/AulaLocal_Curso_Redes/\`).
2. En la App, rol **Profesor** → Paso 0 → Botón **"Vincular Carpeta de Materiales"**.
3. Selecciona la carpeta \`starter_course_pack\` (la que contiene \`course_index.json\`).
4. La app validará automáticamente la estructura. Si hay errores, los mostrará en rojo.
5. Si todo OK → Botón "Continuar" se habilita → Configura tu perfil y secciones.

## Edición de Contenidos
- **Textos (Teoría, Retos, Exámenes)**: Edita los archivos `units/unit_XX.json` con VS Code (tiene autocompletado gracias a esquema JSON referenciado en el JSON).
- **Teoría Larga**: Edita \`assets/unit_XX_theory.md\` (Markdown: tablas, código, negritas).
- **Multimedia**: Reemplaza los placeholders \`.mp3\`, \`.mp4\`, \`.png\` en \`assets/\` por archivos reales.
  * Mantén los **nombres de archivo exactos** referenciados en los JSON.
- **Nuevas Unidades**: 
  1. Crea \`units/unit_12.json\` siguiendo el esquema.
  2. Añade entrada en \`course_index.json\` (incrementa \`order\`, \`enabled: true\`).
  3. Añade assets necesarios en \`assets/\`.

## Validación Offline (PC del Profesor)
Abre \`validate_course.html\` (incluido en el repo) en Chrome/Edge → Arrastra la carpeta \`starter_course_pack\` → Click "Validar".
Te dirá exactamente qué archivos faltan, IDs duplicados, índices incorrectos, etc.

## Notas Importantes
- **NO modifiques \`course_index.json\` a mano** si no entiendes la estructura de referencias.
- Los IDs (\`unit_01\`, \`q1\`, \`opt_a\`) deben ser **únicos globalmente**.
- \`correctIndex\` en exámenes/labs es **0-based** (0 = primera opción).
- \`isCorrect\` en retos: **solo una** opción debe ser \`true\`.
"""
        File(rootDir, "README.md").writeText(readme.trimIndent())
    }

    private fun deleteRecursively(file: File) {
        if (file.isDirectory) {
            file.listFiles()?.forEach { deleteRecursively(it) }
        }
        file.delete()
    }
}
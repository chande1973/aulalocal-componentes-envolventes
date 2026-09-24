# 📷 Plan de Diseño: Evaluación Offline de Resúmenes en Cuaderno (OCR Local + Rúbrica de 20 Puntos)

Este documento registra el diseño para la evaluación offline de resúmenes manuscritos en cuadernos físicos de los estudiantes.

---

## 🎯 1. Principios del Módulo

1. **Procesamiento 100% Offline (Sin Internet)**:
   - Se utiliza **Google ML Kit Text Recognition (OCR Local)** para extraer el texto manuscrito de la foto tomada del cuaderno.
   - El análisis del texto escaneado se realiza en el teléfono mediante comparación semántica local contra la lectura teórica (`unit.topicText`).

2. **Rúbrica de Evaluación sobre 20 Puntos**:
   - **Contenido y Comprensión (10 Pts)**: Verificación de presencia de conceptos e ideas clave de la lección teórica.
   - **Redacción y Coherencia (5 Pts)**: Estructura de párrafos, conectores y claridad de ideas.
   - **Ortografía y Gramática (5 Pts)**: Reglas ortográficas en español y reporte de correcciones.

3. **Cálculo Dinámico de la Nota Final (0 a 20 Puntos)**:
   - La nota final del curso se adapta dinámicamente al número total real de unidades ($N = 11$, $N = 8$, $N = 15$, etc.):
     $$\text{Nota Final} = \frac{\sum \text{Notas de Unidades Evaluadas}}{\text{Total de Unidades Evaluadas}}$$
   - Mantiene la escala estándar de `0.00` a `20.00` puntos, con aprobación en $\ge 11.50$.

---

## 🛠️ 2. Componentes a Implementar

### A. Lado del Alumno (`StudentStudyContent.kt` & `StudentViewModel.kt`)
- Botón **`📷 ESCANEAR RESUMEN EN MI CUADERNO`** al finalizar la lectura de la lección.
- Lanzador de Cámara / Galería para capturar la página del cuaderno.
- Escaneo OCR local en segundo plano e informe interactivo con el desglose de notas y correcciones ortográficas.

### B. Lado del Profesor (`TeacherAulaTab.kt` & `TeacherDialogs.kt`)
- Muestra la nota del resumen del cuaderno escaneado y las observaciones ortográficas en el expediente del estudiante.
- Sincronización automática de notas por la red Wi-Fi local.

---
*Documento de diseño guardado en el proyecto Aula Local.*

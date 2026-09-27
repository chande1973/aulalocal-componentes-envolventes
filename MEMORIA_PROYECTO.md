# 📚 Memoria y Registro de Desarrollo — Aula Local

Este documento sirve como **memoria activa e historial del proyecto** para que cualquier instancia de IA o desarrollador comprenda la arquitectura, el estado actual, el formato de datos y las últimas mejoras implementadas.

---

## 🎯 1. Propósito del Proyecto
**Aula Local** es una aplicación Android **offline-first** diseñada para la gestión de aulas de clases y estudio sin necesidad de conexión a Internet. 
Funciona mediante una red Wi-Fi local o punto de acceso (Hotspot) creado por el dispositivo del profesor:
- **Profesor (Host / Servidor)**: Crea la clase en vivo, transmite la señal por la red local (UDP/NSD), toma asistencia, envía material de clase en ZIP o archivos individuales, emite avisos y recibe preguntas/dudas en tiempo real.
- **Estudiante (Cliente)**: Auto-detecta el servidor del profesor en la red local, marca asistencia, descarga/sincroniza unidades de estudio, rinde exámenes y hace preguntas.

---

## 🏗️ 2. Arquitectura y Stack Tecnológico

| Módulo | Tecnología Utilizada | Archivos Clave |
| :--- | :--- | :--- |
| **UI Framework** | Jetpack Compose + Material 3 (Dark Theme) | `ClassroomUi.kt`, `SetupAssistantScreen.kt`, `TeacherDashboardScreen.kt`, `StudentDashboardScreen.kt` |
| **Arquitectura UI** | MVVM + Coroutines + `StateFlow` | `MainViewModel.kt`, `TeacherViewModel.kt`, `StudentViewModel.kt` |
| **Persistencia** | Room Database | `AppDatabase.kt`, `ClassroomDao.kt`, `Entities.kt` |
| **Red Local (P2P)** | TCP Sockets + UDP Broadcast / NSD | `ClassroomServer.kt`, `ClassroomClient.kt`, `DiscoveryService.kt`, `LocalNetworkManager.kt` |
| **Carga de Cursos** | CSV Parser + SAF DocumentFile + ZipUtils | `CourseRepository.kt`, `CoursePackExporter.kt`, `ZipUtils.kt` |

---

## 📄 3. Especificación del Formato de Curso (`clase.csv`)

La aplicación lee materiales educativos desde una carpeta externa seleccionada por el usuario (SAF). El archivo maestro de datos es **`clase.csv`**.

### A. Fila 1 (Opcional pero Recomendada): Metadatos `COURSE_INFO`
```csv
COURSE_INFO|Título del Curso|Versión|Descripción del Curso
```
*Ejemplo*:
`COURSE_INFO|Mantenimiento y Arquitectura de PC|1|Curso completo de hardware y ensamblaje`

### B. Filas de Contenido (10 Columnas requeridas)
`Indice|Titulo|Descripcion|Narrador|Teoria|Video|RetoPregunta|RetoOpciones|RetoCorrecta|ExamenData`

1. **`Indice`**: Número entero de la unidad (ej. `1`).
2. **`Titulo`**: Nombre de la unidad.
3. **`Descripcion`**: Breve descripción.
4. **`Narrador`**: Texto de la locución introductoria.
5. **`Teoria`**: Texto de lectura teórica (soporta formato Markdown y saltos de línea con `\n`).
6. **`Video`**: Nombre del archivo de video MP4 (ej. `videos_clase/video_unidad1.mp4`). Búsqueda insensible a mayúsculas/minúsculas.
7. **`RetoPregunta`**: Pregunta del reto rápido de unidad.
8. **`RetoOpciones`**: Opciones de respuesta del reto separadas por punto y coma `;` (ej. `Opción A;Opción B;Opción C`).
9. **`RetoCorrecta`**: Texto exacto de la opción correcta del reto.
10. **`ExamenData`**: Preguntas de examen concatenadas con `*`. Cada pregunta usa `~`:
    - `Enunciado~Opción1,Opción2,Opción3~ÍndiceCorrecto` (el índice inicia en `0`).
    - *Ejemplo*: `¿Cuál es el cerebro del PC?~RAM,CPU,Disco~1 * ¿Qué es un bit?~1 u 8,0 ó 1~1`

---

## ⏱️ 4. Historial de Mejoras e Implementaciones Recientes

### 🟢 Versión / Sprint Actual

1. **Versionado de Cursos y Metadatos CSV (`COURSE_INFO`)**:
   - Implementado el parseo de la etiqueta `COURSE_INFO` en `CourseRepository.kt`.
   - Si la versión leída es mayor a la almacenada (`v2 > v1`), la app notifica al estudiante en `StudentDashboardScreen.kt` mediante un banner destacado: *"¡Nueva versión (v2) del curso disponible!"*.

2. **Búsqueda Inteligente de Video (Case-Insensitive)**:
   - Se agregó `findFileCaseInsensitive()` en `CourseRepository.kt` y `MainViewModel.kt`.
   - Resuelve nombres de archivos en la raíz o en `/videos_clase/` independientemente de si la extensión es `.mp4` o `.MP4`.

3. **Validación Rápida "Anti-Errores"**:
   - `CourseRepository.kt` realiza una inspección previa ("pre-flight check") del CSV:
     - ❌ Detecta filas con número insuficiente de columnas.
     - ❌ Detecta errores de sintaxis en preguntas de examen (`*` y `~`).
     - ⚠️ Notifica si faltan archivos de video/audio en la carpeta.
   - Muestra un reporte visual interactivo en `SetupAssistantScreen.kt` al vincular materiales.

4. **Selección Dinámica de Carpeta para Envío ZIP (Profesor/Host)**:
   - El botón **"ENVIAR CARPETA DE UNIDADES (ZIP)"** en `TeacherDashboardScreen.kt` ahora abre el selector nativo de carpetas (`OpenDocumentTree`).
   - Permite seleccionar cualquier carpeta nueva/actualizada para comprimirla en `course_update.zip` y compartirla al instante por la red local, actualizando además la carpeta activa del curso.

5. **Acceso Directo para Cambiar/Vincular Carpeta en Estudiante**:
   - En la pestaña de clase (`StudentClassTab.kt`), se agregó un botón con icono de carpeta (📂) junto al título **"Contenido del Curso"**, permitiendo cambiar o revincular la carpeta de materiales en cualquier momento.

6. **Lógica de Exámenes Únicos y Examen Recuperativo**:
   - **Bloqueo tras presentar**: En cuanto un estudiante envía un examen, se registra `examCompleted = true` y su nota. El botón "REALIZAR EXAMEN" desaparece y muestra en su lugar una tarjeta verde informando: `✅ Examen Completado - Tu Calificación: XX.XX / 20`.
   - **Habilitación de Recuperativo (Profesor)**: En la pestaña de métricas, al pulsar sobre cualquier estudiante, se despliega el diálogo detallado (`StudentDetailAndRecuperativoDialog`). Si el alumno ya presentó una unidad, aparece el botón **`🔄 Habilitar Recuperativo`**.
   - **Sincronización en tiempo real**: Al presionar `Habilitar Recuperativo`, se resetea la nota en la BD del profesor y se envía un mensaje privado directo al alumno. Al sincronizar, la app del estudiante habilita automáticamente el examen de esa unidad para su re-intento.

7. **Orden Pedagógico de Contenidos (Lectura -> Video -> Examen)**:
   - Se reestructuró la pantalla de contenido de la unidad (`StudentStudyContent.kt`):
     1. **1º Lectura Teórica**: El estudiante lee primero el tema completo.
     2. **2º Video Explicativo**: A continuación se muestra el video de apoyo con el título `🎥 Video Explicativo de la Clase`.
     3. **3º Examen**: Al final de la página se presenta la opción de evaluación.

8. **Menú de Configuración y Consola de Logs en el Estudiante (⚙️ / 🐛)**:
   - Se agregó el botón de **Configuración (⚙️)** en la barra superior del estudiante (`StudentDashboardScreen.kt`).
   - Abre un diálogo que contiene la opción **`Ver Registro de Consola / Logs (🐛)`** (`LogViewerDialog`) para inspeccionar o compartir registros del sistema.

9. **Edición de Datos de Perfil con Autorización del Profesor (🔒 ➔ 🔓)**:
   - **Campos protegidos**: En `StudentProfileTab.kt`, los campos de Nombre, Apellido, Cédula, Sección y Avatar están bloqueados por defecto para prevenir alteraciones no autorizadas.
   - **Desbloqueo Dual**:
     - *Desbloqueo Remoto*: El alumno envía una solicitud por Wi-Fi y el profesor presiona **`🔓 AUTORIZAR EDICIÓN DE PERFIL`** desde el diálogo del estudiante (`StudentDetailAndRecuperativoDialog`).
     - *PIN Maestro del Docente*: Permite desbloqueo presencial rápido usando el PIN `1234`.
   - Al guardar los cambios, la app actualiza el perfil local y re-sincroniza los nuevos datos con la base de datos del profesor.

10. **Nueva Semaforización Pedagógica Inicial ("AL DÍA")**:
    - Se eliminó el falso estado en rojo ("EN RIESGO") al instalar la app o iniciar un curso.
    - Cuando el curso está iniciando (0 clases dadas / 0 exámenes rendidos), el alumno inicia automáticamente en estado **`AL DÍA`** (Color Azul Cyan `AccentCyan`) con el mensaje *"Curso iniciado. ¡Bienvenido!"*.
    - Tanto en el panel del estudiante (`AcademicStatusCard`) como en la tabla de semaforización del profesor (`TeacherMetricsTab.kt`), solo se muestra alerta si hay evaluaciones reprobadas o inasistencias reales.

11. **Sistema de Racha de Constancia (🔥 *Streak*)**:
    - Se integró la tarjeta **`🔥 Racha de Constancia`** (`StreakCard`) en la vista de estatus (`StudentStatusTab.kt`).
    - Contabiliza y premia la asistencia consecutiva y unidades completadas con mensajes motivacionales (*"🔥 ¡Vas genial! Llevas N interacciones en racha"*).

12. **Simplificación del Control de Unidades del Profesor (`[ 🟢 IMPARTIR CLASE ]`)**:
    - Se eliminó la duplicidad confusa de controles (tarjeta vs. switch "Leer").
    - Se unificó en un único botón principal de acción directa: **`[ 🟢 IMPARTIR CLASE ]`** / **`[ 📡 IMPARTIENDO EN VIVO ]`**.
    - Un solo clic activa la unidad para la transmisión en vivo por la red local Y desbloquea su contenido de lectura para todos los estudiantes en un solo paso.
    - Se mantiene el chip independiente **`[ 📝 EXAMEN ]`** para activar evaluaciones según el criterio del profesor.

13. **Banner Persistente de Clase Finalizada en la Vista del Alumno (`CLASE FINALIZADA`)**:
    - Se incorporó una tarjeta verde destacada en la pantalla del estudiante (`StudentDashboardBanners.kt`).
    - Al concluir la clase por el profesor, el alumno ve la confirmación visual permanente:  
      > **`✅ CLASE FINALIZADA`**  
      > *"La clase de hoy ha concluido. Tu asistencia ha sido registrada."*

14. **Gamificación de Lectura, Candado Estricto de Video y Monitoreo del Profesor**:
    - **Barra de Avance de Lectura**: Muestra el progreso de desplazamiento en tiempo real (`0%` a `100%`) en la pantalla del alumno (`StudentStudyContent.kt`).
    - **Candado de Video Estricto**: El video explicativo permanece bloqueado (`🔒 Video Bloqueado`) hasta que el alumno se desplace al final del texto y presione **`[ 📖 COMPLETAR LECTURA Y DESBLOQUEAR VIDEO ]`**.
    - **Monitoreo Grupal en Tiempo Real**: En la pestaña Aula del profesor (`TeacherAulaTab.kt`), se muestra la tarjeta de avance de lectura de la clase en vivo (`📖 Progreso de Lectura: X / Y Alumnos - Z%`).
    - **Monitoreo Individual por Alumno**: En el diálogo detallado del alumno (`StudentDetailAndRecuperativoDialog`), se muestra el estado de lectura de cada unidad (`📖 Lectura: Completada` vs `⏳ Lectura: Pendiente`).

15. **Memoria de Continuidad de Lectura (*Scroll Memory*) para Cursos de 36 Semanas**:
    - **Persistencia de Desplazamiento**: `UnitProgress` guarda en Room DB la posición exacta de lectura (`lastScrollPosition`) y el porcentaje de avance acumulado (`readPercentage`).
    - **Reanudación Automática**: Al reabrir una unidad en posteriores encuentros/clases de 2 horas, la pantalla se desplaza automáticamente al punto exacto donde se quedó el alumno.
    - **Monitoreo Grupal Porcentual**: El profesor observa el porcentaje de avance acumulado real de la clase (`📖 Avance Grupal: X / Y Alumnos - Z% Promedio`).

16. **Diseño Estético E-Book "Modo Descanso" para Lecturas de 2 Horas**:
    - **Lector Inteligente de Tamaño de Letra (`A-` / `A+`)**: Permite al alumno ajustar dinámicamente el tamaño de la fuente (`13sp` a `22sp`) según su comodidad visual.
    - **Tarjeta E-Book con Formateador**: Estructura el texto teórico en una tarjeta con margen amplio y resalta automáticamente títulos y encabezados en color Cyan (`AccentCyan`) con viñetas organizadas.
    - **Banner de Tiempo Estimado**: Indica el tiempo aproximado de lectura (`~10-15 min`) y el porcentaje de avance acumulado.

17. **Icono Oficial de Instalación (Opción 1: Escudo Académico Conectado)**:
    - Se configuró como el *Adaptive Launcher Icon* oficial de la app.
    - Diseño vectorial con escudo protector Teal/Cyan, birrete académico blanco con borla dorada y ondas de señal Wi-Fi flotantes sobre fondo azul noche (`ic_launcher_background.xml` e `ic_launcher_foreground.xml`).

18. **Blindaje de Cierre de Clase contra Fallas de Red**:
    - **Protocolo de Cierre Explícito (`CMD_CLASS_FINALIZE_OFFICIAL`)**: Una clase solo se concluye en la app del estudiante cuando recibe la orden de cierre explícita emitida por el profesor al presionar "FINALIZAR CLASE".
    - **Tolerancia e Inmunidad a Caídas de Wi-Fi**: Se eliminó la desconexión por falsos positivos de red. Si el Wi-Fi parpadea o se interrumpe, la app del alumno muestra un aviso inofensivo (`⚠️ Conexión Inestable`) y reintenta conectarse automáticamente en segundo plano sin cerrar la sesión.

19. **Corrección de Falso 100% de Lectura y Semáforo de Eficiencia de Alumnos**:
    - **Solución al Falso 100%**: Se corrigió el cálculo de `scrollProgress` en `StudentStudyContent.kt` durante el primer frame de medición no calculado. Ahora no asume 100% en frame 0 y exige desplazamiento real o clic en `[ 📖 COMPLETAR LECTURA ]`.
    - **Indicador de Eficiencia para el Profesor**: En `StudentDetailAndRecuperativoDialog`, el profesor ve el avance exacto de cada alumno con semáforo inteligente (`🟢 Completada 100%`, `🔵 En Progreso 50-99%`, `🟡 Iniciada 1-49%`, `🔴 Sin Iniciar 0%`).

20. **Eliminación Definitiva de Marcado Automático de Lectura (Falso 100%)**:
    - **Causa Raíz Encontrada y Eliminada**: Se identificó un disparador legacy en `StudentDashboardScreen.kt` (`LaunchedEffect(studyingUnit) { markUnitAsRead(...) }`) que marcaba las unidades como leídas al instante de abrirlas.
    - **Resultado**: Se eliminó ese disparador. Ahora la lectura inicia strictly en `0%`, el video permanece bloqueado y solo se completa al desplazarse hasta el final del tema.

21. **Mini-Reto de Comprensión Lectora y Desbloqueo de Video (+50 Pts 🔥)**:
    - **Trivia Activa de 2 Preguntas**: Al completar el desplazamiento de la lección teórica, se habilita el botón `[ 🎯 INICIAR MINI-RETO DE COMPRENSIÓN ]`.
    - **Retroalimentación y Bonificación**: Al acertar las 2 preguntas de comprensión, la app otorga **+50 Puntos de Racha (🔥)**, marca la lectura como completada y desbloquea el video explicativo.
    - **Insignia en Vista del Profesor**: En el expediente del alumno (`TeacherDialogs.kt`), aparece la insignia `⭐ Reto Superado`.

22. **Fix: Entrada a Clase Activa - Estado de Sincronización Inmediato** (`StudentClassTab.kt`)**:
    - **Problema**: Tras hacer check-in, el alumno veía la pantalla de "Bienvenido/Vincular carpeta" en lugar de "Sincronizando materiales..." porque `isLocked=true` pero `isSyncing=false` (primer sync no completado).
    - **Solución**: Mostrar pantalla de sincronización en cuanto `isLocked=true`. Mensaje dinámico: "Sincronizando..." si `isSyncing=true`, o "Conectado. Esperando primera sincronización..." si `isSyncing=false`.

23. **Fix Completo: Métricas e Historial de Asistencia al Finalizar Clase** (Opción B - Implementación Robusta):
    - **Problema**: Al cerrar la clase, el alumno mostraba `totalClasses=0`, `attendanceCount` no actualizado, y historial vacío.
    - **Causa raíz**: Servidor se apagaba antes del último sync del alumno; historial solo en BD del profesor.
    - **Solución multi-archivo**:
        - **`TeacherViewModel.kt`** (`incrementTotalClasses`): Actualiza `totalClasses` en config del servidor **antes** de apagar + espera 4s (`delay(4000)`) para último sync de alumnos.
        - **`Entities.kt`**: Nuevo método DAO `getAttendanceByStudentList(cardId)` para consulta síncrona.
        - **`ClassroomServer.kt`** (`ACTION_SYNC`): Envía `attendanceHistory` (JSONArray con `date` + `status`) en cada respuesta de sync.
        - **`LocalNetworkManager.kt`** (`syncWithTeacher`): Callback simplificado para recibir `JSONObject` completo y acceder a campos nuevos sin romper firma.
        - **`StudentViewModel.kt`** (`startStudentSyncLoop`): Procesa `attendanceHistory` recibido y lo guarda en BD local del alumno (`attendance_history`).
    - **Resultado**: Alumno ahora ve `totalClasses` correcto, `attendanceCount` actualizado, **y historial fechado completo** en pestaña Estado.

24. **Rediseño Estético y UX Moderno de la Vista del Alumno y Sincronización** (`StudentClassTab.kt` y `StudentComponents.kt`):
    - **Tarjetas de Unidades Enriquecidas (`UnitCard`)**: Insignias dinámicas de recursos (*📖 Teoría*, *🎥 Video/Media*, *⚡ Reto*, *📝 Examen* con conteo), badges de número de unidad (`01`, `02`), descripciones de contenido y estados visuales limpios (*COMPLETADA*, *DISPONIBLE*, *BLOQUEADA*).
    - **Tarjeta de Progreso General del Curso**: Muestra porcentaje global de avance, conteo de unidades leídas vs. totales y barra de progreso lineal.
    - **Pantalla de Espera de Sincronización Avanzada**: Reemplazó el círculo giratorio simple por una pantalla inmersiva que incluye contenedor con icono de red, badge de **`📡 SESIÓN EN VIVO ACTIVA`**, barra de progreso lineal y consejos de conexión para el alumno.

---

## 📌 5. Bitácora de Siguientes Pasos y Mejoras Solicitadas por el Usuario

- [x] **Mejora 1**: Botón de envío ZIP que permite seleccionar dinámicamente la carpeta a comprimir en lugar de usar únicamente la inicial.
- [x] **Mejora 2**: Lógica de examen único y habilitación de Recuperativo por estudiante desde el panel del profesor.
- [x] **Mejora 3**: Reordenamiento pedagógico de la pantalla de unidad (Lectura Teórica -> Video -> Examen).
- [x] **Mejora 4**: Menú de Configuración en el alumno con acceso a Consola de Logs (🐛) y Edición de datos protegida con Autorización del Profesor (remota por Wi-Fi o PIN Maestro `1234`).
- [x] **Mejora 5**: Nueva semaforización pedagógica (Inicio limpio en **AL DÍA** 🔵) y Sistema de **Racha de Constancia (🔥)**.
- [x] **Mejora 6**: Simplificación del control de unidades del profesor a un único botón unificado **`[ 🟢 IMPARTIR CLASE ]`**.
- [x] **Mejora 7**: Banner visual permanente de **`CLASE FINALIZADA`** en la pantalla principal del alumno.
- [x] **Mejora 8**: Gamificación de lectura, Candado Estricto de Video (Lectura ➔ Video) y Monitoreo de Lectura Grupal e Individual para el profesor.
- [x] **Mejora 9**: Memoria de Continuidad de Lectura (*Scroll Memory*) y avance porcentual acumulado para clases de 36 semanas / 2 horas.
- [x] **Mejora 10**: Rediseño estético E-Book "Modo Descanso" con controles de tamaño de letra (`A-` / `A+`), encabezados Cyan y tiempo de lectura estimado.
- [x] **Mejora 11**: Configuración del icono de instalación oficial **Opción 1 (Escudo Académico Conectado)**.
- [x] **Mejora 12**: Blindaje de Cierre de Clase contra caídas temporales de Wi-Fi y Protocolo de Cierre Explícito (`CMD_CLASS_FINALIZE_OFFICIAL`).
- [x] **Mejora 13**: Corrección de cálculo de scroll e Eliminación Definitiva del disparador automático de marcado de lectura al abrir unidad.
- [x] **Mejora 14**: Mini-Reto de Comprensión Lectora (Trivia de 2 preguntas, +50 Pts Racha 🔥 y Desbloqueo de Video).
- [x] **Mejora 15**: Rediseño estético y UX avanzado de la vista del alumno, tarjetas de unidades enriquecidas y pantalla de espera de sincronización.
- [ ] **Plan Registrado**: Evaluación OCR Offline de Resúmenes en Cuaderno con Cámara + Rúbrica de 20 Pts (Contenido 10 Pts, Redacción 5 Pts, Ortografía 5 Pts) y Promedio Dinámico para $N$ Unidades (Detalles en [`PLAN_EVALUACION_OCR.md`](file:///media/edgar/f/Aulalocal/AulaLocal_Backup-desarrollo/PLAN_EVALUACION_OCR.md)).
- [ ] *(Espacio para ir anotando las siguientes observaciones a medida que se pruebe la app)*

---
*Este archivo debe actualizarse continuamente tras cada ciclo de mejoras.*

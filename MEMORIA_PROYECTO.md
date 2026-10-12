# 📚 Memoria Unificada del Proyecto — Aula Local

Este documento sirve como **única fuente de verdad, memoria activa e historial del proyecto**, unificando la arquitectura, estándares de desarrollo, diseño OCR y la bitácora de mejoras.

---

## 🎯 1. Propósito y Arquitectura del Proyecto
**Aula Local** es una aplicación Android **offline-first** diseñada para la gestión de aulas de clases y estudio sin necesidad de conexión a Internet. 
Funciona mediante una red Wi-Fi local o punto de acceso (Hotspot) creado por el dispositivo del profesor:
- **Profesor (Host / Servidor)**: Crea la clase en vivo, transmite la señal por la red local (UDP/NSD), toma asistencia, envía material de clase en ZIP o archivos individuales, emite avisos y recibe preguntas/dudas en tiempo real.
- **Estudiante (Cliente)**: Auto-detecta el servidor del profesor en la red local, marca asistencia, descarga/sincroniza unidades de estudio, rinde exámenes y hace preguntas.

---

## 📐 2. Blueprint y Estándares de Desarrollo Profesional
**Basado en la Arquitectura de Aula Local (Versión Oro 2026)**

### A. Estructura de Paquetes (Arquitectura Limpia)
- `com.example.ui`: Todo lo visual (Screens, ViewModels, Theme).
- `com.example.domain`: El cerebro. Reglas de negocio y Managers (`AttendanceManager`, `CourseManager`, `ReportManager`).
- `com.example.network`: La infraestructura de red (Server, Client, Discovery).
- `com.example.data`: Base de datos (Room), Entities y DAOs.
- `com.example.util`: Herramientas genéricas (Logs, Zips).

### B. Reglas de Oro de Implementación
- **Cero Hardcode:** Prohibido escribir texto directo en el código Kotlin. Todo debe ir en `res/values/strings.xml`.
- **ViewModel Delgado:** Si hay lógica de cálculo pesada, se delega a un Manager en `domain`.
- **Redes Atómicas y Sesiones:** Cada sesión usa un `sessionId` (UUID) único para evitar fantasmas de datos anteriores.
- **Flujo End-to-End (Punta a Punta):** Todo cambio en red, UI o modelos debe diseñarse y verificarse de emisor a receptor.

---

## 📷 3. Módulo OCR de Cuaderno (OCR Local + Rúbrica de 20 Puntos)

1. **Procesamiento 100% Offline (Sin Internet)**:
   - Uso de Google ML Kit Text Recognition para extracción manuscrita local.
   - Comparación semántica local contra la lección teórica (`unit.topicText`).
2. **Rúbrica sobre 20 Puntos**:
   - **Contenido y Comprensión (10 Pts)**: Verificación de presencia de conceptos e ideas clave de la lección teórica.
   - **Redacción y Coherencia (5 Pts)**: Estructura de párrafos, conectores y claridad de ideas.
   - **Ortografía y Gramática (5 Pts)**: Reglas ortográficas en español.
3. **Cálculo Dinámico de Nota Final (0 a 20 Pts)**:
   - Adaptado dinámicamente a $N$ unidades del curso con escala estándar.

---

## 📄 4. Formato del Archivo de Curso (`clase.csv`)

### A. Fila de Metadatos `COURSE_INFO`
```csv
COURSE_INFO|Título del Curso|Versión|Descripción del Curso
```

### B. Filas de Contenido (10 Columnas requeridas)
`Indice|Titulo|Descripcion|Narrador|Teoria|Video|RetoPregunta|RetoOpciones|RetoCorrecta|ExamenData`

---

## ⏱️ 5. Historial de Mejoras e Implementaciones (1 al 25)

1. **Versionado de Cursos y Metadatos CSV (`COURSE_INFO`)**.
2. **Búsqueda Inteligente de Video (Case-Insensitive)** en raíz y `/videos_clase/`.
3. **Validación Rápida "Anti-Errores"** en carga de archivos.
4. **Selección Dinámica de Carpeta para Envío ZIP** (`OpenDocumentTree`).
5. **Acceso Directo para Cambiar/Vincular Carpeta en Estudiante**.
6. **Lógica de Exámenes Únicos y Examen Recuperativo** (Habilitación por profesor).
7. **Orden Pedagógico de Contenidos** (Lectura ➔ Video ➔ Examen).
8. **Menú de Configuración y Consola de Logs** en el Estudiante.
9. **Edición de Datos de Perfil con Autorización del Profesor** (Remota o PIN `1234`).
10. **Semaforización Pedagógica Inicial ("AL DÍA" 🔵)**.
11. **Sistema de Racha de Constancia (🔥 Streak)**.
12. **Simplificación del Control de Unidades del Profesor** (`[ 🟢 IMPARTIR CLASE ]`).
13. **Banner Persistente de Clase Finalizada** (`CLASE FINALIZADA`).
14. **Gamificación de Lectura y Candado Estricto de Video**.
15. **Memoria de Continuidad de Lectura (*Scroll Memory*)**.
16. **Diseño E-Book "Modo Descanso"** con controles de tamaño de letra (`A-` / `A+`).
17. **Icono Oficial de Instalación** (Escudo Académico Conectado).
18. **Blindaje de Cierre de Clase** y protocolo explícito (`CMD_CLASS_FINALIZE_OFFICIAL`).
19. **Corrección de Falso 100% de Lectura y Semáforo de Eficiencia**.
20. **Eliminación Definitiva de Marcado Automático de Lectura**.
21. **Mini-Reto de Comprensión Lectora y Desbloqueo de Video (+50 Pts 🔥)**.
22. **Entrada a Clase Activa** con estado de sincronización inmediato.
23. **Métricas e Historial de Asistencia Completo** al finalizar clase.
24. **Rediseño Estético y UX Moderno** de tarjetas de unidades y pantalla de sincronización.
25. **Barra de Progreso de Lectura Fija (Sticky)** en vista de estudio del alumno.

---

## 📌 6. Bitácora de Tareas Completadas
- [x] Arquitectura Offline-First y Red P2P.
- [x] Módulo OCR Offline con ML Kit.
- [x] Unificación de Botones y Paleta Monocromática Teal-Cyan.
- [x] Consolidación de Documentación en `MEMORIA_PROYECTO.md`.

---
*Este documento es la única fuente de referencia y memoria oficial del proyecto.*

# Blueprint: Estándares de Desarrollo Profesional Android
**Basado en la Arquitectura de Aula Local (Versión Oro 2026)**

Este documento define la estructura y los pasos obligatorios para que cualquier aplicación Android sea escalable, mantenible y robusta desde su creación.

## 📂 1. Estructura de Paquetes (Arquitectura Limpia)
No mezcles lógica. Divide tu código en estos departamentos desde el primer archivo:

- `com.example.ui`: Todo lo visual (Screens, ViewModels, Theme).
- `com.example.domain`: El cerebro. Reglas de negocio y Managers (`AttendanceManager`, `CourseManager`).
- `com.example.network`: La infraestructura de red (Server, Client, Discovery).
- `com.example.data`: Base de datos (Room), Entities y DAOs.
- `com.example.util`: Herramientas genéricas (Logs, Zips).

### 📁 Estructura de Materiales (Modo Excel)
Para que el curso sea intuitivo para los profesores, usa esta estructura:
- `clase.csv`: Archivo Excel maestro con todo el curso.
- `videos_clase/`: Carpeta para los videos MP4 (video_unidad1.mp4, video_unidad2.mp4...).

## 🛠️ 2. Reglas de Oro de Implementación

### A. Gestión de Textos (Cero Hardcode)
- **Regla:** Prohibido escribir texto directo en el código Kotlin. 
- **Acción:** Todo debe ir en `res/values/strings.xml`.

### B. El Principio del "ViewModel Delgado"
- **Regla:** Un ViewModel no debe ser un "God Object".
- **Acción:** Si tiene lógica de cálculo pesada, muévela a un Manager en `domain`.

### C. Redes Atómicas y Sesiones
- **Regla:** Cada sesión de red debe ser única y aislada.
- **Acción:** Usa un `sessionId` (UUID). Si cambia, el cliente limpia su memoria para evitar "fantasmas" de datos anteriores.

### D. Planificación de Contexto Completo y Flujo de Datos Punta a Punta (End-to-End)
- **Regla:** Quedan prohibidos los parches aislados o unidireccionales.
- **Acción:** Al planificar cualquier cambio que afecte la interfaz, el modelo de datos o la red, es obligatorio diseñar y verificar todo el flujo **de punta a punta (End-to-End)**: emisor (UI/ViewModel), transporte (JSON/Red/Protocolo) y receptor (Servidor/DAO/Base de Datos), asegurando que ambos lados procesen y reflejen la información correctamente.

## 🪵 3. Sistema de Logging Eficiente
- **Implementación:** Usa `Channels` de Coroutines para encolar logs.
- **Beneficio:** Escritura en disco asíncrona que no bloquea la UI y ahorra batería.

## 🧪 4. Control de Calidad (Tests Unitarios)
- **Implementación:** Crea tests en `app/src/test`.
- **Patrón:** Given-When-Then. Prueba la lógica core (asistencia, notas, seguridad) antes de generar el APK.

---
*Documento generado por la IA de Android Studio para el Proyecto Aula Local.*

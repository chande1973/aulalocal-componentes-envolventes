cd# Estándares de Ingeniería y Diseño - Aula Local

Este documento define los criterios técnicos y de diseño que deben regir el desarrollo de la aplicación para garantizar un producto profesional, mantenible y eficiente.

## 1. Código Limpio (Kotlin & Android)

Para asegurar la legibilidad y evitar deuda técnica, seguimos estos principios:

### Arquitectura y Estado
- **MVVM Estricto:** Los ViewModels no deben contener referencias a `Context` o clases de la UI (excepto `Application` si es necesario).
- **Uso de Flows:** Preferir `StateFlow` y `SharedFlow` para la comunicación reactiva entre capas.
- **Fuente Única de Verdad:** La base de datos (Room) debe ser el origen de los datos mostrados en la UI; evita estados duplicados en memoria.

### Calidad del Código
- **Funciones de Responsabilidad Única:** Cada función debe hacer una sola cosa y ser lo más corta posible.
- **Inyección de Dependencias:** Aunque el proyecto es pequeño, mantener la separación de componentes (Repositorios, DAOs, ViewModels) para facilitar el testing.
- **Manejo de Errores:** Utilizar tipos sellados (Sealed Classes) como `LoadResult` para manejar estados de éxito, error y carga de forma explícita.

### Validaciones y Resiliencia
- **Cuestionamiento de Datos:** No dar por sentado que los datos externos (archivos, red) son correctos. Validar esquemas y tipos antes de procesar.
- **Robustez en E/S y Red:** Envolver operaciones de entrada/salida y red en bloques `try-catch` específicos para evitar cierres inesperados.
- **Validación Preventiva:** Los repositorios deben actuar como "gatekeepers", rechazando datos que puedan corromper la base de datos local.

## 2. Diseño UI/UX y Accesibilidad

La aplicación se utiliza en entornos educativos, por lo que el diseño debe ser funcional y claro.

### Componentes y Estética
- **Material 3:** Utilizar exclusivamente componentes de la librería Material 3 para mantener la modernidad y consistencia.
- **Jerarquía Visual:** Uso claro de tipografías y colores para distinguir entre las acciones del "Profesor" y el "Alumno".
- **Modo Offline:** Indicar siempre el estado de la conexión local (IP activa, servidor corriendo) mediante banners o iconos claros.

### Validación de Usuario
- **Feedback Inmediato:** Validar campos de entrada (formularios) en tiempo real o al perder el foco para corregir al usuario antes del envío.
- **Mensajes Informativos:** Usar `supportingText` en campos de texto para explicar por qué un dato es inválido (ej: "Mínimo 3 caracteres").
- **Prevención de Errores:** Deshabilitar botones de acción si el formulario es incompleto, pero proporcionar pistas visuales sobre qué falta.

### Accesibilidad y Pedagogía
- **Text-to-Speech (TTS):** Integrar la lectura en voz alta para apoyar la inclusión de estudiantes con dificultades visuales o de lectura.
- **Contraste:** Mantener niveles de contraste altos para facilitar la lectura en condiciones de iluminación variable (salones de clase).

## 3. Optimización y Mejora Continua

### Rendimiento de Red Local (P2P)
- **Carga Eficiente:** No saturar el ancho de banda local. Enviar solo deltas de información cuando sea posible.
- **Descubrimiento UDP:** Mantener el escaneo de red optimizado para que no drene la batería de los dispositivos.

### Gestión de Materiales
- **Multimedia Ligera:** Fomentar el uso de formatos eficientes (.mp4 para video, .csv para datos) para que la app responda rápido incluso en teléfonos de gama baja.

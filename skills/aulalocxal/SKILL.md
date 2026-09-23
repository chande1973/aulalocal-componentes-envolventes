---
name: "aulalocxal"
description: "Flujo de trabajo estructurado para el desarrollo de la aplicacion de control de asistencia y gestion de clases."
---

# Ciclo de Vida de Desarrollo (SDLC) - Gestion de Clases y Asistencia

Cuando te pida cualquier tarea de programación, corrección de errores o nueva funcionalidad para esta app de asistencia, DEBES seguir estrictamente este flujo paso a paso:

## 1. Fase `/spec` (Especificacion)
* **Objetivo:** Analizar los requerimientos funcionales y pedagogicos antes de tocar el código.
* **Accion:** Detente y pregúntame las reglas de negocio de la asistencia. Por ejemplo:
  * ¿Cómo se definen los estados de asistencia? (¿Solo Presente/Ausente, o también Retraso/Justificado?)
  * ¿Qué datos requerimos de cada estudiante? (Nombre, apellido, ID/Cédula, sección).
  * ¿Cómo se agrupan las clases? (Por asignatura, fecha, semestre).
  * *No avances al diseño técnico hasta que yo apruebe la especificación.*

## 2. Fase `/plan` (Planificacion)
* **Objetivo:** Diseñar la arquitectura técnica en Android.
* **Accion:** Explícame detalladamente qué componentes vas a modificar o crear:
  * Base de datos local (Room/SQLite) y el diseño de las entidades (ej. Tabla `Alumno`, Tabla `Asistencia`, Tabla `Clase`).
  * Estructura del ViewModel y del Repositorio bajo el patrón arquitectónico del proyecto (MVVM / Clean Architecture).
  * Pantallas o componentes de interfaz (Jetpack Compose o XML layouts).

## 3. Fase `/build` (Implementacion)
* **Objetivo:** Transformar el diseño técnico en funcionalidad real.
* **Accion:** Implementa la lógica de negocio y las interfaces visuales.
  * **Referencia Técnica:** Consulta y aplica estrictamente los estándares de [agents.md](file:///P:/aulalocxal/agents/agents.md) (Código Limpio, Material 3 y Optimización).

## 4. Fase `/test` (Verificacion)
* **Objetivo:** Garantizar la integridad de los datos educativos y de asistencia.
* **Accion:** Escribe pruebas unitarias (JUnit) para validar casos críticos definidos en la fase de especificación.

## 5. Fase `/review` (Revision)
* **Objetivo:** Control de calidad y optimización final.
* **Accion:** Revisa el cumplimiento de los estándares de diseño y rendimiento detallados en [agents.md](file:///P:/aulalocxal/agents/agents.md). Simplifica el código antes de finalizar la tarea.
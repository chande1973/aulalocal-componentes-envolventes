# Guía de Arquitectura: Componentes Envolventes y Modificadores Reutilizables en Jetpack Compose

Este documento explica cómo estructurar el proyecto **Aula Local** utilizando un Sistema de Diseño con componentes envolventes (*Wrappers*) y modificadores personalizados, evitando la repetición de código de diseño (`CardDefaults`, `RoundedCornerShape`, `BorderStroke`, etc.) en todas las vistas.

---

## 1. El Problema Actual
En múltiples pantallas y diálogos de la aplicación se repiten bloques idénticos de configuración visual:
```kotlin
Card(
    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
    colors = CardDefaults.cardColors(containerColor = CosmicDarkSurface),
    shape = RoundedCornerShape(16.dp),
    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
) {
    // ...
}
```
Si se desea cambiar el color base o el radio de las esquinas en toda la aplicación, hay que modificar docenas de archivos manualmente.

---

## 2. La Solución: Componentes Envolventes y Modificadores

### A. Modificadores de Extensión (`Modifier Extensions`)
Crea un archivo centralizado (ej. `ModifierExtensions.kt`) donde declares estilos reutilizables:

```kotlin
package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CosmicDarkSurface

fun Modifier.cosmicCardStyle(
    borderColor: Color = Color.Gray.copy(alpha = 0.2f),
    cornerRadius: Dp = 16.dp
): Modifier = this
    .fillMaxWidth()
    .clip(RoundedCornerShape(cornerRadius))
    .background(CosmicDarkSurface, RoundedCornerShape(cornerRadius))
    .border(1.dp, borderColor, RoundedCornerShape(cornerRadius))
```

**Uso en vistas:**
```kotlin
Box(modifier = Modifier.cosmicCardStyle()) {
    // Contenido limpio sin boilerplate
}
```

---

### B. Componentes Base Envolventes (`CosmicCard`)
Crea un componente personalizado que envuelva los elementos estándar de Material 3 con los valores por defecto de la aplicación:

```kotlin
package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CosmicDarkSurface

@Composable
fun CosmicCard(
    modifier: Modifier = Modifier,
    containerColor: Color = CosmicDarkSurface,
    borderColor: Color = Color.Gray.copy(alpha = 0.2f),
    cornerRadius: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(cornerRadius),
        border = BorderStroke(1.dp, borderColor),
        content = content
    )
}
```

**Uso en vistas:**
```kotlin
CosmicCard {
    Column(Modifier.padding(16.dp)) {
        Text("Contenido de la unidad", color = Color.White)
    }
}
```

---

### C. Constantes y Fábricas de Estilos (`Theme Defaults`)
Para configuraciones como `NavigationBarItemDefaults.colors(...)`, agrupa los estilos en un objeto compartido:

```kotlin
package com.example.ui.theme

import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AppThemeDefaults {
    val navBarColors @Composable get() = NavigationBarItemDefaults.colors(
        selectedIconColor = AccentCyan,
        selectedTextColor = AccentCyan,
        unselectedIconColor = Color.Gray,
        unselectedTextColor = Color.Gray,
        indicatorColor = AccentCyan.copy(alpha = 0.2f)
    )
}
```

---

## 3. Plan de Trabajo en el Proyecto de Respaldo
1. Abre el proyecto ubicado en `/home/edgar/Escritorio/aulalocal_componetes_envolventes` en Android Studio.
2. Implementa `CosmicCard` y las extensiones de `Modifier` en el paquete `com.example.ui.components`.
3. Reemplaza gradualmente las declaraciones repetitivas en las pantallas (`StudentClassTab`, `TeacherDashboardScreen`, etc.) por `CosmicCard` y `Modifier.cosmicCardStyle()`.
4. Verifica que la app compile correctamente y mantenga el diseño intacto.

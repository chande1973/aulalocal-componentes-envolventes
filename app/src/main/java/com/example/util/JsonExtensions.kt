package com.example.util

import org.json.JSONArray
import org.json.JSONObject

/**
 * Funciones 100% reutilizables y seguras para parseo de JSON.
 * Evita la duplicación de código try/catch en ViewModels, Repositorios y Componentes.
 */
fun String?.toSafeJSONObject(): JSONObject {
    if (this.isNullOrBlank()) return JSONObject()
    return try {
        JSONObject(this)
    } catch (e: Exception) {
        JSONObject()
    }
}

fun String?.toSafeJSONArray(): JSONArray {
    if (this.isNullOrBlank()) return JSONArray()
    return try {
        JSONArray(this)
    } catch (e: Exception) {
        JSONArray()
    }
}

inline fun <T> runSafely(fallback: T, block: () -> T): T {
    return try {
        block()
    } catch (e: Exception) {
        fallback
    }
}

inline fun closeQuietly(autoCloseable: AutoCloseable?) {
    try {
        autoCloseable?.close()
    } catch (e: Exception) {
        // Ignorado silenciosamente para cierre seguro de recursos
    }
}

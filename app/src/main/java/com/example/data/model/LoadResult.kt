package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Representa el resultado de una operación de carga o validación.
 */
sealed interface LoadResult<out T> {
    @JsonClass(generateAdapter = true)
    data class Success<out T>(val data: T) : LoadResult<T>
    
    @JsonClass(generateAdapter = true)
    data class Failure(val errors: List<ValidationError>) : LoadResult<Nothing>
    
    @JsonClass(generateAdapter = true)
    data class PartialSuccess<out T>(val data: T, val warnings: List<ValidationError>) : LoadResult<T>
}

@JsonClass(generateAdapter = true)
data class ValidationError(
    @Json(name = "file") val file: String,
    @Json(name = "pointer") val pointer: String,
    @Json(name = "message") val message: String,
    @Json(name = "severity") val severity: String = "ERROR" // "ERROR" | "WARNING"
)

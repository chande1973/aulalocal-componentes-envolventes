package com.example.util

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object ZipUtils {

    /**
     * Compresses a folder from SAF into a ZIP file.
     * @param context Application context to access content resolver.
     * @param rootFolder The SAF DocumentFile representing the folder to zip.
     * @param zipFile The destination ZIP file on disk.
     * @return True if successful, false otherwise.
     */
    fun zipFolder(context: Context, rootFolder: DocumentFile, zipFile: File): Boolean {
        return try {
            if (zipFile.exists()) {
                zipFile.delete()
            }
            if (!zipFile.parentFile?.exists()!!) {
                zipFile.parentFile?.mkdirs()
            }
            ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
                zos.setMethod(ZipOutputStream.DEFLATED)
                zipDocumentFile(context, zos, rootFolder, "")
            }
            true
        } catch (e: Exception) {
            com.example.data.AppLogger.e("ZipUtils", "Error al comprimir: ${e.message}", e)
            false
        }
    }

    private fun zipDocumentFile(context: Context, zos: ZipOutputStream, folder: DocumentFile, parentPath: String) {
        val files = folder.listFiles()
        for (file in files) {
            val name = file.name ?: continue
            val path = if (parentPath.isEmpty()) name else "$parentPath/$name"
            if (file.isDirectory) {
                zipDocumentFile(context, zos, file, path)
            } else {
                val entry = ZipEntry(path)
                zos.putNextEntry(entry)
                context.contentResolver.openInputStream(file.uri)?.use { inputStream ->
                    inputStream.copyTo(zos)
                }
                zos.closeEntry()
            }
        }
    }

    /**
     * Unzips a file into a target directory.
     * Includes deep cleaning and disk space check.
     */
    fun unzip(
        zipFile: File,
        targetDirectory: File,
        onProgress: ((currentFile: String, extractedCount: Int) -> Unit)? = null
    ): UnzipResult {
        // 1. Check disk space (approximation: 2x zip size)
        val requiredSpace = zipFile.length() * 2
        if (targetDirectory.freeSpace > 0 && targetDirectory.freeSpace < requiredSpace) {
            return UnzipResult.Error("Espacio en disco insuficiente. Se requieren aprox. ${requiredSpace / 1024 / 1024} MB.")
        }

        try {
            // 2. Permission validation (for regular File)
            if (!targetDirectory.exists()) {
                if (!targetDirectory.mkdirs()) {
                    return UnzipResult.Error("No se pudo crear el directorio de destino. Verifique los permisos.")
                }
            }
            
            if (!targetDirectory.canWrite()) {
                return UnzipResult.Error("Sin permisos de escritura en el directorio de destino.")
            }

            // 3. Deep cleaning
            targetDirectory.listFiles()?.forEach { it.deleteRecursively() }

            ZipInputStream(BufferedInputStream(FileInputStream(zipFile))).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                var extractedCount = 0
                while (entry != null) {
                    val fileName = entry.name
                    extractedCount++
                    onProgress?.invoke(fileName, extractedCount)

                    val file = File(targetDirectory, fileName)
                    
                    // Security check: Zip Slip vulnerability
                    if (!file.canonicalPath.startsWith(targetDirectory.canonicalPath)) {
                        throw IOException("Entrada de ZIP maliciosa detectada: ${fileName}")
                    }

                    if (entry.isDirectory) {
                        file.mkdirs()
                    } else {
                        file.parentFile?.mkdirs()
                        FileOutputStream(file).use { fos ->
                            zis.copyTo(fos)
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
            return UnzipResult.Success
        } catch (e: Exception) {
            e.printStackTrace()
            return UnzipResult.Error("Error al descomprimir: ${e.message}")
        }
    }

    sealed class UnzipResult {
        object Success : UnzipResult()
        data class Error(val message: String) : UnzipResult()
    }
}

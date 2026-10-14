package com.example.ui.components

import android.content.Context
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.example.ui.theme.StatusError

@Composable
fun ResetAppDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Context) -> Unit,
    context: Context
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Borrado de Fábrica") },
            text = { Text("Se eliminarán todos los perfiles, secciones y registros. Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm(context)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusError)
                ) {
                    Text("BORRAR TODO")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("CANCELAR")
                }
            }
        )
    }
}

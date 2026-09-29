package com.example.domain

import com.example.data.SyncedStudent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MessagingAndProfileUnlockTest {

    @Test
    fun testProfileUnlockRequestFlag() {
        val student = SyncedStudent(
            cardId = "V-100",
            firstName = "Pedro",
            lastName = "Alvarado",
            email = "",
            avatar = "👤",
            sectionName = "Sección C"
        )

        val message = "Solicitud de desbloqueo de perfil para corrección de datos"
        val isUnlock = message.contains("desbloqueo de perfil", ignoreCase = true)

        val updated = student.copy(
            hasDoubt = true,
            doubtMessage = message,
            isProfileUnlockRequested = isUnlock || student.isProfileUnlockRequested
        )

        assertTrue(updated.hasDoubt)
        assertTrue(updated.isProfileUnlockRequested)
        assertEquals("Solicitud de desbloqueo de perfil para corrección de datos", updated.doubtMessage)
    }
}

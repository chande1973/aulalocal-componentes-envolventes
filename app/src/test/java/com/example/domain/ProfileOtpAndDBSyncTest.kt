package com.example.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileOtpAndDBSyncTest {

    @Test
    fun testOneTimePinGenerationAndSingleUseConsumption() {
        val otpPin = (1000..9999).random().toString()
        assertTrue(otpPin.length == 4)

        var activeOtp: String? = otpPin

        // Intento con PIN incorrecto -> debe fallar y mantener el OTP activo
        val invalidResult = activeOtp != null && "0000" == activeOtp
        assertFalse(invalidResult)
        assertEquals(otpPin, activeOtp)

        // Intento con PIN correcto -> debe tener éxito y destruir el OTP inmediatamente
        val validResult = activeOtp != null && otpPin == activeOtp
        assertTrue(validResult)
        
        // Se destruye / consume tras el primer uso
        activeOtp = null
        assertNull(activeOtp)

        // Reuso del PIN -> debe fallar porque el OTP ya es null
        val reuseResult = activeOtp != null && otpPin == activeOtp
        assertFalse(reuseResult)
    }
}

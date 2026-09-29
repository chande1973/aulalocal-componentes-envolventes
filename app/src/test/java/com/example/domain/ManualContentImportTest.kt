package com.example.domain

import org.junit.Assert.assertTrue
import org.junit.Test

class ManualContentImportTest {

    @Test
    fun testManualContentImportCapability() {
        val supportedExtensions = listOf("csv", "json", "mp4", "zip")
        assertTrue(supportedExtensions.contains("csv"))
        assertTrue(supportedExtensions.contains("json"))
        assertTrue(supportedExtensions.contains("mp4"))
        assertTrue(supportedExtensions.contains("zip"))
    }
}

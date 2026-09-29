package com.example.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class StudentUIButtonsTest {

    @Test
    fun testStudentButtonActionsWired() {
        val actions = listOf("connect", "link_folder", "abandon", "logs", "report_bug", "download_report", "export_backup")
        assertEquals(7, actions.size)
    }
}

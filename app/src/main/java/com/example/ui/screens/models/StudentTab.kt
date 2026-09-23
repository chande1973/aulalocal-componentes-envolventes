package com.example.ui.screens.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.R

sealed class StudentTab(
    val route: String,
    val icon: ImageVector,
    val labelRes: Int
) {
    object Class : StudentTab("class", Icons.AutoMirrored.Filled.MenuBook, R.string.tab_class)
    object Inbox : StudentTab("inbox", Icons.Default.Mail, R.string.tab_inbox)
    object Status : StudentTab("status", Icons.Default.Assessment, R.string.tab_status)
    object Profile : StudentTab("profile", Icons.Default.Person, R.string.tab_profile)

    companion object {
        val items get() = listOf(Class, Inbox, Status, Profile)
    }
}

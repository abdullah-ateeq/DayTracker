package com.example.daytracker.domain

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class EventCategory(
    val displayName: String,
    val defaultColor: Long
) {
    PERSONAL("Personal", 0xFF6366F1), // Indigo
    WORK("Work", 0xFF0EA5E9),       // Ocean Blue
    EDUCATION("Education", 0xFF10B981), // Emerald
    TRAVEL("Travel", 0xFFF59E0B),    // Amber
    OTHER("Other", 0xFF8B5CF6);      // Purple

    val icon: ImageVector
        get() = when (this) {
            PERSONAL -> Icons.Rounded.Person
            WORK -> Icons.Rounded.Work
            EDUCATION -> Icons.Rounded.School
            TRAVEL -> Icons.Rounded.Flight
            OTHER -> Icons.Rounded.Bookmark
        }

    companion object {
        fun fromString(value: String): EventCategory {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: OTHER
        }
    }
}

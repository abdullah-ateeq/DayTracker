package com.example.daytracker.domain

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material.icons.rounded.Work
import androidx.compose.ui.graphics.vector.ImageVector

object EventVisuals {
    val iconList: List<Pair<String, ImageVector>> = listOf(
        "Star" to Icons.Rounded.Star,
        "Favorite" to Icons.Rounded.Favorite,
        "Cake" to Icons.Rounded.Cake,
        "Work" to Icons.Rounded.Work,
        "School" to Icons.Rounded.School,
        "Flight" to Icons.Rounded.Flight,
        "Person" to Icons.Rounded.Person,
        "Rocket" to Icons.Rounded.RocketLaunch,
        "Pets" to Icons.Rounded.Pets,
        "Fitness" to Icons.Rounded.FitnessCenter,
        "Award" to Icons.Rounded.WorkspacePremium,
        "Bookmark" to Icons.Rounded.Bookmark
    )

    val colorList: List<Long> = listOf(
        0xFF6366F1, // Indigo
        0xFF0EA5E9, // Sky Blue
        0xFF10B981, // Emerald Green
        0xFFF59E0B, // Warm Amber
        0xFFEF4444, // Crimson Red
        0xFF8B5CF6, // Royal Purple
        0xFFEC4899, // Pink
        0xFF14B8A6  // Teal
    )

    fun getIconByName(name: String): ImageVector {
        return iconList.firstOrNull { it.first.equals(name, ignoreCase = true) }?.second
            ?: Icons.Rounded.Star
    }
}

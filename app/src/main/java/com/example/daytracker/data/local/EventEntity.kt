package com.example.daytracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val date: LocalDate,
    val description: String = "",
    val category: String = "Personal",
    val iconName: String = "Star",
    val colorHex: Long = 0xFF6366F1,
    val createdAt: Long = System.currentTimeMillis()
)

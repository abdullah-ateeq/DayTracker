package com.example.daytracker.ui

import com.example.daytracker.data.local.EventEntity
import com.example.daytracker.data.preferences.AppDateFormat
import com.example.daytracker.data.preferences.SortOption
import com.example.daytracker.data.preferences.ThemeMode
import com.example.daytracker.domain.DayTrackerCalculator
import com.example.daytracker.domain.EventCalculation
import com.example.daytracker.domain.EventCategory
import java.time.LocalDate

data class EventItemUi(
    val entity: EventEntity,
    val calculation: EventCalculation,
    val category: EventCategory,
    val formattedDate: String
)

data class DayTrackerUiState(
    val isLoading: Boolean = true,
    val allEvents: List<EventItemUi> = emptyList(),
    val filteredEvents: List<EventItemUi> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: EventCategory? = null,
    val sortOption: SortOption = SortOption.NEWEST_DATE,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dateFormat: AppDateFormat = AppDateFormat.MEDIUM,
    val selectedEvent: EventItemUi? = null,
    val today: LocalDate = LocalDate.now(),
    val userMessage: String? = null
) {
    val totalEventsCount: Int
        get() = allEvents.size

    val closestAnniversaryEvent: EventItemUi?
        get() = allEvents
            .filter { !it.calculation.isFuture }
            .minByOrNull { it.calculation.daysUntilAnniversary }
}

package com.example.daytracker.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daytracker.data.local.AppDatabase
import com.example.daytracker.data.local.EventEntity
import com.example.daytracker.data.preferences.AppDateFormat
import com.example.daytracker.data.preferences.SortOption
import com.example.daytracker.data.preferences.ThemeMode
import com.example.daytracker.data.preferences.UserPreferences
import com.example.daytracker.data.preferences.UserPreferencesRepository
import com.example.daytracker.data.repository.EventRepository
import com.example.daytracker.data.repository.EventRepositoryImpl
import com.example.daytracker.domain.DayTrackerCalculator
import com.example.daytracker.domain.EventCalculation
import com.example.daytracker.domain.EventCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

private data class FilterState(
    val query: String,
    val category: EventCategory?,
    val selectedId: Long?,
    val message: String?
)

class DayTrackerViewModel(
    private val eventRepository: EventRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<EventCategory?>(null)
    private val _userMessage = MutableStateFlow<String?>(null)
    private val _selectedEventId = MutableStateFlow<Long?>(null)
    private val _currentDate = MutableStateFlow(LocalDate.now())

    private val _filterState = combine(
        _searchQuery,
        _selectedCategory,
        _selectedEventId,
        _userMessage
    ) { query, category, selectedId, message ->
        FilterState(query, category, selectedId, message)
    }

    val uiState: StateFlow<DayTrackerUiState> = combine(
        eventRepository.getAllEvents(),
        preferencesRepository.userPreferencesFlow,
        _currentDate,
        _filterState
    ) { events, prefs, today, filter ->
        val uiItems = events.map { entity ->
            val calc = DayTrackerCalculator.calculate(entity.date, today)
            EventItemUi(
                entity = entity,
                calculation = calc,
                category = EventCategory.fromString(entity.category),
                formattedDate = entity.date.format(prefs.dateFormat.formatter)
            )
        }

        // Apply filtering and sorting
        val filtered = uiItems
            .filter { item ->
                val matchesQuery = filter.query.isBlank() ||
                        item.entity.title.contains(filter.query, ignoreCase = true) ||
                        item.entity.description.contains(filter.query, ignoreCase = true)
                val matchesCategory = filter.category == null || item.category == filter.category
                matchesQuery && matchesCategory
            }
            .let { list ->
                when (prefs.sortOption) {
                    SortOption.NEWEST_DATE -> list.sortedWith(compareByDescending<EventItemUi> { it.entity.date }.thenByDescending { it.entity.id })
                    SortOption.OLDEST_DATE -> list.sortedWith(compareBy<EventItemUi> { it.entity.date }.thenBy { it.entity.id })
                    SortOption.NAME_ASC -> list.sortedBy { it.entity.title.lowercase() }
                    SortOption.MOST_DAYS -> list.sortedByDescending { it.calculation.daysPassed }
                    SortOption.NEXT_ANNIVERSARY -> list.sortedBy { it.calculation.daysUntilAnniversary }
                }
            }

        val selectedEvent = filter.selectedId?.let { id ->
            uiItems.firstOrNull { it.entity.id == id }
        }

        DayTrackerUiState(
            isLoading = false,
            allEvents = uiItems,
            filteredEvents = filtered,
            searchQuery = filter.query,
            selectedCategory = filter.category,
            sortOption = prefs.sortOption,
            themeMode = prefs.themeMode,
            dateFormat = prefs.dateFormat,
            selectedEvent = selectedEvent,
            today = today,
            userMessage = filter.message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DayTrackerUiState()
    )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onCategoryFilterChange(category: EventCategory?) {
        _selectedCategory.value = category
    }

    fun onSortOptionChange(option: SortOption) {
        viewModelScope.launch {
            preferencesRepository.setSortOption(option)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun setDateFormat(format: AppDateFormat) {
        viewModelScope.launch {
            preferencesRepository.setDateFormat(format)
        }
    }

    fun selectEvent(id: Long?) {
        _selectedEventId.value = id
    }

    fun refreshCurrentDate() {
        val now = LocalDate.now()
        if (_currentDate.value != now) {
            _currentDate.value = now
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun saveEvent(
        id: Long = 0,
        title: String,
        date: LocalDate,
        description: String,
        category: EventCategory,
        iconName: String,
        colorHex: Long,
        onSuccess: (Long) -> Unit
    ) {
        if (title.isBlank()) {
            _userMessage.value = "Event title cannot be empty"
            return
        }

        // Validate future date
        val today = LocalDate.now()
        if (date.isAfter(today)) {
            _userMessage.value = "Event date cannot be in the future"
            return
        }

        viewModelScope.launch {
            val entity = EventEntity(
                id = id,
                title = title.trim(),
                date = date,
                description = description.trim(),
                category = category.name,
                iconName = iconName,
                colorHex = colorHex
            )

            val savedId = if (id == 0L) {
                val newId = eventRepository.insertEvent(entity)
                _userMessage.value = "Event created successfully"
                newId
            } else {
                eventRepository.updateEvent(entity)
                _userMessage.value = "Event updated successfully"
                id
            }
            onSuccess(savedId)
        }
    }

    fun deleteEvent(event: EventEntity, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            eventRepository.deleteEvent(event)
            if (_selectedEventId.value == event.id) {
                _selectedEventId.value = null
            }
            _userMessage.value = "Event deleted"
            onDeleted()
        }
    }

    fun loadSampleEvents() {
        viewModelScope.launch {
            val samples = AppDatabase.getInitialSampleEvents()
            eventRepository.insertEvents(samples)
            _userMessage.value = "Loaded ${samples.size} sample events"
        }
    }

    fun clearAllEvents() {
        viewModelScope.launch {
            eventRepository.deleteAllEvents()
            _selectedEventId.value = null
            _userMessage.value = "All events cleared"
        }
    }

    fun copyEventStatistics(context: Context, item: EventItemUi) {
        val summary = DayTrackerCalculator.formatStatisticsSummary(
            eventName = item.entity.title,
            category = item.category.displayName,
            description = item.entity.description,
            calculation = item.calculation,
            dateFormatter = uiState.value.dateFormat.formatter
        )

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Event Statistics", summary)
        clipboard.setPrimaryClip(clip)
        _userMessage.value = "Statistics copied to clipboard"
    }

    fun shareEventDetails(context: Context, item: EventItemUi) {
        val summary = DayTrackerCalculator.formatStatisticsSummary(
            eventName = item.entity.title,
            category = item.category.displayName,
            description = item.entity.description,
            calculation = item.calculation,
            dateFormatter = uiState.value.dateFormat.formatter
        )

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, summary)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Event Details")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = AppDatabase.getInstance(context)
                    val eventRepo = EventRepositoryImpl(db.eventDao())
                    val prefRepo = UserPreferencesRepository(context)
                    return DayTrackerViewModel(eventRepo, prefRepo) as T
                }
            }
    }
}

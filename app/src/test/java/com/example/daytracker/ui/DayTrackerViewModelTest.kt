package com.example.daytracker.ui

import com.example.daytracker.data.local.EventEntity
import com.example.daytracker.data.preferences.AppDateFormat
import com.example.daytracker.data.preferences.SortOption
import com.example.daytracker.data.preferences.ThemeMode
import com.example.daytracker.data.preferences.UserPreferences
import com.example.daytracker.data.preferences.UserPreferencesRepository
import com.example.daytracker.data.repository.EventRepository
import com.example.daytracker.domain.DayTrackerCalculator
import com.example.daytracker.domain.EventCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DayTrackerViewModelTest {

    private class FakeEventRepository : EventRepository {
        val list = mutableListOf<EventEntity>()
        val flow = MutableStateFlow<List<EventEntity>>(emptyList())

        override fun getAllEvents(): Flow<List<EventEntity>> = flow

        override fun getEventById(id: Long): Flow<EventEntity?> {
            return MutableStateFlow(list.firstOrNull { it.id == id })
        }

        override suspend fun getEventByIdOnce(id: Long): EventEntity? {
            return list.firstOrNull { it.id == id }
        }

        override suspend fun insertEvent(event: EventEntity): Long {
            val newId = if (event.id == 0L) (list.size + 1).toLong() else event.id
            val inserted = event.copy(id = newId)
            list.add(inserted)
            flow.value = list.toList()
            return newId
        }

        override suspend fun insertEvents(events: List<EventEntity>) {
            events.forEach { insertEvent(it) }
        }

        override suspend fun updateEvent(event: EventEntity) {
            val index = list.indexOfFirst { it.id == event.id }
            if (index != -1) {
                list[index] = event
                flow.value = list.toList()
            }
        }

        override suspend fun deleteEvent(event: EventEntity) {
            list.removeAll { it.id == event.id }
            flow.value = list.toList()
        }

        override suspend fun deleteEventById(id: Long): Int {
            val before = list.size
            list.removeAll { it.id == id }
            flow.value = list.toList()
            return before - list.size
        }

        override suspend fun deleteAllEvents(): Int {
            val count = list.size
            list.clear()
            flow.value = emptyList()
            return count
        }
    }

    @Test
    fun `sorting events correctly orders by criteria`() {
        val today = LocalDate.of(2026, 10, 30)

        val event1 = EventEntity(id = 1, title = "Alpha", date = today.minusDays(100), category = "Work")
        val event2 = EventEntity(id = 2, title = "Beta", date = today.minusDays(10), category = "Personal")
        val event3 = EventEntity(id = 3, title = "Gamma", date = today.minusDays(500), category = "Education")

        val uiItems = listOf(event1, event2, event3).map {
            EventItemUi(
                entity = it,
                calculation = DayTrackerCalculator.calculate(it.date, today),
                category = EventCategory.fromString(it.category),
                formattedDate = it.date.toString()
            )
        }

        // Newest first
        val sortedNewest = uiItems.sortedWith(
            compareByDescending<EventItemUi> { it.entity.date }.thenByDescending { it.entity.id }
        )
        assertEquals("Beta", sortedNewest[0].entity.title)
        assertEquals("Alpha", sortedNewest[1].entity.title)
        assertEquals("Gamma", sortedNewest[2].entity.title)

        // Oldest first
        val sortedOldest = uiItems.sortedWith(
            compareBy<EventItemUi> { it.entity.date }.thenBy { it.entity.id }
        )
        assertEquals("Gamma", sortedOldest[0].entity.title)
        assertEquals("Alpha", sortedOldest[1].entity.title)
        assertEquals("Beta", sortedOldest[2].entity.title)

        // Name A-Z
        val sortedName = uiItems.sortedBy { it.entity.title.lowercase() }
        assertEquals("Alpha", sortedName[0].entity.title)
        assertEquals("Beta", sortedName[1].entity.title)
        assertEquals("Gamma", sortedName[2].entity.title)

        // Most days passed
        val sortedDays = uiItems.sortedByDescending { it.calculation.daysPassed }
        assertEquals("Gamma", sortedDays[0].entity.title)
        assertEquals(500L, sortedDays[0].calculation.daysPassed)
    }

    @Test
    fun `filtering by category works as expected`() {
        val today = LocalDate.now()
        val event1 = EventEntity(id = 1, title = "Job", date = today.minusDays(10), category = "Work")
        val event2 = EventEntity(id = 2, title = "Trip", date = today.minusDays(20), category = "Travel")

        val list = listOf(event1, event2).map {
            EventItemUi(
                entity = it,
                calculation = DayTrackerCalculator.calculate(it.date, today),
                category = EventCategory.fromString(it.category),
                formattedDate = it.date.toString()
            )
        }

        val workOnly = list.filter { it.category == EventCategory.WORK }
        assertEquals(1, workOnly.size)
        assertEquals("Job", workOnly[0].entity.title)

        val travelOnly = list.filter { it.category == EventCategory.TRAVEL }
        assertEquals(1, travelOnly.size)
        assertEquals("Trip", travelOnly[0].entity.title)
    }

    @Test
    fun `search by title and description matches query`() {
        val today = LocalDate.now()
        val event1 = EventEntity(id = 1, title = "New Laptop", description = "Bought MacBook Pro for development", date = today.minusDays(5))
        val event2 = EventEntity(id = 2, title = "Birthday Party", description = "Dinner with friends", date = today.minusDays(15))

        val list = listOf(event1, event2)

        val matchesMacBook = list.filter {
            it.title.contains("MacBook", ignoreCase = true) || it.description.contains("MacBook", ignoreCase = true)
        }
        assertEquals(1, matchesMacBook.size)
        assertEquals("New Laptop", matchesMacBook[0].title)

        val matchesParty = list.filter {
            it.title.contains("Party", ignoreCase = true) || it.description.contains("Party", ignoreCase = true)
        }
        assertEquals(1, matchesParty.size)
        assertEquals("Birthday Party", matchesParty[0].title)
    }

    @Test
    fun `fake repository insertion and deletion lifecycle`() = runBlocking {
        val repo = FakeEventRepository()
        assertEquals(0, repo.list.size)

        val event = EventEntity(
            title = "Graduation",
            date = LocalDate.of(2022, 6, 15),
            category = "Education"
        )
        val id = repo.insertEvent(event)
        assertEquals(1L, id)
        assertEquals(1, repo.list.size)

        val retrieved = repo.getEventByIdOnce(id)
        assertNotNull(retrieved)
        assertEquals("Graduation", retrieved?.title)

        // Update
        repo.updateEvent(retrieved!!.copy(title = "Graduation Ceremony"))
        assertEquals("Graduation Ceremony", repo.getEventByIdOnce(id)?.title)

        // Delete
        repo.deleteEventById(id)
        assertNull(repo.getEventByIdOnce(id))
        assertEquals(0, repo.list.size)
    }
}

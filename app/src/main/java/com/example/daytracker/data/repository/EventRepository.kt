package com.example.daytracker.data.repository

import com.example.daytracker.data.local.EventDao
import com.example.daytracker.data.local.EventEntity
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    fun getAllEvents(): Flow<List<EventEntity>>
    fun getEventById(id: Long): Flow<EventEntity?>
    suspend fun getEventByIdOnce(id: Long): EventEntity?
    suspend fun insertEvent(event: EventEntity): Long
    suspend fun insertEvents(events: List<EventEntity>)
    suspend fun updateEvent(event: EventEntity)
    suspend fun deleteEvent(event: EventEntity)
    suspend fun deleteEventById(id: Long): Int
    suspend fun deleteAllEvents(): Int
}

class EventRepositoryImpl(private val eventDao: EventDao) : EventRepository {
    override fun getAllEvents(): Flow<List<EventEntity>> = eventDao.getAllEvents()

    override fun getEventById(id: Long): Flow<EventEntity?> = eventDao.getEventById(id)

    override suspend fun getEventByIdOnce(id: Long): EventEntity? = eventDao.getEventByIdOnce(id)

    override suspend fun insertEvent(event: EventEntity): Long = eventDao.insertEvent(event)

    override suspend fun insertEvents(events: List<EventEntity>) = eventDao.insertEvents(events)

    override suspend fun updateEvent(event: EventEntity) = eventDao.updateEvent(event)

    override suspend fun deleteEvent(event: EventEntity) = eventDao.deleteEvent(event)

    override suspend fun deleteEventById(id: Long): Int = eventDao.deleteEventById(id)

    override suspend fun deleteAllEvents(): Int = eventDao.deleteAllEvents()
}

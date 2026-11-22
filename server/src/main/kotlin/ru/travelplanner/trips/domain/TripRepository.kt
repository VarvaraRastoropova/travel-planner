package ru.travelplanner.trips.domain

import java.util.UUID

interface TripRepository {
    fun create(name: String): Trip
    fun findAll(): List<Trip>
    fun findById(id: UUID): Trip?
    fun rename(id: UUID, name: String, version: Long): Trip
    fun delete(id: UUID)
}

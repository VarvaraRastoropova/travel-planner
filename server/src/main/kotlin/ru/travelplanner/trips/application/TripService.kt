package ru.travelplanner.trips.application

import org.springframework.stereotype.Service
import ru.travelplanner.errors.NotFoundException
import ru.travelplanner.trips.domain.Trip
import ru.travelplanner.trips.domain.TripRepository
import java.util.UUID

@Service
class TripService(private val tripRepository: TripRepository) {
    fun create(name: String): Trip {
        val preparedName = prepareName(name)
        return tripRepository.create(preparedName)
    }

    fun findAll(): List<Trip> {
        return tripRepository.findAll()
    }

    fun get(id: UUID): Trip {
        return tripRepository.findById(id) ?: throw NotFoundException("Error: Trip not found.")
    }

    fun rename(id: UUID, name: String, version: Long): Trip {
        val preparedName = prepareName(name)
        require(version >= 0) {
            "Error: Trip version must not be negative."
        }

        return tripRepository.rename(id, preparedName, version)
    }

    fun delete(id: UUID) {
        tripRepository.delete(id)
    }
    
    private fun prepareName(name: String): String {
        val preparedName = name.trim()
        require(preparedName.length in 1..120) {
            "Error: Trip name must contain between 1 and 120 characters."
        }

        return preparedName
    }
}

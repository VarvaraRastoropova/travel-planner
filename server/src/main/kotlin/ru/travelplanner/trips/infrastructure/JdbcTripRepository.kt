package ru.travelplanner.trips.infrastructure

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import ru.travelplanner.trips.domain.Trip
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID
import java.sql.ResultSet
import org.springframework.transaction.annotation.Transactional
import ru.travelplanner.errors.NotFoundException
import ru.travelplanner.errors.ConflictException
import ru.travelplanner.trips.domain.TripRepository

@Repository
class JdbcTripRepository(private val jdbcTemplate: JdbcTemplate) : TripRepository {
    override fun create(name: String): Trip {
        val trip = Trip(
            id = UUID.randomUUID(),
            name = name,
            version = 0L,
            createdAt = Instant.now()
        )

        jdbcTemplate.update(
            "INSERT INTO trips (id, name, version, created_at) VALUES (?, ?, ?, ?)",
            trip.id,
            trip.name,
            trip.version,
            Timestamp.from(trip.createdAt)
        )

        return trip
    }
    
    override fun findAll(): List<Trip> {
        return jdbcTemplate.query("SELECT * FROM trips ORDER BY created_at DESC") { 
            row, _ -> readTrip(row)
        }
    }

    override fun findById(id: UUID): Trip? {
        val trips = jdbcTemplate.query(
            "SELECT * FROM trips WHERE id = ?",
            { row, _ -> readTrip(row) },
            id
        )

        return trips.firstOrNull()
    }
    
    @Transactional
    override fun rename(id: UUID, name: String, version: Long): Trip {
        val updatedRows = jdbcTemplate.update(
            "UPDATE trips SET name = ?, version = version + 1 WHERE id = ? AND version = ?",
            name,
            id,
            version
        )

        if (updatedRows == 0) {
            if (findById(id) == null) {
                throw NotFoundException("Error: Trip not found.")
            }

            throw ConflictException("Error: The trip has already been modified. Please reload it.")
        }

        return findById(id) ?: throw NotFoundException("Error: Trip not found.")
    }

    override fun delete(id: UUID) {
        val deletedRows = jdbcTemplate.update("DELETE FROM trips WHERE id = ?", id)

        if (deletedRows == 0) {
            throw NotFoundException("Error: Trip not found.")
        }
    }

    private fun readTrip(row: ResultSet): Trip {
        return Trip(
            id = row.getObject("id", UUID::class.java),
            name = row.getString("name"),
            version = row.getLong("version"),
            createdAt = row.getTimestamp("created_at").toInstant()
        )
    }
}

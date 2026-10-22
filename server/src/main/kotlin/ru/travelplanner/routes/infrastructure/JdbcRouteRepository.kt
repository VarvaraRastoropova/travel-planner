package ru.travelplanner.routes.infrastructure

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import ru.travelplanner.errors.NotFoundException
import ru.travelplanner.routes.domain.RoutePoint
import ru.travelplanner.routes.domain.CalculatedRoute
import ru.travelplanner.routes.domain.RouteRepository
import ru.travelplanner.routes.domain.SavedRoute
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.UUID

@Repository
class JdbcRouteRepository(private val jdbcTemplate: JdbcTemplate, private val objectMapper: ObjectMapper) : RouteRepository {
    @Transactional
    override fun save(route: SavedRoute): SavedRoute {
        val trips = jdbcTemplate.query(
            "SELECT id FROM trips WHERE id = ? FOR KEY SHARE",
            { row, _ -> row.getObject("id", UUID::class.java) },
            route.tripId
        )

        if (trips.isEmpty()) {
            throw NotFoundException("Error: Trip not found.")
        }

        jdbcTemplate.update(
            "INSERT INTO routes (id, trip_id, name, points, preview, created_at) VALUES (?, ?, ?, CAST(? AS jsonb), CAST(? AS jsonb), ?)",
            route.id,
            route.tripId,
            route.name,
            objectMapper.writeValueAsString(route.points),
            objectMapper.writeValueAsString(route.preview),
            Timestamp.from(route.createdAt)
        )

        return route
    }

    override fun findAll(tripId: UUID): List<SavedRoute> {
        return jdbcTemplate.query(
            "SELECT * FROM routes WHERE trip_id = ? ORDER BY created_at DESC, id",
            { row, _ -> readRoute(row) },
            tripId
        )
    }

    override fun findById(tripId: UUID, routeId: UUID): SavedRoute? {
        val routes = jdbcTemplate.query(
            "SELECT * FROM routes WHERE trip_id = ? AND id = ?",
            { row, _ -> readRoute(row) },
            tripId,
            routeId
        )

        return routes.firstOrNull()
    }

    private fun readRoute(row: ResultSet): SavedRoute {
        return SavedRoute(
            id = row.getObject("id", UUID::class.java),
            tripId = row.getObject("trip_id", UUID::class.java),
            name = row.getString("name"),
            points = objectMapper.readValue(
                row.getString("points"),
                Array<RoutePoint>::class.java
            ).toList(),
            preview = objectMapper.readValue(
                row.getString("preview"),
                CalculatedRoute::class.java
            ),
            createdAt = row.getTimestamp("created_at").toInstant()
        )
    }
}

package ru.travelplanner.routes.application

import org.springframework.stereotype.Service
import ru.travelplanner.errors.NotFoundException
import ru.travelplanner.routes.domain.RoutePoint
import ru.travelplanner.routes.domain.CalculatedRoute
import ru.travelplanner.routes.domain.RouteProvider
import ru.travelplanner.routes.domain.RouteRepository
import ru.travelplanner.routes.domain.SavedRoute
import ru.travelplanner.trips.domain.TripRepository
import ru.travelplanner.locations.domain.validateCoordinates
import java.time.Instant
import java.util.UUID
import org.slf4j.LoggerFactory

@Service
class RouteService(
    private val routeProvider: RouteProvider,
    private val routeRepository: RouteRepository,
    private val tripRepository: TripRepository
) {
    private val logger = LoggerFactory.getLogger(RouteService::class.java)
    
    fun calculate(points: List<RoutePoint>): CalculatedRoute {
        require(points.size >= 2) {
            "Error: Route must contain at least 2 points."
        }

        points.forEach { point -> validateCoordinates(point.latitude, point.longitude) }

        return routeProvider.buildWalkingRoute(points)
    }

    fun create(tripId: UUID, name: String, points: List<RoutePoint>): SavedRoute {
        val preparedName = name.trim()

        require(preparedName.length in 1..120) {
            "Error: Route name must contain between 1 and 120 characters."
        }

        checkTripExists(tripId)

        val calculatedRoute = calculate(points)

        val route = SavedRoute(
            id = UUID.randomUUID(),
            tripId = tripId,
            name = preparedName,
            points = points,
            preview = calculatedRoute,
            createdAt = Instant.now()
        )

        val savedRoute = routeRepository.save(route)

        logger.info(
            "Route saved: routeId={}, tripId={}, points={}, distanceMeters={}",
            savedRoute.id,
            savedRoute.tripId,
            savedRoute.points.size,
            savedRoute.preview.distanceMeters
        )

        return savedRoute
    }

    fun findAll(tripId: UUID): List<SavedRoute> {
        checkTripExists(tripId)
        return routeRepository.findAll(tripId)
    }

    fun get(tripId: UUID, routeId: UUID): SavedRoute {
        return routeRepository.findById(tripId, routeId) ?: throw NotFoundException("Error: Route not found.")
    }

    private fun checkTripExists(tripId: UUID) {
        if (tripRepository.findById(tripId) == null) {
            throw NotFoundException("Error: Trip not found.")
        }
    }
}

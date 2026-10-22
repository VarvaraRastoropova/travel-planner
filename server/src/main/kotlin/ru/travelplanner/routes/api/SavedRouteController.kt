package ru.travelplanner.routes.api

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import ru.travelplanner.routes.application.RouteService
import ru.travelplanner.routes.domain.RoutePoint
import ru.travelplanner.routes.domain.SavedRoute
import java.util.UUID

data class CreateRouteRequest(
    val name: String,
    val points: List<RoutePoint>
)

@RestController
@RequestMapping("/api/trips/{tripId}/routes")
class SavedRouteController(private val routeService: RouteService) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@PathVariable tripId: UUID, @RequestBody request: CreateRouteRequest): SavedRoute {
        return routeService.create(tripId, request.name, request.points)
    }

    @GetMapping
    fun findAll(@PathVariable tripId: UUID): List<SavedRoute> {
        return routeService.findAll(tripId)
    }

    @GetMapping("/{routeId}")
    fun get(@PathVariable tripId: UUID, @PathVariable routeId: UUID): SavedRoute {
        return routeService.get(tripId, routeId)
    }
}

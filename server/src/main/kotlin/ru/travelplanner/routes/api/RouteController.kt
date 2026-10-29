package ru.travelplanner.routes.api

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.travelplanner.routes.application.RouteService
import ru.travelplanner.routes.domain.RoutePoint
import ru.travelplanner.routes.domain.CalculatedRoute

data class CalculateRouteRequest(
    val points: List<RoutePoint>
)

@RestController
@RequestMapping("/api/routes")
class RouteController(private val routeService: RouteService) {
    @PostMapping("/calculate")
    fun calculate(@RequestBody request: CalculateRouteRequest): CalculatedRoute {
        return routeService.calculate(request.points)
    }
}

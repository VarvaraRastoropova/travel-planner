package ru.travelplanner.routes.domain

interface RouteProvider {
    fun buildWalkingRoute(points: List<RoutePoint>): CalculatedRoute
}

package ru.travelplanner.routes.domain

import java.util.UUID

interface RouteRepository {
    fun save(route: SavedRoute): SavedRoute
    fun findAll(tripId: UUID): List<SavedRoute>
    fun findById(tripId: UUID, routeId: UUID): SavedRoute?
}

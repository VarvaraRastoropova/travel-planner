package ru.travelplanner.routes.domain

import java.time.Instant
import java.util.UUID

data class SavedRoute(
    val id: UUID,
    val tripId: UUID,
    val name: String,
    val points: List<RoutePoint>,
    val preview: CalculatedRoute,
    val createdAt: Instant
)

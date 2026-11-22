package ru.travelplanner.helpers

import com.fasterxml.jackson.databind.JsonNode
import ru.travelplanner.errors.ExternalServiceException
import ru.travelplanner.routes.domain.RoutePoint

object RouteHelper {
    fun validateCoordinates(latitude: Double, longitude: Double) {
        require(latitude.isFinite() && latitude in -90.0..90.0) {
            "Error: Latitude must be between -90 and 90."
        }

        require(longitude.isFinite() && longitude in -180.0..180.0) {
            "Error: Longitude must be between -180 and 180."
        }
    }

    fun toRoutePoint(coordinate: JsonNode): RoutePoint {
        if (
            !coordinate.isArray || coordinate.size() < 2 ||
            !coordinate.path(0).isNumber || !coordinate.path(1).isNumber
        ) {
            throw ExternalServiceException("Error: Geoapify returned invalid route coordinates.")
        }

        val longitude = coordinate.path(0).asDouble()
        val latitude = coordinate.path(1).asDouble()

        try {
            validateCoordinates(latitude, longitude)
        } catch (_: IllegalArgumentException) {
            throw ExternalServiceException("Error: Geoapify returned invalid route coordinates.")
        }

        return RoutePoint(latitude = latitude, longitude = longitude)
    }
}

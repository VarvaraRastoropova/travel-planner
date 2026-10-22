package ru.travelplanner.routes.infrastructure

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import ru.travelplanner.infrastructure.createHttpClient
import org.springframework.web.client.RestClientException
import ru.travelplanner.errors.ExternalServiceException
import ru.travelplanner.errors.NotFoundException
import ru.travelplanner.routes.domain.RoutePoint
import ru.travelplanner.routes.domain.CalculatedRoute
import ru.travelplanner.routes.domain.RouteProvider

@Component
class GeoapifyRouteProvider(
    @Value("\${geoapify.base-url}") baseUrl: String,
    @Value("\${geoapify.api-key}") private val apiKey: String
) : RouteProvider {
    private val client = createHttpClient(baseUrl)

    override fun buildWalkingRoute(points: List<RoutePoint>): CalculatedRoute {
        if (apiKey.isBlank()) {
            throw ExternalServiceException("Error: Geoapify API key is not configured.")
        }

        val waypoints = points.joinToString("|") { point ->
            "${point.latitude},${point.longitude}"
        }

        val response = try {
            client.get()
                .uri { builder ->
                    builder.path("/v1/routing")
                        .queryParam("apiKey", apiKey)
                        .queryParam("waypoints", waypoints)
                        .queryParam("mode", "walk")
                        .queryParam("units", "metric")
                        .queryParam("format", "geojson")
                        .build()
                }
                .retrieve()
                .body(JsonNode::class.java)
        } catch (exception: RestClientException) {
            throw ExternalServiceException("Error: Geoapify route request failed.")
        }

        val features = response?.get("features")

        if (features == null || !features.isArray) {
            throw ExternalServiceException("Error: Geoapify returned an invalid route response.")
        }

        val route = features.firstOrNull() ?: throw NotFoundException("Error: Walking route was not found.")

        val properties = route.path("properties")
        val distance = properties.path("distance")
        val time = properties.path("time")

        if (!distance.isNumber || !time.isNumber) {
            throw ExternalServiceException("Error: Geoapify returned invalid route measurements.")
        }

        val distanceMeters = distance.asDouble()
        val durationSeconds = time.asDouble()

        if (
            !distanceMeters.isFinite() || distanceMeters < 0 ||
            !durationSeconds.isFinite() || durationSeconds < 0
        ) {
            throw ExternalServiceException("Error: Geoapify returned invalid route measurements.")
        }

        val geometry = route.path("geometry")
        val coordinates = geometry.path("coordinates")

        if (
            geometry.path("type").asText() != "MultiLineString" ||
            !coordinates.isArray || coordinates.isEmpty()
        ) {
            throw ExternalServiceException("Error: Geoapify returned invalid route geometry.")
        }

        val paths = coordinates.map { line ->
            if (!line.isArray || line.size() < 2) {
                throw ExternalServiceException("Error: Geoapify returned an invalid route line.")
            }

            line.map { coordinate ->
                toRoutePoint(coordinate)
            }
        }

        return CalculatedRoute(distanceMeters = distanceMeters, durationSeconds = durationSeconds, paths = paths)
    }

    private fun toRoutePoint(coordinate: JsonNode): RoutePoint {
        if (
            !coordinate.isArray || coordinate.size() < 2 ||
            !coordinate.path(0).isNumber || !coordinate.path(1).isNumber
        ) {
            throw ExternalServiceException("Error: Geoapify returned invalid route coordinates.")
        }

        val longitude = coordinate.path(0).asDouble()
        val latitude = coordinate.path(1).asDouble()

        if (
            !latitude.isFinite() || latitude !in -90.0..90.0 ||
            !longitude.isFinite() || longitude !in -180.0..180.0
        ) {
            throw ExternalServiceException("Error: Geoapify returned invalid route coordinates.")
        }

        return RoutePoint(latitude = latitude, longitude = longitude)
    }
}

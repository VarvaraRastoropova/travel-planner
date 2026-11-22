package ru.travelplanner.locations.infrastructure

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import ru.travelplanner.infrastructure.createHttpClient
import org.springframework.web.client.RestClientException
import ru.travelplanner.errors.ExternalServiceException
import ru.travelplanner.errors.NotFoundException
import ru.travelplanner.locations.domain.FoundLocation
import ru.travelplanner.locations.domain.LocationProvider
import ru.travelplanner.locations.domain.LocationBounds
import org.springframework.web.client.HttpClientErrorException

@Component
class GeoapifyLocationProvider(
    @Value("\${geoapify.base-url}") baseUrl: String,
    @Value("\${geoapify.api-key}") private val apiKey: String
) : LocationProvider {
    private val client = createHttpClient(baseUrl)

    override fun findCity(latitude: Double, longitude: Double): FoundLocation {
        val cities = requestLocations(
            "/v1/geocode/reverse",
            mapOf(
                "lat" to latitude.toString(),
                "lon" to longitude.toString(),
                "type" to "city"
            )
        )

        return cities.firstOrNull() ?: throw NotFoundException("Error: City was not found for these coordinates.")
    }

    override fun findCities(query: String): List<FoundLocation> {
        return requestLocations(
            "/v1/geocode/search",
            mapOf(
                "text" to query,
                "type" to "city",
                "bias" to "countrycode:none",
                "limit" to "5"
            )
        )
    }

    override fun findPlaces(query: String, city: FoundLocation): List<FoundLocation> {
        val bounds = city.bounds ?: throw ExternalServiceException("Error: City search bounds are missing.")
        val filter = "rect:${bounds.west},${bounds.south},${bounds.east},${bounds.north}"

        return requestLocations(
            "/v1/geocode/search",
            mapOf(
                "text" to "$query, ${city.name}",
                "filter" to filter,
                "bias" to "countrycode:none",
                "limit" to "5"
            )
        )
    }
    
    override fun findNearby(latitude: Double, longitude: Double, radiusMeters: Int, categories: List<String>): List<FoundLocation> {
        if (apiKey.isBlank()) {
            throw ExternalServiceException("Error: Geoapify API key is not configured.")
        }

        val response = try {
            client.get()
                .uri { builder ->
                    builder.path("/v2/places")
                        .queryParam("apiKey", apiKey)
                        .queryParam("categories", categories.joinToString(","))
                        .queryParam("filter", "circle:$longitude,$latitude,$radiusMeters")
                        .queryParam("bias", "proximity:$longitude,$latitude")
                        .queryParam("limit", 20)
                        .build()
                }
                .retrieve()
                .body(JsonNode::class.java)
        } catch (exception: HttpClientErrorException.BadRequest) {
            throw IllegalArgumentException("Error: Geoapify rejected the place search parameters.")
        } catch (exception: RestClientException) {
            throw ExternalServiceException("Error: Geoapify place search failed.")
        }

        val features = response?.get("features")

        if (features == null || !features.isArray) {
            throw ExternalServiceException("Error: Geoapify returned an invalid place search response.")
        }

        return features.map { feature ->
            toLocation(feature.path("properties"))
        }
    }

    private fun requestLocations(path: String, parameters: Map<String, String>): List<FoundLocation> {
        if (apiKey.isBlank()) {
            throw ExternalServiceException("Error: Geoapify API key is not configured.")
        }

        val response = try {
            client.get()
                .uri { builder ->
                    builder.path(path)
                        .queryParam("format", "json")
                        .queryParam("apiKey", apiKey)

                    parameters.forEach { (name, value) ->
                        builder.queryParam(name, value)
                    }

                    builder.build()
                }
                .retrieve()
                .body(JsonNode::class.java)
        } catch (exception: RestClientException) {
            throw ExternalServiceException("Error: Geoapify request failed.")
        }

        val results = response?.get("results")

        if (results == null || !results.isArray) {
            throw ExternalServiceException("Error: Geoapify returned an invalid response.")
        }

        return results.map { result ->
            toLocation(result)
        }
    }

    private fun toLocation(result: JsonNode): FoundLocation {
        val id = result.path("place_id").asText("")
        val address = result.path("formatted").asText("")
        val name = result.path("name").asText("")
            .ifBlank { result.path("city").asText("") }
            .ifBlank { address }

        if (
            id.isBlank() || address.isBlank() ||
            !result.path("lat").isNumber || !result.path("lon").isNumber
        ) {
            throw ExternalServiceException("Error: Geoapify returned invalid location data.")
        }
        
        val bbox = result.get("bbox")
        val bounds = if (
            bbox != null &&
            listOf("lon1", "lat1", "lon2", "lat2").all { bbox.path(it).isNumber }
        ) {
            LocationBounds(
                west = bbox.path("lon1").asDouble(),
                south = bbox.path("lat1").asDouble(),
                east = bbox.path("lon2").asDouble(),
                north = bbox.path("lat2").asDouble()
            )
        } else {
            null
        }

        return FoundLocation(
            id = id,
            name = name,
            address = address,
            latitude = result.path("lat").asDouble(),
            longitude = result.path("lon").asDouble(),
            bounds = bounds
        )
    }
}

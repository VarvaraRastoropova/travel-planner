package ru.travelplanner.locations.application

import org.springframework.stereotype.Service
import ru.travelplanner.errors.NotFoundException
import ru.travelplanner.locations.domain.FoundLocation
import ru.travelplanner.locations.domain.LocationProvider
import ru.travelplanner.locations.domain.validateCoordinates

@Service
class LocationService(private val locationProvider: LocationProvider) {
    fun findCity(latitude: Double, longitude: Double): FoundLocation {
        validateCoordinates(latitude, longitude)
        return locationProvider.findCity(latitude, longitude)
    }

    fun findCities(query: String): List<FoundLocation> {
        val preparedQuery = prepareQuery(query)
        return locationProvider.findCities(preparedQuery)
    }

    fun findPlaces(query: String, city: String): List<FoundLocation> {
        val preparedQuery = prepareQuery(query)
        val cities = findCities(city)

        if (cities.isEmpty()) {
            throw NotFoundException("Error: City not found.")
        }

        require(cities.size == 1) {
            "Error: Specify a city name that identifies exactly one city."
        }

        return locationProvider.findPlaces(preparedQuery, cities.single())
    }

    fun findNearby(latitude: Double, longitude: Double, radiusMeters: Int, categories: List<String>): List<FoundLocation> {
        validateCoordinates(latitude, longitude)

        require(radiusMeters in 100..10000) {
            "Error: Search radius must be between 100 and 10000 meters."
        }

        require(categories.isNotEmpty() && categories.size <= 10) {
            "Error: Specify between 1 and 10 categories."
        }

        val preparedCategories = categories.map { it.trim() }.distinct()

        require(preparedCategories.all { it.isNotBlank() && it.length <= 120 }) {
            "Error: Category must contain between 1 and 120 characters."
        }

        return locationProvider.findNearby(
            latitude = latitude,
            longitude = longitude,
            radiusMeters = radiusMeters,
            categories = preparedCategories
        )
    }

    private fun prepareQuery(query: String): String {
        val preparedQuery = query.trim()
        require(preparedQuery.length in 1..200) {
            "Error: Search query must contain between 1 and 200 characters."
        }

        return preparedQuery
    }
}

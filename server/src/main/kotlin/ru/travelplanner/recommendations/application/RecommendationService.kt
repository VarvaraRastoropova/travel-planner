package ru.travelplanner.recommendations.application

import org.springframework.stereotype.Service
import ru.travelplanner.errors.NotFoundException
import ru.travelplanner.locations.domain.LocationProvider
import ru.travelplanner.recommendations.domain.PreferenceParser
import ru.travelplanner.recommendations.domain.RecommendationContext
import ru.travelplanner.locations.domain.validateCoordinates

@Service
class RecommendationService(private val preferenceParser: PreferenceParser, private val locationProvider: LocationProvider) {
    fun prepare(text: String, latitude: Double?, longitude: Double?): RecommendationContext {
        val bothMissing = latitude == null && longitude == null
        val bothProvided = latitude != null && longitude != null
        require(bothMissing || bothProvided) {
            "Error: Latitude and longitude must be provided together."
        }

        if (latitude != null && longitude != null) {
            validateCoordinates(latitude, longitude)
        }

        val preferences = preferenceParser.parse(text)
        
        val cityQuery = preferences.city?.trim()
        val cities = if (!cityQuery.isNullOrEmpty()) {
            locationProvider.findCities(cityQuery)
        } else {
            require(latitude != null && longitude != null) {
                "Error: Specify a city or provide your coordinates."
            }

            listOf(locationProvider.findCity(latitude, longitude))
        }

        if (cities.isEmpty()) {
            throw NotFoundException("Error: City was not found. Please clarify its name.")
        }

        val locationQuery = preferences.locationQuery?.trim()
        val places = if (cities.size == 1 && !locationQuery.isNullOrEmpty()) {
            locationProvider.findPlaces(locationQuery, cities.single())
        } else {
            emptyList()
        }

        return RecommendationContext(
            preferences = preferences,
            cityCandidates = cities,
            placeCandidates = places
        )
    }
}

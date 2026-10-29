package ru.travelplanner.locations.domain

interface LocationProvider {
    fun findCity(latitude: Double, longitude: Double): FoundLocation
    fun findCities(query: String): List<FoundLocation>
    fun findPlaces(query: String, city: FoundLocation): List<FoundLocation>
    fun findNearby(latitude: Double, longitude: Double, radiusMeters: Int, categories: List<String>): List<FoundLocation>
}

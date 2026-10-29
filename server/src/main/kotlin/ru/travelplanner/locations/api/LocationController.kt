package ru.travelplanner.locations.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.travelplanner.locations.application.LocationService
import ru.travelplanner.locations.domain.FoundLocation

@RestController
@RequestMapping("/api/locations")
class LocationController(private val locationService: LocationService) {
    @GetMapping("/city")
    fun findCity(@RequestParam latitude: Double, @RequestParam longitude: Double): FoundLocation {
        return locationService.findCity(latitude, longitude)
    }

    @GetMapping("/cities")
    fun findCities(@RequestParam query: String): List<FoundLocation> {
        return locationService.findCities(query)
    }

    @GetMapping("/places")
    fun findPlaces(@RequestParam query: String, @RequestParam city: String): List<FoundLocation> {
        return locationService.findPlaces(query, city)
    }

    @GetMapping("/nearby")
    fun findNearby(
        @RequestParam latitude: Double,
        @RequestParam longitude: Double,
        @RequestParam(defaultValue = "1500") radiusMeters: Int,
        @RequestParam(defaultValue = "tourism.sights,leisure.park") categories: List<String>
    ): List<FoundLocation> {
        return locationService.findNearby(
            latitude = latitude,
            longitude = longitude,
            radiusMeters = radiusMeters,
            categories = categories
        )
    }
}

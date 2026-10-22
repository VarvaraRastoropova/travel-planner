package ru.travelplanner.locations.domain

fun validateCoordinates(latitude: Double, longitude: Double) {
    require(latitude.isFinite() && latitude in -90.0..90.0) {
        "Error: Latitude must be between -90 and 90."
    }

    require(longitude.isFinite() && longitude in -180.0..180.0) {
        "Error: Longitude must be between -180 and 180."
    }
}

package ru.travelplanner.trips.api

data class CreateTripRequest(
    @field:NotBlank
    @field:Size(max = 120)
    val name: String
)

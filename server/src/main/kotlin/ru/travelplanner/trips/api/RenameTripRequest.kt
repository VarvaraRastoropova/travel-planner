package ru.travelplanner.trips.api

data class RenameTripRequest(
    @field:NotBlank
    @field:Size(max = 120)
    val name: String,

    @field:Min(0)
    val version: Long
)

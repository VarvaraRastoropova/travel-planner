package ru.travelplanner.recommendations.api

data class PrepareRecommendationRequest(
    @field:NotBlank
    @field:Size(max = 2000)
    val text: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)

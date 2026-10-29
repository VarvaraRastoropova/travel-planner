package ru.travelplanner.recommendations.domain

import ru.travelplanner.locations.domain.FoundLocation
import ru.travelplanner.routes.domain.Preferences

data class RecommendationContext(
    val preferences: Preferences,
    val cityCandidates: List<FoundLocation>,
    val placeCandidates: List<FoundLocation>
)

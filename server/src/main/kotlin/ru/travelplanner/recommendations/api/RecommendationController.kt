package ru.travelplanner.recommendations.api

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.travelplanner.recommendations.application.RecommendationService
import ru.travelplanner.recommendations.domain.RecommendationContext

data class PrepareRecommendationRequest(
    @field:NotBlank
    @field:Size(max = 2000)
    val text: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)

@RestController
@RequestMapping("/api/recommendations")
class RecommendationController(private val recommendationService: RecommendationService) {
    @PostMapping("/context")
    fun prepare(@Valid @RequestBody request: PrepareRecommendationRequest): RecommendationContext {
        return recommendationService.prepare(
            text = request.text,
            latitude = request.latitude,
            longitude = request.longitude
        )
    }
}

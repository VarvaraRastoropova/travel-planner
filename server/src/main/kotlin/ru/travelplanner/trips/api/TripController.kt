package ru.travelplanner.trips.api

import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.travelplanner.trips.application.TripService
import ru.travelplanner.trips.domain.Trip
import java.net.URI
import java.util.UUID

data class CreateTripRequest(
    @field:NotBlank
    @field:Size(max = 120)
    val name: String
)

data class RenameTripRequest(
    @field:NotBlank
    @field:Size(max = 120)
    val name: String,

    @field:Min(0)
    val version: Long
)

@RestController
@RequestMapping("/api/trips")
class TripController(private val tripService: TripService) {
    @PostMapping
    fun create(@Valid @RequestBody request: CreateTripRequest): ResponseEntity<Trip> {
        val trip = tripService.create(request.name)
        return ResponseEntity.created(URI.create("/api/trips/${trip.id}")).body(trip)
    }

    @GetMapping
    fun findAll(): List<Trip> {
        return tripService.findAll()
    }

    @GetMapping("/{tripId}")
    fun get(@PathVariable tripId: UUID): Trip {
        return tripService.get(tripId)
    }

    @PutMapping("/{tripId}")
    fun rename(@PathVariable tripId: UUID, @Valid @RequestBody request: RenameTripRequest): Trip {
        return tripService.rename(tripId, request.name, request.version)
    }

    @DeleteMapping("/{tripId}")
    fun delete(@PathVariable tripId: UUID): ResponseEntity<Void> {
        tripService.delete(tripId)
        return ResponseEntity.noContent().build()
    }
}

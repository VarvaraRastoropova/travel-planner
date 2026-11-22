package ru.travelplanner

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import ru.travelplanner.routes.domain.RoutePoint
import ru.travelplanner.routes.domain.CalculatedRoute
import ru.travelplanner.routes.domain.RouteRepository
import ru.travelplanner.routes.domain.SavedRoute
import ru.travelplanner.trips.domain.TripRepository
import java.time.Instant
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DatabaseIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var tripRepository: TripRepository

    @Autowired
    private lateinit var routeRepository: RouteRepository

    @Autowired
    private lateinit var transactionManager: PlatformTransactionManager

    @BeforeEach
    fun clearDatabase() {
        jdbcTemplate.update("DELETE FROM trips")
    }

    @Test
    fun createsAndReadsTripThroughApi() {
        val response = mockMvc.perform(
            post("/api/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mapOf("name" to "Moscow trip")))
        )
            .andExpect(status().isCreated)
            .andReturn()

        val tripId = objectMapper.readTree(
            response.response.contentAsString
        ).path("id").asText()

        mockMvc.perform(get("/api/trips/$tripId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Moscow trip"))

        assertEquals(
            "Moscow trip",
            tripRepository.findById(UUID.fromString(tripId))?.name
        )
    }

    @Test
    fun rejectsBlankTripNameThroughApi() {
        mockMvc.perform(
            post("/api/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mapOf("name" to "   ")))
        )
            .andExpect(status().isBadRequest)

        assertTrue(tripRepository.findAll().isEmpty())
    }

    @Test
    fun rejectsOutdatedTripVersion() {
        val trip = tripRepository.create("Original name")

        mockMvc.perform(
            put("/api/trips/${trip.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        mapOf("name" to "Updated name", "version" to trip.version)
                    )
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.version").value(1))

        mockMvc.perform(
            put("/api/trips/${trip.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        mapOf("name" to "Outdated update", "version" to trip.version)
                    )
                )
        )
            .andExpect(status().isConflict)

        assertEquals("Updated name", tripRepository.findById(trip.id)?.name)
        assertEquals(1L, tripRepository.findById(trip.id)?.version)
    }

    @Test
    fun savesRouteAndReadsItOnlyWithinItsTrip() {
        val trip = tripRepository.create("First trip")
        val otherTrip = tripRepository.create("Second trip")
        val route = createSavedRoute(trip.id)

        assertEquals(route, routeRepository.findById(trip.id, route.id))

        mockMvc.perform(get("/api/trips/${trip.id}/routes/${route.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("City walk"))
            .andExpect(jsonPath("$.preview.distanceMeters").value(187.0))

        mockMvc.perform(get("/api/trips/${otherTrip.id}/routes/${route.id}"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun deletingTripAlsoDeletesItsRoutes() {
        val trip = tripRepository.create("Trip to delete")
        createSavedRoute(trip.id)

        mockMvc.perform(delete("/api/trips/${trip.id}"))
            .andExpect(status().isNoContent)

        assertNull(tripRepository.findById(trip.id))
        assertTrue(routeRepository.findAll(trip.id).isEmpty())
    }

    @Test
    fun rollsBackDatabaseChangesWhenTransactionFails() {
        var tripId: UUID? = null
        val transaction = TransactionTemplate(transactionManager)

        assertThrows(IllegalStateException::class.java) {
            transaction.executeWithoutResult {
                tripId = tripRepository.create("Temporary trip").id
                throw IllegalStateException("Error: Test transaction failed.")
            }
        }

        assertNull(tripRepository.findById(requireNotNull(tripId)))
    }
    
    @Test
    fun rejectsRouteWithOnePoint() {
        val request = mapOf(
            "points" to listOf(
                RoutePoint(latitude = 55.7535908, longitude = 37.6215013)
            )
        )

        mockMvc.perform(
            post("/api/routes/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun rejectsInvalidRouteLatitude() {
        val request = mapOf(
            "points" to listOf(
                RoutePoint(latitude = 100.0, longitude = 37.6215013),
                RoutePoint(latitude = 55.752523, longitude = 37.623086)
            )
        )

        mockMvc.perform(
            post("/api/routes/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun rejectsBlankRouteName() {
        val trip = tripRepository.create("Moscow trip")

        val request = mapOf(
            "name" to "   ",
            "points" to listOf(
                RoutePoint(latitude = 55.7535908, longitude = 37.6215013),
                RoutePoint(latitude = 55.752523, longitude = 37.623086)
            )
        )

        mockMvc.perform(
            post("/api/trips/${trip.id}/routes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)

        assertTrue(routeRepository.findAll(trip.id).isEmpty())
    }

    @Test
    fun rejectsRouteForMissingTrip() {
        val tripId = UUID.randomUUID()

        val request = mapOf(
            "name" to "City walk",
            "points" to listOf(
                RoutePoint(latitude = 55.7535908, longitude = 37.6215013),
                RoutePoint(latitude = 55.752523, longitude = 37.623086)
            )
        )

        mockMvc.perform(
            post("/api/trips/$tripId/routes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isNotFound)

        assertTrue(routeRepository.findAll(tripId).isEmpty())
    }

    private fun createSavedRoute(tripId: UUID): SavedRoute {
        val points = listOf(
            RoutePoint(latitude = 55.7535908, longitude = 37.6215013),
            RoutePoint(latitude = 55.752523, longitude = 37.623086)
        )

        val route = SavedRoute(
            id = UUID.randomUUID(),
            tripId = tripId,
            name = "City walk",
            points = points,
            preview = CalculatedRoute(
                distanceMeters = 187.0,
                durationSeconds = 169.476,
                paths = listOf(points)
            ),
            createdAt = Instant.parse("2026-01-01T10:00:00Z")
        )

        return routeRepository.save(route)
    }
}

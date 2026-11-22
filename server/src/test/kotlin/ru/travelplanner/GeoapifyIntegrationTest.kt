package ru.travelplanner

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import ru.travelplanner.routes.domain.RoutePoint
import ru.travelplanner.routes.domain.CalculatedRoute
import ru.travelplanner.routes.domain.RouteRepository
import ru.travelplanner.routes.domain.SavedRoute
import ru.travelplanner.trips.domain.TripRepository

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["geoapify.api-key=\${GEOAPIFY_API_KEY}"]
)
@ActiveProfiles("test")
@Tag("external")
class GeoapifyIntegrationTest {
    @Autowired
    private lateinit var client: TestRestTemplate

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var tripRepository: TripRepository

    @Autowired
    private lateinit var routeRepository: RouteRepository

    private val points = listOf(
        RoutePoint(latitude = 55.7535908, longitude = 37.6215013),
        RoutePoint(latitude = 55.752523, longitude = 37.623086)
    )

    @BeforeEach
    fun clearDatabase() {
        jdbcTemplate.update("DELETE FROM trips")
    }

    @Test
    fun calculatesRealWalkingRouteWithoutSaving() {
        val response = client.postForEntity(
            "/api/routes/calculate",
            mapOf("points" to points),
            String::class.java
        )

        assertEquals(HttpStatus.OK, response.statusCode, response.body)

        val preview = objectMapper.readValue(
            requireNotNull(response.body),
            CalculatedRoute::class.java
        )

        assertTrue(preview.distanceMeters > 0)
        assertTrue(preview.durationSeconds > 0)
        assertTrue(preview.paths.isNotEmpty())
        assertTrue(preview.paths.all { it.size >= 2 })

        val savedRoutesCount = requireNotNull(
            jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM routes",
                Long::class.java
            )
        )

        assertEquals(0L, savedRoutesCount)
    }

    @Test
    fun calculatesSavesAndReadsRealRoute() {
        val trip = tripRepository.create("Moscow trip")

        val request = mapOf(
            "name" to "City walk",
            "points" to points
        )

        val response = client.postForEntity(
            "/api/trips/${trip.id}/routes",
            request,
            String::class.java
        )

        assertEquals(HttpStatus.CREATED, response.statusCode, response.body)

        val createdRoute = objectMapper.readValue(
            requireNotNull(response.body),
            SavedRoute::class.java
        )

        assertEquals(trip.id, createdRoute.tripId)
        assertEquals(points, createdRoute.points)
        assertTrue(createdRoute.preview.distanceMeters > 0)
        assertTrue(createdRoute.preview.durationSeconds > 0)
        assertTrue(createdRoute.preview.paths.isNotEmpty())

        assertNotNull(
            routeRepository.findById(trip.id, createdRoute.id)
        )

        val readResponse = client.getForEntity(
            "/api/trips/${trip.id}/routes/${createdRoute.id}",
            String::class.java
        )

        assertEquals(HttpStatus.OK, readResponse.statusCode, readResponse.body)

        val loadedRoute = objectMapper.readValue(
            requireNotNull(readResponse.body),
            SavedRoute::class.java
        )

        assertEquals(createdRoute.id, loadedRoute.id)
        assertEquals(createdRoute.tripId, loadedRoute.tripId)
        assertEquals(createdRoute.name, loadedRoute.name)
        assertEquals(createdRoute.points, loadedRoute.points)
        assertEquals(createdRoute.preview, loadedRoute.preview)
    }
}

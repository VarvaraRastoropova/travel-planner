package ru.travelplanner

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TravelPlannerApplication

fun main(args: Array<String>) {
    runApplication<TravelPlannerApplication>(*args)
}

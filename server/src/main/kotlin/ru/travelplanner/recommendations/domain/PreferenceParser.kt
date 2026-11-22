package ru.travelplanner.recommendations.domain

import ru.travelplanner.routes.domain.Preferences

interface PreferenceParser {
    fun parse(text: String): Preferences
}

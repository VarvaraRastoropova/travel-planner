package ru.travelplanner.infrastructure

import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestClient

fun createHttpClient(baseUrl: String): RestClient {
    val requestFactory = SimpleClientHttpRequestFactory().apply {
        setConnectTimeout(10_000)
        setReadTimeout(30_000)
    }

    return RestClient.builder()
        .baseUrl(baseUrl)
        .requestFactory(requestFactory)
        .build()
}

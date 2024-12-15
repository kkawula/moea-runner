package com.moea

import okhttp3.mockwebserver.MockWebServer

open class BaseTest {
    lateinit var mockWebServer: MockWebServer

    fun setupMockWebServer() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    fun shutdownMockWebServer() {
        mockWebServer.shutdown()
    }

    fun getBaseUrl() = mockWebServer.url("/")

    fun createMockedApiClient(baseUrl: String): ApiClient {
        return ApiClient(baseUrl)
    }
}
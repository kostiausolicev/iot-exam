package ru.guap

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun testRoot() = testApplication {
        application {
            module()
        }
        client.get("/api/statuses").apply {
            assertEquals(HttpStatusCode.OK, status, "Ожидали 200 OK на GET /api/statuses")
        }
    }

}

package com.example.dried_shrimp

package com.example.api

import io.ktor.server.application.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.serialization.gson.*
import io.ktor.server.plugins.contentnegotiation.*
import com.google.gson.JsonObject

fun Application.chatModule() {

    install(ContentNegotiation) {
        gson()
    }

    val client = HttpClient(CIO)

    routing {

        post("/chat") {
            val request = call.receive<JsonObject>()
            val userMessage = request["message"].asString

            val response: HttpResponse = client.post(
                "https://xxxxxx.openai.azure.com/openai/deployments/gpt-4o/chat/completions?api-version=2024-02-15-preview"
            ) {
                headers {
                    append("api-key", "YOUR_KEY")
                    append("Content-Type", "application/json")
                }
                setBody(
                    """
                    {
                      "messages": [
                        { "role": "system", "content": "你是電商客服助手" },
                        { "role": "user", "content": "$userMessage" }
                      ]
                    }
                    """.trimIndent()
                )
            }

            val body = response.body<JsonObject>()
            val reply = body["choices"].asJsonArray[0].asJsonObject["message"].asJsonObject["content"].asString

            call.respond(mapOf("reply" to reply))
        }
    }
}

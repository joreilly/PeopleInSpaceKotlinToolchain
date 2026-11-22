package dev.johnoreilly.peopleinspace

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class AstroResult(val message: String, val number: Int, val people: List<Assignment>)

@Serializable
data class Assignment(val craft: String, val name: String)

@Serializable
data class IssPosition(val latitude: Double, val longitude: Double)

val json = Json { ignoreUnknownKeys = true }

class PeopleInSpaceApi(
    // The engine (OkHttp on JVM/Android, Darwin on iOS) is picked up from each platform's dependencies.
    private val client: HttpClient = HttpClient { install(ContentNegotiation) { json(json) } },
) {
    suspend fun fetchPeople(): AstroResult =
        client.get("https://people-in-space-proxy.ew.r.appspot.com/astros.json").body()

    suspend fun fetchIssPosition(): IssPosition =
        client.get("https://api.wheretheiss.at/v1/satellites/25544").body()
}

package com.example.backend

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main() {

    // Khởi tạo Firebase
    FirebaseConfig.initialize()

    // Khởi tạo các Repository
    val userRepository = UserRepository()
    val placeRepository = PlaceRepository()
    val favoritePlaceRepository = FavoritePlaceRepository()

    // Khởi động HTTP server
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {

        routing {

            // =========================
            // SERVER
            // =========================

            get("/") {
                call.respondText(
                    """{"message":"Backend is running!"}""",
                    ContentType.Application.Json
                )
            }

            // =========================
            // USERS
            // =========================

            get("/users/{userId}") {

                println(">>> API: nhận request /users/${call.parameters["userId"]}")

                val userId = call.parameters["userId"]

                if (userId == null) {
                    call.respondText(
                        """{"error":"Missing userId"}""",
                        ContentType.Application.Json,
                        HttpStatusCode.BadRequest
                    )
                    return@get
                }

                val user = userRepository.getUser(userId)

                if (user != null) {

                    println(">>> API: đã lấy được User, chuẩn bị tạo JSON")

                    val json = buildJson(
                        mapOf(
                            "username" to user.username,
                            "fullName" to user.fullName,
                            "email" to user.email,
                            "role" to user.role,
                            "preferences" to user.preferences,
                            "createdAt" to user.createdAt
                        )
                    )

                    println(">>> JSON = $json")

                    call.respondText(
                        json,
                        ContentType.Application.Json
                    )
                }
            }

            // =========================
            // PLACES
            // =========================

            get("/places/{placeId}") {

                val placeId = call.parameters["placeId"]

                if (placeId == null) {
                    call.respondText(
                        """{"error":"Missing placeId"}""",
                        ContentType.Application.Json,
                        HttpStatusCode.BadRequest
                    )
                    return@get
                }

                val place = placeRepository.getPlace(placeId)

                if (place != null) {
                    call.respondText(
                        buildJson(
                            mapOf(
                                "placeId" to place.placeId,
                                "name" to place.name,
                                "category" to place.category,
                                "address" to place.address,
                                "latitude" to place.latitude,
                                "longitude" to place.longitude,
                                "priceLevel" to place.priceLevel,
                                "rating" to place.rating,
                                "reviewCount" to place.reviewCount,
                                "googleMapsUri" to place.googleMapsUri,
                                "websiteUri" to place.websiteUri
                            )
                        ),
                        ContentType.Application.Json
                    )
                } else {
                    call.respondText(
                        """{"error":"Place not found"}""",
                        ContentType.Application.Json,
                        HttpStatusCode.NotFound
                    )
                }
            }

            // =========================
            // ALL PLACES
            // =========================

            get("/places") {

                val places = placeRepository.getAllPlaces()

                val json = places.joinToString(
                    prefix = "[",
                    postfix = "]"
                ) { place ->

                    buildJson(
                        mapOf(
                            "placeId" to place.placeId,
                            "name" to place.name,
                            "category" to place.category,
                            "address" to place.address,
                            "latitude" to place.latitude,
                            "longitude" to place.longitude,
                            "priceLevel" to place.priceLevel,
                            "rating" to place.rating,
                            "reviewCount" to place.reviewCount,
                            "googleMapsUri" to place.googleMapsUri,
                            "websiteUri" to place.websiteUri
                        )
                    )
                }

                call.respondText(
                    json,
                    ContentType.Application.Json
                )
            }

            // =========================
            // FAVORITES
            // =========================

            get("/users/{userId}/favorites") {

                val userId = call.parameters["userId"]

                if (userId == null) {
                    call.respondText(
                        """{"error":"Missing userId"}""",
                        ContentType.Application.Json,
                        HttpStatusCode.BadRequest
                    )
                    return@get
                }

                val favorites =
                    favoritePlaceRepository.getFavoritesByUser(userId)

                val json = favorites.joinToString(
                    prefix = "[",
                    postfix = "]"
                ) { favorite ->

                    buildJson(
                        mapOf(
                            "userId" to favorite.userId,
                            "placeId" to favorite.placeId,
                            "createdAt" to favorite.createdAt
                        )
                    )
                }

                call.respondText(
                    json,
                    ContentType.Application.Json
                )
            }

            post("/users/{userId}/favorites/{placeId}") {

                val userId = call.parameters["userId"]
                val placeId = call.parameters["placeId"]

                if (userId == null || placeId == null) {
                    call.respondText(
                        """{"error":"Missing userId or placeId"}""",
                        ContentType.Application.Json,
                        HttpStatusCode.BadRequest
                    )
                    return@post
                }

                favoritePlaceRepository.addFavorite(
                    userId,
                    placeId
                )

                call.respondText(
                    """{"message":"Favorite added successfully"}""",
                    ContentType.Application.Json,
                    HttpStatusCode.Created
                )
            }

            delete("/users/{userId}/favorites/{placeId}") {

                val userId = call.parameters["userId"]
                val placeId = call.parameters["placeId"]

                if (userId == null || placeId == null) {
                    call.respondText(
                        """{"error":"Missing userId or placeId"}""",
                        ContentType.Application.Json,
                        HttpStatusCode.BadRequest
                    )
                    return@delete
                }

                favoritePlaceRepository.removeFavorite(
                    userId,
                    placeId
                )

                call.respondText(
                    """{"message":"Favorite removed successfully"}""",
                    ContentType.Application.Json
                )
            }
        }
    }.start(wait = true)
}

/**
 * Chuyển dữ liệu thành JSON.
 */
fun buildJson(data: Map<String, Any?>): String {

    return data.entries.joinToString(
        prefix = "{",
        postfix = "}"
    ) { (key, value) ->

        "\"${escapeJson(key)}\":${valueToJson(value)}"
    }
}

/**
 * Chuyển từng kiểu dữ liệu sang JSON.
 */
fun valueToJson(value: Any?): String {

    return when (value) {

        null -> "null"

        is String ->
            "\"${escapeJson(value)}\""

        is Number, is Boolean ->
            value.toString()

        is List<*> ->
            value.joinToString(
                prefix = "[",
                postfix = "]"
            ) {
                valueToJson(it)
            }

        else ->
            "\"${escapeJson(value.toString())}\""
    }
}

/**
 * Escape ký tự đặc biệt trong JSON.
 */
fun escapeJson(value: String): String {

    return value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")
}
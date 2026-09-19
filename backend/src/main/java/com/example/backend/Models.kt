package com.example.backend

data class User(
    val username: String = "",
    val fullName: String = "",
    val email: String = "",
    val role: String = "user",
    val preferences: List<String> = emptyList(),
    val createdAt: String = ""
)

data class Place(
    val placeId: String = "",
    val name: String = "",
    val category: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val priceLevel: Long = 0,
    val rating: Double = 0.0,
    val reviewCount: Long = 0,
    val googleMapsUri: String = "",
    val websiteUri: String = ""
)

data class FavoritePlace(
    val userId: String = "",
    val placeId: String = "",
    val createdAt: String = ""
)
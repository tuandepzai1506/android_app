package com.example.backend

class PlaceRepository {

    private val db = FirebaseConfig.getFirestore()

    private val places = db.collection("places")

    fun createPlace(place: Place) {
        places
            .document(place.placeId)
            .set(place)
            .get()
    }

    fun getPlace(placeId: String): Place? {

        val document = places
            .document(placeId)
            .get()
            .get()

        if (!document.exists()) {
            return null
        }

        return Place(
            placeId = document.getString("placeId") ?: document.id,
            name = document.getString("name") ?: "",
            category = document.getString("category") ?: "",
            address = document.getString("address") ?: "",
            latitude = document.getDouble("latitude") ?: 0.0,
            longitude = document.getDouble("longitude") ?: 0.0,
            priceLevel = document.getLong("priceLevel") ?: 0L,
            rating = document.getDouble("rating") ?: 0.0,
            reviewCount = document.getLong("reviewCount") ?: 0L,
            googleMapsUri = document.getString("googleMapsUri") ?: "",
            websiteUri = document.getString("websiteUri") ?: ""
        )
    }

    fun getAllPlaces(): List<Place> {

        val result = places
            .get()
            .get()

        return result.documents.map { document ->

            Place(
                placeId = document.getString("placeId") ?: document.id,
                name = document.getString("name") ?: "",
                category = document.getString("category") ?: "",
                address = document.getString("address") ?: "",
                latitude = document.getDouble("latitude") ?: 0.0,
                longitude = document.getDouble("longitude") ?: 0.0,
                priceLevel = document.getLong("priceLevel") ?: 0L,
                rating = document.getDouble("rating") ?: 0.0,
                reviewCount = document.getLong("reviewCount") ?: 0L,
                googleMapsUri = document.getString("googleMapsUri") ?: "",
                websiteUri = document.getString("websiteUri") ?: ""
            )
        }
    }

    fun updatePlace(place: Place) {

        places
            .document(place.placeId)
            .set(place)
            .get()
    }

    fun deletePlace(placeId: String) {

        places
            .document(placeId)
            .delete()
            .get()
    }
}
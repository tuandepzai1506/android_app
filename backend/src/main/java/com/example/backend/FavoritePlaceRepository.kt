package com.example.backend

class FavoritePlaceRepository {

    private val db = FirebaseConfig.getFirestore()

    private val favorites = db.collection("favorite_places")

    fun addFavorite(userId: String, placeId: String) {

        val documentId = "${userId}_${placeId}"

        val favorite = FavoritePlace(
            userId = userId,
            placeId = placeId,
            createdAt = System.currentTimeMillis().toString()
        )

        favorites
            .document(documentId)
            .set(favorite)
            .get()
    }

    fun getFavoritesByUser(userId: String): List<FavoritePlace> {

        println(">>> favorites: bắt đầu đọc user $userId")

        val result = favorites
            .whereEqualTo("userId", userId)
            .get()
            .get()

        println(">>> favorites: Firestore đã trả kết quả")
        println(">>> favorites: tìm thấy ${result.documents.size} document")

        return result.documents.map { document ->

            println(">>> favorites: đang đọc document ${document.id}")

            FavoritePlace(
                userId = document.getString("userId") ?: "",
                placeId = document.getString("placeId") ?: "",
                createdAt = document.get("createdAt")?.toString() ?: ""
            )
        }
    }

    fun isFavorite(userId: String, placeId: String): Boolean {

        val documentId = "${userId}_${placeId}"

        return favorites
            .document(documentId)
            .get()
            .get()
            .exists()
    }

    fun removeFavorite(userId: String, placeId: String) {

        val documentId = "${userId}_${placeId}"

        favorites
            .document(documentId)
            .delete()
            .get()
    }
}
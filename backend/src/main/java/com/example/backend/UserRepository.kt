package com.example.backend

class UserRepository {

    private val db = FirebaseConfig.getFirestore()

    private val users = db.collection("users")

    fun createUser(userId: String, user: User) {
        users
            .document(userId)
            .set(user)
            .get()
    }

    fun getUser(userId: String): User? {

        println(">>> getUser: bắt đầu đọc user $userId")

        val document = users
            .document(userId)
            .get()
            .get()

        println(">>> getUser: Firestore đã trả kết quả")

        if (!document.exists()) {
            println(">>> getUser: user không tồn tại")
            return null
        }

        println(">>> getUser: user tồn tại, bắt đầu chuyển dữ liệu")

        println(">>> field username")
        val username = document.getString("username") ?: ""

        println(">>> field fullName")
        val fullName = document.getString("fullName") ?: ""

        println(">>> field email")
        val email = document.getString("email") ?: ""

        println(">>> field role")
        val role = document.getString("role") ?: "user"

        println(">>> field preferences")
        val preferences = (document.get("preferences") as? List<*>)
            ?.filterIsInstance<String>()
            ?: emptyList()

        println(">>> field createdAt")
        val createdAt = document.get("createdAt")?.toString() ?: ""

        println(">>> username = [$username]")
        println(">>> fullName = [$fullName]")
        println(">>> email = [$email]")
        println(">>> role = [$role]")
        println(">>> preferences = [$preferences]")
        println(">>> createdAt = [$createdAt]")
        println(">>> chuẩn bị tạo User object")

        println(">>> tạo User object")

        return User(
            username = username,
            fullName = fullName,
            email = email,
            role = role,
            preferences = preferences,
            createdAt = createdAt
        )
    }

    fun getAllUsers(): List<User> {

        val result = users
            .get()
            .get()

        return result.documents.map { document ->

            User(
                username = document.getString("username") ?: "",
                fullName = document.getString("fullName") ?: "",
                email = document.getString("email") ?: "",
                role = document.getString("role") ?: "user",
                preferences = document.get("preferences") as? List<String> ?: emptyList(),
                createdAt = document.getString("createdAt") ?: ""
            )
        }
    }

    fun updateUser(
        userId: String,
        username: String,
        fullName: String,
        email: String,
        role: String,
        preferences: List<String>
    ) {
        users
            .document(userId)
            .update(
                mapOf(
                    "username" to username,
                    "fullName" to fullName,
                    "email" to email,
                    "role" to role,
                    "preferences" to preferences
                )
            )
            .get()
    }

    fun deleteUser(userId: String) {
        users
            .document(userId)
            .delete()
            .get()
    }
}
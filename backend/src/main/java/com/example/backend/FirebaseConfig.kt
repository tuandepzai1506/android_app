package com.example.backend

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import java.io.FileInputStream

object FirebaseConfig {

    private var initialized = false

    fun initialize() {

        if (initialized) {
            return
        }

        val serviceAccountFile = java.io.File(
            System.getProperty("user.dir"),
            "../firebase/serviceAccountKey.json"
        )

        println(">>> Service account path: ${serviceAccountFile.absolutePath}")

        val serviceAccount =
            FileInputStream(serviceAccountFile)

        val options = FirebaseOptions.builder()
            .setCredentials(
                GoogleCredentials.fromStream(serviceAccount)
            )
            .build()

        FirebaseApp.initializeApp(options)

        initialized = true

        println(">>> Firebase connected successfully")
    }

    fun getFirestore(): Firestore {

        if (!initialized) {
            initialize()
        }

        return FirestoreClient.getFirestore()
    }
}
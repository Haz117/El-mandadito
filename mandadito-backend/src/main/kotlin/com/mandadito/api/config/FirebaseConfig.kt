package com.mandadito.api.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import java.io.FileInputStream
import jakarta.annotation.PostConstruct

@Configuration
class FirebaseConfig {

    private val log = LoggerFactory.getLogger(javaClass)

    @Value("\${firebase.credentials.path:}")
    private lateinit var credentialsPath: String

    @PostConstruct
    fun initialize() {
        if (credentialsPath.isBlank()) {
            log.warn("Firebase not configured — set FIREBASE_CREDENTIALS_PATH to enable push notifications")
            return
        }
        if (FirebaseApp.getApps().isNotEmpty()) return
        try {
            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(FileInputStream(credentialsPath)))
                .build()
            FirebaseApp.initializeApp(options)
            log.info("Firebase initialized successfully")
        } catch (e: Exception) {
            log.error("Firebase initialization failed: ${e.message}")
        }
    }
}

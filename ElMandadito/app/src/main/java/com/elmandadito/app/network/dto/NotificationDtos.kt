package com.elmandadito.app.network.dto

data class RegisterTokenRequest(
    val token: String,
    val platform: String = "ANDROID"
)

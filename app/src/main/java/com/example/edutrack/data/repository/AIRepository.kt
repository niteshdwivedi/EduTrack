package com.example.edutrack.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIRepository @Inject constructor() {
    // Note: In a real app, store this securely (e.g., BuildConfig or Secrets Gradle Plugin)
    private val apiKey = "AIzaSyCC5u7R3NZCd2xDZjztBUJ72BSaFsIf8_A"

    private val model = GenerativeModel(
        // CHANGE THIS LINE: Update to a supported model version
        modelName = "gemini-flash-latest",
        apiKey = apiKey
    )

    suspend fun getAIResponse(prompt: String): String? {
        return try {
            val response = model.generateContent(prompt)
            response.text
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}
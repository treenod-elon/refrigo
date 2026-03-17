package com.todaymenu.app.data.remote.gemini.dto

import kotlinx.serialization.Serializable

@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>
)

@Serializable
data class GeminiContent(
    val parts: List<GeminiPart>
)

@Serializable
data class GeminiPart(
    val text: String
)

fun createGeminiRequest(prompt: String): GeminiRequest {
    return GeminiRequest(
        contents = listOf(
            GeminiContent(
                parts = listOf(GeminiPart(text = prompt))
            )
        )
    )
}

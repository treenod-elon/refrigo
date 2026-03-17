package com.todaymenu.app.data.remote.gemini.dto

import kotlinx.serialization.Serializable

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate> = emptyList()
) {
    val text: String
        get() = candidates.firstOrNull()
            ?.content?.parts?.firstOrNull()?.text.orEmpty()
}

@Serializable
data class GeminiCandidate(
    val content: GeminiContent
)

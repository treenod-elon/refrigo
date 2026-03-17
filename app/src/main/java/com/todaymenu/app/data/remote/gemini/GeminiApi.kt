package com.todaymenu.app.data.remote.gemini

import com.todaymenu.app.data.remote.gemini.dto.GeminiRequest
import com.todaymenu.app.data.remote.gemini.dto.GeminiResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApi {
    @POST("v1beta/models/gemini-2.0-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

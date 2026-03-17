package com.todaymenu.app.domain.usecase

import com.todaymenu.app.data.remote.gemini.AiEngineRouter
import com.todaymenu.app.data.remote.gemini.GeminiResponseParser
import javax.inject.Inject

class VoiceInputUseCase @Inject constructor(
    private val aiRouter: AiEngineRouter
) {
    suspend fun parseVoiceInput(recognizedText: String): Result<List<ScannedIngredient>> {
        return try {
            val response = aiRouter.parseVoiceInput(recognizedText)
            response.map { json ->
                GeminiResponseParser.parseWithRetry<List<ScannedIngredientDto>>(
                    rawResponse = json,
                    retryAction = {
                        aiRouter.parseVoiceInput(recognizedText).getOrThrow()
                    }
                ).getOrThrow().map { dto ->
                    ScannedIngredient(
                        name = dto.name,
                        quantity = dto.quantity,
                        unit = dto.unit,
                        category = dto.category
                    )
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

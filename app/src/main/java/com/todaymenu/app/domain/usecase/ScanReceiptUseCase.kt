package com.todaymenu.app.domain.usecase

import com.todaymenu.app.data.remote.gemini.AiEngineRouter
import javax.inject.Inject

data class ScannedIngredient(
    val name: String,
    val quantity: Double = 1.0,
    val unit: String = "개",
    val category: String = "기타",
    val isSelected: Boolean = true
)

class ScanReceiptUseCase @Inject constructor(
    private val aiRouter: AiEngineRouter
) {
    suspend fun parseReceiptText(ocrText: String): Result<List<ScannedIngredient>> {
        return try {
            val response = aiRouter.parseIngredients(ocrText)
            response.map { json ->
                // GeminiResponseParser로 파싱
                com.todaymenu.app.data.remote.gemini.GeminiResponseParser
                    .parseWithRetry<List<ScannedIngredientDto>>(
                        rawResponse = json,
                        retryAction = {
                            aiRouter.parseIngredients(ocrText).getOrThrow()
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

@kotlinx.serialization.Serializable
internal data class ScannedIngredientDto(
    val name: String,
    val quantity: Double = 1.0,
    val unit: String = "개",
    val category: String = "기타"
)

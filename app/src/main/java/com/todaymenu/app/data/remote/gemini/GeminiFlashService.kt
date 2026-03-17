package com.todaymenu.app.data.remote.gemini

import com.todaymenu.app.BuildConfig
import com.todaymenu.app.data.remote.gemini.dto.createGeminiRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiFlashService @Inject constructor(
    private val geminiApi: GeminiApi
) {
    private val apiKey = BuildConfig.GEMINI_API_KEY

    suspend fun generateContent(prompt: String): String {
        val request = createGeminiRequest(prompt)
        val response = geminiApi.generateContent(apiKey, request)
        return response.text
    }

    suspend fun parseIngredients(ocrText: String): Result<String> {
        return try {
            val prompt = """
                당신은 한국 마트 영수증 분석 전문가입니다.
                다음 텍스트는 OCR로 인식된 한국 마트 영수증입니다.
                식재료 항목만 추출하여 JSON 배열로 반환하세요.
                봉투, 할인, 합계, 카드, 포인트 등 비식재료 항목은 제외하세요.

                반드시 순수 JSON만 응답하세요. 마크다운이나 설명 텍스트를 포함하지 마세요.
                형식: [{"name":"재료명","quantity":수량,"unit":"단위","category":"카테고리"}]

                카테고리: 육류/채소/해산물/유제품/양념/과일/곡류·면/냉동식품/음료/기타

                영수증 텍스트:
                $ocrText
            """.trimIndent()
            val response = generateContent(prompt)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun estimateExpiry(name: String, storageType: String): Result<Int> {
        return try {
            val storageLabel = when (storageType) {
                "fridge" -> "냉장"
                "freezer" -> "냉동"
                "room" -> "실온"
                else -> "냉장"
            }
            val prompt = """
                식재료 "$name"을(를) ${storageLabel} 보관할 때 예상 유통기한(일수)을 숫자만 응답하세요.
                다른 텍스트 없이 숫자만 응답하세요.
                예: 7
            """.trimIndent()
            val response = generateContent(prompt)
            val days = response.trim().filter { it.isDigit() }.toIntOrNull() ?: 7
            Result.success(days)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

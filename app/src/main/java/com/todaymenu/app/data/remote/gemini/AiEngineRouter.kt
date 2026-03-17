package com.todaymenu.app.data.remote.gemini

import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI 엔진 라우터 — Nano 가능 여부에 따라 자동 분기
 *
 * Nano 사용 가능 (Z Flip6 등) → 온디바이스 처리 (API 키 불필요)
 * Nano 미지원 (Note 9 등) → Flash 클라우드 폴백
 */
@Singleton
class AiEngineRouter @Inject constructor(
    private val nanoService: GeminiNanoService,
    private val flashService: GeminiFlashService
) {
    suspend fun parseIngredients(text: String): Result<String> {
        return if (nanoService.isAvailable()) {
            nanoService.parseIngredients(text)
        } else {
            flashService.parseIngredients(text)
        }
    }

    suspend fun estimateExpiry(name: String, storageType: String): Result<Int> {
        return if (nanoService.isAvailable()) {
            nanoService.estimateExpiry(name, storageType)
        } else {
            flashService.estimateExpiry(name, storageType)
        }
    }

    suspend fun parseVoiceInput(text: String): Result<String> {
        return if (nanoService.isAvailable()) {
            nanoService.parseVoiceInput(text)
        } else {
            try {
                val prompt = """
                    다음은 음성으로 입력된 한국어 텍스트입니다.
                    식재료 항목을 추출하여 JSON 배열로 반환하세요.
                    한국어 구어체 숫자를 아라비아 숫자로 변환하세요.
                    (예: "세 개" → 3, "한 판" → 1)

                    반드시 순수 JSON만 응답하세요.
                    형식: [{"name":"재료명","quantity":수량,"unit":"단위","category":"카테고리"}]

                    카테고리: 육류/채소/해산물/유제품/양념/과일/곡류·면/냉동식품/음료/기타

                    음성 텍스트:
                    $text
                """.trimIndent()
                val response = flashService.generateContent(prompt)
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun generateContent(prompt: String): String {
        return flashService.generateContent(prompt)
    }

    suspend fun isNanoAvailable(): Boolean = nanoService.isAvailable()
}

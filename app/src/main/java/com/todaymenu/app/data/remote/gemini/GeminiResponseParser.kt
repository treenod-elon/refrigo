package com.todaymenu.app.data.remote.gemini

import kotlinx.serialization.json.Json

object GeminiResponseParser {

    @PublishedApi
    internal val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    fun extractJson(rawResponse: String): String {
        var cleaned = rawResponse.trim()

        // 마크다운 코드블록 제거
        val codeBlockRegex = Regex("""```(?:json)?\s*\n?([\s\S]*?)\n?\s*```""")
        val codeBlockMatch = codeBlockRegex.find(cleaned)
        if (codeBlockMatch != null) {
            cleaned = codeBlockMatch.groupValues[1].trim()
        }

        // 앞뒤 비-JSON 텍스트 제거
        val jsonStartIndex = cleaned.indexOfFirst { it == '{' || it == '[' }
        val jsonEndIndex = cleaned.indexOfLast { it == '}' || it == ']' }
        if (jsonStartIndex >= 0 && jsonEndIndex > jsonStartIndex) {
            cleaned = cleaned.substring(jsonStartIndex, jsonEndIndex + 1)
        }

        // 후행 쉼표 제거
        cleaned = cleaned.replace(Regex(",\\s*([}\\]])"), "$1")

        return cleaned
    }

    suspend inline fun <reified T> parseWithRetry(
        rawResponse: String,
        retryAction: suspend () -> String,
        maxRetries: Int = 2
    ): Result<T> {
        val cleaned = extractJson(rawResponse)
        try {
            val result = json.decodeFromString<T>(cleaned)
            return Result.success(result)
        } catch (_: Exception) {
            // 1차 파싱 실패
        }

        repeat(maxRetries) { attempt ->
            try {
                val retryResponse = retryAction()
                val retryCleaned = extractJson(retryResponse)
                val result = json.decodeFromString<T>(retryCleaned)
                return Result.success(result)
            } catch (e: Exception) {
                if (attempt == maxRetries - 1) {
                    return Result.failure(e)
                }
            }
        }
        return Result.failure(Exception("JSON 파싱 최종 실패"))
    }
}

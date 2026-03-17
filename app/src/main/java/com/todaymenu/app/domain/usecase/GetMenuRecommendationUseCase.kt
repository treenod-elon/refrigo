package com.todaymenu.app.domain.usecase

import com.todaymenu.app.data.remote.gemini.AiEngineRouter
import com.todaymenu.app.data.remote.gemini.GeminiResponseParser
import com.todaymenu.app.domain.model.MenuRecommendation
import com.todaymenu.app.domain.model.RecipeIngredient
import com.todaymenu.app.domain.repository.MenuRepository
import kotlinx.serialization.Serializable
import java.net.URLEncoder
import javax.inject.Inject

class GetMenuRecommendationUseCase @Inject constructor(
    private val menuRepository: MenuRepository,
    private val aiRouter: AiEngineRouter
) {
    suspend operator fun invoke(
        ingredientNames: List<String>,
        cuisineType: String,
        excludeMenuNames: List<String> = emptyList()
    ): Result<List<MenuRecommendation>> {
        // 캐시 키 = 재료 해시 + 유형 + 제외 목록
        val cacheKey = buildCacheKey(ingredientNames, cuisineType, excludeMenuNames)
        val cached = menuRepository.getCachedResponse(cacheKey)
        if (cached != null) {
            val parsed = parseResponse(cached)
            if (parsed.isSuccess) return parsed
        }

        // Gemini API 호출
        val prompt = if (excludeMenuNames.isEmpty()) {
            buildInitialPrompt(ingredientNames, cuisineType)
        } else {
            buildLoadMorePrompt(ingredientNames, cuisineType, excludeMenuNames)
        }

        return try {
            val response = aiRouter.generateContent(prompt)
            // 캐시 저장
            menuRepository.cacheResponse(cacheKey, response)
            parseResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun parseResponse(rawResponse: String): Result<List<MenuRecommendation>> {
        return try {
            val dto = GeminiResponseParser.parseWithRetry<MenuRecommendationResponseDto>(
                rawResponse = rawResponse,
                retryAction = { rawResponse } // 캐시된 응답이므로 재시도 불필요
            ).getOrThrow()

            val recommendations = dto.recommendations.map { it.toDomain() }
            Result.success(recommendations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildCacheKey(
        ingredientNames: List<String>,
        cuisineType: String,
        excludeMenuNames: List<String>
    ): String {
        val ingredientHash = ingredientNames.sorted().hashCode()
        val excludeHash = excludeMenuNames.sorted().hashCode()
        return "menu_${ingredientHash}_${cuisineType}_$excludeHash"
    }

    private fun buildInitialPrompt(ingredientNames: List<String>, cuisineType: String): String {
        val ingredientList = ingredientNames.joinToString(", ")
        return """
당신은 한국 가정식 요리 전문가입니다.

[보유 재료]
$ingredientList

[추천 유형]
$cuisineType  (빠른요리 / 한식 / 중식 / 양식)

[요청]
보유 재료를 최대한 활용하여 "$cuisineType" 유형의 메뉴 3가지를 추천하세요.

[조건]
- 보유 재료 활용률이 높은 순서대로 정렬
- 일반 가정에서 쉽게 만들 수 있는 메뉴 위주
- 기본 양념(소금, 간장, 설탕, 식용유 등)은 있다고 가정
- 각 메뉴별 상세 레시피 포함
- "빠른요리"는 20분 이내 조리 가능한 메뉴만 추천
- 각 메뉴에 대해 유튜브/블로그 검색에 적합한 검색 키워드도 제공하세요
- 각 메뉴의 영문명(nameEn)도 제공하세요 (음식 이미지 검색용, 예: "Kimchi Jjigae")

[응답 형식 - 순수 JSON만]
{
  "cuisineType": "$cuisineType",
  "recommendations": [
    {
      "name": "메뉴명",
      "nameEn": "English Menu Name",
      "description": "한 줄 설명",
      "matchRate": 85,
      "cookingTime": "30분",
      "difficulty": "쉬움|보통|어려움",
      "servings": "2인분",
      "ingredients": [
        {"name": "재료명", "amount": "수량", "owned": true}
      ],
      "seasonings": ["필요한 기본 양념"],
      "steps": ["조리 단계1", "조리 단계2"],
      "tip": "요리 팁",
      "searchKeyword": "메뉴명 레시피"
    }
  ]
}
        """.trimIndent()
    }

    private fun buildLoadMorePrompt(
        ingredientNames: List<String>,
        cuisineType: String,
        excludeMenuNames: List<String>
    ): String {
        val ingredientList = ingredientNames.joinToString(", ")
        val excludeList = excludeMenuNames.joinToString(", ")
        return """
당신은 한국 가정식 요리 전문가입니다.

[보유 재료]
$ingredientList

[추천 유형]
$cuisineType

[이미 추천된 메뉴 — 반드시 제외]
$excludeList

[요청]
위에서 이미 추천된 메뉴를 제외하고, 보유 재료를 활용한 "$cuisineType" 유형의 새로운 메뉴 3가지를 추가로 추천하세요.

[조건]
- 이미 추천된 메뉴와 동일하거나 유사한 메뉴는 절대 포함하지 마세요
- 보유 재료 활용률이 높은 순서대로 정렬
- 이전 추천과 다른 스타일/조리법의 메뉴를 우선 추천
- 기본 양념(소금, 간장, 설탕, 식용유 등)은 있다고 가정
- "빠른요리"는 20분 이내 조리 가능한 메뉴만 추천

[응답 형식 - 순수 JSON만]
{
  "cuisineType": "$cuisineType",
  "recommendations": [
    {
      "name": "메뉴명",
      "nameEn": "English Menu Name",
      "description": "한 줄 설명",
      "matchRate": 85,
      "cookingTime": "30분",
      "difficulty": "쉬움|보통|어려움",
      "servings": "2인분",
      "ingredients": [
        {"name": "재료명", "amount": "수량", "owned": true}
      ],
      "seasonings": ["필요한 기본 양념"],
      "steps": ["조리 단계1", "조리 단계2"],
      "tip": "요리 팁",
      "searchKeyword": "메뉴명 레시피"
    }
  ]
}
        """.trimIndent()
    }
}

@Serializable
internal data class MenuRecommendationResponseDto(
    val cuisineType: String = "",
    val recommendations: List<MenuRecommendationDto> = emptyList()
)

@Serializable
internal data class MenuRecommendationDto(
    val name: String,
    val nameEn: String = "",
    val description: String = "",
    val matchRate: Int = 0,
    val cookingTime: String = "",
    val difficulty: String = "",
    val servings: String = "",
    val ingredients: List<RecipeIngredientDto> = emptyList(),
    val seasonings: List<String> = emptyList(),
    val steps: List<String> = emptyList(),
    val tip: String = "",
    val searchKeyword: String = ""
) {
    fun toDomain(): MenuRecommendation {
        val keyword = searchKeyword.ifEmpty { "$name 레시피" }
        val encodedKeyword = URLEncoder.encode(keyword, "UTF-8")
        return MenuRecommendation(
            menuName = name,
            nameEn = nameEn,
            description = description,
            matchRate = matchRate,
            cookingTime = cookingTime,
            difficulty = difficulty,
            servings = servings,
            ingredients = ingredients.map { it.toDomain() },
            seasonings = seasonings,
            steps = steps,
            tip = tip,
            searchKeyword = keyword,
            youtubeSearchUrl = "https://www.youtube.com/results?search_query=$encodedKeyword",
            blogSearchUrl = "https://search.naver.com/search.naver?where=blog&query=$encodedKeyword"
        )
    }
}

@Serializable
internal data class RecipeIngredientDto(
    val name: String,
    val amount: String = "",
    val owned: Boolean = false
) {
    fun toDomain() = RecipeIngredient(
        name = name,
        amount = amount,
        isAvailable = owned
    )
}

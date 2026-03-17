package com.todaymenu.app.domain.usecase

import com.todaymenu.app.data.remote.gemini.AiEngineRouter
import com.todaymenu.app.data.remote.gemini.GeminiResponseParser
import com.todaymenu.app.domain.model.MealPlan
import com.todaymenu.app.domain.model.MealType
import com.todaymenu.app.domain.model.ShoppingItem
import com.todaymenu.app.domain.repository.MealPlanRepository
import com.todaymenu.app.domain.repository.ShoppingRepository
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

data class MealPlanGenerationResult(
    val mealPlans: List<MealPlan>,
    val shoppingItems: List<ShoppingItem>,
    val nutritionNote: String = ""
)

class GenerateMealPlanUseCase @Inject constructor(
    private val mealPlanRepository: MealPlanRepository,
    private val shoppingRepository: ShoppingRepository,
    private val aiRouter: AiEngineRouter
) {
    suspend operator fun invoke(
        ingredientList: String,
        expiringIngredients: String,
        durationDays: Int = 7,
        mealsPerDay: List<String> = listOf("점심", "저녁"),
        preference: String = "한식위주",
        familySize: Int = 2,
        startDate: Long = System.currentTimeMillis()
    ): Result<MealPlanGenerationResult> {
        val prompt = buildPrompt(
            ingredientList, expiringIngredients,
            durationDays, mealsPerDay, preference, familySize
        )

        return try {
            val response = aiRouter.generateContent(prompt)
            val dto = GeminiResponseParser.parseWithRetry<MealPlanResponseDto>(
                rawResponse = response,
                retryAction = { aiRouter.generateContent(prompt) }
            ).getOrThrow()

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val mealPlans = mutableListOf<MealPlan>()
            val shoppingItems = mutableListOf<ShoppingItem>()

            dto.mealPlan.forEach { dayPlan ->
                val date = try {
                    dateFormat.parse(dayPlan.date)?.time ?: startDate
                } catch (_: Exception) {
                    startDate
                }

                dayPlan.meals.forEach { (mealKey, meal) ->
                    val mealType = when (mealKey) {
                        "breakfast" -> MealType.BREAKFAST
                        "lunch" -> MealType.LUNCH
                        "dinner" -> MealType.DINNER
                        else -> return@forEach
                    }
                    mealPlans.add(
                        MealPlan(
                            date = date,
                            mealType = mealType,
                            menuName = meal.name,
                            recipe = meal.briefRecipe,
                            requiredIngredients = meal.ingredients.map { "${it.name} ${it.amount}" }
                        )
                    )
                }
            }

            dto.shoppingList.forEach { item ->
                shoppingItems.add(
                    ShoppingItem(
                        name = item.name,
                        amount = item.amount,
                        sourceType = "ai"
                    )
                )
            }

            // DB 저장
            mealPlanRepository.insertAll(mealPlans)
            shoppingRepository.insertAll(shoppingItems)

            Result.success(
                MealPlanGenerationResult(
                    mealPlans = mealPlans,
                    shoppingItems = shoppingItems,
                    nutritionNote = dto.nutritionNote
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildPrompt(
        ingredientList: String,
        expiringIngredients: String,
        durationDays: Int,
        mealsPerDay: List<String>,
        preference: String,
        familySize: Int
    ): String {
        val mealsStr = mealsPerDay.joinToString(", ")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(System.currentTimeMillis())

        return """
당신은 한국 가정식 식단 전문가이자 영양사입니다.

[보유 재료 및 수량]
$ingredientList

[유통기한 임박 재료 — 반드시 우선 사용]
$expiringIngredients

[조건]
- 기간: ${durationDays}일 (시작일: $today)
- 끼니: $mealsStr
- 선호 스타일: $preference
- 가족 수: ${familySize}인
- 보유 재료를 최대한 활용
- 유통기한 임박 재료를 처음 3일 내에 소진
- 같은 메뉴 중복 금지
- 탄수화물/단백질/채소 균형 고려
- 부족한 재료는 쇼핑리스트로 정리

[응답 형식 - 순수 JSON만]
{
  "mealPlan": [
    {
      "date": "$today",
      "meals": {
        "lunch": {
          "name": "메뉴명",
          "ingredients": [{"name": "재료", "amount": "양"}],
          "briefRecipe": "간단 레시피"
        },
        "dinner": {
          "name": "메뉴명",
          "ingredients": [{"name": "재료", "amount": "양"}],
          "briefRecipe": "간단 레시피"
        }
      }
    }
  ],
  "shoppingList": [
    {"name": "부족재료", "amount": "필요량"}
  ],
  "nutritionNote": "이번 식단의 영양 포인트"
}
        """.trimIndent()
    }
}

@Serializable
internal data class MealPlanResponseDto(
    val mealPlan: List<DayPlanDto> = emptyList(),
    val shoppingList: List<ShoppingItemDto> = emptyList(),
    val nutritionNote: String = ""
)

@Serializable
internal data class DayPlanDto(
    val date: String = "",
    val meals: Map<String, MealDto> = emptyMap()
)

@Serializable
internal data class MealDto(
    val name: String = "",
    val ingredients: List<MealIngredientDto> = emptyList(),
    val briefRecipe: String = ""
)

@Serializable
internal data class MealIngredientDto(
    val name: String = "",
    val amount: String = ""
)

@Serializable
internal data class ShoppingItemDto(
    val name: String = "",
    val amount: String = ""
)

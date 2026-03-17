package com.todaymenu.app.domain.usecase

import com.todaymenu.app.domain.model.MealPlan
import com.todaymenu.app.domain.repository.MealPlanRepository
import javax.inject.Inject

class GenerateMealPlanUseCase @Inject constructor(
    private val mealPlanRepository: MealPlanRepository
) {
    // Phase 6에서 상세 구현
    suspend operator fun invoke(
        durationDays: Int = 7,
        mealsPerDay: List<String> = listOf("점심", "저녁"),
        preference: String = "한식위주"
    ): Result<List<MealPlan>> {
        return Result.success(emptyList())
    }
}

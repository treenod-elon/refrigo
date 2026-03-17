package com.todaymenu.app.domain.usecase

import com.todaymenu.app.domain.model.MenuRecommendation
import com.todaymenu.app.domain.repository.MenuRepository
import javax.inject.Inject

class GetMenuRecommendationUseCase @Inject constructor(
    private val menuRepository: MenuRepository
) {
    // Phase 4에서 상세 구현
    suspend operator fun invoke(
        ingredientNames: List<String>,
        cuisineType: String,
        excludeMenuNames: List<String> = emptyList()
    ): Result<List<MenuRecommendation>> {
        return Result.success(emptyList())
    }
}

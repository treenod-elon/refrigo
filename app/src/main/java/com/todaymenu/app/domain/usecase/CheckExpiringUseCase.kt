package com.todaymenu.app.domain.usecase

import com.todaymenu.app.domain.model.Ingredient
import com.todaymenu.app.domain.repository.IngredientRepository
import javax.inject.Inject

class CheckExpiringUseCase @Inject constructor(
    private val repository: IngredientRepository
) {
    suspend operator fun invoke(daysBefore: Int = 3): List<Ingredient> {
        val thresholdDate = System.currentTimeMillis() + (daysBefore * 86400000L)
        return repository.getExpiringSoon(thresholdDate)
    }
}

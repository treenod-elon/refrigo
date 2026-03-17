package com.todaymenu.app.domain.usecase

import com.todaymenu.app.domain.model.Ingredient
import com.todaymenu.app.domain.repository.IngredientRepository
import javax.inject.Inject

class AddIngredientUseCase @Inject constructor(
    private val repository: IngredientRepository
) {
    suspend operator fun invoke(ingredient: Ingredient): Long {
        return repository.insert(ingredient)
    }

    suspend fun addAll(ingredients: List<Ingredient>): List<Long> {
        return repository.insertAll(ingredients)
    }
}

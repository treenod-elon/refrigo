package com.todaymenu.app.domain.usecase

import com.todaymenu.app.data.local.db.dao.ExpiryDefaultDao
import com.todaymenu.app.data.local.db.entity.ExpiryDefaultEntity
import com.todaymenu.app.data.remote.gemini.AiEngineRouter
import com.todaymenu.app.domain.model.Ingredient
import com.todaymenu.app.domain.repository.IngredientRepository
import javax.inject.Inject

class EstimateExpiryUseCase @Inject constructor(
    private val aiRouter: AiEngineRouter,
    private val ingredientRepository: IngredientRepository,
    private val expiryDefaultDao: ExpiryDefaultDao
) {
    suspend fun estimateAndUpdate(ingredients: List<Ingredient>): List<Ingredient> {
        val needsEstimation = ingredients.filter { it.expiryDate == null }
        if (needsEstimation.isEmpty()) return ingredients

        val results = mutableListOf<Ingredient>()

        for (item in needsEstimation) {
            // 1순위: 로컬 사전에서 조회
            val localDefault = expiryDefaultDao.find(item.name, item.storageType.value)
                ?: expiryDefaultDao.findFuzzy(item.name, item.storageType.value)

            val estimatedDays = if (localDefault != null) {
                localDefault.estimatedDays
            } else {
                // 2순위: AI 추정 (Nano → Flash 폴백)
                val aiResult = aiRouter.estimateExpiry(item.name, item.storageType.value)
                val days = aiResult.getOrDefault(7)

                // API 결과를 로컬 사전에 영구 저장 (자기학습형 캐시)
                expiryDefaultDao.insert(
                    ExpiryDefaultEntity(
                        ingredientName = item.name,
                        storageType = item.storageType.value,
                        estimatedDays = days,
                        source = "api"
                    )
                )
                days
            }

            val updatedItem = item.copy(
                expiryDate = item.purchaseDate + (estimatedDays * 86400000L),
                isExpiryEstimated = true
            )
            ingredientRepository.update(updatedItem)
            results.add(updatedItem)
        }

        // 이미 유통기한이 있는 재료 + 추정 완료 재료 합치기
        val alreadySet = ingredients.filter { it.expiryDate != null }
        return alreadySet + results
    }
}

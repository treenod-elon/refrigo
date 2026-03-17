package com.todaymenu.app.domain.repository

import com.todaymenu.app.domain.model.MenuRecommendation
import kotlinx.coroutines.flow.Flow

interface MenuRepository {
    fun getAllHistory(): Flow<List<MenuRecommendation>>
    suspend fun getHistoryById(id: Long): MenuRecommendation?
    suspend fun saveHistory(menu: MenuRecommendation, usedIngredientIds: List<Long>): Long
    suspend fun updateRating(id: Long, rating: Int)
    suspend fun getCachedResponse(cacheKey: String): String?
    suspend fun cacheResponse(cacheKey: String, responseJson: String)
}

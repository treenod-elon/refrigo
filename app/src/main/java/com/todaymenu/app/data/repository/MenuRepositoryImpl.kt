package com.todaymenu.app.data.repository

import com.todaymenu.app.data.local.db.dao.ApiCacheDao
import com.todaymenu.app.data.local.db.dao.MenuHistoryDao
import com.todaymenu.app.data.local.db.entity.ApiCacheEntity
import com.todaymenu.app.data.local.db.entity.MenuHistoryEntity
import com.todaymenu.app.domain.model.MenuRecommendation
import com.todaymenu.app.domain.repository.MenuRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MenuRepositoryImpl @Inject constructor(
    private val menuHistoryDao: MenuHistoryDao,
    private val apiCacheDao: ApiCacheDao
) : MenuRepository {

    override fun getAllHistory(): Flow<List<MenuRecommendation>> =
        menuHistoryDao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getHistoryById(id: Long): MenuRecommendation? =
        menuHistoryDao.getById(id)?.toDomain()

    override suspend fun saveHistory(menu: MenuRecommendation, usedIngredientIds: List<Long>): Long {
        val entity = MenuHistoryEntity(
            menuName = menu.menuName,
            description = menu.description,
            recipe = menu.recipe,
            recipeVideoUrl = menu.youtubeSearchUrl.ifEmpty { null },
            recipeBlogUrl = menu.blogSearchUrl.ifEmpty { null },
            usedIngredientIds = usedIngredientIds.joinToString(",")
        )
        return menuHistoryDao.insert(entity)
    }

    override suspend fun updateRating(id: Long, rating: Int) {
        val entity = menuHistoryDao.getById(id) ?: return
        menuHistoryDao.update(entity.copy(rating = rating))
    }

    override suspend fun getCachedResponse(cacheKey: String): String? =
        apiCacheDao.getValid(cacheKey)?.responseJson

    override suspend fun cacheResponse(cacheKey: String, responseJson: String) {
        apiCacheDao.insert(ApiCacheEntity(cacheKey = cacheKey, responseJson = responseJson))
    }
}

private fun MenuHistoryEntity.toDomain() = MenuRecommendation(
    menuName = menuName,
    description = description,
    recipe = recipe,
    youtubeSearchUrl = recipeVideoUrl.orEmpty(),
    blogSearchUrl = recipeBlogUrl.orEmpty()
)

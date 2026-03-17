package com.todaymenu.app.data.repository

import com.todaymenu.app.data.local.db.dao.MealPlanDao
import com.todaymenu.app.data.local.db.entity.MealPlanEntity
import com.todaymenu.app.domain.model.MealPlan
import com.todaymenu.app.domain.model.MealType
import com.todaymenu.app.domain.repository.MealPlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject

class MealPlanRepositoryImpl @Inject constructor(
    private val dao: MealPlanDao
) : MealPlanRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun getByDateRange(startDate: Long, endDate: Long): Flow<List<MealPlan>> =
        dao.getByDateRange(startDate, endDate).map { list -> list.map { it.toDomain() } }

    override fun getByDate(date: Long): Flow<List<MealPlan>> =
        dao.getByDate(date).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): MealPlan? =
        dao.getById(id)?.toDomain()

    override suspend fun insert(mealPlan: MealPlan): Long =
        dao.insert(mealPlan.toEntity())

    override suspend fun insertAll(mealPlans: List<MealPlan>): List<Long> =
        dao.insertAll(mealPlans.map { it.toEntity() })

    override suspend fun update(mealPlan: MealPlan) =
        dao.update(mealPlan.toEntity())

    override suspend fun delete(mealPlan: MealPlan) =
        dao.delete(mealPlan.toEntity())

    override suspend fun deleteByDateRange(startDate: Long, endDate: Long) =
        dao.deleteByDateRange(startDate, endDate)
}

private fun MealPlanEntity.toDomain() = MealPlan(
    id = id,
    date = date,
    mealType = MealType.fromValue(mealType),
    menuName = menuName,
    recipe = recipe,
    requiredIngredients = try {
        Json.decodeFromString<List<String>>(requiredIngredients)
    } catch (_: Exception) {
        emptyList()
    },
    isCompleted = isCompleted
)

private fun MealPlan.toEntity() = MealPlanEntity(
    id = id,
    date = date,
    mealType = mealType.value,
    menuName = menuName,
    recipe = recipe,
    requiredIngredients = kotlinx.serialization.json.JsonArray(
        requiredIngredients.map { kotlinx.serialization.json.JsonPrimitive(it) }
    ).toString(),
    isCompleted = isCompleted
)

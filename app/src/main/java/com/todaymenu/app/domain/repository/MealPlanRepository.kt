package com.todaymenu.app.domain.repository

import com.todaymenu.app.domain.model.MealPlan
import kotlinx.coroutines.flow.Flow

interface MealPlanRepository {
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<MealPlan>>
    fun getByDate(date: Long): Flow<List<MealPlan>>
    suspend fun getById(id: Long): MealPlan?
    suspend fun insert(mealPlan: MealPlan): Long
    suspend fun insertAll(mealPlans: List<MealPlan>): List<Long>
    suspend fun update(mealPlan: MealPlan)
    suspend fun delete(mealPlan: MealPlan)
    suspend fun deleteByDateRange(startDate: Long, endDate: Long)
}

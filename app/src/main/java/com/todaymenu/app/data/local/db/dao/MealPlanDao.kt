package com.todaymenu.app.data.local.db.dao

import androidx.room.*
import com.todaymenu.app.data.local.db.entity.MealPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealPlanDao {

    @Query("SELECT * FROM meal_plans WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, mealType ASC")
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<MealPlanEntity>>

    @Query("SELECT * FROM meal_plans WHERE date = :date ORDER BY mealType ASC")
    fun getByDate(date: Long): Flow<List<MealPlanEntity>>

    @Query("SELECT * FROM meal_plans WHERE id = :id")
    suspend fun getById(id: Long): MealPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mealPlan: MealPlanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mealPlans: List<MealPlanEntity>): List<Long>

    @Update
    suspend fun update(mealPlan: MealPlanEntity)

    @Delete
    suspend fun delete(mealPlan: MealPlanEntity)

    @Query("DELETE FROM meal_plans WHERE date BETWEEN :startDate AND :endDate")
    suspend fun deleteByDateRange(startDate: Long, endDate: Long)
}

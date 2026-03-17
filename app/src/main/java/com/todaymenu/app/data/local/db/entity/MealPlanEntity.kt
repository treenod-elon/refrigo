package com.todaymenu.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_plans")
data class MealPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long,
    val mealType: String,
    val menuName: String,
    val recipe: String,
    val requiredIngredients: String,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

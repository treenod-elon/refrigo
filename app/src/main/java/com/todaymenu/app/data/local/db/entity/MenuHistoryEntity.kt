package com.todaymenu.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "menu_history")
data class MenuHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val menuName: String,
    val description: String,
    val recipe: String,
    val recipeVideoUrl: String? = null,
    val recipeBlogUrl: String? = null,
    val recipeSource: String? = null,
    val usedIngredientIds: String,
    val rating: Int? = null,
    val cookedAt: Long = System.currentTimeMillis()
)

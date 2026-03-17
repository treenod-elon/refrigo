package com.todaymenu.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.todaymenu.app.data.local.db.dao.*
import com.todaymenu.app.data.local.db.entity.*

@Database(
    entities = [
        IngredientEntity::class,
        MenuHistoryEntity::class,
        MealPlanEntity::class,
        ShoppingItemEntity::class,
        ApiCacheEntity::class,
        ExpiryDefaultEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ingredientDao(): IngredientDao
    abstract fun menuHistoryDao(): MenuHistoryDao
    abstract fun mealPlanDao(): MealPlanDao
    abstract fun shoppingItemDao(): ShoppingItemDao
    abstract fun apiCacheDao(): ApiCacheDao
    abstract fun expiryDefaultDao(): ExpiryDefaultDao
}

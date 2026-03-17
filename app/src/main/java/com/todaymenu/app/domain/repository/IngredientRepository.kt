package com.todaymenu.app.domain.repository

import com.todaymenu.app.domain.model.Ingredient
import kotlinx.coroutines.flow.Flow

interface IngredientRepository {
    fun getAllActive(): Flow<List<Ingredient>>
    fun getByStorageType(storageType: String): Flow<List<Ingredient>>
    fun getByCategory(category: String): Flow<List<Ingredient>>
    fun search(query: String): Flow<List<Ingredient>>
    suspend fun getById(id: Long): Ingredient?
    suspend fun getExpiringSoon(thresholdDate: Long): List<Ingredient>
    fun countByCategory(category: String): Flow<Int>
    suspend fun getAllIngredientNames(): List<String>
    suspend fun insert(ingredient: Ingredient): Long
    suspend fun insertAll(ingredients: List<Ingredient>): List<Long>
    suspend fun update(ingredient: Ingredient)
    suspend fun delete(ingredient: Ingredient)
    suspend fun deleteById(id: Long)
}

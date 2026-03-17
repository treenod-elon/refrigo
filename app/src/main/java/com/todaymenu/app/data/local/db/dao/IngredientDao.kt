package com.todaymenu.app.data.local.db.dao

import androidx.room.*
import com.todaymenu.app.data.local.db.entity.IngredientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {

    @Query("SELECT * FROM ingredients WHERE isConsumed = 0 ORDER BY createdAt DESC")
    fun getAllActive(): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM ingredients WHERE isConsumed = 0 AND storageType = :storageType ORDER BY createdAt DESC")
    fun getByStorageType(storageType: String): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM ingredients WHERE isConsumed = 0 AND category = :category ORDER BY createdAt DESC")
    fun getByCategory(category: String): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM ingredients WHERE isConsumed = 0 AND name LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun search(query: String): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM ingredients WHERE id = :id")
    suspend fun getById(id: Long): IngredientEntity?

    @Query("SELECT * FROM ingredients WHERE isConsumed = 0 AND expiryDate IS NOT NULL AND expiryDate <= :thresholdDate ORDER BY expiryDate ASC")
    suspend fun getExpiringSoon(thresholdDate: Long): List<IngredientEntity>

    @Query("SELECT COUNT(*) FROM ingredients WHERE isConsumed = 0 AND category = :category")
    fun countByCategory(category: String): Flow<Int>

    @Query("SELECT DISTINCT name FROM ingredients ORDER BY name ASC")
    suspend fun getAllIngredientNames(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ingredient: IngredientEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ingredients: List<IngredientEntity>): List<Long>

    @Update
    suspend fun update(ingredient: IngredientEntity)

    @Delete
    suspend fun delete(ingredient: IngredientEntity)

    @Query("DELETE FROM ingredients WHERE id = :id")
    suspend fun deleteById(id: Long)
}

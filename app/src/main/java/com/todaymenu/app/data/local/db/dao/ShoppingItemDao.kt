package com.todaymenu.app.data.local.db.dao

import androidx.room.*
import com.todaymenu.app.data.local.db.entity.ShoppingItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingItemDao {

    @Query("SELECT * FROM shopping_list ORDER BY isPurchased ASC, createdAt DESC")
    fun getAll(): Flow<List<ShoppingItemEntity>>

    @Query("SELECT * FROM shopping_list WHERE isPurchased = 0 ORDER BY createdAt DESC")
    fun getUnpurchased(): Flow<List<ShoppingItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ShoppingItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ShoppingItemEntity>): List<Long>

    @Update
    suspend fun update(item: ShoppingItemEntity)

    @Delete
    suspend fun delete(item: ShoppingItemEntity)

    @Query("DELETE FROM shopping_list WHERE isPurchased = 1")
    suspend fun clearPurchased()
}

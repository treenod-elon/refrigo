package com.todaymenu.app.domain.repository

import com.todaymenu.app.domain.model.ShoppingItem
import kotlinx.coroutines.flow.Flow

interface ShoppingRepository {
    fun getAll(): Flow<List<ShoppingItem>>
    fun getUnpurchased(): Flow<List<ShoppingItem>>
    suspend fun insert(item: ShoppingItem): Long
    suspend fun insertAll(items: List<ShoppingItem>): List<Long>
    suspend fun update(item: ShoppingItem)
    suspend fun delete(item: ShoppingItem)
    suspend fun clearPurchased()
}

package com.todaymenu.app.data.repository

import com.todaymenu.app.data.local.db.dao.ShoppingItemDao
import com.todaymenu.app.data.local.db.entity.ShoppingItemEntity
import com.todaymenu.app.domain.model.ShoppingItem
import com.todaymenu.app.domain.repository.ShoppingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ShoppingRepositoryImpl @Inject constructor(
    private val dao: ShoppingItemDao
) : ShoppingRepository {

    override fun getAll(): Flow<List<ShoppingItem>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getUnpurchased(): Flow<List<ShoppingItem>> =
        dao.getUnpurchased().map { list -> list.map { it.toDomain() } }

    override suspend fun insert(item: ShoppingItem): Long =
        dao.insert(item.toEntity())

    override suspend fun insertAll(items: List<ShoppingItem>): List<Long> =
        dao.insertAll(items.map { it.toEntity() })

    override suspend fun update(item: ShoppingItem) =
        dao.update(item.toEntity())

    override suspend fun delete(item: ShoppingItem) =
        dao.delete(item.toEntity())

    override suspend fun clearPurchased() =
        dao.clearPurchased()
}

private fun ShoppingItemEntity.toDomain() = ShoppingItem(
    id = id,
    name = name,
    amount = amount,
    isPurchased = isPurchased,
    sourceType = sourceType
)

private fun ShoppingItem.toEntity() = ShoppingItemEntity(
    id = id,
    name = name,
    amount = amount,
    isPurchased = isPurchased,
    sourceType = sourceType
)

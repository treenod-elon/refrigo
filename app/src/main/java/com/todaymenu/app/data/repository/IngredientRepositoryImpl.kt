package com.todaymenu.app.data.repository

import com.todaymenu.app.data.local.db.dao.IngredientDao
import com.todaymenu.app.data.local.db.entity.IngredientEntity
import com.todaymenu.app.domain.model.Category
import com.todaymenu.app.domain.model.Ingredient
import com.todaymenu.app.domain.model.StorageType
import com.todaymenu.app.domain.repository.IngredientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class IngredientRepositoryImpl @Inject constructor(
    private val dao: IngredientDao
) : IngredientRepository {

    override fun getAllActive(): Flow<List<Ingredient>> =
        dao.getAllActive().map { list -> list.map { it.toDomain() } }

    override fun getByStorageType(storageType: String): Flow<List<Ingredient>> =
        dao.getByStorageType(storageType).map { list -> list.map { it.toDomain() } }

    override fun getByCategory(category: String): Flow<List<Ingredient>> =
        dao.getByCategory(category).map { list -> list.map { it.toDomain() } }

    override fun search(query: String): Flow<List<Ingredient>> =
        dao.search(query).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): Ingredient? =
        dao.getById(id)?.toDomain()

    override suspend fun getExpiringSoon(thresholdDate: Long): List<Ingredient> =
        dao.getExpiringSoon(thresholdDate).map { it.toDomain() }

    override fun countByCategory(category: String): Flow<Int> =
        dao.countByCategory(category)

    override suspend fun getAllIngredientNames(): List<String> =
        dao.getAllIngredientNames()

    override suspend fun insert(ingredient: Ingredient): Long =
        dao.insert(ingredient.toEntity())

    override suspend fun insertAll(ingredients: List<Ingredient>): List<Long> =
        dao.insertAll(ingredients.map { it.toEntity() })

    override suspend fun update(ingredient: Ingredient) =
        dao.update(ingredient.toEntity())

    override suspend fun delete(ingredient: Ingredient) =
        dao.delete(ingredient.toEntity())

    override suspend fun deleteById(id: Long) =
        dao.deleteById(id)
}

private fun IngredientEntity.toDomain() = Ingredient(
    id = id,
    name = name,
    category = Category.fromValue(category),
    quantity = quantity,
    unit = unit,
    purchaseDate = purchaseDate,
    expiryDate = expiryDate,
    isExpiryEstimated = isExpiryEstimated,
    storageType = StorageType.fromValue(storageType),
    memo = memo,
    isConsumed = isConsumed,
    createdAt = createdAt,
    updatedAt = updatedAt
)

private fun Ingredient.toEntity() = IngredientEntity(
    id = id,
    name = name,
    category = category.value,
    quantity = quantity,
    unit = unit,
    purchaseDate = purchaseDate,
    expiryDate = expiryDate,
    isExpiryEstimated = isExpiryEstimated,
    storageType = storageType.value,
    memo = memo,
    isConsumed = isConsumed,
    createdAt = createdAt,
    updatedAt = updatedAt
)

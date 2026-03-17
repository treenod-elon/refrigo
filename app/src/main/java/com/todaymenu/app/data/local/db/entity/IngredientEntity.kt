package com.todaymenu.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients")
data class IngredientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String,
    val quantity: Double = 1.0,
    val unit: String = "개",
    val purchaseDate: Long,
    val expiryDate: Long? = null,
    val isExpiryEstimated: Boolean = false,
    val storageType: String = "fridge",
    val memo: String? = null,
    val isConsumed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

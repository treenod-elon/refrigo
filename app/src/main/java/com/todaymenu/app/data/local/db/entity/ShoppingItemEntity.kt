package com.todaymenu.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_list")
data class ShoppingItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val amount: String? = null,
    val isPurchased: Boolean = false,
    val sourceType: String = "manual",
    val createdAt: Long = System.currentTimeMillis()
)

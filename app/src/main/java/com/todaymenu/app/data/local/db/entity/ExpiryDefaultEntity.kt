package com.todaymenu.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expiry_defaults")
data class ExpiryDefaultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ingredientName: String,
    val storageType: String,
    val estimatedDays: Int,
    val source: String = "local"
)

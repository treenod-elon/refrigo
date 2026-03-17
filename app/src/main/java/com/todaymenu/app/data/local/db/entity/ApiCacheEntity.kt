package com.todaymenu.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "api_cache")
data class ApiCacheEntity(
    @PrimaryKey
    val cacheKey: String,
    val responseJson: String,
    val createdAt: Long = System.currentTimeMillis()
)

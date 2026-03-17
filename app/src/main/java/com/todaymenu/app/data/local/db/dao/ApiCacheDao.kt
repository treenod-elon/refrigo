package com.todaymenu.app.data.local.db.dao

import androidx.room.*
import com.todaymenu.app.data.local.db.entity.ApiCacheEntity

@Dao
interface ApiCacheDao {

    @Query("SELECT * FROM api_cache WHERE cacheKey = :key AND createdAt + 3600000 > :now")
    suspend fun getValid(key: String, now: Long = System.currentTimeMillis()): ApiCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cache: ApiCacheEntity)

    @Query("DELETE FROM api_cache WHERE createdAt + 3600000 <= :now")
    suspend fun clearExpired(now: Long = System.currentTimeMillis())
}

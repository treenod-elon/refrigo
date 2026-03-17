package com.todaymenu.app.data.local.db.dao

import androidx.room.*
import com.todaymenu.app.data.local.db.entity.ExpiryDefaultEntity

@Dao
interface ExpiryDefaultDao {

    @Query("SELECT * FROM expiry_defaults WHERE ingredientName = :name AND storageType = :storageType LIMIT 1")
    suspend fun find(name: String, storageType: String): ExpiryDefaultEntity?

    @Query("SELECT * FROM expiry_defaults WHERE ingredientName LIKE '%' || :name || '%' AND storageType = :storageType LIMIT 1")
    suspend fun findFuzzy(name: String, storageType: String): ExpiryDefaultEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ExpiryDefaultEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ExpiryDefaultEntity>)

    @Query("SELECT COUNT(*) FROM expiry_defaults")
    suspend fun count(): Int
}

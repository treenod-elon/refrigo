package com.todaymenu.app.data.local.db.dao

import androidx.room.*
import com.todaymenu.app.data.local.db.entity.MenuHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuHistoryDao {

    @Query("SELECT * FROM menu_history ORDER BY cookedAt DESC")
    fun getAll(): Flow<List<MenuHistoryEntity>>

    @Query("SELECT * FROM menu_history WHERE id = :id")
    suspend fun getById(id: Long): MenuHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(menuHistory: MenuHistoryEntity): Long

    @Update
    suspend fun update(menuHistory: MenuHistoryEntity)

    @Delete
    suspend fun delete(menuHistory: MenuHistoryEntity)
}

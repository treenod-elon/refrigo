package com.todaymenu.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.todaymenu.app.data.local.db.AppDatabase
import com.todaymenu.app.data.local.db.dao.*
import com.todaymenu.app.data.local.db.entity.ExpiryDefaultEntity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Volatile
    private var INSTANCE: AppDatabase? = null

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "today_menu.db"
            )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // 첫 실행 시 expiry_defaults.json에서 초기 데이터 로드
                        CoroutineScope(Dispatchers.IO).launch {
                            loadExpiryDefaults(context, INSTANCE!!)
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
            INSTANCE = instance
            instance
        }
    }

    private suspend fun loadExpiryDefaults(context: Context, db: AppDatabase) {
        try {
            val jsonString = context.assets.open("expiry_defaults.json")
                .bufferedReader().use { it.readText() }
            val json = Json { ignoreUnknownKeys = true }
            val defaults = json.decodeFromString<List<ExpiryDefaultJson>>(jsonString)
            val entities = defaults.map {
                ExpiryDefaultEntity(
                    ingredientName = it.ingredientName,
                    storageType = it.storageType,
                    estimatedDays = it.estimatedDays,
                    source = it.source
                )
            }
            db.expiryDefaultDao().insertAll(entities)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Serializable
    private data class ExpiryDefaultJson(
        val ingredientName: String,
        val storageType: String,
        val estimatedDays: Int,
        val source: String = "local"
    )

    @Provides
    fun provideIngredientDao(db: AppDatabase): IngredientDao = db.ingredientDao()

    @Provides
    fun provideMenuHistoryDao(db: AppDatabase): MenuHistoryDao = db.menuHistoryDao()

    @Provides
    fun provideMealPlanDao(db: AppDatabase): MealPlanDao = db.mealPlanDao()

    @Provides
    fun provideShoppingItemDao(db: AppDatabase): ShoppingItemDao = db.shoppingItemDao()

    @Provides
    fun provideApiCacheDao(db: AppDatabase): ApiCacheDao = db.apiCacheDao()

    @Provides
    fun provideExpiryDefaultDao(db: AppDatabase): ExpiryDefaultDao = db.expiryDefaultDao()
}

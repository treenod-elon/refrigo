package com.todaymenu.app.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class UserPreferencesData(
    val notificationEnabled: Boolean = true,
    val notificationDaysBefore: Int = 3,
    val defaultStorageType: String = "fridge",
    val cuisinePreference: String = "한식",
    val familySize: Int = 2,
    val isFirstLaunch: Boolean = true
)

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        val NOTIFICATION_DAYS_BEFORE = intPreferencesKey("notification_days_before")
        val DEFAULT_STORAGE_TYPE = stringPreferencesKey("default_storage_type")
        val CUISINE_PREFERENCE = stringPreferencesKey("cuisine_preference")
        val FAMILY_SIZE = intPreferencesKey("family_size")
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
    }

    val userPreferences: Flow<UserPreferencesData> = context.dataStore.data.map { prefs ->
        UserPreferencesData(
            notificationEnabled = prefs[Keys.NOTIFICATION_ENABLED] ?: true,
            notificationDaysBefore = prefs[Keys.NOTIFICATION_DAYS_BEFORE] ?: 3,
            defaultStorageType = prefs[Keys.DEFAULT_STORAGE_TYPE] ?: "fridge",
            cuisinePreference = prefs[Keys.CUISINE_PREFERENCE] ?: "한식",
            familySize = prefs[Keys.FAMILY_SIZE] ?: 2,
            isFirstLaunch = prefs[Keys.IS_FIRST_LAUNCH] ?: true
        )
    }

    suspend fun updateNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATION_ENABLED] = enabled }
    }

    suspend fun updateNotificationDaysBefore(days: Int) {
        context.dataStore.edit { it[Keys.NOTIFICATION_DAYS_BEFORE] = days }
    }

    suspend fun updateDefaultStorageType(type: String) {
        context.dataStore.edit { it[Keys.DEFAULT_STORAGE_TYPE] = type }
    }

    suspend fun updateCuisinePreference(preference: String) {
        context.dataStore.edit { it[Keys.CUISINE_PREFERENCE] = preference }
    }

    suspend fun updateFamilySize(size: Int) {
        context.dataStore.edit { it[Keys.FAMILY_SIZE] = size }
    }

    suspend fun setFirstLaunchCompleted() {
        context.dataStore.edit { it[Keys.IS_FIRST_LAUNCH] = false }
    }
}

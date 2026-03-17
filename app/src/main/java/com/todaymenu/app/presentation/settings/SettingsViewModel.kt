package com.todaymenu.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todaymenu.app.data.local.datastore.UserPreferences
import com.todaymenu.app.data.local.datastore.UserPreferencesData
import com.todaymenu.app.data.remote.gemini.AiEngineRouter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val preferences: UserPreferencesData = UserPreferencesData(),
    val isNanoAvailable: Boolean = false,
    val snackbarMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val aiRouter: AiEngineRouter
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadPreferences()
        checkNanoAvailability()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            userPreferences.userPreferences.collect { prefs ->
                _uiState.update { it.copy(preferences = prefs) }
            }
        }
    }

    private fun checkNanoAvailability() {
        viewModelScope.launch {
            val available = aiRouter.isNanoAvailable()
            _uiState.update { it.copy(isNanoAvailable = available) }
        }
    }

    fun setNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch { userPreferences.updateNotificationEnabled(enabled) }
    }

    fun setNotificationDaysBefore(days: Int) {
        viewModelScope.launch { userPreferences.updateNotificationDaysBefore(days) }
    }

    fun setDefaultStorage(type: String) {
        viewModelScope.launch { userPreferences.updateDefaultStorageType(type) }
    }

    fun setCuisinePreference(pref: String) {
        viewModelScope.launch { userPreferences.updateCuisinePreference(pref) }
    }

    fun setFamilySize(size: Int) {
        viewModelScope.launch { userPreferences.updateFamilySize(size) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}

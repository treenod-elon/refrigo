package com.todaymenu.app.presentation.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todaymenu.app.data.backup.BackupManager
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
    val snackbarMessage: String? = null,
    val backupFileName: String = ""
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val aiRouter: AiEngineRouter,
    private val backupManager: BackupManager
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

    fun getBackupFileName(): String = backupManager.getBackupFileName()

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            backupManager.exportToUri(uri).onSuccess {
                _uiState.update { it.copy(snackbarMessage = "백업이 완료되었어요!") }
            }.onFailure { e ->
                _uiState.update { it.copy(snackbarMessage = "백업 실패: ${e.message}") }
            }
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            backupManager.importFromUri(uri).onSuccess {
                _uiState.update { it.copy(snackbarMessage = "복원이 완료되었어요! 앱을 재시작해 주세요.") }
            }.onFailure { e ->
                _uiState.update { it.copy(snackbarMessage = "복원 실패: ${e.message}") }
            }
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}

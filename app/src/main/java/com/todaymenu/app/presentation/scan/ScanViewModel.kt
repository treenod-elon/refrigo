package com.todaymenu.app.presentation.scan

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todaymenu.app.domain.model.Category
import com.todaymenu.app.domain.model.Ingredient
import com.todaymenu.app.domain.model.StorageType
import com.todaymenu.app.domain.usecase.AddIngredientUseCase
import com.todaymenu.app.domain.usecase.EstimateExpiryUseCase
import com.todaymenu.app.domain.usecase.ScanReceiptUseCase
import com.todaymenu.app.domain.usecase.ScannedIngredient
import com.todaymenu.app.domain.repository.IngredientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScanUiState(
    val phase: ScanPhase = ScanPhase.CAMERA,
    val capturedImageUri: Uri? = null,
    val ocrText: String = "",
    val scannedIngredients: List<ScannedIngredient> = emptyList(),
    val purchaseDate: Long = System.currentTimeMillis(),
    val isProcessing: Boolean = false,
    val processingMessage: String = "",
    val errorMessage: String? = null,
    val isSaved: Boolean = false
)

enum class ScanPhase {
    CAMERA,
    PROCESSING,
    RESULT
}

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val scanReceiptUseCase: ScanReceiptUseCase,
    private val addIngredientUseCase: AddIngredientUseCase,
    private val estimateExpiryUseCase: EstimateExpiryUseCase,
    private val ingredientRepository: IngredientRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    fun processOcrText(ocrText: String) {
        _uiState.update {
            it.copy(
                phase = ScanPhase.PROCESSING,
                ocrText = ocrText,
                isProcessing = true,
                processingMessage = "영수증을 분석하고 있어요...",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            scanReceiptUseCase.parseReceiptText(ocrText)
                .onSuccess { ingredients ->
                    _uiState.update {
                        it.copy(
                            phase = ScanPhase.RESULT,
                            scannedIngredients = ingredients,
                            isProcessing = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            phase = ScanPhase.CAMERA,
                            isProcessing = false,
                            errorMessage = "인식에 실패했어요. 다시 촬영하거나 직접 입력해 주세요."
                        )
                    }
                }
        }
    }

    fun setCapturedImageUri(uri: Uri) {
        _uiState.update { it.copy(capturedImageUri = uri) }
    }

    fun toggleIngredientSelection(index: Int) {
        _uiState.update { state ->
            val updated = state.scannedIngredients.toMutableList()
            if (index in updated.indices) {
                updated[index] = updated[index].copy(isSelected = !updated[index].isSelected)
            }
            state.copy(scannedIngredients = updated)
        }
    }

    fun updateIngredient(index: Int, ingredient: ScannedIngredient) {
        _uiState.update { state ->
            val updated = state.scannedIngredients.toMutableList()
            if (index in updated.indices) {
                updated[index] = ingredient
            }
            state.copy(scannedIngredients = updated)
        }
    }

    fun addManualIngredient(name: String) {
        if (name.isBlank()) return
        _uiState.update { state ->
            state.copy(
                scannedIngredients = state.scannedIngredients + ScannedIngredient(
                    name = name.trim(),
                    quantity = 1.0,
                    unit = "개",
                    category = "기타",
                    isSelected = true
                )
            )
        }
    }

    fun removeIngredient(index: Int) {
        _uiState.update { state ->
            val updated = state.scannedIngredients.toMutableList()
            if (index in updated.indices) {
                updated.removeAt(index)
            }
            state.copy(scannedIngredients = updated)
        }
    }

    fun setPurchaseDate(millis: Long) {
        _uiState.update { it.copy(purchaseDate = millis) }
    }

    fun saveSelectedIngredients() {
        val state = _uiState.value
        val selected = state.scannedIngredients.filter { it.isSelected }
        if (selected.isEmpty()) return

        _uiState.update { it.copy(isProcessing = true, processingMessage = "재료를 저장하고 있어요...") }

        viewModelScope.launch {
            val ingredients = selected.map { scanned ->
                Ingredient(
                    name = scanned.name,
                    category = Category.fromValue(scanned.category),
                    quantity = scanned.quantity,
                    unit = scanned.unit,
                    purchaseDate = state.purchaseDate,
                    storageType = StorageType.FRIDGE
                )
            }

            val ids = addIngredientUseCase.addAll(ingredients)

            // 유통기한 미입력 재료 AI 자동 추정
            val needsEstimation = ids.mapNotNull { id -> ingredientRepository.getById(id) }
                .filter { it.expiryDate == null }
            if (needsEstimation.isNotEmpty()) {
                estimateExpiryUseCase.estimateAndUpdate(needsEstimation)
            }

            _uiState.update {
                it.copy(
                    isProcessing = false,
                    isSaved = true
                )
            }
        }
    }

    fun retakePhoto() {
        _uiState.update {
            ScanUiState() // 초기화
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

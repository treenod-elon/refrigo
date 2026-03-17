package com.todaymenu.app.presentation.fridge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todaymenu.app.domain.model.Category
import com.todaymenu.app.domain.model.Ingredient
import com.todaymenu.app.domain.model.StorageType
import com.todaymenu.app.domain.repository.IngredientRepository
import com.todaymenu.app.domain.usecase.AddIngredientUseCase
import com.todaymenu.app.domain.usecase.EstimateExpiryUseCase
import com.todaymenu.app.domain.usecase.ScannedIngredient
import com.todaymenu.app.domain.usecase.VoiceInputUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FridgeUiState(
    val ingredients: List<Ingredient> = emptyList(),
    val filteredIngredients: Map<Category, List<Ingredient>> = emptyMap(),
    val selectedStorageType: StorageType? = null,
    val sortType: SortType = SortType.NEWEST,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val isLoading: Boolean = true,
    val expandedCategories: Set<Category> = Category.all.toSet(),
    val editingIngredient: Ingredient? = null,
    val showAddDialog: Boolean = false,
    val snackbarMessage: String? = null,
    val isVoiceProcessing: Boolean = false,
    val voiceParsedIngredients: List<ScannedIngredient> = emptyList()
)

enum class SortType(val label: String) {
    NEWEST("최신순"),
    NAME("이름순"),
    EXPIRY("임박순")
}

@HiltViewModel
class FridgeViewModel @Inject constructor(
    private val repository: IngredientRepository,
    private val addIngredientUseCase: AddIngredientUseCase,
    private val estimateExpiryUseCase: EstimateExpiryUseCase,
    private val voiceInputUseCase: VoiceInputUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FridgeUiState())
    val uiState: StateFlow<FridgeUiState> = _uiState.asStateFlow()

    init {
        loadIngredients()
    }

    private fun loadIngredients() {
        viewModelScope.launch {
            repository.getAllActive().collect { allIngredients ->
                _uiState.update { state ->
                    val filtered = applyFilters(allIngredients, state)
                    state.copy(
                        ingredients = allIngredients,
                        filteredIngredients = groupByCategory(filtered),
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun applyFilters(ingredients: List<Ingredient>, state: FridgeUiState): List<Ingredient> {
        var result = ingredients

        // 보관위치 필터
        if (state.selectedStorageType != null) {
            result = result.filter { it.storageType == state.selectedStorageType }
        }

        // 검색 필터
        if (state.searchQuery.isNotBlank()) {
            result = result.filter { it.name.contains(state.searchQuery, ignoreCase = true) }
        }

        // 정렬
        result = when (state.sortType) {
            SortType.NEWEST -> result.sortedByDescending { it.createdAt }
            SortType.NAME -> result.sortedBy { it.name }
            SortType.EXPIRY -> result.sortedBy { it.expiryDate ?: Long.MAX_VALUE }
        }

        return result
    }

    private fun groupByCategory(ingredients: List<Ingredient>): Map<Category, List<Ingredient>> {
        return ingredients.groupBy { it.category }
            .toSortedMap(compareBy { it.ordinal })
    }

    fun setStorageFilter(storageType: StorageType?) {
        _uiState.update { state ->
            val newState = state.copy(selectedStorageType = storageType)
            newState.copy(filteredIngredients = groupByCategory(applyFilters(state.ingredients, newState)))
        }
    }

    fun setSortType(sortType: SortType) {
        _uiState.update { state ->
            val newState = state.copy(sortType = sortType)
            newState.copy(filteredIngredients = groupByCategory(applyFilters(state.ingredients, newState)))
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { state ->
            val newState = state.copy(searchQuery = query)
            newState.copy(filteredIngredients = groupByCategory(applyFilters(state.ingredients, newState)))
        }
    }

    fun toggleSearch() {
        _uiState.update { it.copy(isSearchActive = !it.isSearchActive, searchQuery = "") }
        if (!_uiState.value.isSearchActive) {
            _uiState.update { state ->
                state.copy(filteredIngredients = groupByCategory(applyFilters(state.ingredients, state)))
            }
        }
    }

    fun toggleCategory(category: Category) {
        _uiState.update { state ->
            val expanded = state.expandedCategories.toMutableSet()
            if (expanded.contains(category)) expanded.remove(category) else expanded.add(category)
            state.copy(expandedCategories = expanded)
        }
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true) }
    }

    fun hideAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun showEditSheet(ingredient: Ingredient) {
        _uiState.update { it.copy(editingIngredient = ingredient) }
    }

    fun hideEditSheet() {
        _uiState.update { it.copy(editingIngredient = null) }
    }

    fun addIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            val id = addIngredientUseCase(ingredient)
            // 유통기한 미입력 시 AI 자동 추정
            if (ingredient.expiryDate == null) {
                val saved = repository.getById(id)
                if (saved != null) {
                    estimateExpiryUseCase.estimateAndUpdate(listOf(saved))
                }
            }
            _uiState.update { it.copy(showAddDialog = false, snackbarMessage = "${ingredient.name}이(가) 추가되었어요!") }
        }
    }

    fun addIngredients(ingredients: List<Ingredient>) {
        viewModelScope.launch {
            val ids = addIngredientUseCase.addAll(ingredients)
            // 유통기한 미입력 재료 일괄 추정
            val needsEstimation = ingredients.zip(ids)
                .filter { (ing, _) -> ing.expiryDate == null }
                .mapNotNull { (_, id) -> repository.getById(id) }
            if (needsEstimation.isNotEmpty()) {
                estimateExpiryUseCase.estimateAndUpdate(needsEstimation)
            }
            _uiState.update { it.copy(snackbarMessage = "${ingredients.size}개 재료가 추가되었어요!") }
        }
    }

    fun updateIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            repository.update(ingredient.copy(updatedAt = System.currentTimeMillis()))
            _uiState.update { it.copy(editingIngredient = null, snackbarMessage = "${ingredient.name} 수정 완료!") }
        }
    }

    fun deleteIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            repository.delete(ingredient)
            _uiState.update { it.copy(snackbarMessage = "${ingredient.name} 삭제됨") }
        }
    }

    fun consumeIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            repository.update(ingredient.copy(isConsumed = true, updatedAt = System.currentTimeMillis()))
            _uiState.update { it.copy(snackbarMessage = "${ingredient.name} 사용 완료!") }
        }
    }

    fun processVoiceInput(recognizedText: String) {
        _uiState.update { it.copy(isVoiceProcessing = true, voiceParsedIngredients = emptyList()) }
        viewModelScope.launch {
            voiceInputUseCase.parseVoiceInput(recognizedText)
                .onSuccess { parsed ->
                    _uiState.update { it.copy(isVoiceProcessing = false, voiceParsedIngredients = parsed) }
                }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(
                            isVoiceProcessing = false,
                            voiceParsedIngredients = emptyList(),
                            snackbarMessage = "음성 분석에 실패했어요. 다시 시도해 주세요."
                        )
                    }
                }
        }
    }

    fun confirmVoiceIngredients(selected: List<ScannedIngredient>) {
        val ingredients = selected.map { scanned ->
            Ingredient(
                name = scanned.name,
                category = Category.fromValue(scanned.category),
                quantity = scanned.quantity,
                unit = scanned.unit,
                purchaseDate = System.currentTimeMillis(),
                storageType = StorageType.FRIDGE
            )
        }
        addIngredients(ingredients)
        _uiState.update { it.copy(voiceParsedIngredients = emptyList()) }
    }

    fun dismissVoiceResult() {
        _uiState.update { it.copy(voiceParsedIngredients = emptyList(), isVoiceProcessing = false) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}

package com.todaymenu.app.presentation.recommend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todaymenu.app.domain.model.Ingredient
import com.todaymenu.app.domain.model.MenuRecommendation
import com.todaymenu.app.domain.repository.IngredientRepository
import com.todaymenu.app.domain.repository.MenuRepository
import com.todaymenu.app.domain.usecase.GetMenuRecommendationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecommendUiState(
    val availableIngredients: List<Ingredient> = emptyList(),
    val selectedIngredientNames: Set<String> = emptySet(),
    val cuisineType: String = "빠른요리",
    val recommendations: List<MenuRecommendation> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = false,
    val excludeMenuNames: List<String> = emptyList(),
    val selectedRecipe: MenuRecommendation? = null,
    val errorMessage: String? = null,
    val snackbarMessage: String? = null
)

@HiltViewModel
class RecommendViewModel @Inject constructor(
    private val ingredientRepository: IngredientRepository,
    private val menuRepository: MenuRepository,
    private val getMenuRecommendationUseCase: GetMenuRecommendationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecommendUiState())
    val uiState: StateFlow<RecommendUiState> = _uiState.asStateFlow()

    companion object {
        private const val MAX_RECOMMENDATIONS = 12
        private val CUISINE_TYPES = listOf("빠른요리", "한식", "중식", "양식")
    }

    init {
        loadIngredients()
    }

    private fun loadIngredients() {
        viewModelScope.launch {
            val ingredients = ingredientRepository.getAllActive().first()
            _uiState.update { state ->
                state.copy(
                    availableIngredients = ingredients,
                    selectedIngredientNames = ingredients.map { it.name }.toSet()
                )
            }
        }
    }

    fun toggleIngredient(name: String) {
        _uiState.update { state ->
            val updated = state.selectedIngredientNames.toMutableSet()
            if (updated.contains(name)) updated.remove(name) else updated.add(name)
            state.copy(selectedIngredientNames = updated)
        }
    }

    fun selectAllIngredients() {
        _uiState.update { state ->
            state.copy(selectedIngredientNames = state.availableIngredients.map { it.name }.toSet())
        }
    }

    fun deselectAllIngredients() {
        _uiState.update { it.copy(selectedIngredientNames = emptySet()) }
    }

    fun setCuisineType(type: String) {
        if (type == _uiState.value.cuisineType) return
        _uiState.update {
            it.copy(
                cuisineType = type,
                recommendations = emptyList(),
                excludeMenuNames = emptyList(),
                canLoadMore = false
            )
        }
    }

    fun getRecommendations() {
        val state = _uiState.value
        if (state.selectedIngredientNames.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "재료를 하나 이상 선택해 주세요.") }
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
                recommendations = emptyList(),
                excludeMenuNames = emptyList()
            )
        }

        viewModelScope.launch {
            getMenuRecommendationUseCase(
                ingredientNames = state.selectedIngredientNames.toList(),
                cuisineType = state.cuisineType
            ).onSuccess { menus ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        recommendations = menus,
                        excludeMenuNames = menus.map { m -> m.menuName },
                        canLoadMore = menus.size < MAX_RECOMMENDATIONS
                    )
                }
            }.onFailure {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "추천을 불러오지 못했어요. 다시 시도해 주세요."
                    )
                }
            }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.recommendations.size >= MAX_RECOMMENDATIONS || state.isLoadingMore) return

        _uiState.update { it.copy(isLoadingMore = true) }

        viewModelScope.launch {
            getMenuRecommendationUseCase(
                ingredientNames = state.selectedIngredientNames.toList(),
                cuisineType = state.cuisineType,
                excludeMenuNames = state.excludeMenuNames
            ).onSuccess { newMenus ->
                _uiState.update {
                    val all = it.recommendations + newMenus
                    it.copy(
                        isLoadingMore = false,
                        recommendations = all,
                        excludeMenuNames = all.map { m -> m.menuName },
                        canLoadMore = all.size < MAX_RECOMMENDATIONS
                    )
                }
            }.onFailure {
                _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        snackbarMessage = "추가 메뉴를 불러오지 못했어요."
                    )
                }
            }
        }
    }

    fun showRecipeDetail(menu: MenuRecommendation) {
        _uiState.update { it.copy(selectedRecipe = menu) }
    }

    fun hideRecipeDetail() {
        _uiState.update { it.copy(selectedRecipe = null) }
    }

    fun completeCooking(menu: MenuRecommendation) {
        viewModelScope.launch {
            // 보유 재료 중 사용된 재료 소진
            val ownedIngredients = _uiState.value.availableIngredients
            val usedIds = mutableListOf<Long>()
            menu.ingredients.filter { it.isAvailable }.forEach { recipeIng ->
                val matched = ownedIngredients.find { it.name == recipeIng.name }
                if (matched != null) {
                    ingredientRepository.update(matched.copy(isConsumed = true, updatedAt = System.currentTimeMillis()))
                    usedIds.add(matched.id)
                }
            }

            // 히스토리 저장
            menuRepository.saveHistory(menu, usedIds)

            _uiState.update {
                it.copy(
                    selectedRecipe = null,
                    snackbarMessage = "${menu.menuName} 요리 완료! 재료가 차감되었어요."
                )
            }

            // 재료 목록 갱신
            loadIngredients()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}

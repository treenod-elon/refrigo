package com.todaymenu.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todaymenu.app.domain.model.Category
import com.todaymenu.app.domain.model.Ingredient
import com.todaymenu.app.domain.model.MenuRecommendation
import com.todaymenu.app.domain.repository.IngredientRepository
import com.todaymenu.app.domain.usecase.CheckExpiringUseCase
import com.todaymenu.app.domain.usecase.GetMenuRecommendationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val expiringIngredients: List<Ingredient> = emptyList(),
    val categoryCounts: Map<Category, Int> = emptyMap(),
    val totalIngredientCount: Int = 0,
    val todayRecommendation: MenuRecommendation? = null,
    val isLoadingRecommendation: Boolean = false,
    val isEmpty: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val ingredientRepository: IngredientRepository,
    private val checkExpiringUseCase: CheckExpiringUseCase,
    private val getMenuRecommendationUseCase: GetMenuRecommendationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            ingredientRepository.getAllActive().collect { ingredients ->
                val categoryCounts = ingredients.groupBy { it.category }
                    .mapValues { it.value.size }

                _uiState.update {
                    it.copy(
                        categoryCounts = categoryCounts,
                        totalIngredientCount = ingredients.size,
                        isEmpty = ingredients.isEmpty()
                    )
                }

                // 유통기한 임박 재료 체크 (7일 이내)
                val expiring = checkExpiringUseCase(daysBefore = 7)
                _uiState.update { it.copy(expiringIngredients = expiring) }

                // 오늘의 추천 메뉴 (재료가 있을 때만)
                if (ingredients.isNotEmpty() && _uiState.value.todayRecommendation == null) {
                    loadTodayRecommendation(ingredients)
                }
            }
        }
    }

    private fun loadTodayRecommendation(ingredients: List<Ingredient>) {
        _uiState.update { it.copy(isLoadingRecommendation = true) }
        viewModelScope.launch {
            val names = ingredients.map { it.name }
            getMenuRecommendationUseCase(
                ingredientNames = names,
                cuisineType = "빠른요리"
            ).onSuccess { menus ->
                _uiState.update {
                    it.copy(
                        todayRecommendation = menus.firstOrNull(),
                        isLoadingRecommendation = false
                    )
                }
            }.onFailure {
                _uiState.update { it.copy(isLoadingRecommendation = false) }
            }
        }
    }

    fun refreshRecommendation() {
        viewModelScope.launch {
            val ingredients = ingredientRepository.getAllActive().first()
            if (ingredients.isNotEmpty()) {
                _uiState.update { it.copy(todayRecommendation = null) }
                loadTodayRecommendation(ingredients)
            }
        }
    }
}

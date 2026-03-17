package com.todaymenu.app.presentation.mealplan

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todaymenu.app.domain.model.Ingredient
import com.todaymenu.app.domain.model.MealPlan
import com.todaymenu.app.domain.model.MealType
import com.todaymenu.app.domain.model.ShoppingItem
import com.todaymenu.app.domain.repository.IngredientRepository
import com.todaymenu.app.domain.repository.MealPlanRepository
import com.todaymenu.app.domain.repository.ShoppingRepository
import com.todaymenu.app.domain.usecase.CheckExpiringUseCase
import com.todaymenu.app.domain.usecase.GenerateMealPlanUseCase
import com.todaymenu.app.domain.model.Category
import com.todaymenu.app.domain.model.StorageType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class MealPlanUiState(
    val weekStart: Long = getWeekStart(),
    val mealPlans: List<MealPlan> = emptyList(),
    val shoppingItems: List<ShoppingItem> = emptyList(),
    val isGenerating: Boolean = false,
    val generatingMessage: String = "",
    val nutritionNote: String = "",
    val snackbarMessage: String? = null,
    val errorMessage: String? = null,
    // 생성 옵션
    val durationDays: Int = 7,
    val selectedMeals: List<String> = listOf("점심", "저녁"),
    val preference: String = "한식위주",
    val showGenerateOptions: Boolean = false
)

@HiltViewModel
class MealPlanViewModel @Inject constructor(
    private val mealPlanRepository: MealPlanRepository,
    private val shoppingRepository: ShoppingRepository,
    private val ingredientRepository: IngredientRepository,
    private val generateMealPlanUseCase: GenerateMealPlanUseCase,
    private val checkExpiringUseCase: CheckExpiringUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MealPlanUiState())
    val uiState: StateFlow<MealPlanUiState> = _uiState.asStateFlow()

    init {
        loadCurrentWeek()
        loadShoppingItems()
    }

    private fun loadCurrentWeek() {
        viewModelScope.launch {
            val start = _uiState.value.weekStart
            val end = start + 7 * 86400000L
            mealPlanRepository.getByDateRange(start, end).collect { plans ->
                _uiState.update { it.copy(mealPlans = plans) }
            }
        }
    }

    private fun loadShoppingItems() {
        viewModelScope.launch {
            shoppingRepository.getAll().collect { items ->
                _uiState.update { it.copy(shoppingItems = items) }
            }
        }
    }

    fun navigateWeek(forward: Boolean) {
        val offset = if (forward) 7 * 86400000L else -7 * 86400000L
        _uiState.update { it.copy(weekStart = it.weekStart + offset) }
        loadCurrentWeek()
    }

    fun toggleGenerateOptions() {
        _uiState.update { it.copy(showGenerateOptions = !it.showGenerateOptions) }
    }

    fun setDuration(days: Int) {
        _uiState.update { it.copy(durationDays = days) }
    }

    fun toggleMeal(meal: String) {
        _uiState.update { state ->
            val updated = state.selectedMeals.toMutableList()
            if (updated.contains(meal)) updated.remove(meal) else updated.add(meal)
            state.copy(selectedMeals = updated)
        }
    }

    fun setPreference(pref: String) {
        _uiState.update { it.copy(preference = pref) }
    }

    fun generateMealPlan() {
        _uiState.update {
            it.copy(isGenerating = true, generatingMessage = "AI가 식단을 생성하고 있어요...", errorMessage = null)
        }

        viewModelScope.launch {
            val ingredients = ingredientRepository.getAllActive().first()
            val expiring = checkExpiringUseCase(daysBefore = 7)
            val ingredientList = ingredients.joinToString("\n") { "${it.name} ${it.quantity}${it.unit}" }
            val expiringList = expiring.joinToString(", ") { it.name }
            val state = _uiState.value

            generateMealPlanUseCase(
                ingredientList = ingredientList,
                expiringIngredients = expiringList.ifEmpty { "없음" },
                durationDays = state.durationDays,
                mealsPerDay = state.selectedMeals,
                preference = state.preference,
                startDate = state.weekStart
            ).onSuccess { result ->
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        nutritionNote = result.nutritionNote,
                        showGenerateOptions = false,
                        snackbarMessage = "식단이 생성되었어요!"
                    )
                }
                loadCurrentWeek()
            }.onFailure {
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        errorMessage = "식단 생성에 실패했어요. 다시 시도해 주세요."
                    )
                }
            }
        }
    }

    fun toggleShoppingItem(item: ShoppingItem) {
        viewModelScope.launch {
            val updated = item.copy(isPurchased = !item.isPurchased)
            shoppingRepository.update(updated)

            // 구매 완료 → 냉장고에 자동 추가
            if (updated.isPurchased) {
                val ingredient = Ingredient(
                    name = item.name,
                    category = Category.ETC,
                    quantity = 1.0,
                    unit = item.amount?.replace(Regex("[0-9.]+"), "")?.trim() ?: "개",
                    purchaseDate = System.currentTimeMillis(),
                    storageType = StorageType.FRIDGE
                )
                ingredientRepository.insert(ingredient)
                _uiState.update { it.copy(snackbarMessage = "${item.name}이(가) 냉장고에 추가되었어요!") }
            }
        }
    }

    fun addShoppingItem(name: String, amount: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            shoppingRepository.insert(
                ShoppingItem(name = name.trim(), amount = amount.trim().ifBlank { null }, sourceType = "manual")
            )
        }
    }

    fun deleteShoppingItem(item: ShoppingItem) {
        viewModelScope.launch { shoppingRepository.delete(item) }
    }

    fun deleteMealPlan(mealPlan: MealPlan) {
        viewModelScope.launch {
            mealPlanRepository.delete(mealPlan)
            _uiState.update { it.copy(snackbarMessage = "${mealPlan.menuName} 삭제됨") }
        }
    }

    fun shareShoppingList(context: Context) {
        val items = _uiState.value.shoppingItems.filter { !it.isPurchased }
        if (items.isEmpty()) return
        val text = buildString {
            appendLine("🛒 장보기 목록")
            appendLine()
            items.forEachIndexed { i, item ->
                val amount = item.amount?.let { " ($it)" } ?: ""
                appendLine("${i + 1}. ${item.name}$amount")
            }
        }
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "장보기 목록 공유"))
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}

private fun getWeekStart(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

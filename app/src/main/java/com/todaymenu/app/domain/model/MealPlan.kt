package com.todaymenu.app.domain.model

data class MealPlan(
    val id: Long = 0,
    val date: Long,
    val mealType: MealType,
    val menuName: String,
    val recipe: String,
    val requiredIngredients: List<String> = emptyList(),
    val isCompleted: Boolean = false
)

enum class MealType(val label: String, val value: String) {
    BREAKFAST("아침", "아침"),
    LUNCH("점심", "점심"),
    DINNER("저녁", "저녁");

    companion object {
        fun fromValue(value: String): MealType =
            entries.firstOrNull { it.value == value } ?: LUNCH
    }
}

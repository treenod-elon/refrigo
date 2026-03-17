package com.todaymenu.app.domain.model

data class MenuRecommendation(
    val menuName: String,
    val nameEn: String = "",
    val description: String,
    val recipe: String = "",
    val cookingTime: String = "",
    val difficulty: String = "",
    val servings: String = "",
    val matchRate: Int = 0,
    val ingredients: List<RecipeIngredient> = emptyList(),
    val seasonings: List<String> = emptyList(),
    val steps: List<String> = emptyList(),
    val tip: String = "",
    val searchKeyword: String = "",
    val youtubeSearchUrl: String = "",
    val blogSearchUrl: String = "",
    val imageUrl: String? = null
)

data class RecipeIngredient(
    val name: String,
    val amount: String,
    val isAvailable: Boolean = false
)

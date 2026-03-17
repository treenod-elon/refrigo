package com.todaymenu.app.domain.model

data class MenuRecommendation(
    val menuName: String,
    val description: String,
    val recipe: String = "",
    val cookingTime: String = "",
    val difficulty: String = "",
    val matchRate: Int = 0,
    val ingredients: List<RecipeIngredient> = emptyList(),
    val youtubeSearchUrl: String = "",
    val blogSearchUrl: String = "",
    val imageUrl: String? = null
)

data class RecipeIngredient(
    val name: String,
    val amount: String,
    val isAvailable: Boolean = false
)

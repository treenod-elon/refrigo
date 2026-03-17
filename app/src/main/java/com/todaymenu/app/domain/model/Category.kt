package com.todaymenu.app.domain.model

import androidx.compose.ui.graphics.Color

enum class Category(
    val label: String,
    val value: String,
    val color: Color
) {
    MEAT("육류", "육류", Color(0xFFE53935)),
    VEGETABLE("채소", "채소", Color(0xFF4CAF50)),
    SEAFOOD("해산물", "해산물", Color(0xFF1E88E5)),
    DAIRY("유제품", "유제품", Color(0xFFFFA726)),
    SEASONING("양념", "양념", Color(0xFF8D6E63)),
    FRUIT("과일", "과일", Color(0xFFAB47BC)),
    GRAIN("곡류·면", "곡류·면", Color(0xFFD4A24E)),
    FROZEN("냉동식품", "냉동식품", Color(0xFF42A5F5)),
    BEVERAGE("음료", "음료", Color(0xFF26C6DA)),
    ETC("기타", "기타", Color(0xFF78909C));

    companion object {
        fun fromValue(value: String): Category =
            entries.firstOrNull { it.value == value } ?: ETC

        val all: List<Category> = entries.toList()
    }
}

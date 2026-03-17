package com.todaymenu.app.domain.model

data class ShoppingItem(
    val id: Long = 0,
    val name: String,
    val amount: String? = null,
    val isPurchased: Boolean = false,
    val sourceType: String = "manual"
)

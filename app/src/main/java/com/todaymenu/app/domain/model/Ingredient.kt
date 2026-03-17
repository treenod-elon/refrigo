package com.todaymenu.app.domain.model

data class Ingredient(
    val id: Long = 0,
    val name: String,
    val category: Category,
    val quantity: Double = 1.0,
    val unit: String = "개",
    val purchaseDate: Long,
    val expiryDate: Long? = null,
    val isExpiryEstimated: Boolean = false,
    val storageType: StorageType = StorageType.FRIDGE,
    val memo: String? = null,
    val isConsumed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class StorageType(val label: String, val value: String) {
    FRIDGE("냉장", "fridge"),
    FREEZER("냉동", "freezer"),
    ROOM("실온", "room");

    companion object {
        fun fromValue(value: String): StorageType =
            entries.firstOrNull { it.value == value } ?: FRIDGE
    }
}

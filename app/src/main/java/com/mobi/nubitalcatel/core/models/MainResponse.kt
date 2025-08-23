package com.mobi.nubitalcatel.core.models

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?
)

data class Widget(
    val id: String,
    val type: String,
    val title: String,
    val content: String,
    val order: Int
)
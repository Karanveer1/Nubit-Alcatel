package com.mobi.nubitalcatel.core.models

data class WidgetOrder(
    val id: Int,
    val name: String,
    val status: Int,
    val type: Int,
    val time: String,
    val order_type: String,
    val sort_order: Int
)

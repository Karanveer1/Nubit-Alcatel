package com.mobi.nubitalcatel.core.models

data class MiddleNewsPojo(
    val title: String,
    val description: String,
    val image: String,
    val postedDate: String,
    val postedTime: String,
    val adUnitId: String? = null
)

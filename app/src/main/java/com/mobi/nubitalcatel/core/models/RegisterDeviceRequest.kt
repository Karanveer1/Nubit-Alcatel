package com.mobi.nubitalcatel.core.models

data class RegisterDeviceRequest(
    val id: Int = 0,
    val androdId: String,
    val imeiPrimary: String,
    val imeiSecondary: String,
    val manufacturer: String,
    val osVersion: String,
    val apiLevel: String,
    val timeZone: String,
    val localeLanguage: String,
    val localeCountry: String,
    val status: Int,
    val token: String
)

package com.mobi.nubitalcatel.utils

data class NubitConfig internal constructor(
    val partnerId: String,
    val apiKey: String,
    val enableLogging: Boolean = false,
    val defaultLocale: String = "en",
    val customThemeRes: Int? = 1
) {
    class Builder(private val partnerId: String, private val apiKey: String) {
        private var enableLogging: Boolean = false
        private var defaultLocale: String = "en"
        private var customThemeRes: Int? = null

        fun setLoggingEnabled(enabled: Boolean) = apply { this.enableLogging = enabled }
        fun setDefaultLocale(locale: String) = apply { this.defaultLocale = locale }
        fun setCustomTheme(themeRes: Int) = apply { this.customThemeRes = themeRes }

        fun build(): NubitConfig {
            return NubitConfig(
                partnerId = partnerId,
                apiKey = apiKey,
                enableLogging = enableLogging,
                defaultLocale = defaultLocale,
                customThemeRes = customThemeRes
            )
        }
    }
}

//to call for OEM's
/**
 * val config = MinusOneConfig.Builder(
 *     partnerId = "oem_samsung_123",
 *     apiKey = "XYZ-SECRET-KEY"
 * )
 *     .setLoggingEnabled(true)
 *     .setDefaultLocale("en")
 *     .setCustomTheme(R.style.MinusOneCustomTheme)
 *     .build()
 *
 * MinusOneSdk.init(context, config)
 */
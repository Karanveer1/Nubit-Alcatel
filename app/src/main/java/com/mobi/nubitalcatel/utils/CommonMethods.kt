package com.mobi.nubitalcatel.utils

import android.content.Context
import android.content.SharedPreferences

class CommonMethods {

    companion object {
        private const val PREFS_NAME = "app_prefs"
        private const val KEY_BEARER_TOKEN = "bearer_token"
        const val ACTION_STOP_AUDIO = "com.nubit.ACTION_STOP_AUDIO"


        fun Context.loadJSONFromAssets(fileName: String): String {
            return assets.open(fileName).bufferedReader().use { it.readText() }
        }

        // Save token
        fun saveBearerToken(context: Context, token: String) {
            val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_BEARER_TOKEN, token).apply()
        }

        // Get token
        fun getBearerToken(context: Context): String? {
            val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getString(KEY_BEARER_TOKEN, null)
        }

        // Save token
        fun saveInt(context: Context, key: String, token: Int) {
            val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putInt(key, token).apply()
        }

        // Get token
        fun getInt(context: Context, key: String): Int? {
            val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getInt(key, 0)
        }

        // Clear token (optional)
        fun clearBearerToken(context: Context) {
            val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().remove(KEY_BEARER_TOKEN).apply()
        }

        fun saveJson(key: String, value: String, context: Context) {
            context.getSharedPreferences("nubit_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString(key, value)
                .apply()
        }

        fun loadJson(key: String, context: Context): String? {
            return context.getSharedPreferences("nubit_prefs", Context.MODE_PRIVATE)
                .getString(key, null)
        }
    }
}

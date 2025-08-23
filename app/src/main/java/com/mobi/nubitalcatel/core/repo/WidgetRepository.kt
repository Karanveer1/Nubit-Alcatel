package com.mobi.nubitalcatel.core.repo

import com.mobi.nubitalcatel.core.models.Widget
import com.mobi.nubitalcatel.core.models.WidgetOrder
import com.mobi.nubitalcatel.core.network.ApiService

class WidgetRepository(private val api: ApiService) {

    suspend fun fetchWidgets(order: String): Result<List<WidgetOrder>> {
        return try {
            val response = api.getWidgets(order)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
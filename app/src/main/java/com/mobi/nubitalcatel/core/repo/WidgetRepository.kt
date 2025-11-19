package com.mobi.nubitalcatel.core.repo

import com.mobi.nubitalcatel.core.models.WidgetOrder
import com.mobi.nubitalcatel.core.network.ApiService

class WidgetRepository(private val api: ApiService) {

    suspend fun fetchWidgets(): Result<List<WidgetOrder>> {
        return try {
            val response = api.getData()
            if (response.responseStatus) {
                Result.success(response.responseObject)
            } else {
                Result.failure(Exception(response.responseMessage.ifEmpty { "Unknown error" }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

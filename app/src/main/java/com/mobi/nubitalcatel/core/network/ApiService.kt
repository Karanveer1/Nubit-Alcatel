package com.mobi.nubitalcatel.core.network

import com.mobi.nubitalcatel.core.models.ApiResponse
import com.mobi.nubitalcatel.core.models.Widget
import com.mobi.nubitalcatel.core.models.WidgetOrder
import retrofit2.http.GET
import retrofit2.http.Query


interface ApiService {
    @GET("widgets")
    suspend fun getWidgets(
        @Query("placementOrder") order: String
    ): ApiResponse<List<WidgetOrder>>
}
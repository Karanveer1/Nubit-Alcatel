package com.mobi.nubitalcatel.core.network

import com.mobi.nubitalcatel.core.models.ApiContainerResponse
import com.mobi.nubitalcatel.core.models.ApiNewsCateContainerResponse
import com.mobi.nubitalcatel.core.models.ApiNewsDataContainerResponse
import com.mobi.nubitalcatel.core.models.ApiResponse
import com.mobi.nubitalcatel.core.models.ApiViewContainerResponse
import com.mobi.nubitalcatel.core.models.KeyValue
import com.mobi.nubitalcatel.core.models.RegisterDeviceRequest
import com.mobi.nubitalcatel.core.models.RegisterDeviceResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url


interface ApiService {
    @POST("master/layout")
    suspend fun getData(): ApiResponse
    @GET("master/getMasterContainer")
    suspend fun getMasterContainer(): ApiContainerResponse
    @GET("master/getViewType")
    suspend fun getViewTypes(): ApiViewContainerResponse
    @GET("master/getCategory")
    suspend fun getNewsCategories(): ApiNewsCateContainerResponse
    @POST("content/getContent?pageNumber=0&pageSize=10")
    suspend fun getNewsData(
        @Body body: List<KeyValue>
    ): ApiNewsDataContainerResponse
    @POST
    suspend fun getNewsDataNew(
        @Url url: String,
        @Body body: List<KeyValue>
    ): ApiNewsDataContainerResponse

}
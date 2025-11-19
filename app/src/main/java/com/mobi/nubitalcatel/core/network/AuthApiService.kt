package com.mobi.nubitalcatel.core.network

import com.mobi.nubitalcatel.core.models.ApiResponse
import com.mobi.nubitalcatel.core.models.RegisterDeviceRequest
import com.mobi.nubitalcatel.core.models.RegisterDeviceResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST


interface AuthApiService {

    @POST("device/registerDevice")
    suspend fun registerDevice(
        @Body request: RegisterDeviceRequest,
        @Header("DEVICE-TYPE") deviceType: String = "Android",
        @Header("VER") version: String = "1"
    ): Response<RegisterDeviceResponse>
}
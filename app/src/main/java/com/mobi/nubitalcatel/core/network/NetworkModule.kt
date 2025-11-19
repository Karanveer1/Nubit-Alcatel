package com.mobi.nubitalcatel.core.network

import android.content.Context
import com.mobi.nubitalcatel.utils.CommonMethods
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    private const val BASE_URL = "https://api.mobinity.in/minusone-service/"
    private const val AUTH_BASE_URL = "https://api.mobinity.in/auth-service/"
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = Interceptor { chain ->
        val token = appContext?.let { CommonMethods.getBearerToken(it) }
        val requestBuilder = chain.request().newBuilder()
            .addHeader("accept", "application/json")
            .addHeader("DEVICE-TYPE", "Android")
            .addHeader("VER", "1")

        token?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }

        chain.proceed(requestBuilder.build())
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    // Retrofit for MinusOne APIs
    private val retrofitMinusOne: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Retrofit for MinusOne APIs
    private val retrofitMinusOneTest: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Retrofit for Auth APIs
    private val retrofitAuth: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(AUTH_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Services
    val minusOneApi: ApiService by lazy {
        retrofitMinusOne.create(ApiService::class.java)
    }

    // Services
    val minusOneApiTest: ApiService by lazy {
        retrofitMinusOneTest.create(ApiService::class.java)
    }

    val authApi: AuthApiService by lazy {
        retrofitAuth.create(AuthApiService::class.java)
    }
}

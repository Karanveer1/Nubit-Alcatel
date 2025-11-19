package com.mobi.nubitalcatel.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.mobi.nubitalcatel.R
import com.mobi.nubitalcatel.core.models.RegisterDeviceRequest
import com.mobi.nubitalcatel.core.network.NetworkModule
import com.mobi.nubitalcatel.utils.CommonMethods
import com.mobi.nubitalcatel.utils.NubitConfig
import com.mobi.nubitalcatel.utils.NubitManager
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.TimeZone

class HomeActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
//        registerMinusOne(this)

        val config = NubitConfig(
            apiKey = "YOUR_API_KEY",
            partnerId = "USER_ID"
        )
        NubitManager.initialize(this, config)
//        saveToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxNiIsImlhdCI6MTc2MDc2NTY0MSwiZXhwIjoxNzYxNjI5NjQxfQ.m8okdNmXYPMCr2T7Hhqvm2yb81FSnEsxElEapxre0sM")

        Log.e("checktoken", ">>" + CommonMethods.getBearerToken(this))
        if (CommonMethods.getBearerToken(this).isNullOrEmpty()) {
            registerDevice()
        } else {
            if (savedInstanceState == null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, NubitManager.getMinusOneFragment())
                    .commit()
            }
        }
    }

    fun generateRandomString(length: Int): String {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("HardwareIds")
    fun getDeviceInfo(context: Context): Map<String, String> {
        val deviceInfo = mutableMapOf<String, String>()

        // Android ID (unique to device + app-signing key)
        val androidId =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

        // Manufacturer
        val manufacturer = Build.MANUFACTURER

        // OS Version
        val osVersion = Build.VERSION.RELEASE

        // API Level
        val apiLevel = Build.VERSION.SDK_INT.toString()

        // Time Zone
        val timeZone = TimeZone.getDefault().id

        // Locale Language & Country
        val locale = Locale.getDefault()
        val localeLanguage = locale.language
        val localeCountry = locale.country

        // IMEI (Needs READ_PHONE_STATE permission & works only on Android < 10)
        var imeiPrimary = generateRandomString(12)
        var imeiSecondary = generateRandomString(12)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            try {
                val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                imeiPrimary = tm.getImei(0) ?: ""
                imeiSecondary = tm.getImei(1) ?: ""
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        deviceInfo["androidId"] = androidId ?: ""
        deviceInfo["imeiPrimary"] = imeiPrimary
        deviceInfo["imeiSecondary"] = imeiSecondary
        deviceInfo["manufacturer"] = manufacturer
        deviceInfo["osVersion"] = osVersion
        deviceInfo["apiLevel"] = apiLevel
        deviceInfo["timeZone"] = timeZone
        deviceInfo["localeLanguage"] = localeLanguage
        deviceInfo["localeCountry"] = localeCountry

        return deviceInfo
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun registerDevice() {
        lifecycleScope.launch {
            try {

                val deviceInfo = getDeviceInfo(this@HomeActivity)

                val request = RegisterDeviceRequest(
                    androdId = deviceInfo["androidId"] ?: "",
                    imeiPrimary = deviceInfo["imeiPrimary"] ?: "",
                    imeiSecondary = deviceInfo["imeiSecondary"] ?: "",
                    manufacturer = deviceInfo["manufacturer"] ?: "",
                    osVersion = deviceInfo["osVersion"] ?: "",
                    apiLevel = deviceInfo["apiLevel"] ?: "",
                    timeZone = deviceInfo["timeZone"] ?: "",
                    localeLanguage = deviceInfo["localeLanguage"] ?: "",
                    localeCountry = deviceInfo["localeCountry"] ?: "",
                    status = 0,
                    token = ""
                )

                val response = NetworkModule.authApi.registerDevice(
                    request
                )

                if (response.isSuccessful) {
                    // Save token from headers or body
                    val body = response.body()
                    val newToken = body?.responseObject?.token ?: ""
                    saveToken(newToken)
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, NubitManager.getMinusOneFragment())
                        .commit()
                } else {
                    Log.e("RegisterDevice", "Failed: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun registerMinusOne(context: Context) {
        try {
            val intent = Intent("com.tcl.android.launcher.action.ADD_MINUS_ONE")
            intent.setPackage("com.tcl.android.launcher")
            intent.putExtra("minus_one_package", context.packageName)
            intent.putExtra("minus_one_activity", "com.mobi.nubitalcatel.ui.activity.MinusOneActivity")
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val intent = Intent(CommonMethods.ACTION_STOP_AUDIO)
        sendBroadcast(intent)
    }

    override fun onPause() {
        super.onPause()
        // Ensure any playing media is stopped as soon as activity goes to background
        val intent = Intent(CommonMethods.ACTION_STOP_AUDIO)
        sendBroadcast(intent)
    }

    private fun saveToken(token: String) {
        CommonMethods.saveBearerToken(this, token)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, NubitManager.getMinusOneFragment())
            .commit()
    }
}
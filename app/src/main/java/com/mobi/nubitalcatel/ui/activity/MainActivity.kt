package com.mobi.nubitalcatel.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
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


class MainActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val config = NubitConfig(
            apiKey = "YOUR_API_KEY",
            partnerId = "USER_ID"
        )
        NubitManager.initialize(this, config)

        Log.e("checktoken",">>" + CommonMethods.getBearerToken(this))
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

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("HardwareIds")
    fun getDeviceInfo(context: Context): Map<String, String> {
        val deviceInfo = mutableMapOf<String, String>()

        // Android ID (unique to device + app-signing key)
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

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
        var imeiPrimary = ""
        var imeiSecondary = ""
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

                val deviceInfo = getDeviceInfo(this@MainActivity)

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
                    val newToken = response.headers()["Authorization"] ?: ""
                    saveToken(newToken)
                } else {
                    Log.e("RegisterDevice", "Failed: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveToken(token: String) {
       CommonMethods.saveBearerToken(this,token)
    }
}

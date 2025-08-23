package com.mobi.nubitalcatel.utils

import android.app.Fragment
import android.content.Context
import com.mobi.nubitalcatel.ui.fragments.NubitFragment

object NubitManager {

    fun initialize(context: Context, config: NubitConfig) {
        // Init your SDK, analytics, APIs, etc.
    }

    fun getMinusOneFragment(): NubitFragment {
        return NubitFragment()
    }
}

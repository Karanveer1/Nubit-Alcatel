package com.mobi.nubitalcatel.data.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobi.nubitalcatel.core.models.WidgetOrder
import com.mobi.nubitalcatel.core.network.NetworkModule
import com.mobi.nubitalcatel.core.repo.WidgetRepository
import kotlinx.coroutines.launch

class WidgetViewModel : ViewModel() {

    private val repo = WidgetRepository(NetworkModule.minusOneApi)

    private val _widgets = MutableLiveData<List<WidgetOrder>>()
    val widgets: LiveData<List<WidgetOrder>> = _widgets

//    fun loadWidgets(order: String) {
//        viewModelScope.launch {
//            val result = repo.fetchWidgets(order)
//            result.onSuccess { data ->
//                _widgets.value = data
//            }.onFailure { exception ->
//                Log.e("WidgetViewModel", "Error: ${exception.message}", exception)
//            }
//        }
//    }
}

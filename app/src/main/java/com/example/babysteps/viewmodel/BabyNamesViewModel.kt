package com.example.babysteps.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.babysteps.api.RetrofitInstance
import kotlinx.coroutines.launch

class BabyNamesViewModel : ViewModel() {

    private val _babyNames = MutableLiveData<List<String>>()
    val babyNames: LiveData<List<String>> get() = _babyNames

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val apiKey = "ZphwFpRAdSKlddOQusSUWg==TQeibpLB8K7BAcva"

    fun fetchBabyNames(gender: String? = null, popularOnly: Boolean? = true) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getBabyNames(apiKey, gender, popularOnly)
                if (response.isSuccessful) {
                    _babyNames.value = response.body()
                } else {
                    _error.value = "Error: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Exception: ${e.message}"
            }
        }
    }
}

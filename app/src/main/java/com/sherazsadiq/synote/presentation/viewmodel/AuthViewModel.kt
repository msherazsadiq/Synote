package com.sherazsadiq.synote.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sherazsadiq.synote.data.FirebaseRepository
import com.sherazsadiq.synote.utils.ResultState
import kotlinx.coroutines.launch
import javax.inject.Inject

class AuthViewModel: ViewModel() {

    private val firebaseRepository = FirebaseRepository()

    private val _authState = MutableLiveData<ResultState<Boolean>>()
    val authState: LiveData<ResultState<Boolean>> = _authState

    fun login(email: String, password: String) {
        _authState.value = ResultState.Loading
        viewModelScope.launch {
            val result = firebaseRepository.login(email, password)
            _authState.value = result
        }
    }

    fun signUp(email: String, password: String) {
        _authState.value = ResultState.Loading
        viewModelScope.launch {
            val result = firebaseRepository.signup(email, password)
            _authState.value = result
        }
    }
}

package com.klavs.bindle.uix.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthResult
import com.klavs.bindle.data.repo.auth.AuthRepository
import com.klavs.bindle.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogInViewModel @Inject constructor(private val authRepo: AuthRepository) : ViewModel() {
    val logInState : MutableState<Resource<AuthResult>> = mutableStateOf(Resource.Idle())


    fun logIn(email: String, password: String) {
        logInState.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            logInState.value = authRepo.loginUser(email, password)
        }
    }
}
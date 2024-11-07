package com.klavs.bindle.uix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.klavs.bindle.data.repo.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MenuViewModel @Inject constructor(private val authRepository: AuthRepository) :ViewModel() {

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser : StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    var currentUserJob: Job? = null
    init {
        currentUserJob= viewModelScope.launch(Dispatchers.Main) {
            authRepository.getCurrentUser().collect{
                _currentUser.value = it
            }
        }
    }

    fun signOut(){
        viewModelScope.launch(Dispatchers.Main) { authRepository.signOut() }
    }
}
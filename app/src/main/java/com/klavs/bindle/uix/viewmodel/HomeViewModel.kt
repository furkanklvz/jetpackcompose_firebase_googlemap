package com.klavs.bindle.uix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.klavs.bindle.data.datastore.AppPref
import com.klavs.bindle.data.repo.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepo: AuthRepository
) : ViewModel() {
    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser : StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.Main) {
            authRepo.getCurrentUser().collect{
                _currentUser.value = it
            }
        }
    }


}
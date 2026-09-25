package com.klavs.bindle.uix.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.AuthResult
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.klavs.bindle.data.repo.auth.AuthRepository
import com.klavs.bindle.data.repo.messaging.MessagingRepository
import com.klavs.bindle.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogInViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val messagingRepo: MessagingRepository
) : ViewModel() {
    val logInState: MutableState<Resource<AuthResult>> = mutableStateOf(Resource.Idle())
    val resetPasswordEmailState: MutableState<Resource<Boolean>> = mutableStateOf(Resource.Idle())
    val signInWithGoogleState: MutableState<Resource<AuthResult>> = mutableStateOf(Resource.Idle())


    fun logIn(email: String, password: String) {
        logInState.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            val signInResult = authRepo.loginUser(email, password)
            if (signInResult is Resource.Success && signInResult.data?.user != null) {
                if (signInResult.data.user != null) {
                    messagingRepo.updateToken(
                        uid = signInResult.data.user!!.uid
                    )
                }
                logInState.value = signInResult
            } else {
                logInState.value = signInResult
            }
        }
    }

    fun onSignInWithGoogle(credential: Credential) {
        signInWithGoogleState.value = Resource.Loading()
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            viewModelScope.launch(Dispatchers.Main) {
                val signInState =
                    authRepo.signInWithGoogle(googleIdTokenCredential.idToken)
                when (signInState) {
                    is Resource.Success -> {
                        authRepo.reloadUserInformation()
                        if (signInState.data?.user != null) {
                            messagingRepo.updateToken(
                                uid = signInState.data!!.user!!.uid
                            )
                        }
                        signInWithGoogleState.value = signInState
                    }

                    else -> {
                        signInWithGoogleState.value = signInState
                    }
                }

            }
        }
    }

    fun sendPasswordResetEmail(value: String) {
        resetPasswordEmailState.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            resetPasswordEmailState.value = authRepo.sendPasswordResetEmail(value)
        }
    }
}
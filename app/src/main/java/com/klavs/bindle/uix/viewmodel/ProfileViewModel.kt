package com.klavs.bindle.uix.viewmodel

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.klavs.bindle.data.entity.User
import com.klavs.bindle.data.repo.auth.AuthRepository
import com.klavs.bindle.data.repo.firestore.FirestoreRepository
import com.klavs.bindle.data.repo.storage.StorageRepository
import com.klavs.bindle.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val firestoreRepo: FirestoreRepository,
    private val storageRepo: StorageRepository,
    auth: FirebaseAuth
) : ViewModel() {

    val userDataState: MutableState<Resource<User>> = mutableStateOf(Resource.Idle())
    val resetPasswordState: MutableState<Resource<Boolean>> = mutableStateOf(Resource.Idle())
    val uploadPictureState: MutableState<Resource<Boolean>> = mutableStateOf(Resource.Idle())
    val updateUserDataState: MutableState<Resource<Boolean>> = mutableStateOf(Resource.Idle())
    val updateEmailState: MutableState<Resource<Boolean>> = mutableStateOf(Resource.Idle())
    val sendEmailVerificationState: MutableState<Resource<Boolean>> =
        mutableStateOf(Resource.Idle())
    val checkUniqueUsernameState: MutableState<Resource<Boolean>> = mutableStateOf(Resource.Idle())

    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    var currentUserJob: Job? = null

    init {
        currentUserJob = viewModelScope.launch(Dispatchers.Main) {
            authRepo.getCurrentUser().collect {
                _currentUser.value = it
            }
        }
        if (currentUser.value!= null){
            viewModelScope.launch {
                currentUser.value!!.reload().await()
            }
        }
    }

    fun sendEmailVerification() {
        sendEmailVerificationState.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            sendEmailVerificationState.value = authRepo.sendEmailVerification()
        }
    }

    fun updateEmail(password: String, newEmail: String) {
        updateEmailState.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            if (currentUser.value != null) {
                updateEmailState.value =
                    authRepo.updateEmail(password = password, newEmail = newEmail)
            } else {
                updateEmailState.value = Resource.Error(message = "User not found")
            }
        }
    }

    fun updateUserData(newUserData: HashMap<String, Any?>) {
        updateUserDataState.value = Resource.Loading()
        if (currentUser.value != null) {
            if (newUserData.isNotEmpty()) {
                viewModelScope.launch(Dispatchers.Main) {
                    updateUserDataState.value =
                        firestoreRepo.updateUserData(
                            uid = currentUser.value!!.uid,
                            newUser = newUserData
                        )

                }
            } else {
                updateUserDataState.value = Resource.Success(data = true)
            }
        } else {
            updateUserDataState.value = Resource.Error(message = "User not found")
        }
    }

    fun resetPassword(currentPassword: String, newPassword: String) {
        resetPasswordState.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            resetPasswordState.value = authRepo.updatePassword(
                newPassword = newPassword,
                currentPassword = currentPassword
            )
        }
    }

    fun sendResetPasswordEmail() {
        if (currentUser.value != null) {
            viewModelScope.launch(Dispatchers.Main) {
                authRepo.sendPasswordResetEmail(email = currentUser.value!!.email!!)
            }

        }
    }

    fun getUserInfos() {
        userDataState.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            if (currentUser.value != null) {
                userDataState.value = firestoreRepo.getUserData(currentUser.value!!.uid)
            } else {
                userDataState.value = Resource.Error(message = "User not found")
            }
        }
    }

    fun updateProfilePicture(pictureUri: Uri?) {
        uploadPictureState.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            if (pictureUri != null) {
                val uploadToStorageState = storageRepo.uploadImage(
                    imageUri = pictureUri,
                    "profilePictures/${currentUser.value!!.uid}",
                    maxSize = 384
                )
                if (uploadToStorageState is Resource.Success) {
                    val updateAuthState = authRepo.updateUserPhotoUrl(uploadToStorageState.data)
                    if (updateAuthState is Resource.Success) {
                        uploadPictureState.value =
                            firestoreRepo.updateProfilePictureUri(newProfilePictureUri = uploadToStorageState.data)
                    }
                } else {
                    uploadPictureState.value = Resource.Error(message = "Network error")
                }
            } else {
                uploadPictureState.value = authRepo.updateUserPhotoUrl(null)
                if (uploadPictureState.value is Resource.Success) {
                    uploadPictureState.value =
                        firestoreRepo.updateProfilePictureUri(newProfilePictureUri = null)
                    storageRepo.deleteImage("profilePictures/${currentUser.value!!.uid}")
                }
            }
        }
    }

    fun checkUniqueUsername(userName: String, myUid: String) {
        checkUniqueUsernameState.value = Resource.Loading()
        viewModelScope.launch {
            checkUniqueUsernameState.value =
                firestoreRepo.checkUniqueUsername(username = userName, myUid = myUid)
        }
    }
}
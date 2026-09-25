package com.klavs.bindle.uix.viewmodel

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.klavs.bindle.R
import com.klavs.bindle.data.repo.auth.AuthRepository
import com.klavs.bindle.data.repo.firestore.FirestoreRepository
import com.klavs.bindle.data.repo.storage.StorageRepository
import com.klavs.bindle.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val firestoreRepo: FirestoreRepository,
    private val storageRepo: StorageRepository,
    private val db: FirebaseFirestore,
    auth: FirebaseAuth
) : ViewModel() {

    val resetPasswordState: MutableState<Resource<Boolean>> = mutableStateOf(Resource.Idle())
    val uploadPictureState: MutableState<Resource<Boolean>> = mutableStateOf(Resource.Idle())
    val updateUserDataState: MutableState<Resource<Boolean>> = mutableStateOf(Resource.Idle())
    val updateEmailState: MutableState<Resource<Boolean>> = mutableStateOf(Resource.Idle())
    val sendEmailVerificationState: MutableState<Resource<Boolean>> =
        mutableStateOf(Resource.Idle())
    val checkUniqueUsernameState: MutableState<Resource<Boolean>> = mutableStateOf(Resource.Idle())

    private val _deletingAccountResource = MutableStateFlow<Resource<Unit>>(Resource.Idle())
    val deletingAccountResource = _deletingAccountResource.asStateFlow()

    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser = _currentUser.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.Main) {
            authRepo.getCurrentUser().collect { user ->
                _currentUser.value = user
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
            updateEmailState.value =
                authRepo.updateEmail(password = password, newEmail = newEmail)
        }
    }

    fun deleteAccount(user: FirebaseUser, password: String){
        _deletingAccountResource.value = Resource.Loading()
        viewModelScope.launch {
            _deletingAccountResource.value = authRepo.deleteAccount(user = user, password = password)
        }
    }


    fun updateUserData(newUserData: HashMap<String, Any?>, myUid: String) {
        updateUserDataState.value = Resource.Loading()
        if (newUserData.isNotEmpty()) {
            viewModelScope.launch(Dispatchers.Main) {
                updateUserDataState.value =
                    firestoreRepo.updateUserData(
                        uid = myUid,
                        newUser = newUserData
                    )

            }
        } else {
            updateUserDataState.value = Resource.Success(data = true)
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

    fun sendResetPasswordEmail(email: String) {
        viewModelScope.launch(Dispatchers.Main) {
            authRepo.sendPasswordResetEmail(email = email)
        }
    }

    fun updateProfilePicture(pictureUri: Uri?, myUid: String) {
        uploadPictureState.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            if (pictureUri != null) {
                val uploadToStorageState = storageRepo.uploadImage(
                    imageUri = pictureUri,
                    "profilePictures/$myUid",
                    maxSize = 384
                )
                if (uploadToStorageState is Resource.Success) {
                    val updateAuthState = authRepo.updateUserPhotoUrl(uploadToStorageState.data)
                    if (updateAuthState is Resource.Success) {
                        uploadPictureState.value =
                            firestoreRepo.updateProfilePictureUri(newProfilePictureUri = uploadToStorageState.data)
                    }
                } else {
                    uploadPictureState.value = Resource.Error(messageResource = R.string.something_went_wrong)
                }
            } else {
                uploadPictureState.value = authRepo.updateUserPhotoUrl(null)
                if (uploadPictureState.value is Resource.Success) {
                    uploadPictureState.value =
                        firestoreRepo.updateProfilePictureUri(newProfilePictureUri = null)
                    storageRepo.deleteImage("profilePictures/$myUid")
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
package com.klavs.bindle.uix.viewmodel

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klavs.bindle.R
import com.klavs.bindle.data.entity.User
import com.klavs.bindle.data.repo.auth.AuthRepository
import com.klavs.bindle.data.repo.firestore.FirestoreRepository
import com.klavs.bindle.data.repo.messaging.MessagingRepository
import com.klavs.bindle.data.repo.storage.StorageRepository
import com.klavs.bindle.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateUserViewModel @Inject constructor(
    private val firestoreRepo: FirestoreRepository,
    private val authRepo: AuthRepository,
    private val storageRepo: StorageRepository,
    private val messagingRepo: MessagingRepository
) : ViewModel() {
    val checkUniqueUsername: MutableState<Resource<Boolean>> = mutableStateOf(Resource.Idle())
    val checkUniqueEmail: MutableState<Resource<Boolean>> = mutableStateOf(Resource.Idle())
    val registerResponse: MutableState<Resource<Boolean>> = mutableStateOf(Resource.Idle())

    fun checkUniqueUsername(username: String) {
        checkUniqueUsername.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            checkUniqueUsername.value = firestoreRepo.checkUniqueUsername(username)

        }
    }

    fun checkUniqueEmail(email: String) {
        checkUniqueEmail.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            checkUniqueEmail.value = firestoreRepo.checkUniqueEmail(email)
        }
    }

    fun registerUser(user: User) {
        registerResponse.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            val authState =  authRepo.createUserWithEmailAndPassword(
                user.email,
                user.password?:""
            )
            if (authState is Resource.Success) {
                val userImageUri = user.profilePictureUrl
                val profilePictureUrl: Uri? =
                    if (userImageUri == "default") null else {
                        val downloadUrl = storageRepo.uploadImage(
                            imageUri = Uri.parse(userImageUri),
                            path = "profilePictures/${authState.data!!.user?.uid}",
                            maxSize = 384
                        )
                        downloadUrl.data
                    }
                val userModel = User(
                    uid = authState.data!!.user?.uid?:"",
                    userName = user.userName,
                    email = user.email,
                    profilePictureUrl = profilePictureUrl?.toString(),
                    realName = user.realName,
                    gender = user.gender,
                    birthDate = user.birthDate,
                    phoneNumber = user.phoneNumber,
                    tickets = 5,
                    acceptedTermsAndPrivacyPolicy = true
                )

                val loadToFirestoreState = firestoreRepo.registerUser(userModel = userModel)
                if (loadToFirestoreState is Resource.Success){
                    messagingRepo.updateToken(uid = userModel.uid)
                }
                registerResponse.value = loadToFirestoreState
            }else{
                registerResponse.value = Resource.Error(messageResource = authState.messageResource?: R.string.something_went_wrong)
            }
        }
    }
}
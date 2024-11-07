package com.klavs.bindle.uix.viewmodel

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klavs.bindle.data.entity.User
import com.klavs.bindle.data.repo.auth.AuthRepository
import com.klavs.bindle.data.repo.firestore.FirestoreRepository
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
    private val storageRepo: StorageRepository
) : ViewModel() {
    val checkUniqueUsername: MutableState<Resource<Boolean>> = mutableStateOf(Resource.Idle())
    val checkUniqueEmail: MutableState<Resource<Boolean>> = mutableStateOf(Resource.Idle())
    val registerResponse: MutableState<Resource<Boolean>> = mutableStateOf(Resource.Idle())

    fun checkUniqueUsername(username: String) {
        checkUniqueUsername.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            checkUniqueUsername.value = firestoreRepo.checkUniqueUsername(username)
            Log.e("checkUniqueUsername", checkUniqueUsername.value.toString())
        }
    }

    fun checkUniqueEmail(email: String) {
        checkUniqueEmail.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            checkUniqueEmail.value = firestoreRepo.checkUniqueEmail(email)
        }
    }

    fun registerUser(userInfos: HashMap<String, Any?>) {
        registerResponse.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            val authState =  authRepo.registerUser(
                userInfos["email"] as String,
                userInfos["password"] as String
            )
            if (authState is Resource.Success) {
                val userImageUri = userInfos["profilePictureUrl"] as String
                val profilePictureUri: Uri? =
                    if (userImageUri == "default") null else {
                        val downloadUrl = storageRepo.uploadImage(
                            imageUri = Uri.parse(userImageUri),
                            path = "profilePictures/${authState.data!!.user!!.uid}",
                            maxSize = 384
                        )
                        downloadUrl.data
                    }
                val userModel = User(
                    uid = authState.data!!.user!!.uid,
                    userName = userInfos["userName"] as String,
                    email = userInfos["email"] as String,
                    profilePictureUrl = profilePictureUri.toString(),
                    realName = userInfos["realName"] as String,
                    gender = userInfos["gender"] as String,
                    birthDate = userInfos["birthDay"] as Long,
                    phoneNumber = userInfos["phoneNumber"] as String,
                )
                registerResponse.value = firestoreRepo.registerUser(userModel = userModel)
            }else{
                registerResponse.value = Resource.Error(message = "auth error")
            }
        }
    }
}
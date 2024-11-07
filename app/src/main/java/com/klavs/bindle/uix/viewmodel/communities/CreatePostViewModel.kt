package com.klavs.bindle.uix.viewmodel.communities

import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.klavs.bindle.data.entity.Post
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
import javax.inject.Inject


@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val firestoreRepo: FirestoreRepository,
    private val authRepo: AuthRepository,
    private val storageRepo: StorageRepository,
    private val db: FirebaseFirestore
) : ViewModel() {
    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    var currentUserJob: Job? = null

    val createPostResource = mutableStateOf<Resource<Boolean>>(Resource.Idle())

    fun createPost(post: Post, communityId: String) {
        createPostResource.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            val postsRef = db.collection("communities").document(communityId).collection("posts")
            val createPostDocResource = firestoreRepo.addDocument(
                collectionRef = postsRef,
                data = post
            )
            if (createPostDocResource is Resource.Success) {
                if (post.imageUrl != null) {
                    val imageRef = "postPictures/${createPostDocResource.data!!}"
                    val uploadPictureResource = storageRepo.uploadImage(
                        imageUri = post.imageUrl.toUri(),
                        path = imageRef,
                        maxSize = 720
                    )
                    if (uploadPictureResource is Resource.Success) {
                        val postRef =
                            db.collection("communities").document(communityId).collection("posts")
                                .document(createPostDocResource.data)
                        val updateImageUrlResource = firestoreRepo.updateField(
                            documentRef = postRef,
                            fieldName = "imageUrl",
                            data = uploadPictureResource.data!!.toString()
                        )
                        if (updateImageUrlResource is Resource.Success) {
                            createPostResource.value = Resource.Success(true)
                        } else {
                            createPostResource.value = Resource.Success(false)
                        }
                    } else {
                        createPostResource.value = Resource.Success(false)
                    }
                } else {
                    createPostResource.value = Resource.Success(data = true)
                }
            } else {
                createPostResource.value =
                    Resource.Error(message = "Post cannot be shared right now, please try again later")
            }
        }
    }

    init {
        currentUserJob = viewModelScope.launch(Dispatchers.Main) {
            authRepo.getCurrentUser().collect {
                _currentUser.value = it
            }
        }
    }
}
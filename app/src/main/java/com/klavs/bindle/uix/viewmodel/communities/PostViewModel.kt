package com.klavs.bindle.uix.viewmodel.communities

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.klavs.bindle.data.entity.Like
import com.klavs.bindle.data.entity.Post
import com.klavs.bindle.data.entity.PostComment
import com.klavs.bindle.data.repo.auth.AuthRepository
import com.klavs.bindle.data.repo.firestore.FirestoreRepository
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
class PostViewModel @Inject
constructor(
    private val firestoreRepo: FirestoreRepository,
    private val authRepo: AuthRepository,
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModel() {
    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    var currentUserJob: Job? = null

    init {
        currentUserJob = viewModelScope.launch(Dispatchers.Main) {
            authRepo.getCurrentUser().collect { firebaseUser ->
                _currentUser.value = firebaseUser
            }
        }
    }

    val numOfComment: MutableState<Pair<String, Int>?> = mutableStateOf(null)


    var newCommentJob: Job? = null
    var newPostJob: Job? = null
    var lastPost: DocumentSnapshot? = null
    var lastComment: DocumentSnapshot? = null
    var firstCommentLoaded = mutableStateOf(false)

    private val _newPost = MutableStateFlow<Resource<Post?>>(Resource.Idle())
    val newPost: StateFlow<Resource<Post?>> = _newPost.asStateFlow()

    private val _pagedPosts = MutableStateFlow<Resource<List<Post>>>(Resource.Idle())
    val pagedPosts: StateFlow<Resource<List<Post>>> = _pagedPosts.asStateFlow()

    private val _pagedComments = MutableStateFlow<Resource<List<PostComment>>>(Resource.Idle())
    val pagedComments: StateFlow<Resource<List<PostComment>>> = _pagedComments.asStateFlow()
    private val _newComment = MutableStateFlow<Resource<PostComment?>>(Resource.Idle())
    val newComment: StateFlow<Resource<PostComment?>> = _newComment.asStateFlow()

    fun writeComment(comment: PostComment, communityId: String, postId: String) {
        val commentsRef = db.collection("communities").document(communityId).collection("posts")
            .document(postId).collection("comments")
        viewModelScope.launch {
            firestoreRepo.addDocument(
                collectionRef = commentsRef,
                data = comment
            )
        }
    }

    fun likeThePost(postId: String, communityId: String) {
        if (currentUser.value != null) {
            viewModelScope.launch(Dispatchers.Main) {
                val likeModel = Like(
                    uid = currentUser.value!!.uid,
                    postId = postId
                )
                val postRef =
                    db.collection("communities").document(communityId).collection("posts")
                        .document(postId).collection("likes")
                firestoreRepo.addDocument(
                    documentName = currentUser.value!!.uid,
                    collectionRef = postRef,
                    data = likeModel
                )
            }
        }
    }

    fun getCommentCount(postId: String, communityId: String) {
        val commentsRef =
            db.collection("communities").document(communityId)
                .collection("posts").document(postId)
                .collection("comments")
        viewModelScope.launch {
            numOfComment.value =
                postId to firestoreRepo.countDocumentsWithoutResource(query = commentsRef)
        }
    }

    fun undoLikeThePost(postId: String, communityId: String) {
        if (currentUser.value != null) {
            viewModelScope.launch(Dispatchers.Main) {
                val likeRef =
                    db.collection("communities").document(communityId).collection("posts")
                        .document(postId).collection("likes").document(currentUser.value!!.uid)
                firestoreRepo.deleteDocument(
                    documentRef = likeRef
                )
            }
        }
    }

    fun getPostsWithPaging(communityId: String, pageSize: Int) {
        _pagedPosts.value = Resource.Loading()
        if (currentUser.value != null) {
            viewModelScope.launch {
                val postsRef =
                    db.collection("communities").document(communityId).collection("posts")
                        .orderBy("date", Query.Direction.DESCENDING)
                val resource = firestoreRepo.getDocumentsWithPaging(
                    query = postsRef,
                    pageSize = pageSize.toLong(),
                    lastDocument = lastPost
                )
                when (resource) {
                    is Resource.Error -> {
                        _pagedPosts.value = Resource.Error(message = resource.message!!)
                    }

                    is Resource.Idle -> {}
                    is Resource.Loading -> {
                        _pagedPosts.value = Resource.Loading()
                    }

                    is Resource.Success -> {
                        lastPost =
                            if (resource.data!!.querySnapshot.documents.size < pageSize) null else {
                                resource.data.lastDocument
                            }


                        _pagedPosts.value =
                            Resource.Success(data = resource.data.querySnapshot.mapNotNull { postSnapshot ->
                                val postRef = db.collection("communities").document(communityId)
                                    .collection("posts").document(postSnapshot.id)
                                val likesRef =
                                    db.collection("communities").document(communityId)
                                        .collection("posts").document(postSnapshot.id)
                                        .collection("likes")
                                val commentsRef =
                                    db.collection("communities").document(communityId)
                                        .collection("posts").document(postSnapshot.id)
                                        .collection("comments")
                                val numOfLikes =
                                    firestoreRepo.countDocumentsWithoutResource(query = likesRef)
                                val numOfComments =
                                    firestoreRepo.countDocumentsWithoutResource(query = commentsRef)
                                val liked = firestoreRepo.checkIfUserLikedPost(
                                    postRef = postRef,
                                    uid = currentUser.value!!.uid
                                )
                                postSnapshot.toObject(Post::class.java).copy(
                                    id = postSnapshot.id,
                                    numOfLikes = numOfLikes,
                                    numOfComments = numOfComments,
                                    liked = liked
                                )
                            })
                    }
                }
            }
        } else {
            _pagedPosts.value = Resource.Error(message = "User not logged in")
        }
    }

    fun listenToNewPost(communityId: String) {
        newPostJob = viewModelScope.launch(Dispatchers.Main) {
            val postsRef = db.collection("communities").document(communityId).collection("posts")
                .orderBy("date", Query.Direction.DESCENDING)
            firestoreRepo.listenToNewDoc(
                query = postsRef
            ).collect { resource ->
                when (resource) {
                    is Resource.Error -> {
                        _newPost.value = Resource.Error(message = resource.message!!)
                    }

                    is Resource.Idle -> {}
                    is Resource.Loading -> {
                        _newPost.value = Resource.Loading()
                    }

                    is Resource.Success -> {
                        if (lastPost == null) {
                            lastPost = resource.data!!.lastOrNull()
                        }
                        _newPost.value =
                            Resource.Success(
                                data = if (resource.data!!.isEmpty) {
                                    null
                                } else {
                                    val doc = resource.data.documents[0]
                                    val postRef = db.collection("communities").document(communityId)
                                        .collection("posts").document(doc.id)
                                    val likesRef =
                                        db.collection("communities").document(communityId)
                                            .collection("posts").document(doc.id)
                                            .collection("likes")
                                    val commentsRef =
                                        db.collection("communities").document(communityId)
                                            .collection("posts").document(doc.id)
                                            .collection("comments")
                                    val numOfLikes =
                                        firestoreRepo.countDocumentsWithoutResource(query = likesRef)
                                    val numOfComments =
                                        firestoreRepo.countDocumentsWithoutResource(query = commentsRef)
                                    val liked = firestoreRepo.checkIfUserLikedPost(
                                        postRef = postRef,
                                        uid = currentUser.value!!.uid
                                    )
                                    val userResource = firestoreRepo.getUserData(doc.data?.get("senderUid") as String)
                                    if (userResource is Resource.Success){
                                        doc.toObject(
                                            Post::class.java
                                        )!!.copy(
                                            id = doc.id,
                                            numOfLikes = numOfLikes,
                                            numOfComments = numOfComments,
                                            liked = liked,
                                            senderUserName = userResource.data!!.userName,
                                            senderImageUrl = userResource.data.profilePictureUrl
                                        )
                                    }else {
                                        doc.toObject(
                                            Post::class.java
                                        )!!.copy(
                                            id = doc.id,
                                            numOfLikes = numOfLikes,
                                            numOfComments = numOfComments,
                                            liked = liked
                                        )
                                    }
                                }
                            )
                    }
                }
            }
        }
    }

    fun listenToNewComment(communityId: String, postId: String) {
        newCommentJob = viewModelScope.launch(Dispatchers.Main) {
            val commentsRef = db.collection("communities").document(communityId).collection("posts")
                .document(postId).collection("comments").orderBy("date", Query.Direction.DESCENDING)
            firestoreRepo.listenToNewDoc(
                query = commentsRef
            ).collect { resource ->
                when (resource) {
                    is Resource.Error -> {
                        _newComment.value = Resource.Error(message = resource.message!!)
                    }

                    is Resource.Idle -> {}
                    is Resource.Loading -> {
                        _newComment.value = Resource.Loading()
                    }

                    is Resource.Success -> {
                        if (!firstCommentLoaded.value) {
                            lastComment = resource.data!!.lastOrNull()
                            Log.e("posts", "last comment changed")
                            firstCommentLoaded.value = true
                        }
                        _newComment.value =
                            Resource.Success(
                                data = if (resource.data!!.isEmpty) {
                                    null
                                } else {
                                    val userDoc = firestoreRepo.getUserData(
                                        resource.data.documents[0].data?.get("senderUid") as String
                                    )
                                    if (userDoc is Resource.Success) {
                                        resource.data.documents[0].toObject(
                                            PostComment::class.java
                                        )!!.copy(
                                            id = resource.data.documents[0].id,
                                            senderUserName = userDoc.data!!.userName,
                                            senderProfileImageUrl = userDoc.data.profilePictureUrl
                                        )
                                    } else {
                                        resource.data.documents[0].toObject(
                                            PostComment::class.java
                                        )!!.copy(id = resource.data.documents[0].id)
                                    }
                                }
                            )
                    }
                }
            }
        }
    }

    fun getCommentsWithPaging(communityId: String, postId: String, pageSize: Int) {
        _pagedComments.value = Resource.Loading()
        if (currentUser.value != null) {
            viewModelScope.launch {
                val commentsRef =
                    db.collection("communities").document(communityId).collection("posts")
                        .document(postId).collection("comments")
                        .orderBy("date", Query.Direction.DESCENDING)
                val resource = firestoreRepo.getDocumentsWithPaging(
                    query = commentsRef,
                    pageSize = pageSize.toLong(),
                    lastDocument = lastComment
                )
                when (resource) {
                    is Resource.Error -> {
                        _pagedComments.value = Resource.Error(message = resource.message!!)
                    }

                    is Resource.Idle -> {}
                    is Resource.Loading -> {
                        _pagedComments.value = Resource.Loading()
                    }

                    is Resource.Success -> {
                        lastComment = resource.data!!.lastDocument


                        _pagedComments.value =
                            Resource.Success(data = resource.data.querySnapshot.mapNotNull { docSnapshot ->
                                val userDoc =
                                    firestoreRepo.getUserData(docSnapshot.data["senderUid"] as String)
                                if (userDoc is Resource.Success) {
                                    docSnapshot.toObject(PostComment::class.java).copy(
                                        id = docSnapshot.id,
                                        senderUserName = userDoc.data!!.userName,
                                        senderProfileImageUrl = userDoc.data.profilePictureUrl
                                    )
                                } else {
                                    docSnapshot.toObject(PostComment::class.java).copy(
                                        id = docSnapshot.id,
                                        isMyComment = currentUser.value!!.uid == docSnapshot.get("senderUid")
                                    )
                                }
                            })
                    }
                }
            }
        } else {
            _pagedComments.value = Resource.Error(message = "User not logged in")
        }
    }
}
package com.klavs.bindle.uix.viewmodel.communities

import android.util.Log
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.klavs.bindle.R
import com.klavs.bindle.data.entity.sealedclasses.CommunityRoles
import com.klavs.bindle.data.entity.Like
import com.klavs.bindle.data.entity.Post
import com.klavs.bindle.data.entity.PostComment
import com.klavs.bindle.data.entity.PostReport
import com.klavs.bindle.data.entity.UserReport
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
class PostViewModel @Inject constructor(
    private val firestoreRepo: FirestoreRepository,
    private val storageRepo: StorageRepository,
    private val db: FirebaseFirestore
) : ViewModel() {

    val communityFlowListState = LazyListState()

    val commentOnState = mutableStateOf<Resource<PostComment>>(Resource.Idle())
    val deleteCommentState = mutableStateOf<Resource<String>>(Resource.Idle())
    val deletePostState = mutableStateOf<Resource<String>>(Resource.Idle())

    private val _postResource = MutableStateFlow<Resource<Post>>(Resource.Idle())
    val postResource = _postResource.asStateFlow()


    var myRolePriority = MutableStateFlow<Resource<Int>>(Resource.Idle())

    var lastPost: DocumentSnapshot? = null
    var lastComment: DocumentSnapshot? = null


    val pagedPostsState = mutableStateOf<Resource<List<Post>?>>(Resource.Idle())
    val postList = mutableStateListOf<Post>()

    private val _pagedComments = MutableStateFlow<Resource<List<PostComment>>>(Resource.Idle())
    val pagedComments: StateFlow<Resource<List<PostComment>>> = _pagedComments.asStateFlow()


    var listenToPostJob: Job? = null
    var listenToMyRoleJob: Job? = null


    fun listenToPost(communityId: String, postId: String, myUid: String) {
        _postResource.value = Resource.Loading()
        listenToPostJob?.cancel()
        listenToPostJob = viewModelScope.launch(Dispatchers.Main) {
            val postRef = db.collection("communities").document(communityId).collection("posts")
                .document(postId)
            firestoreRepo.getDocumentWithListener(
                docRef = postRef
            ).collect { postDocResource ->
                if (postDocResource is Resource.Success) {
                    if (postDocResource.data!!.exists()) {
                        val post = postDocResource.data.toObject(Post::class.java)
                        if (post != null) {
                            val userResource =
                                firestoreRepo.getUserData(postDocResource.data.getString("uid")!!)
                            if (userResource is Resource.Success) {
                                val memberRef = db.collection("communities").document(communityId)
                                    .collection("members").document(userResource.data?.uid ?: "")
                                val memberResource = firestoreRepo.getDocument(
                                    docRef = memberRef,
                                    source = Source.SERVER
                                )
                                if (memberResource is Resource.Success) {
                                    val commentsRef =
                                        db.collection("communities").document(communityId)
                                            .collection("posts").document(postId)
                                            .collection("comments")
                                    val likesRef =
                                        db.collection("communities").document(communityId)
                                            .collection("posts").document(postId)
                                            .collection("likes")
                                    val numOfLikes = firestoreRepo.countDocumentsWithoutResource(
                                        query = likesRef
                                    )
                                    val numOfComments = firestoreRepo.countDocumentsWithoutResource(
                                        query = commentsRef
                                    )
                                    val didILiked = firestoreRepo.checkIfUserLikedPost(
                                        postRef = postRef,
                                        uid = myUid
                                    )
                                    if (memberResource.data!!.exists()) {
                                        _postResource.value = Resource.Success(
                                            data = post.copy(
                                                liked = didILiked,
                                                numOfLikes = numOfLikes,
                                                numOfComments = numOfComments,
                                                userRolePriority = memberResource.data.getLong("rolePriority")
                                                    ?.toInt() ?: CommunityRoles.Member.rolePriority,
                                                id = postId,
                                                userPhotoUrl = userResource.data?.profilePictureUrl,
                                                userName = userResource.data?.userName
                                            )
                                        )
                                    } else {
                                        _postResource.value = Resource.Success(
                                            data = post.copy(
                                                liked = didILiked,
                                                numOfLikes = numOfLikes,
                                                numOfComments = numOfComments,
                                                userRolePriority = CommunityRoles.NotMember.rolePriority,
                                                id = postId,
                                                userPhotoUrl = userResource.data?.profilePictureUrl,
                                                userName = userResource.data?.userName
                                            )
                                        )
                                    }
                                } else {
                                    _postResource.value =
                                        Resource.Error(messageResource = R.string.something_went_wrong_try_again_later)
                                }
                            } else {
                                _postResource.value =
                                    Resource.Error(messageResource = R.string.something_went_wrong_try_again_later)
                            }
                        } else {
                            _postResource.value =
                                Resource.Error(messageResource = R.string.something_went_wrong_try_again_later)
                        }
                    } else {
                        _postResource.value =
                            Resource.Error(messageResource = R.string.post_has_been_deleted)
                    }
                } else {
                    _postResource.value =
                        Resource.Error(messageResource = R.string.something_went_wrong_try_again_later)
                }
            }
        }
    }

    fun listenToMyRole(communityId: String, myUid: String) {
        myRolePriority.value = Resource.Loading()
        listenToMyRoleJob?.cancel()
        listenToMyRoleJob = viewModelScope.launch(Dispatchers.Main) {
            val memberRef = db.collection("communities").document(communityId).collection("members")
                .document(myUid)
            firestoreRepo.getDocumentWithListener(
                docRef = memberRef
            ).collect { resource ->
                if (resource is Resource.Success) {
                    if (resource.data!!.exists()) {
                        myRolePriority.value =
                            Resource.Success(
                                data = (resource.data.data?.get("rolePriority") as? Long)?.toInt()
                                    ?: CommunityRoles.Member.rolePriority
                            )
                    } else {
                        myRolePriority.value =
                            Resource.Error(messageResource = R.string.you_are_not_a_member_of_the_community_anymore)
                    }
                } else {
                    myRolePriority.value =
                        Resource.Error(messageResource = R.string.something_went_wrong_try_again_later)
                }
            }
        }
    }

    fun commentOn(
        comment: PostComment,
        communityId: String,
        postId: String,
        rolePriority: Int,
        currentUser: FirebaseUser
    ) {
        val commentsRef = db.collection("communities").document(communityId).collection("posts")
            .document(postId).collection("comments")
        viewModelScope.launch(Dispatchers.Main) {
            val uploadCommentState = firestoreRepo.addDocument(
                collectionRef = commentsRef, data = comment
            )
            when (uploadCommentState) {
                is Resource.Error -> {
                    commentOnState.value =
                        Resource.Error(messageResource = R.string.something_went_wrong_try_again_later)
                }

                is Resource.Success -> {
                    val commentedPost = comment.copy(
                        id = uploadCommentState.data ?: "",
                        senderUserName = currentUser.displayName,
                        senderProfileImageUrl = currentUser.photoUrl?.toString(),
                        senderRolePriority = rolePriority,
                        isMyComment = true
                    )
                    commentOnState.value = Resource.Success(data = commentedPost)
                }

                else -> {}
            }
        }
    }

    fun sendUserReport(
        reportedUser: String,
        uid: String,
        reportType: Int,
        description: String,
        messageId: String,
        messageContent: String
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            val reportCode: String = when (reportType) {
                0 -> "CHILD_ABUSE_OR_ILLEGAL_CONTENT"
                1 -> "DISRUPTIVE_BEHAVIOR_OR_HARASSMENT"
                2 -> "SOMETHING_ELSE"
                else -> "UNDEFINED"
            }
            val reportRef = db.collection("reports")
            firestoreRepo.addDocument(
                collectionRef = reportRef,
                data = UserReport(
                    uid = uid,
                    messageContent = messageContent,
                    description = description,
                    reportCode = reportCode,
                    timestamp = Timestamp.now(),
                    reportedUserId = reportedUser,
                    messageId = messageId
                )
            )
        }
    }

    fun sendReport(
        uid: String,
        reportType: Int,
        description: String,
        postId: String,
        postOwnerUid: String
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            val reportCode: String = when (reportType) {
                0 -> "SEXUAL_CONTENT_OR_NUDITY"
                1 -> "VIOLENCE"
                2 -> "SOMETHING_ELSE"
                else -> "UNDEFINED"
            }
            val reportRef = db.collection("reports")
            firestoreRepo.addDocument(
                collectionRef = reportRef,
                data = PostReport(
                    postId = postId,
                    postOwnerUid = postOwnerUid,
                    uid = uid,
                    description = description,
                    reportCode = reportCode,
                    timestamp = Timestamp.now()
                )
            )
        }
    }

    fun deleteComment(communityId: String, postId: String, commentId: String) {
        viewModelScope.launch(Dispatchers.Main) {
            val commentRef = db.collection("communities").document(communityId).collection("posts")
                .document(postId).collection("comments").document(commentId)
            deleteCommentState.value = firestoreRepo.deleteDocument(
                documentRef = commentRef
            )
        }
    }

    fun deletePost(communityId: String, postId: String) {
        viewModelScope.launch(Dispatchers.Main) {
            val postRef = db.collection("communities").document(communityId).collection("posts")
                .document(postId)
            deletePostState.value = firestoreRepo.deleteDocument(
                documentRef = postRef
            )
        }
    }

    fun likeThePost(postId: String, communityId: String, myUid: String) {
        viewModelScope.launch(Dispatchers.Main) {
            val likeModel = Like(
                uid = myUid, postId = postId
            )
            val postRef = db.collection("communities").document(communityId).collection("posts")
                .document(postId).collection("likes")
            firestoreRepo.addDocument(
                documentName = myUid,
                collectionRef = postRef,
                data = likeModel
            )
        }

    }


    fun undoLikeThePost(postId: String, communityId: String, myUid: String) {
        viewModelScope.launch(Dispatchers.Main) {
            val likeRef = db.collection("communities").document(communityId).collection("posts")
                .document(postId).collection("likes").document(myUid)
            firestoreRepo.deleteDocument(
                documentRef = likeRef
            )
        }

    }


    fun getPostsWithPaging(communityId: String, pageSize: Int, myUid: String) {
        pagedPostsState.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            val postsRef =
                db.collection("communities").document(communityId).collection("posts")
                    .orderBy("date", Query.Direction.DESCENDING)
            val resource = firestoreRepo.getDocumentsWithPaging(
                query = postsRef, pageSize = pageSize.toLong(), lastDocument = lastPost
            )
            when (resource) {
                is Resource.Error -> {
                    pagedPostsState.value =
                        Resource.Error(messageResource = R.string.something_went_wrong_try_again_later)
                }

                is Resource.Success -> {
                    lastPost =
                        if (resource.data!!.size() < pageSize) null else {
                            resource.data.lastOrNull()
                        }


                    pagedPostsState.value =
                        Resource.Success(data = resource.data.mapNotNull { postSnapshot ->
                            val postRef = db.collection("communities").document(communityId)
                                .collection("posts").document(postSnapshot.id)
                            val likesRef = db.collection("communities").document(communityId)
                                .collection("posts").document(postSnapshot.id)
                                .collection("likes")
                            val commentsRef = db.collection("communities").document(communityId)
                                .collection("posts").document(postSnapshot.id)
                                .collection("comments")
                            val numOfLikes =
                                firestoreRepo.countDocumentsWithoutResource(query = likesRef)
                            val numOfComments =
                                firestoreRepo.countDocumentsWithoutResource(query = commentsRef)
                            val liked = firestoreRepo.checkIfUserLikedPost(
                                postRef = postRef, uid = myUid
                            )
                            val userState = firestoreRepo.getUserData(
                                uid = (postSnapshot["uid"] as? String) ?: ""
                            )
                            if (userState is Resource.Success) {
                                val memberRef =
                                    db.collection("communities").document(communityId)
                                        .collection("members").document(userState.data!!.uid)
                                val memberState = firestoreRepo.getDocument(
                                    docRef = memberRef
                                )
                                if (memberState is Resource.Success) {
                                    if (memberState.data!!.exists()) {
                                        postSnapshot.toObject(Post::class.java).copy(
                                            id = postSnapshot.id,
                                            numOfLikes = numOfLikes,
                                            numOfComments = numOfComments,
                                            userName = userState.data.userName,
                                            userRolePriority = (memberState.data.data?.get("rolePriority") as? Long)?.toInt()
                                                ?: CommunityRoles.Member.rolePriority,
                                            userPhotoUrl = userState.data.profilePictureUrl,
                                            liked = liked
                                        )
                                    } else {
                                        postSnapshot.toObject(Post::class.java).copy(
                                            id = postSnapshot.id,
                                            numOfLikes = numOfLikes,
                                            numOfComments = numOfComments,
                                            userName = userState.data.userName,
                                            userRolePriority = CommunityRoles.NotMember.rolePriority,
                                            userPhotoUrl = userState.data.profilePictureUrl,
                                            liked = liked
                                        )
                                    }
                                } else {
                                    postSnapshot.toObject(Post::class.java).copy(
                                        id = postSnapshot.id,
                                        numOfLikes = numOfLikes,
                                        numOfComments = numOfComments,
                                        userName = userState.data.userName,
                                        userRolePriority = CommunityRoles.NotMember.rolePriority,
                                        userPhotoUrl = userState.data.profilePictureUrl,
                                        liked = liked
                                    )
                                }
                            } else {
                                postSnapshot.toObject(Post::class.java).copy(
                                    id = postSnapshot.id,
                                    numOfLikes = numOfLikes,
                                    numOfComments = numOfComments,
                                    userName = "",
                                    userRolePriority = CommunityRoles.NotMember.rolePriority,
                                    liked = liked
                                )
                            }
                        })
                }

                else -> {}
            }
        }
    }


    fun getCommentsWithPaging(communityId: String, postId: String, pageSize: Int, myUid: String) {
        Log.d("communityPage", "getCommentsWithPaging called")
        _pagedComments.value = Resource.Loading()
        viewModelScope.launch {
            val commentsRef =
                db.collection("communities").document(communityId).collection("posts")
                    .document(postId).collection("comments")
                    .orderBy("date", Query.Direction.DESCENDING)
            val resource = firestoreRepo.getDocumentsWithPaging(
                query = commentsRef, pageSize = pageSize.toLong(), lastDocument = lastComment
            )
            if (resource is Resource.Success) {
                lastComment =
                    if (resource.data!!.size() < pageSize) null else {
                        resource.data.lastOrNull()
                    }


                _pagedComments.value =
                    Resource.Success(data = resource.data.map { commentSnapshot ->
                        val userDoc =
                            firestoreRepo.getUserData(
                                (commentSnapshot.data["senderUid"] as? String) ?: ""
                            )
                        if (userDoc is Resource.Success) {
                            val memberRef =
                                db.collection("communities").document(communityId)
                                    .collection("members")
                                    .document((commentSnapshot.data["senderUid"] as? String) ?: "")
                            val memberDoc = firestoreRepo.getDocument(
                                docRef = memberRef
                            )
                            Log.d("communityflow", memberDoc.toString())
                            if (memberDoc is Resource.Success) {
                                if (memberDoc.data!!.exists()) {
                                    commentSnapshot.toObject(PostComment::class.java).copy(
                                        isMyComment = commentSnapshot.data["senderUid"] == myUid,
                                        id = commentSnapshot.id,
                                        senderUserName = userDoc.data!!.userName,
                                        senderRolePriority = (memberDoc.data.data?.get("rolePriority") as? Long)?.toInt(),
                                        senderProfileImageUrl = userDoc.data.profilePictureUrl
                                    )
                                } else {
                                    commentSnapshot.toObject(PostComment::class.java).copy(
                                        isMyComment = commentSnapshot.data["senderUid"] == myUid,
                                        id = commentSnapshot.id,
                                        senderRolePriority = CommunityRoles.NotMember.rolePriority,
                                        senderUserName = userDoc.data?.userName,
                                        senderProfileImageUrl = userDoc.data?.profilePictureUrl
                                    )
                                }
                            } else {
                                commentSnapshot.toObject(PostComment::class.java).copy(
                                    isMyComment = commentSnapshot.data["senderUid"] == myUid,
                                    id = commentSnapshot.id,
                                    senderUserName = userDoc.data?.userName,
                                    senderProfileImageUrl = userDoc.data?.profilePictureUrl
                                )
                            }
                        } else {
                            commentSnapshot.toObject(PostComment::class.java).copy(
                                id = commentSnapshot.id,
                                isMyComment = myUid == commentSnapshot.get(
                                    "senderUid"
                                )
                            )
                        }
                    }
                    )
            } else {
                _pagedComments.value =
                    Resource.Error(messageResource = resource.messageResource!!)
            }
        }
    }
}
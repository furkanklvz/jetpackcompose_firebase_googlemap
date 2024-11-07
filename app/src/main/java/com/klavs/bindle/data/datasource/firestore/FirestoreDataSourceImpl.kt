package com.klavs.bindle.data.datasource.firestore

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
import com.klavs.bindle.data.entity.JoinedCommunities
import com.klavs.bindle.data.entity.Member
import com.klavs.bindle.data.entity.PagedData
import com.klavs.bindle.data.entity.User
import com.klavs.bindle.resource.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreDataSourceImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) :
    FirestoreDataSource {
    override suspend fun checkUniqueUsername(username: String, myUid: String?): Resource<Boolean> {
        return try {
            val usersRef = if (myUid != null) db.collection("users").whereNotEqualTo("uid", myUid)
                .whereEqualTo("userName", username)
            else db.collection("users").whereEqualTo("userName", username)
            val result =
                usersRef.limit(1).get(Source.SERVER)
                    .await()
            if (result.isEmpty) Resource.Success(true) else Resource.Success(false)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(e.localizedMessage ?: "An error occurred")
        }
    }

    override suspend fun checkUniqueEmail(email: String): Resource<Boolean> {
        return try {
            val result =
                db.collection("users").whereEqualTo("email", email).limit(1).get(Source.SERVER).await()
            if (result.isEmpty) Resource.Success(true) else Resource.Success(false)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(e.localizedMessage ?: "Unknown error")
        }
    }

    override suspend fun registerUser(userModel: User): Resource<Boolean> {
        return try {
            db.collection("users").document(userModel.uid!!).set(userModel).await()
            val profileUpdates = userProfileChangeRequest {
                displayName = userModel.userName
                val photo = userModel.profilePictureUrl
                if (photo != null) {
                    photoUri = photo.toUri()
                }
            }
            auth.currentUser!!.updateProfile(profileUpdates).await()
            Resource.Success(true)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(e.localizedMessage ?: "Unknown error")
        }
    }

    override suspend fun updateProfilePictureUri(newProfilePictureUri: Uri?): Resource<Boolean> {
        return try {
            if (auth.currentUser != null) {
                if (newProfilePictureUri != null) {
                    db.collection("users").document(auth.currentUser!!.uid)
                        .update("profilePictureUrl", newProfilePictureUri).await()
                } else {
                    db.collection("users").document(auth.currentUser!!.uid)
                        .update("profilePictureUrl", null).await()
                }
                Resource.Success(true)
            } else {
                Resource.Error(message = "user not found")
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(e.localizedMessage ?: "Unknown error")
        }
    }

    override suspend fun getUserData(uid: String): Resource<User> {
        return try {
            val document = db.collection("users").document(uid).get().await()
            if (document.exists()) {
                val user = document.toObject(User::class.java)?.copy(uid = document.id)
                if (user != null) {
                    Resource.Success(data = user)
                } else {
                    Resource.Error(message = "user not found")
                }
            } else {
                Resource.Error(message = "user not found")
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(e.localizedMessage ?: "Unknown error")
        }
    }

    override suspend fun updateUserData(
        uid: String,
        newUser: HashMap<String, Any?>
    ): Resource<Boolean> {
        return try {
            db.collection("users").document(uid).update(newUser).await()
            if (newUser.containsKey("userName")){
                val profileChangeRequest =  userProfileChangeRequest {
                    displayName = newUser["userName"] as String
                }
                auth.currentUser!!.updateProfile(profileChangeRequest).await()
                auth.currentUser!!.reload().await()
            }
            Resource.Success(data = true)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error("Data's could not be updated")
        }
    }


    override suspend fun addDocument(
        documentName: String?,
        collectionRef: CollectionReference,
        data: Any
    ): Resource<String> {
        return try {
            if (documentName != null) {
                collectionRef.document(documentName).set(data).await()
                Resource.Success(data = "")
            } else {
                val doc = collectionRef.add(data).await()
                Resource.Success(data = doc.id)
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(message = e.message.toString())
        }
    }

    override suspend fun getDocument(
        docRef: DocumentReference,
        source: Source
    ): Resource<DocumentSnapshot?> {
        return try {
            val document = docRef.get(source).await()
            if (document.exists()) {
                Resource.Success(data = document)
            } else {
                Resource.Success(data = null)
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(message = e.message.toString())
        }

    }

    override suspend fun addItemToMapField(
        documentRef: DocumentReference,
        fieldName: String,
        data: Any
    ): Resource<Boolean> {
        return try {
            documentRef.update(fieldName, FieldValue.arrayUnion(data)).await()
            Resource.Success(data = true)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(message = e.message.toString())
        }
    }

    override fun memberCheck(
        collectionRef: CollectionReference,
        fieldName: String,
        value: String
    ): Flow<Resource<Boolean>> = callbackFlow {
        val listener = collectionRef.whereEqualTo(fieldName, value).limit(1)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    trySend(Resource.Error(message = "Please check your internet connection")).isSuccess
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    trySend(Resource.Success(data = true)).isSuccess
                } else {
                    trySend(Resource.Success(data = false)).isSuccess
                }
            }
        awaitClose {
            listener.remove()
        }
    }


    override fun countDocuments(
        query: Query
    ): Flow<Resource<Int>> = flow {
        try {
            val snapshot = query.count().get(AggregateSource.SERVER).await()
            emit(Resource.Success(data = snapshot.count.toInt()))
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            emit(Resource.Error(message = e.message.toString()))
        }
    }

    override suspend fun getDocumentsWithPaging(
        query: Query,
        pageSize: Long,
        lastDocument: DocumentSnapshot?
    ): Resource<PagedData> {
        return try {
            val snapshot = if (lastDocument != null) {
                if (lastDocument.exists()) {
                    query.startAfter(lastDocument).limit(pageSize).get().await()
                } else {
                    null
                }
            } else {
                query.limit(pageSize).get().await()
            }
            if (snapshot != null) {
                Resource.Success(
                    data = PagedData(
                        querySnapshot = snapshot,
                        lastDocument = snapshot.documents.lastOrNull()
                    )
                )
            } else {
                Resource.Error(message = "Error")
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(message = e.message.toString())
        }
    }

    override fun listenToNewDoc(
        query: Query
    ): Flow<Resource<QuerySnapshot>> = callbackFlow {
        trySend(Resource.Loading())
        val listener =
            query.limit(1).addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(message = error.localizedMessage ?: "Unknown error"))
                } else {
                    if (querySnapshot != null) {
                        trySend(Resource.Success(data = querySnapshot))
                    }
                }
            }

        awaitClose {
            listener.remove()
        }
    }

    override suspend fun deleteDocument(documentRef: DocumentReference): Resource<String> {
        return try {
            documentRef.delete().await()
            Resource.Success(data = documentRef.id)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(message = e.message.toString())
        }
    }

    override suspend fun acceptJoiningRequestForCommunity(
        member: Member,
        community: JoinedCommunities,
    ): Resource<String> {
        return try {
            val requestRef =
                db.collection("communities").document(community.id!!).collection("joiningRequests")
                    .document(member.uid)
            val membersRef =
                db.collection("communities").document(community.id).collection("members")
            val joinnedCommunityRef =
                db.collection("users").document(member.uid).collection("joinedCommunities")
                    .document(
                        community.id
                    )
            requestRef.delete().await()
            membersRef.document(member.uid).set(member).await()
            joinnedCommunityRef.set(community).await()
            Resource.Success(data = member.uid)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(message = e.message.toString())
        }
    }

    override suspend fun removeMember(uid: String, communityId: String): Resource<String> {
        return try {
            val memberRef = db.collection("communities").document(communityId).collection("members")
                .document(uid)
            val joinedCommunityRef =
                db.collection("users").document(uid).collection("joinedCommunities")
                    .document(communityId)
            memberRef.delete().await()
            joinedCommunityRef.delete().await()
            Resource.Success(data = uid)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(message = e.message.toString())
        }
    }

    override suspend fun promoteMember(uid: String, communityId: String): Resource<String> {
        return try {
            val memberRef = db.collection("communities").document(communityId).collection("members")
                .document(uid)
            val joinedCommunityRef =
                db.collection("users").document(uid).collection("joinedCommunities")
                    .document(communityId)
            memberRef.update("rolePriority", 1).await()
            joinedCommunityRef.update("rolePriority", 1).await()
            Resource.Success(data = uid)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(message = e.message.toString())
        }
    }

    override suspend fun demoteMember(uid: String, communityId: String): Resource<String> {
        return try {
            val memberRef = db.collection("communities").document(communityId).collection("members")
                .document(uid)
            val joinedCommunityRef =
                db.collection("users").document(uid).collection("joinedCommunities")
                    .document(communityId)
            memberRef.update("rolePriority", 2).await()
            joinedCommunityRef.update("rolePriority", 2).await()
            Resource.Success(data = uid)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(message = e.message.toString())
        }
    }

    override suspend fun updateField(
        documentRef: DocumentReference,
        fieldName: String,
        data: Any?
    ): Resource<Any> {
        return try {
            documentRef.update(fieldName, data ?: FieldValue.delete()).await()
            Resource.Success(data = data ?: "null")
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(message = e.localizedMessage ?: "Unknown error")
        }
    }

    override suspend fun checkIfUserLikedPost(postRef: DocumentReference, uid: String): Boolean {
        return try {
            val result = postRef.collection("likes").whereEqualTo("uid", uid).get().await()
            !result.isEmpty
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            false
        }
    }

    override suspend fun countDocumentsWithoutResource(query: Query): Int {
        return try {
            val snapshot = query.count().get(AggregateSource.SERVER).await()
            snapshot.count.toInt()
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            0
        }
    }

    override suspend fun addItemIntoListInDocument(
        documentRef: DocumentReference,
        fieldName: String,
        data: Any
    ): Resource<Boolean> {
        return try {
            documentRef.update(fieldName, FieldValue.arrayUnion(data)).await()
            Resource.Success(data = true)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(message = e.message.toString())
        }

    }

    override fun getCollectionWithListener(collectionRef: CollectionReference): Flow<Resource<QuerySnapshot>> =
        callbackFlow {
            val listener = collectionRef
                .addSnapshotListener { querySnapshot, error ->
                    if (error != null) {
                        FirebaseCrashlytics.getInstance().recordException(error)
                        trySend(Resource.Error(message = error.message.toString())).isSuccess
                    } else {
                        if (querySnapshot != null) {
                            trySend(Resource.Success(data = querySnapshot)).isSuccess
                        }
                    }
                }
            awaitClose {
                listener.remove()
            }
        }

    override fun getDocumentWithListener(docRef: DocumentReference): Flow<Resource<DocumentSnapshot?>> =
        callbackFlow {
            val listener = docRef.addSnapshotListener { docSnapshot, error ->
                if (error != null) {
                    FirebaseCrashlytics.getInstance().recordException(error)
                    trySend(Resource.Error(message = error.message.toString())).isSuccess
                } else {
                    trySend(Resource.Success(data = docSnapshot)).isSuccess
                }
            }
            awaitClose {
                listener.remove()
            }
        }


}
package com.klavs.bindle.data.datasource.firestore

import android.net.Uri
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
import com.klavs.bindle.data.entity.JoinedCommunities
import com.klavs.bindle.data.entity.Member
import com.klavs.bindle.data.entity.PagedData
import com.klavs.bindle.data.entity.User
import com.klavs.bindle.resource.Resource
import kotlinx.coroutines.flow.Flow

interface FirestoreDataSource {
    suspend fun checkUniqueUsername(username: String, myUid: String? = null): Resource<Boolean>
    suspend fun checkUniqueEmail(email: String): Resource<Boolean>
    suspend fun registerUser(userModel: User): Resource<Boolean>
    suspend fun updateProfilePictureUri(newProfilePictureUri: Uri?): Resource<Boolean>
    suspend fun getUserData(uid:String): Resource<User>
    suspend fun updateUserData(uid:String, newUser: HashMap<String, Any?>): Resource<Boolean>
    suspend fun addItemIntoListInDocument(documentRef: DocumentReference,fieldName:String, data: Any): Resource<Boolean>
    fun getCollectionWithListener(collectionRef: CollectionReference): Flow<Resource<QuerySnapshot>>
    fun getDocumentWithListener(docRef: DocumentReference): Flow<Resource<DocumentSnapshot?>>
    suspend fun addDocument(documentName: String? = null, collectionRef: CollectionReference, data: Any): Resource<String>
    suspend fun getDocument(docRef: DocumentReference, source: Source = Source.DEFAULT): Resource<DocumentSnapshot?>
    suspend fun addItemToMapField(documentRef: DocumentReference, fieldName: String, data: Any): Resource<Boolean>
    fun memberCheck(collectionRef: CollectionReference, fieldName: String, value: String): Flow<Resource<Boolean>>
    fun countDocuments(query: Query): Flow<Resource<Int>>
    suspend fun getDocumentsWithPaging(query: Query, pageSize: Long, lastDocument: DocumentSnapshot? = null): Resource<PagedData>
    fun listenToNewDoc(query: Query): Flow<Resource<QuerySnapshot>>
    suspend fun deleteDocument(documentRef: DocumentReference): Resource<String>
    suspend fun acceptJoiningRequestForCommunity(member: Member, community: JoinedCommunities): Resource<String>
    suspend fun removeMember(uid: String, communityId:String): Resource<String>
    suspend fun promoteMember(uid: String, communityId: String): Resource<String>
    suspend fun demoteMember(uid: String, communityId: String): Resource<String>
    suspend fun updateField(documentRef: DocumentReference, fieldName: String, data: Any?): Resource<Any>
    suspend fun checkIfUserLikedPost(postRef: DocumentReference, uid: String): Boolean
    suspend fun countDocumentsWithoutResource(query: Query): Int
}
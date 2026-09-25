package com.klavs.bindle.data.repo.firestore

import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
import com.klavs.bindle.data.datasource.firestore.FirestoreDataSource
import com.klavs.bindle.data.entity.Event
import com.klavs.bindle.data.entity.User
import com.klavs.bindle.data.entity.community.Community
import com.klavs.bindle.resource.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirestoreRepositoryImpl @Inject constructor(val ds: FirestoreDataSource) :
    FirestoreRepository {
    override suspend fun checkUniqueUsername(username: String, myUid: String?): Resource<Boolean> =
        withContext(Dispatchers.IO) { ds.checkUniqueUsername(username) }

    override suspend fun checkUniqueEmail(email: String): Resource<Boolean> =
        withContext(Dispatchers.IO) { ds.checkUniqueEmail(email) }

    override suspend fun registerUser(userModel: User): Resource<Boolean> =
        withContext(Dispatchers.IO) { ds.registerUser(userModel) }

    override suspend fun updateProfilePictureUri(newProfilePictureUri: Uri?): Resource<Boolean> =
        withContext(Dispatchers.IO) { ds.updateProfilePictureUri(newProfilePictureUri = newProfilePictureUri) }

    override suspend fun getUserDataFlow(uid: String): Flow<Resource<User>> =
         ds.getUserDataFlow(uid).flowOn(Dispatchers.IO)

    override suspend fun getUserData(uid: String): Resource<User> =
        withContext(Dispatchers.IO){ds.getUserData(
            uid = uid
        )}

    override suspend fun updateUserData(
        uid: String,
        newUser: HashMap<String, Any?>
    ): Resource<Boolean> = withContext(Dispatchers.IO) { ds.updateUserData(uid, newUser) }


    override suspend fun addDocument(
        documentName: String?,
        collectionRef: CollectionReference,
        data: Any
    ): Resource<String> =
        withContext(Dispatchers.IO) { ds.addDocument(documentName, collectionRef, data) }

    override suspend fun getDocument(
        docRef: DocumentReference,
        source: Source
    ): Resource<DocumentSnapshot> = withContext(Dispatchers.IO) { ds.getDocument(docRef, source) }

    override suspend fun getCollection(query: Query, source: Source): Resource<QuerySnapshot> =
        withContext(Dispatchers.IO) { ds.getCollection(query, source) }

    override suspend fun addItemToMapField(
        documentRef: DocumentReference,
        fieldName: String,
        data: Any
    ): Resource<Boolean> =
        withContext(Dispatchers.IO) { ds.addItemToMapField(documentRef, fieldName, data) }

    override fun memberCheck(
        collectionRef: CollectionReference,
        fieldName: String,
        value: String
    ): Flow<Resource<Boolean>> =
        ds.memberCheck(collectionRef = collectionRef, fieldName = fieldName, value = value)
            .flowOn(Dispatchers.IO)


    override fun countDocuments(query: Query): Flow<Resource<Int>> =
        ds.countDocuments(query).flowOn(Dispatchers.IO)

    override suspend fun getDocumentsWithPaging(
        query: Query,
        pageSize: Long,
        lastDocument: DocumentSnapshot?
    ): Resource<QuerySnapshot> = withContext(Dispatchers.IO){ds.getDocumentsWithPaging(query, pageSize, lastDocument)}

    override fun listenToNewDoc(
        query: Query
    ): Flow<Resource<DocumentSnapshot?>> =
        ds.listenToNewDoc(query).flowOn(Dispatchers.IO)

    override suspend fun deleteDocument(documentRef: DocumentReference): Resource<String> =
        withContext(Dispatchers.IO){ds.deleteDocument(documentRef)}

    override suspend fun acceptJoiningRequestForCommunity(
        uid: String,
        communityId: String
    ): Resource<String> = withContext(Dispatchers.IO){ds.acceptJoiningRequestForCommunity(uid, communityId)}

    override suspend fun acceptJoiningRequestForEvent(
        uid: String,
        eventId: String,
        ownerUid: String
    ): Resource<String> =
        withContext(Dispatchers.IO){ds.acceptJoiningRequestForEvent(
            uid = uid,
            eventId = eventId,
            ownerUid = ownerUid
        )}

    override suspend fun removeMember(uid: String, communityId: String): Resource<String> =
        withContext(Dispatchers.IO){ds.removeMember(uid = uid, communityId = communityId)}

    override suspend fun promoteMember(uid: String, communityId: String): Resource<String> =
        withContext(Dispatchers.IO){ds.promoteMember(uid = uid, communityId = communityId)}

    override suspend fun demoteMember(uid: String, communityId: String): Resource<String> =
        withContext(Dispatchers.IO){ds.demoteMember(uid = uid, communityId = communityId)}

    override suspend fun updateField(
        documentRef: DocumentReference,
        fieldName: String,
        data: Any?
    ): Resource<Any?> =
        withContext(Dispatchers.IO){ds.updateField(documentRef, fieldName, data)}

    override suspend fun checkIfUserLikedPost(postRef: DocumentReference, uid: String): Boolean =
        withContext(Dispatchers.IO){ds.checkIfUserLikedPost(postRef, uid)}

    override suspend fun countDocumentsWithoutResource(query: Query): Int? =
        withContext(Dispatchers.IO){ds.countDocumentsWithoutResource(query)}

    override suspend fun createEvent(event: Event, newTickets: Long): Resource<Event> =
        withContext(Dispatchers.IO){ds.createEvent(event, newTickets)}

    override suspend fun getEvent(eventId: String): Resource<Event> =
        withContext(Dispatchers.IO){ds.getEvent(eventId)}

    override suspend fun getEvents(query: Query, listSize: Int?): Resource<List<Event>> =
        withContext(Dispatchers.IO){ds.getEvents(query, listSize)}

    override suspend fun refundTicket(uid: String, amount: Long): Resource<String> =
        withContext(Dispatchers.IO){ds.refundTicket(
            uid = uid,
            amount = amount
        )}

    override suspend fun searchDocumentByFieldNameStartWith(
        query: Query,
        field: String,
        startWith: String
    ): Resource<QuerySnapshot> = withContext(Dispatchers.IO){ds.searchDocumentByFieldNameStartWith(
        query = query,
        field = field,
        startWith = startWith
    )}

    override suspend fun getEventsNearMe(latLng: LatLng): Resource<List<Event>> =
        withContext(Dispatchers.IO){ds.getEventsNearMe(
            latLng = latLng
        )}

    override suspend fun getSuggestedCommunities(limit: Int, lastDoc: DocumentSnapshot?): Resource<Pair<List<Community>, DocumentSnapshot?>> =
        withContext(Dispatchers.IO){ds.getSuggestedCommunities(
            limit = limit,
            lastDoc = lastDoc
        )}


    override suspend fun addItemIntoListInDocument(
        documentRef: DocumentReference,
        fieldName: String,
        data: Any
    ): Resource<Boolean> =
        withContext(Dispatchers.IO) { ds.addItemIntoListInDocument(documentRef, fieldName, data) }

    override fun getCollectionWithListener(query: Query): Flow<Resource<QuerySnapshot>> =
        ds.getCollectionWithListener(query).flowOn(Dispatchers.IO)

    override fun getDocumentWithListener(docRef: DocumentReference): Flow<Resource<DocumentSnapshot?>> =
        ds.getDocumentWithListener(docRef = docRef).flowOn(Dispatchers.IO)
}
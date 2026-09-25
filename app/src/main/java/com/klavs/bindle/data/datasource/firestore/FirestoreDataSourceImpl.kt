package com.klavs.bindle.data.datasource.firestore

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Query.Direction
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
import com.klavs.bindle.R
import com.klavs.bindle.data.entity.sealedclasses.CommunityRoles
import com.klavs.bindle.data.entity.Event
import com.klavs.bindle.data.entity.Member
import com.klavs.bindle.data.entity.User
import com.klavs.bindle.data.entity.community.Community
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
            val usersRef = if (myUid != null) db.collection("users")
                .whereNotEqualTo(FieldPath.documentId(), myUid)
                .whereEqualTo("username", username)
            else db.collection("users").whereEqualTo("userName", username)
            val result =
                usersRef.limit(1).get(Source.SERVER)
                    .await()
            if (result.isEmpty) Resource.Success(true) else Resource.Success(false)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(R.string.something_went_wrong)
        }
    }

    override suspend fun checkUniqueEmail(email: String): Resource<Boolean> {
        return try {
            val result =
                db.collection("users").whereEqualTo("email", email).limit(1).get(Source.SERVER)
                    .await()
            if (result.isEmpty) Resource.Success(true) else Resource.Success(false)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(R.string.something_went_wrong)
        }
    }

    override suspend fun registerUser(userModel: User): Resource<Boolean> {
        return try {
            Log.e("greeting", "registerUser worked")
            db.collection("users").document(userModel.uid!!).set(userModel).await()
            val profileUpdates = userProfileChangeRequest {
                displayName = userModel.userName
                val photo = userModel.profilePictureUrl
                if (photo != null) {
                    photoUri = photo.toUri()
                }
            }
            auth.currentUser!!.updateProfile(profileUpdates).await()
            auth.currentUser!!.reload().await()
            Resource.Success(true)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(R.string.something_went_wrong)
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
                Resource.Error(R.string.something_went_wrong)
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(R.string.something_went_wrong)
        }
    }

    override suspend fun getUserDataFlow(uid: String): Flow<Resource<User>> = callbackFlow {
        val listenerRegistration =
            db.collection("users").document(uid).addSnapshotListener { docSnapshot, error ->
                if (error != null) {
                    Log.e("error from datasource", "User data is null")
                    trySend(Resource.Error(messageResource = R.string.something_went_wrong)).isSuccess
                } else if (docSnapshot != null) {
                    if (docSnapshot.exists()) {
                        val user = docSnapshot.toObject(User::class.java)
                        if (user != null) {
                            trySend(Resource.Success(data = user))
                        } else {
                            FirebaseCrashlytics.getInstance()
                                .recordException(Exception("User data is null"))
                            Log.e("error from datasource", "User data is null")
                            trySend(Resource.Error(messageResource = R.string.something_went_wrong))
                        }
                    } else {
                        Log.e("error from datasource", "User does not exist")
                        trySend(Resource.Error(messageResource = R.string.user_does_not_exist))
                    }
                } else {
                    FirebaseCrashlytics.getInstance()
                        .recordException(Exception("User data is null"))
                    Log.e("error from datasource", "User data is null")
                    trySend(Resource.Error(messageResource = R.string.something_went_wrong))
                }
            }
        awaitClose {
            listenerRegistration.remove()
        }
    }

    override suspend fun getUserData(uid: String): Resource<User> {
        return try {
            val userRef = db.collection("users").document(uid)
            val userSnapshot = userRef.get().await()
            if (userSnapshot.exists()) {
                val userObject = userSnapshot.toObject(User::class.java)
                if (userSnapshot != null) {
                    Resource.Success(data = userObject!!)
                } else {
                    FirebaseCrashlytics.getInstance()
                        .recordException(Exception("User data is null"))
                    Resource.Error(messageResource = R.string.something_went_wrong)
                }
            } else {
                Resource.Error(messageResource = R.string.user_does_not_exist)
            }
        } catch (e: Exception) {
            Log.e("error from datasource", e.message ?: "")
            FirebaseCrashlytics.getInstance()
                .recordException(e)
            Resource.Error(R.string.something_went_wrong)
        }
    }

    override suspend fun updateUserData(
        uid: String,
        newUser: HashMap<String, Any?>
    ): Resource<Boolean> {
        return try {
            db.collection("users").document(uid).update(newUser).await()
            if (newUser.containsKey("userName")) {
                val profileChangeRequest = userProfileChangeRequest {
                    displayName = (newUser["userName"] as? String) ?: ""
                }
                auth.currentUser?.updateProfile(profileChangeRequest)?.await()
                auth.currentUser?.reload()?.await()
            }
            Resource.Success(data = true)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("error from datasource", e.message ?: "")
            Resource.Error(R.string.something_went_wrong)
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
            Log.e("error from datasource", e.message ?: "")
            Resource.Error(messageResource = R.string.something_went_wrong)
        }
    }

    override suspend fun getDocument(
        docRef: DocumentReference,
        source: Source
    ): Resource<DocumentSnapshot> {
        return try {
            val document = docRef.get(source).await()
            Resource.Success(data = document)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("error from datasource", e.message ?: "")
            Resource.Error(messageResource = R.string.something_went_wrong)
        }

    }

    override suspend fun getCollection(query: Query, source: Source): Resource<QuerySnapshot> {
        return try {
            val snapshot = query.get(source).await()
            Resource.Success(data = snapshot)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("error from datasource", e.message ?: "")
            Resource.Error(messageResource = R.string.something_went_wrong)
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
            Resource.Error(messageResource = R.string.something_went_wrong)
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
                    trySend(Resource.Error(messageResource = R.string.something_went_wrong)).isSuccess
                } else {
                    if (snapshot != null && !snapshot.isEmpty) {
                        trySend(Resource.Success(data = true)).isSuccess
                    } else {
                        trySend(Resource.Success(data = false)).isSuccess
                    }
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
            emit(Resource.Error(messageResource = R.string.something_went_wrong))
        }
    }

    override suspend fun getDocumentsWithPaging(
        query: Query,
        pageSize: Long,
        lastDocument: DocumentSnapshot?
    ): Resource<QuerySnapshot> {
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
                    data = snapshot
                )
            } else {
                Resource.Error(messageResource = R.string.something_went_wrong)
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("error from datasource", e.message ?: "")
            Resource.Error(messageResource = R.string.something_went_wrong)
        }
    }

    override fun listenToNewDoc(
        query: Query
    ): Flow<Resource<DocumentSnapshot?>> = callbackFlow {
        val listener =
            query.limit(1).addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    Log.e("error from datasource", error.message.toString())
                    trySend(Resource.Error(messageResource = R.string.something_went_wrong))
                } else {
                    if (querySnapshot != null) {
                        trySend(Resource.Success(data = if (querySnapshot.isEmpty) null else querySnapshot.documents[0]))
                        Log.e(
                            "events",
                            "yeni mesaj: ${querySnapshot.documents[0].getString("message")}"
                        )
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
            Resource.Error(messageResource = R.string.something_went_wrong)
        }
    }

    override suspend fun acceptJoiningRequestForCommunity(
        uid: String,
        communityId: String
    ): Resource<String> {
        return try {
            val requestRef =
                db.collection("communities").document(communityId).collection("joiningRequests")
                    .document(uid)
            val memberRef =
                db.collection("communities").document(communityId).collection("members")
                    .document(uid)
            val batch = db.batch()
            batch.delete(requestRef)
            val memberModel = Member(
                uid = uid,
                rolePriority = CommunityRoles.Member.rolePriority,
            )
            batch.set(memberRef, memberModel)
            batch.commit().await()
            Resource.Success(data = uid)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(messageResource = R.string.something_went_wrong)
        }
    }

    override suspend fun acceptJoiningRequestForEvent(
        uid: String,
        eventId: String,
        ownerUid: String
    ): Resource<String> {
        return try {
            val requestRef =
                db.collection("events").document(eventId).collection("requests")
                    .document(uid)
            val participantsRef =
                db.collection("events").document(eventId).collection("participants")
            requestRef.delete().await()
            participantsRef.document(uid)
                .set(
                    hashMapOf(
                        "uid" to ownerUid,
                        "date" to null
                    )
                ).await()
            Resource.Success(data = uid)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(messageResource = R.string.something_went_wrong)
        }
    }

    override suspend fun removeMember(uid: String, communityId: String): Resource<String> {
        return try {
            val memberRef = db.collection("communities").document(communityId).collection("members")
                .document(uid)
            memberRef.delete().await()
            Resource.Success(data = uid)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(messageResource = R.string.something_went_wrong)
        }
    }

    override suspend fun promoteMember(uid: String, communityId: String): Resource<String> {
        return try {
            val memberRef = db.collection("communities").document(communityId).collection("members")
                .document(uid)
            memberRef.update("rolePriority", 1).await()
            Resource.Success(data = uid)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(messageResource = R.string.something_went_wrong)
        }
    }

    override suspend fun demoteMember(uid: String, communityId: String): Resource<String> {
        return try {
            val memberRef = db.collection("communities").document(communityId).collection("members")
                .document(uid)
            memberRef.update("rolePriority", 2).await()
            Resource.Success(data = uid)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(messageResource = R.string.something_went_wrong)
        }
    }

    override suspend fun updateField(
        documentRef: DocumentReference,
        fieldName: String,
        data: Any?
    ): Resource<Any?> {
        return try {
            documentRef.update(fieldName, data ?: FieldValue.delete()).await()
            Resource.Success(data = data)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("error from datasource", e.message ?: "")
            Resource.Error(messageResource = R.string.something_went_wrong)
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

    override suspend fun countDocumentsWithoutResource(query: Query): Int? {
        return try {
            val snapshot = query.count().get(AggregateSource.SERVER).await()
            snapshot.count.toInt()
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            null
        }
    }

    override suspend fun createEvent(event: Event, newTickets: Long): Resource<Event> {
        return try {
            val eventRef = db.collection("events")
            val eventDoc = eventRef.add(event).await()
            val participantRef =
                eventRef.document(eventDoc.id).collection("participants").document(event.ownerUid)
            val batch = db.batch()
            batch.set(
                participantRef, hashMapOf(
                    "uid" to event.ownerUid,
                    "date" to null
                )
            )
            val userRef = db.collection("users").document(event.ownerUid)
            batch.update(userRef, "tickets", newTickets)
            batch.commit().await()
            Resource.Success(data = event.copy(id = eventDoc.id))
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(messageResource = R.string.something_went_wrong)
        }
    }

    override suspend fun getEvent(eventId: String): Resource<Event> {
        return try {
            val eventRef = db.collection("events").document(eventId)
            val eventSnapshot = eventRef.get().await()
            if (eventSnapshot.exists() && eventSnapshot.data != null) {
                val eventObject = eventSnapshot.toObject(Event::class.java)?.copy(id = eventId)
                if (eventObject != null) {
                    Resource.Success(
                        data = eventSnapshot.toObject(Event::class.java)?.copy(id = eventId)!!
                    )
                } else {
                    Resource.Error(messageResource = R.string.event_does_not_exist)
                }
            } else {
                Resource.Error(messageResource = R.string.something_went_wrong)
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(messageResource = R.string.something_went_wrong)
        }
    }

    override suspend fun getEvents(query: Query, listSize: Int?): Resource<List<Event>> {
        return try {
            val eventList = mutableListOf<Event>()
            val querySnapshot =
                listSize?.let { query.limit(listSize.toLong()).get().await() } ?: query.get()
                    .await()
            if (querySnapshot.isEmpty) {
                Resource.Success(data = emptyList())
            } else {
                querySnapshot.forEach { eventSnapshot ->
                    eventList.add(
                        eventSnapshot.toObject(Event::class.java).copy(id = eventSnapshot.id)
                    )
                }
                Resource.Success(data = eventList)
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("error from datasource", "${e.message}")
            Resource.Error(messageResource = R.string.something_went_wrong)
        }
    }

    override suspend fun refundTicket(uid: String, amount: Long): Resource<String> {
        return try {
            val userRef = db.collection("users").document(uid)
            val userDoc = userRef.get(Source.SERVER).await()
            if (userDoc.exists()) {
                val userTickets = userDoc.getLong("tickets")
                if (userTickets != null) {
                    userRef.update("tickets", userTickets + amount).await()
                    Resource.Success(data = uid)
                } else {
                    FirebaseCrashlytics.getInstance().log("user tickets not exist: $uid")
                    Resource.Error(messageResource = R.string.something_went_wrong)
                }
            } else {
                FirebaseCrashlytics.getInstance().log("user not exist: $uid")
                Resource.Error(messageResource = R.string.something_went_wrong)
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(messageResource = R.string.something_went_wrong)
        }
    }

    override suspend fun searchDocumentByFieldNameStartWith(
        query: Query,
        field: String,
        startWith: String
    ): Resource<QuerySnapshot> {
        return try {
            val ref = query.whereGreaterThanOrEqualTo(field, startWith)
                .whereLessThanOrEqualTo(field, "$startWith\uF8FF")
            val snapshot = ref.get().await()
            Resource.Success(snapshot)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("error from datasource", e.message ?: "unknown error")
            Resource.Error(messageResource = R.string.something_went_wrong)
        }
    }

    override suspend fun getEventsNearMe(latLng: LatLng): Resource<List<Event>> {
        return try {
            val foundEvents = mutableListOf<DocumentSnapshot>()
            val rangeList = listOf(
                0.04,
                1.0,
                2.5,
                360.0
            )

            for (range in rangeList) {
                val minLatitude = latLng.latitude - range
                val minLongitude = latLng.longitude - range
                val maxLatitude = latLng.latitude + range
                val maxLongitude = latLng.longitude + range

                var eventsRef: Query = db.collection("events")

                if (foundEvents.isNotEmpty()) {
                    eventsRef =
                        eventsRef.whereNotIn(FieldPath.documentId(), foundEvents.map { it.id })
                }
                eventsRef = eventsRef.whereGreaterThan("date", Timestamp.now())
                    .whereEqualTo("privateEvent", false)
                    .whereGreaterThan("latitude", minLatitude)
                    .whereLessThan("latitude", maxLatitude)
                    .whereGreaterThan("longitude", minLongitude)
                    .whereLessThan("longitude", maxLongitude)
                    .limit(3L - foundEvents.size)

                val eventsSnapshot = eventsRef.get().await()

                if (!eventsSnapshot.isEmpty) {
                    foundEvents.addAll(eventsSnapshot.documents.filter { it.exists() })
                }
                if (foundEvents.size >= 3) {
                    break
                }
            }


            if (foundEvents.isEmpty()) {
                return Resource.Success(data = emptyList())
            }

            val events = foundEvents.mapNotNull { documentSnapshot ->
                documentSnapshot.toObject(Event::class.java)?.copy(id = documentSnapshot.id)
            }
            Resource.Success(data = events)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("error from datasource", e.message ?: "unknown error")
            Resource.Error(messageResource = R.string.something_went_wrong)
        }
    }

    override suspend fun getSuggestedCommunities(
        limit: Int,
        lastDoc: DocumentSnapshot?
    ): Resource<Pair<List<Community>, DocumentSnapshot?>> {
        return try {
            var query = db.collection("communities").orderBy(
                "numOfMembers",
                Direction.DESCENDING
            ).limit(limit.toLong())

            if (lastDoc != null) {
                query = query.startAfter(lastDoc)
            }

            val querySnapshot = query.get().await()
            val communities = querySnapshot
                .filter { it.exists() }
                .mapNotNull {
                    it.toObject(Community::class.java).copy(id = it.id)
                }
            Resource.Success(data = communities to querySnapshot.lastOrNull())
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("error from datasource", e.message ?: "unknown error")
            Resource.Error(messageResource = R.string.something_went_wrong)
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
            Log.e("error from datasource", e.message ?: "unknown error")
            Resource.Error(messageResource = R.string.something_went_wrong)
        }

    }

    override fun getCollectionWithListener(query: Query): Flow<Resource<QuerySnapshot>> =
        callbackFlow {
            val listener = query
                .addSnapshotListener { querySnapshot, error ->
                    if (error != null) {
                        FirebaseCrashlytics.getInstance().recordException(error)
                        Log.e("error from datasource", error.message ?: "unknown error")
                        trySend(Resource.Error(messageResource = R.string.something_went_wrong))
                    } else {
                        if (querySnapshot != null) {
                            Log.e("error from datasource", "new item")
                            trySend(Resource.Success(data = querySnapshot))
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
                    Log.e("error from datasource", error.message ?: "unknown error")
                    trySend(Resource.Error(messageResource = R.string.something_went_wrong))
                } else {
                    if (docSnapshot != null) {
                        if (docSnapshot.exists()) {
                            trySend(Resource.Success(data = docSnapshot)).isSuccess
                        } else {
                            trySend(Resource.Error(messageResource = R.string.something_went_wrong))
                        }
                    } else {
                        trySend(Resource.Error(messageResource = R.string.something_went_wrong))
                    }
                }
            }
            awaitClose {
                listener.remove()
            }
        }
}
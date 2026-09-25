package com.klavs.bindle.data.datasource.auth

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.klavs.bindle.R
import com.klavs.bindle.data.entity.User
import com.klavs.bindle.resource.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthDataSourceImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage
) : AuthDataSource {
    override suspend fun loginUser(email: String, password: String): Resource<AuthResult> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Resource.Success(result)
        } catch (e: Exception) {
            Log.e("error from datasource", e.message ?: "unknown error")
            if (e is FirebaseAuthInvalidCredentialsException) {
                Resource.Error(R.string.signed_in_user_not_found)
            } else {
                FirebaseCrashlytics.getInstance().recordException(e)
                Resource.Error(R.string.something_went_wrong_try_again_later)
            }
        }
    }

    override suspend fun createUserWithEmailAndPassword(
        email: String,
        password: String
    ): Resource<AuthResult> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            Resource.Success(result)
        } catch (e: FirebaseAuthUserCollisionException) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(R.string.email_already_exists)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(R.string.something_went_wrong)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Resource<AuthResult> {
        return try {
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(firebaseCredential).await()
            Resource.Success(data = result)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(R.string.cannot_signed_in)
        }
    }

    override suspend fun reloadUserInformation(): Resource<Boolean> {
        return try {

            val currentUser = auth.currentUser
            if (currentUser != null) {
                val userDocRef = db.collection("users").document(currentUser.uid)
                val userDocSnapshot =
                    userDocRef.get(Source.SERVER).await()
                if (userDocSnapshot.exists()) {
                    if (userDocSnapshot != null) {
                        val userChangeRequest = userProfileChangeRequest {
                            displayName = userDocSnapshot.get("userName") as? String
                            photoUri =
                                (userDocSnapshot.get("profilePictureUrl") as? String)?.toUri()
                        }
                        currentUser.updateProfile(userChangeRequest).await()
                        currentUser.reload().await()
                        Resource.Success(data = true)
                    } else {
                        Resource.Error(messageResource = R.string.something_went_wrong)
                    }
                } else {
                    val userData = User(
                        uid = currentUser.uid,
                        email = currentUser.email?:"",
                        userName = currentUser.displayName ?: "",
                        profilePictureUrl = currentUser.photoUrl?.toString(),
                        phoneNumber = currentUser.phoneNumber,
                        tickets = 5,
                        acceptedTermsAndPrivacyPolicy = false
                    )
                    userDocRef.set(userData).await()
                    Resource.Success(data = true)
                }
            } else {
                Resource.Error(messageResource = R.string.something_went_wrong)
            }


        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(R.string.something_went_wrong)
        }
    }

    override suspend fun checkIfUserExists(email: String): Resource<Boolean> {
        return try {
            Log.e("greeting", "checkIfUserExists worked")
            val currentUser = auth.currentUser
            val hasEmailPassword =
                currentUser!!.providerData.any { it.providerId == EmailAuthProvider.PROVIDER_ID }

            if (hasEmailPassword) {
                Resource.Success(data = true)
            } else {
                Resource.Success(data = false)
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(R.string.something_went_wrong)
        }
    }


    override suspend fun signOut(uid: String) {
        val userRef = db.collection("users").document(uid)
        try {
            auth.signOut()
            userRef.update("fcmToken", null).await()
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("error from datasource", e.message ?: "unknown error")
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Resource<Boolean> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Resource.Success(true)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(R.string.email_cannot_be_sent)
        }
    }

    override suspend fun sendEmailVerification(): Resource<Boolean> {
        return try {
            auth.currentUser?.sendEmailVerification()?.await()
            Resource.Success(true)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(R.string.email_cannot_be_sent)
        }
    }

    override suspend fun updatePassword(
        currentPassword: String,
        newPassword: String
    ): Resource<Boolean> {
        return try {
            if (auth.currentUser != null) {
                val credential =
                    EmailAuthProvider.getCredential(auth.currentUser!!.email!!, currentPassword)
                auth.currentUser!!.reauthenticate(credential).await()
                auth.currentUser!!.updatePassword(newPassword).await()
                Resource.Success(data = true)
            } else {
                Resource.Error(messageResource = R.string.something_went_wrong)
            }

        } catch (e: Exception) {
            if (e is FirebaseAuthInvalidCredentialsException){
                Log.e("error from datasource", e.toString())
                return Resource.Error(messageResource = R.string.wrong_password_please_check_it)
            }else {
                Log.e("error from datasource", e.toString())
                FirebaseCrashlytics.getInstance().recordException(e)
                Resource.Error(messageResource = R.string.something_went_wrong)
            }
        }
    }

    override suspend fun updateEmail(password: String, newEmail: String): Resource<Boolean> {
        return try {
            val credential =
                EmailAuthProvider.getCredential(auth.currentUser!!.email!!, password)
            auth.currentUser!!.reauthenticate(credential).await()
            auth.currentUser!!.verifyBeforeUpdateEmail(newEmail).await()
            Resource.Success(true)
        } catch (e: Exception) {
            if (e is FirebaseAuthInvalidCredentialsException){
                Log.e("error from datasource", e.toString())
                return Resource.Error(messageResource = R.string.wrong_password_please_check_it)
            }else {
                Log.e("error from datasource", e.toString())
                FirebaseCrashlytics.getInstance().recordException(e)
                Resource.Error(messageResource = R.string.something_went_wrong)
            }
        }
    }

    override suspend fun updateUserPhotoUrl(imageUri: Uri?): Resource<Boolean> {
        return try {
            val profileChangeRequest = userProfileChangeRequest {
                photoUri = imageUri
            }
            if (auth.currentUser != null) {
                auth.currentUser!!.updateProfile(profileChangeRequest).await()
                auth.currentUser!!.reload().await()
                Resource.Success(true)
            } else {
                Resource.Error(messageResource = R.string.something_went_wrong)
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(messageResource = R.string.something_went_wrong)
        }
    }

    override fun getCurrentUser(): Flow<FirebaseUser?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(authStateListener)
        awaitClose {
            auth.removeAuthStateListener(authStateListener)
        }
    }

    override suspend fun deleteAccount(user: FirebaseUser, password: String): Resource<Unit> {
        val uid = user.uid
        return try {
            val credential =
                EmailAuthProvider.getCredential(auth.currentUser!!.email!!, password)
            user.reauthenticate(credential).await()
            val userRef = db.collection("users").document(uid)
            userRef.delete().await()
            user.delete().await()
            val photoRef = storage.reference.child("profilePictures/$uid")
            photoRef.delete().await()
            FirebaseCrashlytics.getInstance().log("An account has been deleted: $uid")
            Resource.Success(data = Unit)
        } catch (e: Exception) {
            if (e is StorageException && e.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                Log.e(
                    "error from datasource",
                    "Belirtilen referans bulunamadı fakat yine de başarılı: ${e.message}"
                )
                Resource.Success(data = Unit)
            }else if (e is FirebaseAuthInvalidCredentialsException){
                Log.e("error from datasource", e.toString())
                Resource.Error(messageResource = R.string.wrong_password)
            } else {
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e("error from datasource", e.message ?: "unknown error")
                Resource.Error(messageResource = R.string.something_went_wrong)
            }
        }
    }

}
package com.klavs.bindle.data.datasource.auth

import android.net.Uri
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.klavs.bindle.resource.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthDataSourceImpl @Inject constructor(private val auth: FirebaseAuth) : AuthDataSource {
    override suspend fun loginUser(email: String, password: String): Resource<AuthResult> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Resource.Success(result)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(e.localizedMessage ?: "An error occurred")
        }
    }

    override suspend fun registerUser(email: String, password: String): Resource<AuthResult> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            Resource.Success(result)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(e.localizedMessage ?: "An error occurred")
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun sendPasswordResetEmail(email: String): Resource<Boolean> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Resource.Success(true)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(e.localizedMessage ?: "unknown error")
        }
    }

    override suspend fun sendEmailVerification(): Resource<Boolean> {
        return try {
            auth.currentUser?.sendEmailVerification()?.await()
            Resource.Success(true)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(e.localizedMessage ?: "unknown error")
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
                Resource.Error(message = "user not found")
            }

        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(message = e.localizedMessage ?: "unknown error")
        }
    }

    override suspend fun updateEmail(password: String, newEmail: String): Resource<Boolean> {
        return try {
            val credential =
                EmailAuthProvider.getCredential(auth.currentUser!!.email!!, password)
            auth.currentUser!!.reauthenticate(credential).await()
            auth.currentUser!!.verifyBeforeUpdateEmail(newEmail).await()
            Resource.Success(true)
        }
        catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(message = e.localizedMessage ?: "unknown error")
        }
    }

    override suspend fun updateUserPhotoUrl(imageUri: Uri?): Resource<Boolean> {
        return try {
            val profileChangeRequest = userProfileChangeRequest {
                if (imageUri != null) {
                    photoUri = imageUri
                } else {
                    photoUri = null
                }
            }
            if (auth.currentUser != null) {
                auth.currentUser!!.updateProfile(profileChangeRequest).await()
                auth.currentUser!!.reload().await()
                Resource.Success(true)
            } else {
                Resource.Error(message = "user not found")
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(message = e.localizedMessage ?: "unknown error")
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

}
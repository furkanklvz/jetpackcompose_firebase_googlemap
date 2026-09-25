package com.klavs.bindle.data.repo.auth

import android.net.Uri
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.klavs.bindle.data.datasource.auth.AuthDataSource
import com.klavs.bindle.resource.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(val ds: AuthDataSource) : AuthRepository {
    override suspend fun loginUser(email: String, password: String): Resource<AuthResult> =
        withContext(Dispatchers.IO) { ds.loginUser(email, password) }


    override suspend fun createUserWithEmailAndPassword(email: String, password: String): Resource<AuthResult> =
        withContext(Dispatchers.IO) { ds.createUserWithEmailAndPassword(email, password) }

    override suspend fun signOut(uid: String) = withContext(Dispatchers.IO){ds.signOut(uid)}
    override suspend fun signInWithGoogle(idToken: String): Resource<AuthResult> =
        withContext(Dispatchers.IO){ds.signInWithGoogle(idToken)}

    override suspend fun checkIfUserExists(email: String): Resource<Boolean> =
        withContext(Dispatchers.IO){ds.checkIfUserExists(email)}

    override suspend fun reloadUserInformation(): Resource<Boolean> =
        withContext(Dispatchers.IO){ds.reloadUserInformation()}


    override suspend fun sendPasswordResetEmail(email: String): Resource<Boolean> =
        withContext(Dispatchers.IO){ds.sendPasswordResetEmail(email)}

    override suspend fun sendEmailVerification(): Resource<Boolean> =
        withContext(Dispatchers.IO){ds.sendEmailVerification()}

    override suspend fun updatePassword(currentPassword: String, newPassword: String): Resource<Boolean> =
        withContext(Dispatchers.IO){ds.updatePassword(newPassword = newPassword, currentPassword = currentPassword)}

    override suspend fun updateEmail(password: String, newEmail: String): Resource<Boolean> =
        withContext(Dispatchers.IO){ds.updateEmail(password = password, newEmail = newEmail)}

    override suspend fun updateUserPhotoUrl(imageUri: Uri?): Resource<Boolean> =
        withContext(Dispatchers.IO){ds.updateUserPhotoUrl(imageUri)}

    override fun getCurrentUser(): Flow<FirebaseUser?> = ds.getCurrentUser().flowOn(Dispatchers.IO)
    override suspend fun deleteAccount(user: FirebaseUser, password: String): Resource<Unit> =
        withContext(Dispatchers.IO){ds.deleteAccount(user, password)}
}
package com.klavs.bindle.data.repo.storage

import android.net.Uri
import com.klavs.bindle.resource.Resource

interface StorageRepository {
    suspend fun uploadImage(imageUri: Uri, path: String, maxSize:Int): Resource<Uri>
    suspend fun deleteImage(path: String): Resource<Boolean>
}
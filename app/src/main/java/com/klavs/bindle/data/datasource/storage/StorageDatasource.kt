package com.klavs.bindle.data.datasource.storage

import android.net.Uri
import com.klavs.bindle.resource.Resource

interface StorageDatasource {
    suspend fun uploadImage(imageUri: Uri, path: String, maxSize:Int): Resource<Uri>
    suspend fun deleteImage(path: String): Resource<Boolean>
}
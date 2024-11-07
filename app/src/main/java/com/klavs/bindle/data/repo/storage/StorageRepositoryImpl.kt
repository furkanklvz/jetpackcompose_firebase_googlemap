package com.klavs.bindle.data.repo.storage

import android.net.Uri
import com.klavs.bindle.data.datasource.storage.StorageDatasource
import com.klavs.bindle.resource.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StorageRepositoryImpl @Inject constructor(val ds: StorageDatasource) : StorageRepository {
    override suspend fun uploadImage(imageUri: Uri, path: String, maxSize:Int): Resource<Uri> =
        withContext(Dispatchers.IO){ds.uploadImage(imageUri = imageUri, path = path, maxSize = maxSize)}

    override suspend fun deleteImage(path: String): Resource<Boolean> =
        withContext(Dispatchers.IO){ds.deleteImage(path = path)}
}
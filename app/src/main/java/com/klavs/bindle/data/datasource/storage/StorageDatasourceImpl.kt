package com.klavs.bindle.data.datasource.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.klavs.bindle.resource.Resource
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class StorageDatasourceImpl @Inject constructor(
    private val storage: FirebaseStorage,
    private val context: Context
) : StorageDatasource {
    override suspend fun uploadImage(imageUri: Uri, path: String, maxSize: Int): Resource<Uri> {
        return try {
            val ref = storage.reference.child(path)
            val resizedImage = resizeImage(context = context, imageUri = imageUri, maxSize = maxSize)
            val result = ref.putBytes(resizedImage).await()
            val downloadUrl = result.storage.downloadUrl.await()
            Resource.Success(data = downloadUrl)

        } catch (e: Exception) {
            Resource.Error(message = e.localizedMessage ?: "unknown error")
        }
    }

    override suspend fun deleteImage(path: String): Resource<Boolean> {
        return try {
            val ref = storage.reference.child(path)
            ref.delete().await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(message = e.localizedMessage ?: "unknown error")
        }
    }


    private fun resizeImage(context: Context, imageUri: Uri, maxSize: Int): ByteArray {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)


        var width = originalBitmap.width
        var height = originalBitmap.height
        val bitmapOrani: Double = width.toDouble() / height.toDouble()

        if (bitmapOrani > 1) {
            width = maxSize
            height = (width / bitmapOrani).toInt()
        } else {
            height = maxSize
            width = (height * bitmapOrani).toInt()
        }

        val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true)

        val exif = ExifInterface(context.contentResolver.openInputStream(imageUri)!!)
        val orientation =
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)

        val correctedBitmap = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(resizedBitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(resizedBitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(resizedBitmap, 270f)
            else -> resizedBitmap
        }

        val outputStream = ByteArrayOutputStream()
        correctedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        return outputStream.toByteArray()
    }

    fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }
}

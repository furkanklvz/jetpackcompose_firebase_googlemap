package com.klavs.bindle.uix.view

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestOptions
import com.skydoves.landscapist.glide.GlideImage

@Composable
fun GlideImageLoader(url: String, context:Context, modifier: Modifier) {
    GlideImage(
        imageModel = { url },
        requestBuilder = {
            val thumbnailRequest = Glide
                .with(context)
                .asBitmap()
                .load(url)
                .apply(RequestOptions().override(100))

            Glide
                .with(context)
                .asBitmap()
                .apply(
                    RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                )
                .thumbnail(thumbnailRequest)
                .transition(withCrossFade())
        },
        modifier = modifier
    )
}
package com.klavs.bindle.uix.view.loading

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.klavs.bindle.R

@Composable
fun LoadingAnimation(
    size: Dp = 160.dp,
    speed: Float = 1f
) {
    val composition =
        rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.loading_anim))
    LottieAnimation(
        speed = speed,
        contentScale = ContentScale.Crop,
        composition = composition.value,
        iterations = 1000,
        clipToCompositionBounds = true,
        modifier = Modifier
            .size(size)
    )
}
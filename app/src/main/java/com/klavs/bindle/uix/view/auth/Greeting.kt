package com.klavs.bindle.uix.view.auth


import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.klavs.bindle.R
import com.klavs.bindle.data.entity.sealedclasses.BottomNavItem
import com.klavs.bindle.ui.theme.logoFont
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Greeting(navController: NavHostController) {
    val pagerState = rememberPagerState(
        pageCount = {
            4
        },
        initialPage = 0
    )
    val scope = rememberCoroutineScope()
    BackHandler{}
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.logo_no_background),
                            contentDescription = "welcome",
                            modifier = Modifier
                                .padding(end = 10.dp)
                                .size(IconButtonDefaults.largeIconSize),
                            contentScale = ContentScale.Crop
                        )
                        Text(stringResource(R.string.app_name), fontFamily = logoFont)
                    }
                }

            )
        }
    ) { innerPadding ->
        Box(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (pagerState.currentPage != 0) {
                        IconButton(
                            modifier = Modifier
                                .padding(start = 5.dp)
                                .align(Alignment.CenterStart),
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }


                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
                                contentDescription = "next"
                            )
                        }
                    }
                    if (pagerState.currentPage != pagerState.pageCount - 1) {
                        TextButton(
                            modifier = Modifier
                                .padding(end = 5.dp)
                                .align(Alignment.CenterEnd),
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(stringResource(R.string.next), style = MaterialTheme.typography.titleMedium)
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                                    contentDescription = "next",
                                    modifier = Modifier.size(IconButtonDefaults.xSmallIconSize)
                                )
                            }

                        }
                    } else {
                        FilledTonalButton(
                            modifier = Modifier
                                .padding(end = 5.dp)
                                .align(Alignment.CenterEnd),
                            onClick = {
                                navController.navigate(BottomNavItem.Home.route)

                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(stringResource(R.string.begin))
                                Icon(
                                    imageVector = Icons.Filled.RocketLaunch,
                                    contentDescription = "start",
                                    modifier = Modifier.size(IconButtonDefaults.xSmallIconSize)
                                )
                            }
                        }
                    }
                }
                HorizontalPager(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth()
                        .weight(1f),
                    state = pagerState
                ) { page ->
                    Box(
                        Modifier
                            .padding(10.dp)
                            .fillMaxWidth()
                            .fillMaxHeight(0.85f)
                    ) {
                        when (page) {
                            0 -> Page1()
                            1 -> Page2()
                            2 -> Page3()
                            3 -> Page4()
                        }
                    }
                }
                Row(
                    Modifier
                        .wrapContentHeight()
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 28.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(pagerState.pageCount) { iteration ->
                        val color =
                            if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.surfaceContainerHighest
                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(16.dp)
                        )
                    }
                }

            }
        }

    }
}

@Composable
private fun Page1() {
    PageContent(
        animResource = R.raw.map_locations_anim,
        text = stringResource(R.string.greeting_page_discover_text, stringResource(R.string.app_name)),
        title = stringResource(R.string.discover)
    )

}

@Composable
private fun Page2() {
    PageContent(
        animResource = R.raw.event2_anim,
        text = stringResource(R.string.greeting_page_event_text),
        title = stringResource(R.string.events)
    )

}

@Composable
private fun Page3() {
    PageContent(
        animResource = R.raw.community_anim,
        text = stringResource(R.string.greeting_page_community_text),
        title = stringResource(R.string.communities)
    )
}

@Composable
private fun Page4() {
    PageContent(
        animResource = R.raw.account_anim,
        text = stringResource(R.string.greeting_page_account_text, stringResource(R.string.app_name)),
        title = stringResource(R.string.your_account)
    )
}

@Composable
private fun PageContent(animResource: Int, text: String, title: String){
    Column(
        Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .fillMaxHeight(0.6f)
        ) {
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animResource))
            LottieAnimation(
                composition = composition,
                iterations = Int.MAX_VALUE
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                text,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        }

    }
}

@Preview(showSystemUi = true)
@Composable
private fun GreetingPreview() {
    Greeting(navController = rememberNavController())

}
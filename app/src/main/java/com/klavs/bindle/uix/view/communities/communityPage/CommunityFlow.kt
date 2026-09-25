package com.klavs.bindle.uix.view.communities.communityPage

import android.net.Uri
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Report
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.klavs.bindle.R
import com.klavs.bindle.util.TimeFunctions
import com.klavs.bindle.data.entity.community.Community
import com.klavs.bindle.data.entity.sealedclasses.CommunityRoles
import com.klavs.bindle.data.entity.Event
import com.klavs.bindle.data.entity.Post
import com.klavs.bindle.data.entity.PostComment
import com.klavs.bindle.resource.Resource
import com.klavs.bindle.ui.theme.LightRed
import com.klavs.bindle.uix.viewmodel.communities.CommunityPageViewModel
import com.klavs.bindle.uix.viewmodel.communities.PostViewModel
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CommunityFlow(
    community: Community,
    numberOfMembers: Int,
    numOfEvents: Int,
    onMembersClick: () -> Unit,
    currentUser: FirebaseUser?,
    onEditClick: (Boolean, String) -> Unit,
    onEventClick: (Event) -> Unit,
    rolePriority: Int?,
    showSnackbar: (String) -> Unit,
    onSendRequest: () -> Unit,
    onJoinCommunity: () -> Unit,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    didISendRequest: Boolean?,
    navController: NavHostController,
    vmPost: PostViewModel,
    viewModelCommunity: CommunityPageViewModel,
    onCommentClick: (String) -> Unit,
    showReportDialog: (Post) -> Unit
) {
    val context = LocalContext.current
    var isError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()
    var refreshing by remember { mutableStateOf(false) }


    LaunchedEffect(true) {
        if (vmPost.pagedPostsState.value is Resource.Idle || vmPost.pagedPostsState.value is Resource.Error) {
            vmPost.lastPost = null
            vmPost.postList.clear()
            viewModelCommunity.getUpcomingEvents(community.id)
            if (currentUser != null) {
                vmPost.getPostsWithPaging(
                    communityId = community.id,
                    pageSize = 3,
                    myUid = currentUser.uid
                )
            }
        }
    }
    LaunchedEffect(true) {
        if (viewModelCommunity.createPostResource.value is Resource.Success) {
            vmPost.postList.add(0, viewModelCommunity.createPostResource.value.data!!)
            Log.d("communityPage", "yeni post oluşturuldu ve eklendi")
            vmPost.communityFlowListState.animateScrollToItem(1, 0)
            showSnackbar(context.getString(R.string.post_created))
            viewModelCommunity.createPostResource.value = Resource.Idle()
        }
    }

    var launched by remember { mutableStateOf(false) }
    LaunchedEffect(vmPost.pagedPostsState.value) {
        if (launched) {
            when (val state = vmPost.pagedPostsState.value) {
                is Resource.Error -> {
                    isLoading = false
                    isError = true
                    vmPost.pagedPostsState.value = Resource.Idle()
                }

                is Resource.Idle -> {
                    isLoading = false
                    isError = false
                }

                is Resource.Loading -> {
                    if (!refreshing) {
                        isError = false
                        isLoading = true
                    }
                }

                is Resource.Success -> {
                    if (refreshing) {
                        vmPost.postList.clear()
                        vmPost.postList.addAll(state.data ?: mutableListOf())
                        Log.e("communityPage", "posts updated")
                        refreshing = false
                    } else {
                        isLoading = false
                        isError = false
                        vmPost.postList.addAll(state.data ?: mutableListOf())
                        Log.e("communityPage", "new posts added")
                    }
                }
            }
        } else {
            launched = true
        }
    }
    LaunchedEffect(vmPost.deletePostState.value) {
        when (val resource = vmPost.deletePostState.value) {
            is Resource.Error -> {
                showSnackbar(resource.messageResource?.let { context.getString(it) } ?: "")
                vmPost.deletePostState.value = Resource.Idle()
            }

            is Resource.Success -> {
                vmPost.postList.remove(vmPost.postList.find { it.id == resource.data!! })
                if (vmPost.postList.isEmpty()) {
                    if (currentUser != null) {
                        vmPost.getPostsWithPaging(
                            communityId = community.id,
                            pageSize = 3,
                            myUid = currentUser.uid
                        )
                    }
                }
                showSnackbar(context.getString(R.string.post_deleted))
                vmPost.deletePostState.value = Resource.Idle()
            }

            else -> {}
        }
    }
    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = {
            if (currentUser != null) {
                vmPost.lastPost = null
                refreshing = true
                vmPost.getPostsWithPaging(
                    communityId = community.id,
                    pageSize = 3,
                    myUid = currentUser.uid
                )
                viewModelCommunity.getUpcomingEvents(community.id)
                viewModelCommunity.getNumberOfRequests(community.id)
                viewModelCommunity.getNumOfMembers(community.id)
                viewModelCommunity.getNumOfEvents(community.id)
            }
        },
        state = pullToRefreshState,
        contentAlignment = Alignment.TopCenter,
        indicator = {
            PullToRefreshDefaults.LoadingIndicator(
                state = pullToRefreshState,
                isRefreshing = refreshing,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    ) {

        LazyColumn(
            state = vmPost.communityFlowListState,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(10.dp)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                CommunityHeadLine(
                    community = community,
                    numberOfMembers = numberOfMembers,
                    numOfEvents = numOfEvents,
                    rolePriority = rolePriority,
                    onEditClick = { isName, value ->
                        onEditClick(isName, value)
                    },
                    onMembersClick = {
                        if (rolePriority != null) {
                            onMembersClick()
                        }
                    },
                    onEventClick = { event ->
                        if (rolePriority != null) {
                            onEventClick(event)
                        }
                    },
                    onEventListClick = {
                        if (rolePriority != null) {
                            val encodedCommunityName = Uri.encode(community.name)
                            navController.navigate("community_events_list_page/${community.id}/$encodedCommunityName")
                        }
                    },
                    viewModel = viewModelCommunity,
                    showSnackbar = { showSnackbar(it) },
                    scope = scope,
                    snackbarHostState = snackbarHostState
                )
            }
            if (rolePriority == null || currentUser == null) {
                item {
                    NonMemberView(
                        didISendRequest = didISendRequest,
                        byRequestOnly = community.participationByRequestOnly,
                        onSendRequest = onSendRequest,
                        onJoinCommunity = onJoinCommunity
                    )
                }
            } else {
                if (vmPost.postList.isEmpty()) {
                    if (!isLoading) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.no_post_yet),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                } else {
                    items(vmPost.postList) { post ->
                        PostRow(
                            post = post,
                            onLikeClick = { liked ->
                                val index =
                                    vmPost.postList.indexOf(vmPost.postList.find { it.id == post.id })
                                if (liked) {
                                    vmPost.postList[index] =
                                        vmPost.postList[index].copy(liked = true)
                                    vmPost.postList[index] = vmPost.postList[index].copy(
                                        numOfLikes = (post.numOfLikes ?: 0) + 1
                                    )
                                    vmPost.likeThePost(
                                        postId = post.id, communityId = community.id,
                                        myUid = currentUser.uid
                                    )

                                } else {
                                    vmPost.postList[index] =
                                        vmPost.postList[index].copy(liked = false)
                                    vmPost.postList[index] =
                                        vmPost.postList[index].copy(
                                            numOfLikes = (post.numOfLikes ?: 0) - 1
                                        )
                                    vmPost.undoLikeThePost(
                                        postId = post.id, communityId = community.id,
                                        myUid = currentUser.uid
                                    )

                                }
                            },
                            rolePriority = rolePriority,
                            onDeletePostClick = {
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = context.getString(R.string.are_you_sure_you_want_to_delete_the_post),
                                        actionLabel = context.getString(R.string.delete),
                                        duration = SnackbarDuration.Long,
                                        withDismissAction = true
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        vmPost.deletePost(
                                            communityId = community.id,
                                            postId = post.id
                                        )
                                    }
                                }
                            },
                            currentUser = currentUser,
                            onCommentClick = {
                                onCommentClick(post.id)
                                vmPost.getCommentsWithPaging(
                                    communityId = community.id, postId = post.id, pageSize = 6,
                                    myUid = currentUser.uid
                                )

                            },
                            onPostClick = {
                                navController.navigate("post/${community.id}/${post.id}") {
                                    restoreState = true
                                    launchSingleTop = true
                                }
                            },
                            onReportClick = {
                                showReportDialog(post)
                            }
                        )
                        Spacer(Modifier.height(15.dp))
                    }
                }

                if (isLoading) {
                    item {
                        Spacer(Modifier.height(5.dp))
                        Column(Modifier.fillMaxWidth()) {
                            CircularWavyProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        }
                    }

                } else {
                    if (vmPost.lastPost != null) {
                        item {
                            Spacer(Modifier.height(5.dp))
                            Column(Modifier.fillMaxWidth()) {
                                IconButton(
                                    onClick = {
                                        vmPost.getPostsWithPaging(
                                            communityId = community.id, pageSize = 6,
                                            myUid = currentUser.uid
                                        )

                                    }, modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                        .border(
                                            1.dp,
                                            IconButtonDefaults.iconButtonColors().contentColor,
                                            CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Add,
                                        contentDescription = "load more posts"
                                    )
                                }
                            }
                        }

                        if (isError) {
                            item {
                                Spacer(Modifier.height(5.dp))
                                Column(Modifier.fillMaxWidth()) {
                                    Text(
                                        stringResource(R.string.something_went_wrong_try_again_later),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.titleSmall,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(100.dp)) }

        }
    }
}

@Composable
private fun NonMemberView(
    didISendRequest: Boolean?,
    byRequestOnly: Boolean,
    onSendRequest: () -> Unit,
    onJoinCommunity: () -> Unit
) {
    Box(Modifier.padding(15.dp)) {
        if (byRequestOnly) {
            when (didISendRequest) {
                true -> {
                    FilledTonalButton(
                        onClick = {},
                        enabled = false
                    ) {
                        Text(stringResource(R.string.request_sent))
                    }
                }

                false -> {
                    FilledTonalButton(
                        onClick = onSendRequest
                    ) {
                        Text(stringResource(R.string.send_request))
                    }
                }

                null -> {
                    FilledTonalButton(
                        onClick = onSendRequest
                    ) {
                        Text(stringResource(R.string.send_request))
                    }
                }
            }
        } else {
            FilledTonalButton(
                onClick = onJoinCommunity
            ) {
                Text(stringResource(R.string.join_community))
            }
        }
    }

}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UpcomingEvents(
    onEventClick: (Event) -> Unit,
    eventList: List<Event>,
    onEventListClick: () -> Unit
) {

    if (eventList.isNotEmpty()) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.padding(5.dp),
            color = Color.Transparent
        ) {
            Column(Modifier.padding(10.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.upcoming_events),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TextButton(
                        onClick = onEventListClick
                    ) {
                        Text(
                            "${eventList.size} ${stringResource(R.string.upcoming_events)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                            contentDescription = "see all",
                            modifier = Modifier
                                .size(IconButtonDefaults.xSmallIconSize)
                                .padding(start = 4.dp)
                        )
                    }
                }
                LazyRow(
                    Modifier
                        .fillMaxWidth(0.95f)
                        .padding(top = 5.dp)
                ) {
                    items(eventList) { event ->
                        EventColumn(event = event,
                            onEventClick = { onEventClick(event) })
                        Spacer(Modifier.width(20.dp))
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EventColumn(
    event: Event,
    onEventClick: () -> Unit
) {
    OutlinedButton(
        contentPadding = PaddingValues(3.dp),
        shape = RoundedCornerShape(30.dp),
        onClick = {
            onEventClick.invoke()
        }

    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .width(250.dp)
                .height(70.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
            ) {
                if (event.privateInfo) {
                    Text(
                        "${stringResource(R.string._private)}\n${stringResource(R.string.date)}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        TimeFunctions().convertTimestampToLocalizeDate(
                            event.date,
                            singleLine = false
                        ),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
            VerticalDivider(color = Color.LightGray, modifier = Modifier.height(50.dp))
            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .padding(4.dp)
                    .weight(3f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    event.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )

                if (!event.privateInfo) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.AccessTime,
                            contentDescription = "time",
                            modifier = Modifier.size(IconButtonDefaults.xSmallIconSize)
                        )
                        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                        Text(
                            sdf.format(event.date.toDate()),
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }
            }
        }

    }
}

@Composable
private fun PostRow(
    post: Post,
    onLikeClick: (Boolean) -> Unit,
    onDeletePostClick: () -> Unit,
    currentUser: FirebaseUser?,
    rolePriority: Int,
    onPostClick: () -> Unit,
    onCommentClick: () -> Unit,
    onReportClick: () -> Unit
) {
    val context = LocalContext.current
    Surface(
        shape = RoundedCornerShape(10.dp),
        shadowElevation = 5.dp,
        modifier = Modifier.clickable {
            onPostClick.invoke()
        },
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(8.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(4f)
                ) {
                    Box(modifier = Modifier.size(40.dp)) {
                        if (post.userPhotoUrl == null) {
                            Image(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = post.userName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(CircleShape)
                                    .background(Color.LightGray, CircleShape)
                            )
                        } else {
                            GlideImage(imageModel = { post.userPhotoUrl.toUri() },
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(CircleShape),
                                requestBuilder = {
                                    val thumbnailRequest = Glide
                                        .with(context)
                                        .asBitmap()
                                        .load(post.userPhotoUrl)
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
                                })
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = post.userName ?: "",
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        when (post.userRolePriority) {
                            CommunityRoles.Member.rolePriority -> {}
                            CommunityRoles.Moderator.rolePriority -> {
                                Text(
                                    text = stringResource(CommunityRoles.Moderator.roleNameResource),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.secondaryContainer,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 2.dp)
                                )
                            }

                            CommunityRoles.Admin.rolePriority -> {
                                Text(
                                    text = stringResource(CommunityRoles.Admin.roleNameResource),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.primary,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 2.dp)
                                )
                            }

                            CommunityRoles.NotMember.rolePriority -> {
                                Text(
                                    text = stringResource(CommunityRoles.NotMember.roleNameResource),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onError,
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.error,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 2.dp)
                                )
                            }
                        }
                    }
                }
                var text by remember {
                    mutableStateOf(
                        TimeFunctions().convertTimestampToLocalizeTime(
                            post.date,
                            context
                        )
                    )
                }
                LaunchedEffect(true) {
                    while (true) {
                        delay(60100)
                        text = TimeFunctions().convertTimestampToLocalizeTime(post.date, context)
                    }
                }
                Text(
                    text,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1.5f)
                )

            }
            Spacer(Modifier.height(10.dp))
            Text(
                post.content, style = MaterialTheme.typography.bodyMedium
            )
            if (post.imageUrl != null) {
                val screenWidth = LocalConfiguration.current.screenWidthDp.dp
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .width(screenWidth * 0.95f)
                        .height(screenWidth * 0.95f * 1.33f)
                        .clip(RoundedCornerShape(5.dp))
                        .align(Alignment.CenterHorizontally)
                ) {
                    GlideImage(imageModel = { post.imageUrl.toUri() },
                        modifier = Modifier.matchParentSize(),
                        imageOptions = ImageOptions(contentScale = ContentScale.Crop),
                        requestBuilder = {
                            val thumbnailRequest = Glide
                                .with(context)
                                .asBitmap()
                                .load(post.imageUrl.toUri())
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
                        failure = {
                            Text(
                                stringResource(R.string.image_cannot_be_loaded),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    )
                }
            }
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (post.liked == true) {
                            IconButton(onClick = {
                                onLikeClick(false)
                            }) {
                                Icon(
                                    Icons.Rounded.Favorite, null, tint = Color.Red
                                )
                            }
                        } else {
                            IconButton(onClick = {
                                onLikeClick(true)
                            }) {
                                Icon(Icons.Rounded.FavoriteBorder, null)
                            }
                        }
                        Text(post.numOfLikes?.toString() ?: "")
                    }

                    if (post.commentsOn) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onCommentClick
                            ) {
                                Icon(Icons.Rounded.ChatBubbleOutline, null)
                            }
                            Text(post.numOfComments?.toString() ?: "")
                        }
                    }
                }

                val isAdminOrOwner = rolePriority == CommunityRoles.Admin.rolePriority
                        || (post.uid == currentUser?.uid)
                val isModeratorAndIsMemberPost =
                    rolePriority == CommunityRoles.Moderator.rolePriority
                            && post.userRolePriority == CommunityRoles.Member.rolePriority

                if (isAdminOrOwner || isModeratorAndIsMemberPost
                ) {
                    var postOptionsMenuExpanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { postOptionsMenuExpanded = true }) {
                            Icon(Icons.Rounded.MoreVert, "more")
                        }
                        DropdownMenu(
                            expanded = postOptionsMenuExpanded,
                            onDismissRequest = { postOptionsMenuExpanded = false }
                        ) {
                            if (currentUser != null) {
                                if (currentUser.uid != post.uid) {
                                    DropdownMenuItem(
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Rounded.Report,
                                                contentDescription = "report"
                                            )
                                        },
                                        text = {
                                            Text(stringResource(R.string.report))
                                        },
                                        onClick = {
                                            onReportClick()
                                            postOptionsMenuExpanded = false
                                        }
                                    )
                                }
                            }
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = "report",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                text = {
                                    Text(
                                        stringResource(R.string.delete_the_post),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    onDeletePostClick()
                                    postOptionsMenuExpanded = false
                                }
                            )
                        }
                    }

                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CommentsBottomSheet(
    state: SheetState,
    vmPost: PostViewModel,
    currentUser: FirebaseUser,
    communityId: String,
    rolePriority: Int,
    postId: String,
    reportUserDialog: (PostComment) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var textingComment by remember { mutableStateOf("") }
    val pagedComments by vmPost.pagedComments.collectAsState()
    val commentList = remember { mutableStateListOf<PostComment>() }
    var isError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(pagedComments) {
        when (pagedComments) {
            is Resource.Error -> {
                isLoading = false
                isError = true
            }

            is Resource.Idle -> {
                isLoading = false
                isError = false
            }

            is Resource.Loading -> {
                isError = false
                isLoading = true
            }

            is Resource.Success -> {
                isLoading = false
                isError = false
                commentList.addAll(pagedComments.data!!)
            }
        }
    }
    LaunchedEffect(vmPost.commentOnState.value) {
        when (val resource = vmPost.commentOnState.value) {
            is Resource.Error -> {
                Toast.makeText(
                    context,
                    resource.messageResource?.let { context.getString(it) } ?: "",
                    Toast.LENGTH_SHORT
                ).show()
                vmPost.commentOnState.value = Resource.Idle()
            }

            is Resource.Success -> {
                commentList.add(0, resource.data!!)
                vmPost.commentOnState.value = Resource.Idle()
            }

            else -> {}
        }
    }
    LaunchedEffect(vmPost.deleteCommentState.value) {
        when (val resource = vmPost.deleteCommentState.value) {
            is Resource.Error -> {
                val toast = Toast.makeText(
                    context,
                    resource.messageResource?.let { context.getString(it) } ?: "",
                    Toast.LENGTH_SHORT
                )
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
                vmPost.deleteCommentState.value = Resource.Idle()
            }

            is Resource.Success -> {
                commentList.remove(commentList.find { it.id == resource.data!! })
                vmPost.deleteCommentState.value = Resource.Idle()
            }

            else -> {}
        }
    }
    ModalBottomSheet(
        modifier = Modifier.padding(
            top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
        ),
        sheetState = state,
        onDismissRequest = {
            commentList.clear()
            vmPost.lastComment = null
            onDismiss()
        }) {
        Text(
            stringResource(R.string.comments),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(start = 8.dp)
                .align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(6.dp))
        Column(
            Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                if (commentList.isNotEmpty() || isLoading) {
                    items(commentList) {comment->
                        CommentRow(
                            comment = comment,
                            onDeleteClick = {
                                vmPost.deleteComment(
                                    communityId = communityId, postId = postId, commentId = comment.id
                                )
                            },
                            onReportClick = {
                                reportUserDialog(comment)
                            }
                        )
                    }
                    if (isLoading) {
                        item {
                            Spacer(Modifier.height(5.dp))
                            Column(Modifier.fillMaxWidth()) {
                                CircularWavyProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                            }
                        }
                    } else {
                        if (vmPost.lastComment != null) {

                            item {
                                Spacer(Modifier.height(5.dp))
                                Column(Modifier.fillMaxWidth()) {
                                    IconButton(
                                        onClick = {
                                            vmPost.getCommentsWithPaging(
                                                communityId = communityId,
                                                postId = postId,
                                                pageSize = 8,
                                                myUid = currentUser.uid
                                            )
                                        },
                                        modifier = Modifier
                                            .align(Alignment.CenterHorizontally)
                                            .border(
                                                1.dp,
                                                IconButtonDefaults.iconButtonColors().contentColor,
                                                CircleShape
                                            )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Add,
                                            contentDescription = "load more comments"
                                        )
                                    }
                                }
                            }

                            if (isError) {
                                item {
                                    Spacer(Modifier.height(5.dp))
                                    Column(Modifier.fillMaxWidth()) {
                                        Text(
                                            stringResource(R.string.something_went_wrong),
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.titleSmall,
                                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_comment_yet),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }


            }
            CommentTextField(
                value = textingComment,
                onValueChange = { textingComment = it },
                onSendClick = {
                    vmPost.commentOn(
                        comment = PostComment(
                            senderUid = currentUser.uid,
                            commentText = textingComment,
                            date = Timestamp.now()
                        ),
                        rolePriority = rolePriority,
                        communityId = communityId,
                        postId = postId,
                        currentUser = currentUser
                    )
                    textingComment = ""

                },
                currentUser = currentUser
            )
        }

    }
}

@Composable
private fun CommentTextField(
    value: String,
    currentUser: FirebaseUser,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    var textIsEmpty by remember { mutableStateOf(false) }

    LaunchedEffect(true) {
        focusRequester.requestFocus()

    }

    TextField(isError = textIsEmpty,
        leadingIcon = {
            Box(modifier = Modifier.size(TextFieldDefaults.MinHeight * 0.7f)) {
                if (currentUser.photoUrl != null) {
                    GlideImage(imageModel = { currentUser.photoUrl },
                        modifier = Modifier
                            .matchParentSize()
                            .clip(CircleShape),
                        requestBuilder = {
                            val thumbnailRequest = Glide
                                .with(context)
                                .asBitmap()
                                .load(currentUser.photoUrl)
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
                        failure = {
                            Image(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = "you",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(CircleShape)
                                    .background(Color.LightGray, CircleShape)
                            )
                        })
                } else {
                    Image(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = "you",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .matchParentSize()
                            .clip(CircleShape)
                            .background(Color.LightGray, CircleShape)
                    )
                }
            }
        },
        value = value,
        onValueChange = {
            onValueChange(it)
            textIsEmpty = false
        },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        trailingIcon = {
            IconButton(onClick = {
                if (value.isNotBlank()) {
                    onSendClick()
                } else {
                    textIsEmpty = true
                }
            }) {
                Icon(imageVector = Icons.AutoMirrored.Rounded.Send, contentDescription = "send")
            }
        },
        shape = RoundedCornerShape(15.dp),
        placeholder = { Text(stringResource(R.string.write_comment_placeholder)) },
        supportingText = if (textIsEmpty) {
            {
                Text(stringResource(R.string.comment_cannot_be_empty))
            }
        } else null,
        colors = TextFieldDefaults.colors(
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            errorContainerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        ))
}

@Composable
fun CommentRow(comment: PostComment, 
               onDeleteClick: () -> Unit,
               onReportClick: () -> Unit
) {
    var commentMenuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    ListItem(
        headlineContent = { Text(comment.commentText) },
        overlineContent = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(comment.senderUserName ?: "", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.width(10.dp))
                    var text by remember {
                        mutableStateOf(
                            TimeFunctions().convertTimestampToLocalizeTime(
                                comment.date,
                                context
                            )
                        )
                    }
                    LaunchedEffect(true) {
                        while (true) {
                            delay(60100)
                            text = TimeFunctions().convertTimestampToLocalizeTime(
                                comment.date,
                                context
                            )
                        }
                    }
                    Text(
                        text,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Log.d("communityFlow", "CommentRow: ${comment.commentText} -> ${comment.senderRolePriority}")
                when (comment.senderRolePriority) {

                    CommunityRoles.Moderator.rolePriority -> {
                        Text(
                            text = stringResource(CommunityRoles.Moderator.roleNameResource),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 2.dp)
                        )
                    }
                    CommunityRoles.Admin.rolePriority -> {
                        Text(
                            text = stringResource(CommunityRoles.Admin.roleNameResource),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 2.dp)
                        )
                    }
                    CommunityRoles.NotMember.rolePriority -> {
                        Text(
                            text = stringResource(CommunityRoles.NotMember.roleNameResource),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.errorContainer,
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 2.dp)
                        )
                    }

                    else -> {}
                }
                Spacer(Modifier.height(4.dp))

            }
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(TextFieldDefaults.MinHeight * 0.7f)
                    .clip(CircleShape)
            ) {
                if (comment.senderProfileImageUrl != null) {
                    GlideImage(imageModel = { comment.senderProfileImageUrl.toUri() },
                        imageOptions = ImageOptions(contentScale = ContentScale.Crop),
                        modifier = Modifier
                            .matchParentSize()
                            .clip(CircleShape),
                        requestBuilder = {
                            val thumbnailRequest = Glide
                                .with(context)
                                .asBitmap()
                                .load(comment.senderProfileImageUrl.toUri())
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
                        })
                } else {
                    Image(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = comment.senderUserName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.LightGray, CircleShape)
                    )
                }
            }
        },
        trailingContent =

        {
            Box {
                IconButton(onClick = {
                    commentMenuExpanded = !commentMenuExpanded
                }

                ) {
                    Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = "more")
                }
                DropdownMenu(
                    expanded = commentMenuExpanded,
                    onDismissRequest = { commentMenuExpanded = false },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    if (comment.isMyComment!!) {
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = "delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            text = {
                                Text(
                                    stringResource(R.string.delete_the_comment),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }, onClick = {
                                onDeleteClick()
                                commentMenuExpanded = false

                            })
                    } else {
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Report,
                                    contentDescription = "report"
                                )
                            },
                            text = {
                                Text(
                                    stringResource(R.string.report)
                                )
                            }, onClick = {
                                onReportClick()
                                commentMenuExpanded = false
                            })
                    }
                }
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}


@Preview
@Composable
private fun PostsPreview() {

    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        UpcomingEvents(
            onEventClick = {},
            eventList = listOf(
                Event(
                    privateInfo = false,
                    title = "satranç turnuvasısatranç turnuvasısatranç turnuvasısatranç turnuvası",
                    date = Timestamp(
                        time = LocalDateTime.of(
                            2024, 12, 25, 16, 30, 0
                        ).toInstant(ZoneOffset.UTC)
                    )
                ),
                Event(title = "satranç turnuvası", date = Timestamp.now())
            ),
            onEventListClick = {}
        )
        NonMemberView(
            didISendRequest = true,
            byRequestOnly = true,
            onSendRequest = {},
            onJoinCommunity = {}
        )
    }


}
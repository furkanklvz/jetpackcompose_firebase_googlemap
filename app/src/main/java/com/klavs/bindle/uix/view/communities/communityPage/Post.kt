package com.klavs.bindle.uix.view.communities.communityPage

import android.net.Uri
import android.util.Log
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
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Report
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.klavs.bindle.R
import com.klavs.bindle.data.entity.sealedclasses.CommunityRoles
import com.klavs.bindle.data.entity.Post
import com.klavs.bindle.data.entity.PostComment
import com.klavs.bindle.resource.Resource
import com.klavs.bindle.uix.view.GlideImageLoader
import com.klavs.bindle.uix.viewmodel.communities.PostViewModel
import com.klavs.bindle.util.TimeFunctions
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Post(
    postId: String,
    communityId: String,
    navController: NavHostController,
    currentUser: FirebaseUser,
    viewModel: PostViewModel
) {
    Log.e(
        "liststate",
        "first visible item index in post: ${viewModel.communityFlowListState.firstVisibleItemIndex}"
    )
    Log.e(
        "liststate",
        "first visible item offset in post: ${viewModel.communityFlowListState.firstVisibleItemScrollOffset}"
    )

    val context = LocalContext.current
    val postResource by viewModel.postResource.collectAsState()
    var post by remember { mutableStateOf<Post?>(null) }
    val commentsResource by viewModel.pagedComments.collectAsState()
    val commentsList = remember { mutableStateListOf<PostComment>() }
    val myRole by viewModel.myRolePriority.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.listenToPostJob?.cancel()
            viewModel.listenToMyRoleJob?.cancel()
        }
    }

    LaunchedEffect(viewModel.deleteCommentState.value) {
        if (viewModel.deleteCommentState.value is Resource.Success) {
            commentsList.removeIf { it.id == viewModel.deleteCommentState.value.data }
        }
    }
    LaunchedEffect(viewModel.deletePostState.value) {
        if (viewModel.deletePostState.value is Resource.Success) {
            navController.popBackStack()
        }
    }
    LaunchedEffect(viewModel.commentOnState.value) {
        if (viewModel.commentOnState.value is Resource.Success) {
            commentsList.add(0, viewModel.commentOnState.value.data!!)
        }
    }

    LaunchedEffect(postResource) {
        if (postResource is Resource.Success && postResource.data != null) {
            post = postResource.data!!
        }
    }

    LaunchedEffect(true) {
        viewModel.lastComment = null
        viewModel.getCommentsWithPaging(
            communityId = communityId,
            postId = postId,
            pageSize = 8,
            myUid = currentUser.uid
        )
        viewModel.listenToPost(
            communityId = communityId,
            postId = postId,
            myUid = currentUser.uid
        )
        viewModel.listenToMyRole(
            communityId = communityId,
            myUid = currentUser.uid
        )
    }
    LaunchedEffect(commentsResource) {
        if (commentsResource is Resource.Success && commentsResource.data != null) {
            commentsList.addAll(commentsResource.data!!)
        }
    }

    PostPageContent(
        onBackClick = { navController.popBackStack() },
        postResource = postResource,
        commentsResource = commentsResource,
        post = post,
        onDeletePostClick = {
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = context.getString(R.string.are_you_sure_you_want_to_delete_the_post),
                    actionLabel = context.getString(R.string.delete),
                    duration = SnackbarDuration.Long,
                    withDismissAction = true
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.deletePost(
                        communityId = communityId,
                        postId = postId
                    )
                }
            }
        },
        onLikeClick = { liked ->
            if (!liked) {
                viewModel.undoLikeThePost(
                    postId = postId,
                    communityId = communityId,
                    myUid = currentUser.uid
                )
                post = post?.copy(liked = false)
                post =
                    post?.copy(
                        numOfLikes = post?.numOfLikes?.minus(
                            1
                        )
                    )
            } else {
                viewModel.likeThePost(
                    postId = postId,
                    communityId = communityId,
                    myUid = currentUser.uid
                )
                post = post?.copy(liked = true)
                post =
                    post?.copy(numOfLikes = post?.numOfLikes?.plus(1))
            }
        },
        myUid = currentUser.uid,
        onDeleteCommentClick = {
            viewModel.deleteComment(
                communityId = communityId,
                postId = postId,
                commentId = it
            )
        },
        commentsList = commentsList,
        myRole = myRole,
        lastComment = viewModel.lastComment,
        loadMoreComments = {
            viewModel.getCommentsWithPaging(
                communityId = communityId,
                postId = postId,
                pageSize = 7,
                myUid = currentUser.uid
            )
        },
        onSendClick = {
            viewModel.commentOn(
                comment = PostComment(
                    senderUid = currentUser.uid,
                    commentText = it,
                    date = Timestamp.now()
                ),
                communityId = communityId,
                postId = postId,
                rolePriority = myRole.data!!,
                currentUser = currentUser
            )
        },
        myPhotoUrl = currentUser.photoUrl,
        sendReport = { reportType, optionalMessage ->
            if (postResource.data != null) {
                viewModel.sendReport(
                    uid = currentUser.uid,
                    reportType = reportType,
                    description = optionalMessage,
                    postId = postId,
                    postOwnerUid = postResource.data!!.uid
                )
            }
        },
        scope = scope,
        snackbarHostState = snackbarHostState,
        sendUserReport = {comment,pair->
            viewModel.sendUserReport(
                reportedUser = comment.senderUid,
                uid = currentUser.uid,
                reportType = pair.first,
                description = pair.second,
                messageId = comment.id!!,
                messageContent = comment.commentText
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PostPageContent(
    onBackClick: () -> Unit,
    postResource: Resource<Post>,
    commentsResource: Resource<List<PostComment>>,
    post: Post?,
    myUid: String,
    myPhotoUrl: Uri?,
    onDeletePostClick: () -> Unit,
    onDeleteCommentClick: (String) -> Unit,
    commentsList: List<PostComment>,
    onLikeClick: (Boolean) -> Unit,
    sendUserReport: (comment:PostComment, Pair<Int,String>) -> Unit,
    lastComment: DocumentSnapshot?,
    onSendClick: (String) -> Unit,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    loadMoreComments: () -> Unit,
    myRole: Resource<Int>,
    sendReport: (reportType: Int, optionalMessage: String) -> Unit
) {
    var typingComment by remember { mutableStateOf("") }
    var focusToTextField by remember { mutableStateOf(false) }
    var reportDialog by remember { mutableStateOf(false) }
    var reportUserDialog by remember { mutableStateOf<PostComment?>(null) }
    val context = LocalContext.current
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.post))
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            Modifier
                .padding(top = innerPadding.calculateTopPadding())
                .fillMaxSize()
        ) {
            if (reportUserDialog != null){
                var selectedContent by remember { mutableStateOf<Int?>(null) }
                var optionalDescription by remember { mutableStateOf("") }
                val messageLimit = 100
                Dialog(onDismissRequest = { reportUserDialog = null }) {
                    Card {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                stringResource(R.string.report_the_user),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(10.dp),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                stringResource(R.string.please_select_one) + ":",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .align(Alignment.Start)
                            )
                            ListItem(
                                modifier = Modifier.clickable { selectedContent = 0 },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                headlineContent = {
                                    Text(
                                        stringResource(R.string.child_abuse_or_illegal_content),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                trailingContent = {
                                    if (selectedContent == 0) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = "sexual content"
                                        )
                                    }
                                }
                            )
                            HorizontalDivider()
                            ListItem(
                                modifier = Modifier.clickable { selectedContent = 1 },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                headlineContent = {
                                    Text(
                                        stringResource(R.string.disruptive_behavior_or_harassment),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                trailingContent = {
                                    if (selectedContent == 1) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = "violence"
                                        )
                                    }
                                }
                            )
                            HorizontalDivider()
                            ListItem(
                                modifier = Modifier.clickable { selectedContent = 2 },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                headlineContent = {
                                    Text(
                                        stringResource(R.string.something_else),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                trailingContent = {
                                    if (selectedContent == 2) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = "something else"
                                        )
                                    }
                                }
                            )
                            HorizontalDivider(
                                thickness = 3.dp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .fillMaxWidth(0.2f)
                                    .clip(CircleShape)
                            )
                            TextField(
                                label = {
                                    Text(
                                        stringResource(R.string.optional_description),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                value = optionalDescription,
                                minLines = 1,
                                modifier = Modifier.fillMaxWidth(0.9f),
                                supportingText = {
                                    Text("$messageLimit/${optionalDescription.length}")
                                },
                                maxLines = 4,
                                textStyle = TextStyle(
                                    fontStyle = MaterialTheme.typography.bodyMedium.fontStyle,
                                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                                ),
                                onValueChange = {
                                    if (it.length <= messageLimit) {
                                        optionalDescription = it
                                    }
                                }
                            )
                            Row(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(
                                    onClick = { reportUserDialog = null }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                        contentDescription = "back"
                                    )
                                }
                                OutlinedButton(
                                    enabled = selectedContent != null,
                                    onClick = {
                                        sendUserReport(
                                            reportUserDialog!!,
                                            selectedContent!! to optionalDescription
                                        )
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                context.getString(R.string.report_sent)
                                            )
                                        }
                                        reportUserDialog = null
                                    }
                                ) {
                                    Text(stringResource(R.string.send))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.Send,
                                        contentDescription = "send",
                                        modifier = Modifier
                                            .padding(start = 4.dp)
                                            .size(IconButtonDefaults.xSmallIconSize)
                                    )
                                }
                            }

                        }
                    }
                }
            }
            if (reportDialog) {
                var selectedContent by remember { mutableStateOf<Int?>(null) }
                var optionalMessage by remember { mutableStateOf("") }
                val messageLimit = 100
                Dialog(onDismissRequest = { reportDialog = false }) {
                    Card {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                stringResource(R.string.report_the_post),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(10.dp),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                stringResource(R.string.please_select_one) + ":",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .align(Alignment.Start)
                            )
                            ListItem(
                                modifier = Modifier.clickable { selectedContent = 0 },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                headlineContent = {
                                    Text(
                                        stringResource(R.string.sexual_content_or_nudity),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                trailingContent = {
                                    if (selectedContent == 0) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = "sexual content"
                                        )
                                    }
                                }
                            )
                            HorizontalDivider()
                            ListItem(
                                modifier = Modifier.clickable { selectedContent = 1 },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                headlineContent = {
                                    Text(
                                        stringResource(R.string.violence),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                trailingContent = {
                                    if (selectedContent == 1) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = "violence"
                                        )
                                    }
                                }
                            )
                            HorizontalDivider()
                            ListItem(
                                modifier = Modifier.clickable { selectedContent = 2 },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                headlineContent = {
                                    Text(
                                        stringResource(R.string.something_else),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                trailingContent = {
                                    if (selectedContent == 2) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = "something else"
                                        )
                                    }
                                }
                            )
                            HorizontalDivider(
                                thickness = 3.dp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .fillMaxWidth(0.2f)
                                    .clip(CircleShape)
                            )
                            TextField(
                                label = {
                                    Text(
                                        stringResource(R.string.optional_description),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                value = optionalMessage,
                                minLines = 1,
                                modifier = Modifier.fillMaxWidth(0.9f),
                                supportingText = {
                                    Text("$messageLimit/${optionalMessage.length}")
                                },
                                maxLines = 4,
                                textStyle = TextStyle(
                                    fontStyle = MaterialTheme.typography.bodyMedium.fontStyle,
                                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                                ),
                                onValueChange = {
                                    if (it.length <= messageLimit) {
                                        optionalMessage = it
                                    }
                                }
                            )
                            Row(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(
                                    onClick = { reportDialog = false }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                        contentDescription = "back"
                                    )
                                }
                                OutlinedButton(
                                    enabled = selectedContent != null,
                                    onClick = {
                                        sendReport(selectedContent!!, optionalMessage)
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                context.getString(R.string.report_sent)
                                            )
                                        }
                                        reportDialog = false
                                    }
                                ) {
                                    Text(stringResource(R.string.send))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.Send,
                                        contentDescription = "send",
                                        modifier = Modifier
                                            .padding(start = 4.dp)
                                            .size(IconButtonDefaults.xSmallIconSize)
                                    )
                                }
                            }

                        }
                    }
                }
            }
            when (postResource) {
                is Resource.Error -> {
                    Text(
                        text = commentsResource.messageResource
                            ?.let { stringResource(it) } ?: "",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                is Resource.Idle -> {}
                is Resource.Loading -> {
                    CircularWavyProgressIndicator(
                        Modifier
                            .align(Alignment.Center)
                            .padding(6.dp)
                            .size(40.dp)
                    )
                }

                is Resource.Success -> {
                    Column(Modifier.fillMaxSize()) {

                        LazyColumn(
                            Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainer,
                                    RoundedCornerShape(25.dp)
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            item {
                                PostContent(
                                    post = post ?: Post(),
                                    onLikeClick = { onLikeClick(it) },
                                    onDeletePostClick = onDeletePostClick,
                                    myUid = myUid,
                                    rolePriority = myRole.data
                                        ?: CommunityRoles.NotMember.rolePriority,
                                    onCommentClick = {
                                        focusToTextField = true
                                    },
                                    onReportClick = { reportDialog = true }
                                )

                            }
                            item {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Text(
                                        stringResource(R.string.comments),
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(18.dp)
                                    )
                                }

                            }
                            if (postResource.data?.commentsOn == true){
                                if (commentsList.isEmpty()) {
                                    item {
                                        Box(Modifier.fillMaxSize()) {
                                            Text(
                                                text = stringResource(R.string.no_comment_yet),
                                                modifier = Modifier.padding(10.dp).align(Alignment.Center),
                                                style = MaterialTheme.typography.titleSmall
                                            )
                                        }
                                    }
                                } else {
                                    items(commentsList) { comment ->
                                        CommentRow(
                                            comment = comment,
                                            onDeleteClick = {
                                                onDeleteCommentClick(comment.id ?: "")
                                            },
                                            onReportClick = {
                                                reportUserDialog = comment
                                            }
                                        )
                                    }
                                }

                                when (commentsResource) {
                                    is Resource.Error -> {
                                        item {
                                            Box(Modifier.fillMaxWidth()) {
                                                Text(
                                                    text = commentsResource.messageResource
                                                        ?.let { stringResource(it) } ?: "",
                                                    modifier = Modifier.align(Alignment.Center),
                                                    style = MaterialTheme.typography.titleSmall
                                                )
                                            }
                                        }
                                    }

                                    is Resource.Loading -> {
                                        item {
                                            Box(Modifier.fillMaxWidth()) {
                                                CircularWavyProgressIndicator(
                                                    Modifier
                                                        .align(Alignment.Center)
                                                        .padding(6.dp)
                                                        .size(40.dp)
                                                )
                                            }
                                        }
                                    }

                                    else -> {
                                        if (lastComment != null) {
                                            item {
                                                Box(Modifier.fillMaxWidth()) {
                                                    IconButton(
                                                        onClick = loadMoreComments,
                                                        modifier = Modifier
                                                            .align(Alignment.Center)
                                                            .padding(6.dp)
                                                            .size(40.dp)
                                                            .border(
                                                                1.dp,
                                                                IconButtonDefaults.iconButtonColors().contentColor,
                                                                CircleShape
                                                            )
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Rounded.Add,
                                                            contentDescription = "add"
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }else{
                                item {
                                    Box(Modifier.fillMaxSize()) {
                                        Text(
                                            text = stringResource(R.string.comments_are_disabled),
                                            modifier = Modifier.padding(10.dp).align(Alignment.Center),
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                    }
                                }
                            }

                        }
                        if (myRole is Resource.Success
                            && myRole.data != CommunityRoles.NotMember.rolePriority
                            && myRole.data != null
                            && postResource.data?.commentsOn == true) {
                            CommentTextField(
                                value = typingComment,
                                photoUrl = myPhotoUrl,
                                innerPadding = innerPadding,
                                onValueChange = {
                                    typingComment = it
                                },
                                onSendClick = {
                                    onSendClick(typingComment)
                                    typingComment = ""
                                },
                                focus = focusToTextField
                            )
                        }
                    }
                }
            }


        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CommentTextField(
    value: String,
    photoUrl: Uri?,
    onValueChange: (String) -> Unit,
    innerPadding: PaddingValues,
    focus: Boolean,
    onSendClick: () -> Unit
) {
    val context = LocalContext.current
    var textIsEmpty by remember { mutableStateOf(false) }
    val focusRequester by remember { mutableStateOf(FocusRequester()) }

    LaunchedEffect(focus) {
        if (focus) {
            focusRequester.requestFocus()
        }
    }

    TextField(
        isError = textIsEmpty,
        leadingIcon = {
            Box(modifier = Modifier.size(TextFieldDefaults.MinHeight * 0.7f)) {
                if (photoUrl != null) {
                    GlideImage(imageModel = { photoUrl },
                        modifier = Modifier
                            .matchParentSize()
                            .clip(CircleShape),
                        requestBuilder = {
                            val thumbnailRequest = Glide
                                .with(context)
                                .asBitmap()
                                .load(photoUrl)
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
                        loading = {
                            CircularWavyProgressIndicator(
                                modifier = Modifier
                                    .fillMaxSize(0.5f)
                                    .align(Alignment.Center)
                            )
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
            .consumeWindowInsets(innerPadding)
            .imePadding()
            .padding(5.dp)
            .focusRequester(focusRequester)
            .fillMaxWidth(),
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
        placeholder = { Text(stringResource(R.string.write_a_comment)) },
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PostContent(
    post: Post,
    onLikeClick: (Boolean) -> Unit,
    onDeletePostClick: () -> Unit,
    myUid: String?,
    rolePriority: Int,
    onCommentClick: () -> Unit,
    onReportClick: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
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
                    .width(screenWidth*0.95f)
                    .height(screenWidth*0.95f*1.33f)
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
                    loading = {
                        CircularWavyProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.Center)
                        )
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
                    || (post.uid == myUid)
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
                        if (myUid != post.uid) {
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
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(R.string.delete_the_post),
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = "delete the post",
                                    tint = MaterialTheme.colorScheme.error
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

@Preview(locale = "tr")
@Composable
private fun PostPagePreview() {
    PostPageContent(
        onBackClick = {},
        postResource = Resource.Success(
            data = Post(
                commentsOn = false,
                id = "0",
                content = "content",
                date = Timestamp.now()
            )
        ),
        commentsResource = Resource.Success(
            data = listOf(
                PostComment(
                    id = "0",
                    commentText = "comment",
                    senderUid = "0",
                    senderUserName = "furkan",
                    isMyComment = true
                )
            )
        ),
        post = Post(
            id = "0",
            content = "content",
            date = Timestamp.now(),
            commentsOn = false),
        myUid = "0",
        myPhotoUrl = null,
        onDeletePostClick = {},
        onDeleteCommentClick = {},
        commentsList = listOf(
            PostComment(
                senderRolePriority = 1,
                id = "0",
                commentText = "comment",
                senderUid = "0",
                senderUserName = "furkan",
                isMyComment = true
            ),
            PostComment(
                senderRolePriority = 0,
                id = "0",
                commentText = "comment",
                senderUid = "0",
                senderUserName = "furkan",
                isMyComment = true
            )
        ),
        onLikeClick = {},
        lastComment = null,
        onSendClick = {},
        loadMoreComments = {},
        myRole = Resource.Success(data = CommunityRoles.Admin.rolePriority),
        sendReport = { _, _ -> },
        scope = rememberCoroutineScope(),
        snackbarHostState = remember { SnackbarHostState() },
        sendUserReport = { _, _ -> }
    )
}
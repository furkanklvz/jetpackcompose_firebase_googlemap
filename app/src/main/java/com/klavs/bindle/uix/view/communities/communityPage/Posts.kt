package com.klavs.bindle.uix.view.communities.communityPage

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Timestamp
import com.klavs.bindle.data.entity.Community
import com.klavs.bindle.data.entity.Post
import com.klavs.bindle.data.entity.PostComment
import com.klavs.bindle.resource.Resource
import com.klavs.bindle.uix.viewmodel.communities.PostViewModel
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage

@Composable
fun Posts(
    community: Community,
    numberOfMembers: Resource<Int>,
    numOfActiveEvents: Resource<Int>,
    onMembersClick: () -> Unit,
    rolePriority:Int,
    onCommentClick: (String) -> Unit
) {
    val viewModel: PostViewModel = hiltViewModel()
    val newPost by viewModel.newPost.collectAsState()
    val pagedPosts by viewModel.pagedPosts.collectAsState()
    val postList = remember { mutableStateListOf<Post>() }
    var isError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var firstPostLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.numOfComment.value) {
        val newCommentCount: Pair<String, Int>? by viewModel.numOfComment
        if (newCommentCount !=null){
        val index = postList.indexOf(postList.find { it.id == newCommentCount!!.first })
        postList[index] = postList[index].copy(numOfComments = newCommentCount!!.second)
        }
    }

    LaunchedEffect(true) {
        viewModel.listenToNewPost(communityId = community.id!!)
    }
    LaunchedEffect(newPost) {
        when (newPost) {
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
                if (newPost.data != null) {
                    Log.e("posts", "first post like count: ${newPost.data!!.numOfLikes}")
                    Log.e(
                        "posts",
                        "first post comment count: ${newPost.data!!.numOfComments}"
                    )
                    Log.e("posts", "first post is liked: ${newPost.data!!.liked}")
                    if (postList.isEmpty()) {
                        postList.add(0, newPost.data!!)
                        if (!firstPostLoaded) {
                            viewModel.getPostsWithPaging(
                                communityId = community.id!!,
                                pageSize = 3
                            )
                            firstPostLoaded = true
                        }
                    } else {
                        if (postList[0].id == newPost.data!!.id) {
                            postList[0] = newPost.data!!
                        } else {
                            postList.add(0, newPost.data!!)
                        }
                    }
                } else {
                    firstPostLoaded = true
                }
            }
        }
    }
    LaunchedEffect(pagedPosts) {
        when (pagedPosts) {
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
                postList.addAll(pagedPosts.data!!)
            }
        }
    }
    DisposableEffect(true) {
        onDispose {
            viewModel.newPostJob?.cancel()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            CommunityHeadLine(
                community = community,
                numberOfMembers = numberOfMembers,
                numOfActiveEvents = numOfActiveEvents,
                rolePriority = rolePriority,
                onMembersClick = onMembersClick
            )
        }

        if (postList.isNotEmpty()) {
            items(postList) { post ->
                PostRow(post = post,
                    onLikeClick = { liked ->
                        val index = postList.indexOf(postList.find { it.id == post.id })
                        if (liked) {
                            postList[index] = postList[index].copy(liked = true)
                            postList[index] = postList[index].copy(numOfLikes = (post.numOfLikes
                                ?: 0) + 1
                            )
                            viewModel.likeThePost(
                                postId = post.id,
                                communityId = community.id!!
                            )
                        } else {
                            postList[index] = postList[index].copy(liked = false)
                            postList[index] = postList[index].copy(numOfLikes = (post.numOfLikes?:0) -1 )
                            viewModel.undoLikeThePost(
                                postId = post.id,
                                communityId = community.id!!
                            )
                        }
                    },
                    onCommentClick = {
                        onCommentClick(post.id)
                        viewModel.listenToNewComment(communityId = community.id!!, postId = post.id)
                    })
                Spacer(Modifier.height(15.dp))
            }
            if (isLoading) {
                item{
                    Spacer(Modifier.height(5.dp))
                    Column(Modifier.fillMaxWidth()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                }

            } else if (viewModel.lastPost != null) {
                item {
                    Spacer(Modifier.height(5.dp))
                    Column(Modifier.fillMaxWidth()) {
                        TextButton(
                            onClick = {
                                viewModel.getPostsWithPaging(
                                    communityId = community.id!!,
                                    pageSize = 6
                                )
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Load more post")
                        }
                    }
                }

                if (isError) {
                    item {
                        Spacer(Modifier.height(5.dp))
                        Column(Modifier.fillMaxWidth()) {
                            Text(
                                "Posts could not be loaded, please try again later",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }

                }
            }
        } else {
            item {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No posts yet", style = MaterialTheme.typography.titleMedium)
                }
            }

        }

    }
}

@Composable
private fun PostRow(post: Post,
                    onLikeClick: (Boolean) -> Unit,
                    onCommentClick: () -> Unit) {

    Surface(
        shape = RoundedCornerShape(10.dp),
        shadowElevation = 5.dp,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(8.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(4f)
                ) {
                    Box(modifier = Modifier.size(40.dp)) {
                        if (post.senderImageUrl == null) {
                            Image(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = post.senderUserName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(CircleShape)
                                    .background(Color.LightGray, CircleShape)
                            )
                        } else {
                            GlideImage(
                                imageModel = { post.senderImageUrl.toUri() },
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(CircleShape),
                                loading = {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .fillMaxSize(0.5f)
                                            .align(Alignment.Center)
                                    )
                                }
                            )
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = post.senderUserName?:"",
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    convertMillisToLocalizeDate(post.date),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1.5f)
                )

            }
            Spacer(Modifier.height(10.dp))
            Text(
                post.content,
                style = MaterialTheme.typography.bodyMedium
            )
            if (post.imageUrl != null) {
                Spacer(Modifier.height(10.dp))
                Box(modifier = Modifier.fillMaxWidth(0.95f)) {
                    GlideImage(
                        imageModel = { post.imageUrl.toUri() },
                        modifier = Modifier.fillMaxWidth(),
                        loading = {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .fillMaxSize(0.2f)
                                    .align(Alignment.Center)
                            )
                        },
                        failure = {
                            Text(
                                "Image could not be loaded",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    )
                }
            }
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (post.liked == true) {
                        IconButton(
                            onClick = {
                                onLikeClick(false)
                            }
                        ) {
                            Icon(
                                Icons.Rounded.Favorite, null,
                                tint = Color.Red
                            )
                        }
                    } else {
                        IconButton(
                            onClick = {
                                onLikeClick(true)
                            }
                        ) {
                            Icon(Icons.Rounded.FavoriteBorder, null)
                        }
                    }
                    Text(post.numOfLikes.toString())
                }

                if (post.commentsOn) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onCommentClick
                        ) {
                            Icon(Icons.Rounded.ChatBubbleOutline, null)
                        }
                        Text(post.numOfComments.toString())
                    }
                }
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsBottomSheet(
    state: SheetState,
    communityId: String,
    postId: String,
    onDismiss: () -> Unit
) {
    var textingComment by remember { mutableStateOf("") }
    val viewModel: PostViewModel = hiltViewModel()
    val currentUser by viewModel.currentUser.collectAsState()
    val newComment by viewModel.newComment.collectAsState()
    val pagedComments by viewModel.pagedComments.collectAsState()
    val commentList = remember { mutableStateListOf<PostComment>() }
    var isError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var firstCommentLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(newComment) {
        when (newComment) {
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
                if (newComment.data != null) {
                    if (commentList.isEmpty()) {
                        commentList.add(0, newComment.data!!)
                        if (!firstCommentLoaded) {
                            viewModel.getCommentsWithPaging(
                                communityId = communityId,
                                postId = postId,
                                pageSize = 8
                            )
                            firstCommentLoaded = true
                        }
                    } else {
                        if (commentList[0].id == newComment.data!!.id) {
                            commentList[0] = newComment.data!!
                        } else {
                            commentList.add(0, newComment.data!!)
                        }
                    }
                } else {
                    firstCommentLoaded = true
                }
            }
        }
    }
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
    ModalBottomSheet(
        sheetState = state,
        onDismissRequest = {
            onDismiss()
            viewModel.getCommentCount(postId = postId, communityId = communityId)
            commentList.clear()
            viewModel.newCommentJob?.cancel()
            viewModel.lastComment = null
            viewModel.firstCommentLoaded.value = false
        }
    ) {
        Text(
            "Comments", style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 8.dp)
        )
        Spacer(Modifier.height(6.dp))
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .weight(7f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (commentList.isNotEmpty()) {
                    items(commentList) {
                        CommentRow(comment = it)
                        Spacer(Modifier.height(16.dp))
                    }
                    if (isLoading) {
                        item {
                            Spacer(Modifier.height(5.dp))
                            Column(Modifier.fillMaxWidth()) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                            }
                        }
                    } else if (viewModel.lastComment != null) {
                        item {
                            Spacer(Modifier.height(5.dp))
                            Column(Modifier.fillMaxWidth()) {
                                TextButton(
                                    onClick = {
                                        viewModel.getCommentsWithPaging(
                                            communityId = communityId,
                                            postId = postId,
                                            pageSize = 8
                                        )
                                    },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text("Load more comments")
                                }
                            }
                        }
                        if (isError) {
                            item {
                                Spacer(Modifier.height(5.dp))
                                Column(Modifier.fillMaxWidth()) {
                                    Text(
                                        "Comments could not be loaded, please try again later",
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.titleSmall,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
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
                                text = "No comments yet",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
            CommentTextField(value = textingComment,
                onValueChange = { textingComment = it },
                onSendClick = {
                    viewModel.writeComment(
                        comment = PostComment(
                            senderUid = currentUser!!.uid,
                            commentText = textingComment,
                            date = Timestamp.now().toDate().time
                        ),
                        communityId = communityId,
                        postId = postId
                    )
                    textingComment = ""
                })
        }

    }
}

@Composable
private fun CommentTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: PostViewModel = hiltViewModel()
    val currentUser by viewModel.currentUser.collectAsState()
    val focusRequester = remember { FocusRequester() }
    var textIsEmpty by remember { mutableStateOf(false) }

    LaunchedEffect(true) {
        focusRequester.requestFocus()
    }

    TextField(
        isError = textIsEmpty,
        leadingIcon = {
            if (currentUser != null) {
                Box(modifier = Modifier.size(TextFieldDefaults.MinHeight * 0.7f)) {
                    if (currentUser!!.photoUrl != null) {
                        GlideImage(
                            imageModel = { currentUser!!.photoUrl },
                            modifier = Modifier
                                .matchParentSize()
                                .clip(CircleShape),
                            loading = {
                                CircularProgressIndicator(
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
                            }
                        )
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
            IconButton(
                onClick = {
                    if (value.isNotEmpty()) {
                        if (currentUser != null) {
                            onSendClick()
                        } else {
                            Toast.makeText(
                                context,
                                "Please login to comment",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        textIsEmpty = true
                    }
                }
            ) {
                Icon(imageVector = Icons.AutoMirrored.Rounded.Send, contentDescription = "send")
            }
        },
        shape = RoundedCornerShape(15.dp),
        placeholder = { Text("Write a comment...") },
        supportingText = if (textIsEmpty) {
            {
                Text("Comment can not be empty")
            }
        } else null,
        colors = TextFieldDefaults.colors(
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            errorContainerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        )
    )
}

@Composable
private fun CommentRow(comment: PostComment) {
    Row(
        Modifier.fillMaxWidth(0.95f)
    ) {
        Box(
            modifier = Modifier.size(TextFieldDefaults.MinHeight * 0.7f)
        ) {
            if (comment.senderProfileImageUrl != null) {
                GlideImage(
                    imageModel = { comment.senderProfileImageUrl.toUri() },
                    imageOptions = ImageOptions(contentScale = ContentScale.Crop),
                    modifier = Modifier
                        .matchParentSize()
                        .clip(CircleShape),
                    loading = {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .fillMaxSize(0.5f)
                                .align(Alignment.Center)
                        )
                    }
                )
            } else {
                Image(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = comment.senderUserName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .matchParentSize()
                        .clip(CircleShape)
                        .background(Color.LightGray, CircleShape)
                )
            }
        }
        Spacer(Modifier.width(9.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(comment.senderUserName, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.width(10.dp))
                Text(
                    convertMillisToLocalizeDate(comment.date),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(comment.commentText, style = MaterialTheme.typography.bodyMedium)
        }


    }
}


@Preview
@Composable
private fun PostsPreview() {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        PostRow(
            post = Post(
                senderUserName = "furkan",
                content = "Deneme yazısı",
                commentsOn = true,
            ),
            onLikeClick = {},
            onCommentClick = {}
        )
        CommentRow(
            comment = PostComment(
                senderUserName = "Furkan",
                commentText = "bu bir deneme yorumudurbu bir deneme yorumudur" +
                        "bu bir deneme yorumudurbu bir deneme yorumudur" +
                        "bu bir deneme yorumudurbu bir deneme yorumudur" +
                        "bu bir deneme yorumudurbu bir deneme yorumudur",
                date = System.currentTimeMillis() - 1000L,
                isMyComment = true
            )
        )
    }

}
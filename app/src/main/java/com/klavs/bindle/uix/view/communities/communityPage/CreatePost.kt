package com.klavs.bindle.uix.view.communities.communityPage

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Comment
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CommentsDisabled
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import coil3.compose.rememberAsyncImagePainter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestOptions
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.klavs.bindle.R
import com.klavs.bindle.data.entity.Post
import com.klavs.bindle.resource.Resource
import com.klavs.bindle.uix.viewmodel.communities.CommunityPageViewModel
import com.klavs.bindle.uix.viewmodel.communities.PostViewModel
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun CreatePost(
    communityId: String,
    currentUser: FirebaseUser,
    navController: NavHostController,
    viewModel: CommunityPageViewModel
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val permissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionState =
            rememberPermissionState(permission = android.Manifest.permission.READ_MEDIA_IMAGES)
        permissionState

    } else {
        val permissionState =
            rememberPermissionState(permission = android.Manifest.permission.READ_EXTERNAL_STORAGE)
        permissionState
    }
    var mediaIsGranted by remember {
        mutableStateOf(
            permissionState.status.isGranted
        )
    }

    var selectedImageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }
    val hasLaunched = remember { mutableStateOf(false) }
    LaunchedEffect(key1 = permissionState.status.isGranted) {
        mediaIsGranted = permissionState.status.isGranted
        if (hasLaunched.value) {
            if (mediaIsGranted) {
                galleryLauncher.launch("image/*")
            }
        } else {
            hasLaunched.value = true
        }
    }

    var postText by remember { mutableStateOf("") }
    var postTextIsEmpty by remember { mutableStateOf(false) }
    val characterLimit = 400
    var commentsOn by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.createPostResource.value) {
        when (val resource = viewModel.createPostResource.value) {
            is Resource.Error -> {
                isLoading = false
                scope.launch {
                    snackbarHostState.showSnackbar(resource.messageResource?.let { context.getString(it) }?:"")
                }
            }

            is Resource.Idle -> {
                isLoading = false
            }

            is Resource.Loading -> {
                isLoading = true
            }

            is Resource.Success -> {
                isLoading = false
                navController.popBackStack()
            }
        }
    }
    if (isLoading) {
        BackHandler(true) {}
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .zIndex(2f)
        ) {
            CircularWavyProgressIndicator(Modifier.align(Alignment.Center))
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
        TopAppBar(
            navigationIcon = {
                IconButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "back"
                    )
                }
            },
            title = { Text(stringResource(R.string.create_post), style = MaterialTheme.typography.titleSmall) }
        )
    }) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(top = innerPadding.calculateTopPadding())
                .fillMaxSize()
        ) {
            Column(
                Modifier
                    .fillMaxWidth(0.95f)
                    .align(Alignment.TopCenter)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp)) {
                        if (currentUser.photoUrl == null) {
                            Image(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = "",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(CircleShape)
                                    .background(Color.LightGray, CircleShape)
                            )
                        } else {
                            GlideImage(
                                imageModel = { currentUser.photoUrl },
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
                                loading = {
                                    CircularWavyProgressIndicator(
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
                        currentUser.displayName ?: "",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                Spacer(Modifier.height(10.dp))
                TextField(
                    isError = postTextIsEmpty,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        errorContainerColor = Color.Transparent
                    ),
                    value = postText,
                    onValueChange = {
                        postTextIsEmpty = false
                        if (it.length <= characterLimit) {
                            postText = it
                        }
                    },
                    minLines = 8,
                    supportingText = {
                        Text(
                            if (postTextIsEmpty) stringResource(R.string.empty_post_warning_message)
                            else "${postText.length}/${characterLimit}"
                        )
                    },
                    placeholder = {
                        Text(stringResource(R.string.what_is_on_your_mind))
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                if (selectedImageUri == null) {
                    IconButton(
                        onClick = {
                            if (mediaIsGranted) {
                                galleryLauncher.launch("image/*")
                            } else {
                                if (!permissionState.status.shouldShowRationale) {

                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = context.getString(R.string.media_permission_rationale),
                                            withDismissAction = true,
                                            duration = SnackbarDuration.Long,
                                            actionLabel = context.getString(R.string.permission_settings)
                                        )
                                        if (SnackbarResult.ActionPerformed == result){
                                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                data = Uri.fromParts("package", context.packageName, null)
                                            }
                                            context.startActivity(intent)
                                        }
                                    }
                                }else {
                                    permissionState.launchPermissionRequest()
                                }
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Rounded.AddAPhoto, "add image")
                    }
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "remove image",
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable {
                                selectedImageUri = null
                            }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.90f)
                            .heightIn(max = 300.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(model = selectedImageUri),
                            contentDescription = "image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                }
                HorizontalDivider()
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.comments_on), style = MaterialTheme.typography.titleMedium)
                    Switch(
                        checked = commentsOn,
                        onCheckedChange = {
                            commentsOn = it
                        },
                        thumbContent = {
                            if (commentsOn) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Comment,
                                    contentDescription = "comments on",
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.CommentsDisabled,
                                    contentDescription = "comments on",
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        }
                    )
                }
                Spacer(Modifier.height(15.dp))
                FloatingActionButton(
                    onClick = {
                        if (postText.isNotBlank() || selectedImageUri != null) {
                            val postModel = Post(
                                uid = currentUser.uid,
                                content = postText,
                                date = Timestamp.now(),
                                imageUrl = selectedImageUri?.toString(),
                                commentsOn = commentsOn
                            )
                            viewModel.createPost(
                                post = postModel,
                                communityId = communityId,
                                currentUser = currentUser
                            )
                        } else {
                            postTextIsEmpty = true
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.post_confirm), modifier = Modifier.padding(horizontal = 5.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = "send"
                        )
                    }

                }
                Spacer(Modifier.height(15.dp))
            }
        }

    }

}

@Composable
@Preview
private fun CreatePostPreview() {
    //CreatePost(communityId = "", rememberNavController())
}
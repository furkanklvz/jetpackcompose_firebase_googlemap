package com.klavs.bindle.uix.view.communities

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowRightAlt
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PostAdd
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.firebase.Timestamp
import com.klavs.bindle.R
import com.klavs.bindle.data.entity.sealedclasses.BottomNavItem
import com.klavs.bindle.data.entity.community.CommunityFirestore
import com.klavs.bindle.resource.Resource
import com.klavs.bindle.ui.theme.Green2
import com.klavs.bindle.uix.viewmodel.NavHostViewModel
import com.klavs.bindle.uix.viewmodel.communities.CommunityViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun CreateCommunity(
    navController: NavHostController,
    myUid: String,
    navHostViewModel: NavHostViewModel,
    viewModel: CommunityViewModel
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var communityName by remember { mutableStateOf("") }
    var communityDescription by remember { mutableStateOf("") }
    var onlyAdminsCanCreateEvent by remember { mutableStateOf(false) }
    var onlyAdminsCanCreatePost by remember { mutableStateOf(false) }
    var joinWithRequest by remember { mutableStateOf(false) }
    var communityNameIsEmpty by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val userResourceFlow by navHostViewModel.userResourceFlow.collectAsState()

    val permissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionState =
            rememberPermissionState(permission = Manifest.permission.READ_MEDIA_IMAGES)
        permissionState

    } else {
        val permissionState =
            rememberPermissionState(permission = Manifest.permission.READ_EXTERNAL_STORAGE)
        permissionState
    }
    val mediaIsGranted = remember {
        mutableStateOf(
            permissionState.status.isGranted
        )
    }

    val selectedImageUri = remember {
        mutableStateOf<Uri?>(null)
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri.value = uri
        }
    }
    val hasLaunched = remember { mutableStateOf(false) }
    LaunchedEffect(key1 = permissionState.status.isGranted) {
        mediaIsGranted.value = permissionState.status.isGranted
        if (hasLaunched.value) {
            if (mediaIsGranted.value) {
                galleryLauncher.launch("image/*")
            }
        } else {
            hasLaunched.value = true
        }
    }

    LaunchedEffect(key1 = viewModel.createCommunityState.value) {
        when (val result = viewModel.createCommunityState.value) {
            is Resource.Error -> {
                isLoading = false
                isError = true
                errorMessage = result.messageResource?.let { context.getString(it) } ?: ""
            }

            is Resource.Idle -> {}
            is Resource.Loading -> {
                isLoading = true
            }

            is Resource.Success -> {
                isLoading = false
                navController.navigate(BottomNavItem.Communities.route) {
                    popUpTo("create_community") {
                        inclusive = true
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "back"
                        )
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.create_community),
                        style = MaterialTheme.typography.titleSmall
                    )
                })
        }) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .zIndex(3f)
                ) {
                    CircularWavyProgressIndicator()
                }
            }
            if (isError) {
                AlertDialog(
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = "error"
                        )
                    },
                    title = { Text(text = stringResource(R.string.error)) },
                    text = { Text(text = errorMessage) },
                    onDismissRequest = { isError = false },
                    confirmButton = {
                        Button(onClick = { isError = false }) {
                            Text(text = stringResource(R.string.okay))
                        }
                    },
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.community_picture),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                    ) {
                        if (selectedImageUri.value == null) {
                            Image(
                                imageVector = Icons.Rounded.Groups,
                                contentDescription = "picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        CircleShape
                                    )
                                    .clickable {
                                        if (mediaIsGranted.value) {
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
                                                    if (SnackbarResult.ActionPerformed == result) {
                                                        val intent =
                                                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                                data = Uri.fromParts(
                                                                    "package",
                                                                    context.packageName,
                                                                    null
                                                                )
                                                            }
                                                        context.startActivity(intent)
                                                    }
                                                }
                                            } else {
                                                permissionState.launchPermissionRequest()
                                            }
                                        }
                                    }
                            )
                            IconButton(
                                onClick = {
                                    if (mediaIsGranted.value) {
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
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AddPhotoAlternate,
                                    contentDescription = "add photo",
                                    tint = Color.Black,
                                    modifier = Modifier
                                        .background(
                                            Green2,
                                            CircleShape
                                        )
                                        .padding(5.dp)
                                )
                            }
                        } else {
                            Image(
                                painter = rememberAsyncImagePainter(model = selectedImageUri.value),
                                contentScale = ContentScale.Crop,
                                contentDescription = "community picture",
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(CircleShape)
                                    .clickable {
                                        if (mediaIsGranted.value) {
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
                            )
                        }
                    }
                    if (selectedImageUri.value != null) {
                        TextButton(
                            onClick = {
                                selectedImageUri.value = null
                            },
                        ) {
                            Text(text = stringResource(R.string.remove_picture))
                        }
                    }

                }
                Column(Modifier.padding(10.dp)) {
                    TextField(
                        isError = communityNameIsEmpty,
                        label = { Text(text = stringResource(R.string.community_name)) },
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            errorContainerColor = Color.Transparent,
                        ),
                        modifier = Modifier.fillMaxWidth(0.9f),
                        value = communityName,
                        onValueChange = {
                            if (it.isNotBlank()) {
                                communityNameIsEmpty = false
                            }
                            if (it.length <= 40) {
                                communityName = it
                            }
                        },
                        supportingText = {
                            Column {
                                if (communityNameIsEmpty) {
                                    Text(stringResource(R.string.community_name_cannot_be_empty))
                                }
                                Text(text = "${communityName.length}/40")
                            }

                        }
                    )
                }
                Column(Modifier.padding(10.dp)) {
                    Text(
                        text = stringResource(R.string.community_description),
                        style = MaterialTheme.typography.titleMedium
                    )
                    TextField(
                        placeholder = {
                            Text(
                                text = stringResource(R.string.community_description_placeholder),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        minLines = 5,
                        maxLines = 12,
                        modifier = Modifier.fillMaxWidth(0.9f),
                        shape = CircleShape,
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                        ),
                        value = communityDescription,
                        onValueChange = {
                            if (it.length <= 400) {
                                communityDescription = it
                            }
                        },
                        supportingText = { Text(text = "${communityDescription.length}/400") }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.community_settings),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
                Spacer(modifier = Modifier.height(10.dp))
                ListItem(
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Outlined.Celebration,
                            contentDescription = "creating event method"
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = onlyAdminsCanCreateEvent,
                            onCheckedChange = {
                                onlyAdminsCanCreateEvent = !onlyAdminsCanCreateEvent
                            },
                            thumbContent = {
                                if (onlyAdminsCanCreateEvent) {
                                    Icon(
                                        imageVector = Icons.Outlined.Lock,
                                        contentDescription = "on",
                                        modifier = Modifier.size(SwitchDefaults.IconSize)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Outlined.Public,
                                        contentDescription = "off",
                                        modifier = Modifier.size(SwitchDefaults.IconSize)
                                    )
                                }
                            }
                        )
                    },
                    headlineContent = {
                        Text(
                            text = stringResource(R.string.only_admins_can_create_events_switch_text),
                        )
                    },
                    supportingContent = {
                        if (!onlyAdminsCanCreateEvent) {
                            Text(stringResource(R.string.only_admins_can_create_events_switch_description))
                        }
                    }
                )

                Spacer(modifier = Modifier.height(18.dp))
                ListItem(
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Outlined.PostAdd,
                            contentDescription = "sharing post method"
                        )
                    },
                    headlineContent = {
                        Text(
                            text = stringResource(R.string.only_admins_can_share_posts_switch_text),
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = onlyAdminsCanCreatePost,
                            onCheckedChange = {
                                onlyAdminsCanCreatePost = !onlyAdminsCanCreatePost
                            },
                            thumbContent = {
                                if (onlyAdminsCanCreatePost) {
                                    Icon(
                                        imageVector = Icons.Outlined.Lock,
                                        contentDescription = "on",
                                        modifier = Modifier.size(SwitchDefaults.IconSize)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Outlined.Public,
                                        contentDescription = "off",
                                        modifier = Modifier.size(SwitchDefaults.IconSize)
                                    )
                                }
                            }
                        )
                    },
                    supportingContent = {
                        if (!onlyAdminsCanCreatePost) {
                            Text(
                                stringResource(R.string.only_admins_can_share_posts_switch_description),
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(18.dp))
                ListItem(
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Outlined.PersonAdd,
                            contentDescription = "join method"
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = joinWithRequest,
                            onCheckedChange = {
                                joinWithRequest = !joinWithRequest
                            }
                        )
                    },
                    headlineContent = {
                        Text(
                            text = stringResource(R.string.join_community_with_request_switch_text),
                        )
                    },
                    supportingContent = {
                        if (!joinWithRequest) {
                            Text(
                                stringResource(R.string.join_community_with_request_switch_description),
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    stringResource(R.string.creating_community_supporting_content),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
                Spacer(modifier = Modifier.height(5.dp))
                FloatingActionButton(
                    onClick = {
                        if (userResourceFlow is Resource.Success && userResourceFlow.data!!.tickets >= 2) {
                            if (communityName.isBlank()) {
                                communityNameIsEmpty = true
                            } else {
                                val community = CommunityFirestore(
                                    name = communityName.trim(),
                                    description = communityDescription.trim(),
                                    eventCreationRestriction = onlyAdminsCanCreateEvent,
                                    postSharingRestriction = onlyAdminsCanCreatePost,
                                    participationByRequestOnly = joinWithRequest,
                                    communityPictureUrl = selectedImageUri.value?.toString(),
                                    creatorUid = myUid,
                                    creationTimestamp = Timestamp.now()
                                )
                                viewModel.createCommunity(
                                    community = community,
                                    myUid = myUid,
                                    newTickets = userResourceFlow.data!!.tickets - 2
                                )
                            }
                        } else {
                            scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.something_went_wrong_try_again_later)) }
                        }
                    },
                    modifier = Modifier
                        .padding(end = 5.dp)
                        .align(Alignment.End)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.create),
                            modifier = Modifier.padding(end = 4.dp, start = 7.dp)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.NavigateNext,
                            contentDescription = "ok"
                        )
                    }

                }
                Spacer(modifier = Modifier.height(20.dp))

            }

        }

    }

}

@Preview
@Composable
private fun CreateCommunityPreview() {
    //CreateCommunity(navController = rememberNavController())
}
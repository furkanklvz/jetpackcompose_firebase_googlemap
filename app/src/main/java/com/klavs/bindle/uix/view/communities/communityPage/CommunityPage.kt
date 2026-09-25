package com.klavs.bindle.uix.view.communities.communityPage

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.RotateRight
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.outlined.AddModerator
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.PersonAddAlt1
import androidx.compose.material.icons.outlined.RemoveModerator
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.PersonRemove
import androidx.compose.material.icons.rounded.PostAdd
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.firebase.auth.FirebaseUser
import com.klavs.bindle.R
import com.klavs.bindle.data.entity.community.Community
import com.klavs.bindle.data.entity.sealedclasses.CommunityRoles
import com.klavs.bindle.data.entity.Event
import com.klavs.bindle.data.entity.Post
import com.klavs.bindle.data.entity.PostComment
import com.klavs.bindle.data.entity.User
import com.klavs.bindle.resource.Resource
import com.klavs.bindle.uix.view.communities.communityPage.bottomsheets.MembersBottomSheet
import com.klavs.bindle.uix.view.communities.communityPage.bottomsheets.RequestsBottomSheet
import com.klavs.bindle.uix.view.communities.communityPage.bottomsheets.SettingsBottomSheet
import com.klavs.bindle.uix.view.communities.communityPage.bottomsheets.UpdateCommunityNameOrDescriptionBottomSheet
import com.klavs.bindle.util.EventBottomSheet
import com.klavs.bindle.uix.viewmodel.NavHostViewModel
import com.klavs.bindle.uix.viewmodel.communities.CommunityPageViewModel
import com.klavs.bindle.uix.viewmodel.communities.PostViewModel
import com.klavs.bindle.util.TicketDialog
import com.klavs.bindle.util.UnverifiedAccountAlertDialog
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CommunityPage(
    navController: NavHostController,
    communityId: String,
    vmPost: PostViewModel,
    currentUser: FirebaseUser?,
    navHostViewModel: NavHostViewModel,
    viewModel: CommunityPageViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val community by viewModel.community.collectAsState()
    val rolePriority by viewModel.userRolePriority.collectAsState()
    val didISendRequest by viewModel.didISendRequest.collectAsState()
    val userResource by navHostViewModel.userResourceFlow.collectAsState()

    LaunchedEffect(rolePriority) {
        if (rolePriority != CommunityRoles.Member.rolePriority) {
            viewModel.getNumberOfRequests(communityId)
        }
    }

    var firstRolePriorityEffectHasLaunched by remember { mutableStateOf(false) }
    var secondRolePriorityEffectHasLaunched by remember { mutableStateOf(false) }
    LaunchedEffect(rolePriority) {
        if (firstRolePriorityEffectHasLaunched) {
            if (secondRolePriorityEffectHasLaunched) {
                if (viewModel.leaveCommunityState.value !is Resource.Success) {
                    when (rolePriority) {
                        CommunityRoles.Moderator.rolePriority -> {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = context.getString(R.string.you_are_now_moderator)
                                )
                            }
                        }

                        CommunityRoles.Admin.rolePriority -> {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = context.getString(R.string.you_are_now_admin)
                                )
                            }
                        }

                        CommunityRoles.Member.rolePriority -> {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = context.getString(R.string.you_are_now_member)
                                )
                            }
                        }

                    }
                }
            } else {
                secondRolePriorityEffectHasLaunched = true
            }
        } else {
            firstRolePriorityEffectHasLaunched = true
        }
    }

    LaunchedEffect(viewModel.changeAdminState.value) {
        when (viewModel.changeAdminState.value) {
            is Resource.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(viewModel.changeAdminState.value.messageResource?.let {
                        context.getString(
                            it
                        )
                    } ?: "")
                }
            }

            is Resource.Success -> {
                scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.admin_changed)) }
            }

            else -> {}
        }
    }

    LaunchedEffect(true) {
        if (community !is Resource.Success) {
            viewModel.listenToCommunity(communityId)
            viewModel.getNumOfMembers(communityId)
            viewModel.getNumOfEvents(communityId)
        }

        if (viewModel.myStatusJob == null && currentUser != null) {
            viewModel.listenToMyStatus(
                communityId,
                myUid = currentUser.uid
            )
        }

    }

    LaunchedEffect(viewModel.leaveCommunityState.value) {
        when (viewModel.leaveCommunityState.value) {
            is Resource.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(viewModel.leaveCommunityState.value.messageResource?.let {
                        context.getString(
                            it
                        )
                    } ?: "")
                }
                viewModel.leaveCommunityState.value = Resource.Idle()
            }

            is Resource.Success -> {
                viewModel.leaveCommunityState.value = Resource.Idle()
            }

            else -> {}
        }
    }

    DisposableEffect(true) {
        onDispose {
            viewModel.myStatusJob?.cancel()
            viewModel.stopListeningToDidISendRequest()
        }
    }
    when (community) {
        is Resource.Error -> {
            Scaffold(topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(
                            onClick = { navController.popBackStack() }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "go back"
                            )
                        }
                    }
                )
            }) { innerPadding ->
                Box(
                    Modifier
                        .padding(top = innerPadding.calculateTopPadding())
                        .fillMaxSize()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = "community has been deleted"
                        )
                        Text(community.messageResource?.let { stringResource(it) } ?: "")
                    }
                }
            }

        }

        is Resource.Idle -> {}
        is Resource.Loading -> {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularWavyProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }

        is Resource.Success -> {
            Content(
                community = community.data!!,
                navController = navController,
                currentUser = currentUser,
                rolePriority = rolePriority,
                vmPost = vmPost,
                viewModel = viewModel,
                navHostViewModel = navHostViewModel,
                didISendRequest = didISendRequest,
                scope = scope,
                snackbarHostState = snackbarHostState,
                userResource = userResource
            )
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Content(
    community: Community,
    rolePriority: Int?,
    currentUser: FirebaseUser?,
    vmPost: PostViewModel,
    didISendRequest: Boolean?,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    userResource: Resource<User>,
    navHostViewModel: NavHostViewModel,
    viewModel: CommunityPageViewModel,
    navController: NavHostController
) {
    val eventBottomSheetState = rememberModalBottomSheetState()
    var showEventBottomSheet by remember { mutableStateOf<Event?>(null) }
    var showMembersBottomSheet by remember { mutableStateOf(false) }
    val membersBottomSheetState = rememberModalBottomSheetState()
    var showRequestsBottomSheet by remember { mutableStateOf(false) }
    val requestsBottomSheetState = rememberModalBottomSheetState()
    var showSettingsBottomSheet by remember { mutableStateOf(false) }
    val settingsBottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    var showTicketDialog by remember { mutableStateOf(false) }
    var commentsForPost by remember { mutableStateOf<String?>(null) }
    val commentsBottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val updateNameOrDescriptionBottomSheetState = rememberModalBottomSheetState()
    var updateNameOrDescriptionBottomSheetValue by remember {
        mutableStateOf<Pair<Boolean, String>?>(
            null
        )
    }
    val context = LocalContext.current
    var userToRemove by remember { mutableStateOf<String?>(null) }
    var userToBeModerator by remember { mutableStateOf<String?>(null) }
    var userToBeRemovedFromModerator by remember { mutableStateOf<String?>(null) }
    var userToBeAdmin by remember { mutableStateOf<String?>(null) }
    val numberOfMembers by viewModel.numberOfMembers.collectAsState()
    val numberOfRequests by viewModel.numberOfRequests.collectAsState()
    val numOfEvents by viewModel.numOfEvents.collectAsState()
    var openSharingDialog by remember { mutableStateOf(false) }
    var openLeavingDialog by remember { mutableStateOf(false) }
    var reportDialog by remember { mutableStateOf<Post?>(null) }
    var reportUserDialog by remember { mutableStateOf<PostComment?>(null) }
    var showDeleteCommunityDialog by remember { mutableStateOf(false) }
    var showUnverifiedAccountAlertDialog by remember { mutableStateOf(false) }
    val userResourceFlow by navHostViewModel.userResourceFlow.collectAsState()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            if ((!community.postSharingRestriction || rolePriority != CommunityRoles.Member.rolePriority) && (rolePriority != null && currentUser != null)) {
                Log.d("communityPage", "rolePriorityin community page: $rolePriority")
                FloatingActionButton(
                    onClick = {
                        navController.navigate("createPost/${community.id}")
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.PostAdd,
                            contentDescription = null,
                            modifier = Modifier.padding(start = 5.dp)
                        )
                        Text(
                            stringResource(R.string.create_post),
                            modifier = Modifier.padding(horizontal = 5.dp)
                        )
                    }
                }
            }
        },
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                actions = {
                    if (rolePriority != null) {
                        if (rolePriority != CommunityRoles.Member.rolePriority) {
                            BadgedBox(
                                badge = {
                                    if ((numberOfRequests ?: 0) > 0) {
                                        Badge {
                                            Text(
                                                text = if ((numberOfRequests ?: 0) > 99) "99+"
                                                else numberOfRequests.toString()
                                            )
                                        }
                                    }
                                }
                            ) {
                                IconButton(
                                    onClick = {
                                        showRequestsBottomSheet = true
                                        viewModel.getRequestsWithPaging(
                                            communityId = community.id,
                                            pageSize = 10
                                        )
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.PersonAdd,
                                        contentDescription = "joining requests"
                                    )
                                }
                            }
                            IconButton(
                                onClick = { showSettingsBottomSheet = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Settings,
                                    contentDescription = "settings"
                                )
                            }
                        }
                        Box {
                            var expanded by remember { mutableStateOf(false) }
                            IconButton(
                                onClick = {
                                    expanded = !expanded
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MoreVert,
                                    contentDescription = "menu"
                                )
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.PersonAddAlt1,
                                            contentDescription = "share"
                                        )
                                    },
                                    text = {
                                        Text(
                                            text = stringResource(R.string.invite_people),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    },
                                    onClick = {
                                        openSharingDialog = true
                                        expanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.ExitToApp,
                                            contentDescription = "leave the community"
                                        )
                                    },
                                    text = {
                                        Text(
                                            text = stringResource(R.string.leave_the_community),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    },
                                    onClick = {
                                        openLeavingDialog = true
                                        expanded = false
                                    }
                                )
                                if (rolePriority == CommunityRoles.Admin.rolePriority) {
                                    DropdownMenuItem(
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Rounded.DeleteOutline,
                                                contentDescription = "delete the community",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        },
                                        text = {
                                            Text(
                                                text = stringResource(R.string.delete_the_community),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        },
                                        onClick = {
                                            showDeleteCommunityDialog = true
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "back"
                        )
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.community_page),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            if (reportUserDialog != null) {
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
                                        if (currentUser != null) {
                                            vmPost.sendUserReport(
                                                reportedUser = reportUserDialog!!.senderUid,
                                                uid = currentUser.uid,
                                                reportType = selectedContent!!,
                                                description = optionalDescription,
                                                messageId = reportUserDialog!!.id,
                                                messageContent = reportUserDialog!!.commentText
                                            )
                                        }
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
            if (reportDialog != null) {
                var selectedContent by remember { mutableStateOf<Int?>(null) }
                var optionalMessage by remember { mutableStateOf("") }
                val messageLimit = 100
                Dialog(onDismissRequest = { reportDialog = null }) {
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
                                    onClick = { reportDialog = null }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                        contentDescription = "back"
                                    )
                                }
                                OutlinedButton(
                                    enabled = selectedContent != null,
                                    onClick = {
                                        if (currentUser != null) {
                                            vmPost.sendReport(
                                                description = optionalMessage,
                                                postId = reportDialog!!.id,
                                                postOwnerUid = reportDialog!!.uid,
                                                uid = currentUser.uid,
                                                reportType = selectedContent!!
                                            )

                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    context.getString(R.string.report_sent)
                                                )
                                            }
                                        }
                                        reportDialog = null
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
            if (showDeleteCommunityDialog) {
                AlertDialog(
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.WarningAmber,
                            contentDescription = "warning"
                        )
                    },
                    text = {
                        Text(stringResource(R.string.delete_the_community_alert_dialog_text))
                    },
                    confirmButton = {
                        Button(
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            onClick = {
                                community.id.let {
                                    if (currentUser != null) {
                                        viewModel.deleteTheCommunity(
                                            communityId = it
                                        )
                                    }
                                }
                                showDeleteCommunityDialog = false
                            }
                        ) {
                            Text(stringResource(R.string.delete_the_community))
                        }
                    },
                    dismissButton = {
                        IconButton(
                            modifier = Modifier.padding(end = 15.dp),
                            onClick = { showDeleteCommunityDialog = false }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "cancel"
                            )
                        }
                    },
                    onDismissRequest = { showDeleteCommunityDialog = false }
                )
            }
            if (showTicketDialog) {
                if (userResourceFlow is Resource.Success) {
                    if (currentUser != null) {
                        TicketDialog(
                            onDismiss = { showTicketDialog = false },
                            uid = currentUser.uid,
                            tickets = userResourceFlow.data!!.tickets,
                            paddingValues = innerPadding
                        )
                    } else {
                        showTicketDialog = false
                    }
                } else {
                    showTicketDialog = false
                }
            }
            if (showUnverifiedAccountAlertDialog) {
                UnverifiedAccountAlertDialog(
                    onDismiss = { showUnverifiedAccountAlertDialog = false }
                ) {
                    navController.navigate("menu_profile")
                }
            }
            if (openSharingDialog) {
                ShareDialog(communityId = community.id) { openSharingDialog = false }
            }
            if (openLeavingDialog) {
                LeavingDialog(
                    userRolePriority = rolePriority,
                    onConfirm = {
                        if (currentUser != null) {
                            viewModel.leaveTheCommunity(
                                communityId = community.id,
                                myUid = currentUser.uid
                            )
                        }
                        openLeavingDialog = false
                    }) {
                    openLeavingDialog = false
                }
            }
            if (userToRemove != null) {
                RemoveMemberDialog(
                    onConfirm = {
                        viewModel.removeMember(communityId = community.id, uid = userToRemove!!)
                        userToRemove = null
                    }
                ) { userToRemove = null }
            }
            if (userToBeModerator != null) {
                PromoteDialog(
                    onConfirm = {
                        viewModel.promoteMember(
                            communityId = community.id,
                            uid = userToBeModerator!!
                        )
                        userToBeModerator = null
                    }
                ) { userToBeModerator = null }
            }
            if (userToBeRemovedFromModerator != null) {
                DemoteDialog(
                    onConfirm = {
                        viewModel.demoteMember(
                            communityId = community.id,
                            uid = userToBeRemovedFromModerator!!
                        )
                        userToBeRemovedFromModerator = null
                    }
                ) { userToBeRemovedFromModerator = null }
            }
            if (userToBeAdmin != null) {
                if (currentUser != null) {
                    MakeAdminDialog(
                        onConfirm = {
                            viewModel.changeAdmin(
                                communityId = community.id, uid = userToBeAdmin!!,
                                myUid = currentUser.uid
                            )
                            userToBeAdmin = null
                        },
                    ) { userToBeAdmin = null }
                }
            }
            if (showMembersBottomSheet && rolePriority != null) {
                MembersBottomSheet(
                    state = membersBottomSheetState,
                    userRole = rolePriority,
                    numOfMembers = numberOfMembers,
                    communityId = community.id,
                    onDismiss = {
                        scope.launch { membersBottomSheetState.hide() }.invokeOnCompletion {
                            if (!membersBottomSheetState.isVisible) {
                                showMembersBottomSheet = false
                            }
                        }
                        viewModel.lastMember = null
                        viewModel.getNumOfMembers(community.id)
                        viewModel.removeMemberState.value = Resource.Idle()
                        viewModel.promoteMemberState.value = Resource.Idle()
                        viewModel.demoteMemberState.value = Resource.Idle()
                    },
                    onDeleteUser = { userToRemove = it },
                    onAddModeratorUser = { userToBeModerator = it },
                    onRemoveModeratorUser = { userToBeRemovedFromModerator = it },
                    onMakeAdminUser = { userToBeAdmin = it },
                    viewModel = viewModel
                )
            }
            if (showEventBottomSheet != null) {
                if (currentUser != null) {
                    EventBottomSheet(
                        onDismiss = {
                            showEventBottomSheet = null
                        },
                        sheetState = eventBottomSheetState,
                        context = LocalContext.current,
                        onCommunityClick = { navController.navigate("community_page/$it") },
                        currentUser = currentUser,
                        event = showEventBottomSheet!!,
                        navHostViewModel = navHostViewModel,
                        showTicketDialog = { showTicketDialog = true },
                        showUnverifiedAccountAlertDialog = {
                            showUnverifiedAccountAlertDialog = true
                        },
                        navigateToEventPage = {
                            navController.navigate("event_page/${showEventBottomSheet!!.id}")
                        },
                        onLogInClick = { navController.navigate("log_in") }
                    )
                }
            }
            if (updateNameOrDescriptionBottomSheetValue != null) {
                UpdateCommunityNameOrDescriptionBottomSheet(
                    isName = updateNameOrDescriptionBottomSheetValue!!.first,
                    state = updateNameOrDescriptionBottomSheetState,
                    value = updateNameOrDescriptionBottomSheetValue!!.second,
                    onNameChange = {
                        viewModel.updateCommunityField(
                            communityId = community.id,
                            changedFieldName = "name",
                            newValue = it
                        )
                    },
                    onDescriptionChange = {
                        viewModel.updateCommunityField(
                            communityId = community.id,
                            changedFieldName = "description",
                            newValue = it
                        )
                    },
                    onDismiss = {
                        updateNameOrDescriptionBottomSheetValue = null
                        viewModel.updateCommunityFieldState.value = Resource.Idle()
                    },
                    viewModel = viewModel
                )
            }
            if (commentsForPost != null && rolePriority != null) {
                if (currentUser != null) {
                    CommentsBottomSheet(
                        state = commentsBottomSheetState,
                        communityId = community.id,
                        postId = commentsForPost!!,
                        rolePriority = rolePriority,
                        onDismiss = { commentsForPost = null },
                        vmPost = vmPost,
                        currentUser = currentUser,
                        reportUserDialog = { reportUserDialog = it }
                    )
                }
            }
            if (showRequestsBottomSheet) {
                RequestsBottomSheet(
                    state = requestsBottomSheetState,
                    community = community,
                    numOfRequests = numberOfRequests,
                    onDismiss = {
                        scope.launch { requestsBottomSheetState.hide() }.invokeOnCompletion {
                            if (!requestsBottomSheetState.isVisible) {
                                showRequestsBottomSheet = false
                            }
                        }
                        viewModel.lastRequest = null
                        viewModel.rejectRequestState.value = Resource.Idle()
                        viewModel.acceptRequestState.value = Resource.Idle()
                        viewModel.getNumberOfRequests(community.id)
                        viewModel.getNumOfMembers(community.id)
                    },
                    viewModel = viewModel
                )
            }
            if (showSettingsBottomSheet) {
                SettingsBottomSheet(
                    state = settingsBottomSheetState,
                    eventCreationRestriction = community.eventCreationRestriction,
                    postSharingRestriction = community.postSharingRestriction,
                    participationByRequestOnly = community.participationByRequestOnly,
                    onEventCreationChange = {
                        viewModel.updateCommunityField(
                            communityId = community.id,
                            changedFieldName = "eventCreationRestriction",
                            newValue = it
                        )
                    },
                    onPostSharingChange = {
                        viewModel.updateCommunityField(
                            communityId = community.id,
                            changedFieldName = "postSharingRestriction",
                            newValue = it
                        )
                    },
                    onParticipationByRequestChange = {
                        viewModel.updateCommunityField(
                            communityId = community.id,
                            changedFieldName = "participationByRequestOnly",
                            newValue = it
                        )
                    },
                    onDismiss = {
                        showSettingsBottomSheet = false
                        viewModel.updateCommunityFieldState.value = Resource.Idle()
                    },
                    viewModel = viewModel
                )
            }
            CommunityFlow(
                community = community,
                numberOfMembers = numberOfMembers ?: 1,
                numOfEvents = numOfEvents ?: 0,
                rolePriority = rolePriority,
                onMembersClick = {
                    showMembersBottomSheet = true
                    viewModel.getMembersWithPaging(
                        communityId = community.id,
                        pageSize = 10
                    )
                },
                onEditClick = { isName, value ->
                    updateNameOrDescriptionBottomSheetValue = isName to value
                },
                currentUser = currentUser,
                onCommentClick = { postId ->
                    commentsForPost = postId
                },
                navController = navController,
                onEventClick = { event ->
                    showEventBottomSheet = event
                },
                vmPost = vmPost,
                showSnackbar = {
                    scope.launch {
                        snackbarHostState.showSnackbar(it)
                    }
                },
                onSendRequest = {
                    if (currentUser != null) {
                        if (currentUser.isEmailVerified) {
                            if (userResource is Resource.Success) {
                                if (userResource.data!!.tickets > 1) {
                                    viewModel.sendJoinRequest(
                                        communityId = community.id,
                                        myUid = currentUser.uid,
                                        newTickets = userResource.data.tickets - 2
                                    )
                                } else {
                                    showTicketDialog = true
                                }
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = context.getString(R.string.something_went_wrong)
                                    )
                                }
                            }
                        } else {
                            showUnverifiedAccountAlertDialog = true
                        }
                    } else {
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = context.getString(R.string.please_sign_in),
                                withDismissAction = true,
                                duration = SnackbarDuration.Long,
                                actionLabel = context.getString(R.string.sign_in)
                            )
                            when (result) {
                                SnackbarResult.ActionPerformed -> navController.navigate("log_in")
                                SnackbarResult.Dismissed -> {}
                            }
                        }
                    }

                },
                onJoinCommunity = {
                    if (currentUser != null) {
                        if (currentUser.isEmailVerified) {
                            if (userResource is Resource.Success) {
                                if (userResource.data!!.tickets > 1) {
                                    viewModel.joinTheCommunity(
                                        communityId = community.id,
                                        myUid = currentUser.uid,
                                        newTickets = userResource.data.tickets - 2
                                    )
                                } else {
                                    showTicketDialog = true
                                }
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = context.getString(R.string.something_went_wrong)
                                    )
                                }
                            }
                        } else {
                            showUnverifiedAccountAlertDialog = true
                        }
                    } else {
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = context.getString(R.string.please_sign_in),
                                withDismissAction = true,
                                duration = SnackbarDuration.Long,
                                actionLabel = context.getString(R.string.sign_in)
                            )
                            when (result) {
                                SnackbarResult.ActionPerformed -> navController.navigate("log_in")
                                SnackbarResult.Dismissed -> {}
                            }
                        }
                    }

                },
                didISendRequest = didISendRequest,
                viewModelCommunity = viewModel,
                showReportDialog = { reportDialog = it },
                scope = scope,
                snackbarHostState = snackbarHostState
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CommunityHeadLine(
    community: Community,
    onMembersClick: () -> Unit,
    numberOfMembers: Int,
    onEditClick: (Boolean, String) -> Unit,
    onEventClick: (Event) -> Unit,
    rolePriority: Int?,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    showSnackbar: (String) -> Unit,
    viewModel: CommunityPageViewModel,
    onEventListClick: () -> Unit,
    numOfEvents: Int
) {
    val context = LocalContext.current
    val permissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionState =
            rememberPermissionState(permission = Manifest.permission.READ_MEDIA_IMAGES)
        permissionState

    } else {
        val permissionState =
            rememberPermissionState(permission = Manifest.permission.READ_EXTERNAL_STORAGE)
        permissionState
    }
    var mediaIsGranted by remember {
        mutableStateOf(
            permissionState.status.isGranted
        )
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            if (rolePriority != CommunityRoles.Member.rolePriority && rolePriority != null) {
                viewModel.updateCommunityPicture(uri, communityId = community.id)
            }
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
    var pictureOptionsExpanded by remember { mutableStateOf(false) }
    val eventList = remember { mutableStateListOf<Event>() }
    LaunchedEffect(viewModel.upcomingEventsState.value) {
        if (viewModel.upcomingEventsState.value is Resource.Success) {
            eventList.clear()
            eventList.addAll(viewModel.upcomingEventsState.value.data!!)
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(9.dp))
        var pictureUpdating by remember { mutableStateOf(false) }
        LaunchedEffect(viewModel.updateCommunityPictureState.value) {
            when (val resource = viewModel.updateCommunityPictureState.value) {
                is Resource.Error -> {
                    pictureUpdating = false
                    showSnackbar(resource.messageResource?.let { context.getString(it) } ?: "")
                    viewModel.updateCommunityPictureState.value = Resource.Idle()
                }

                is Resource.Idle -> {}
                is Resource.Loading -> {
                    pictureUpdating = true
                }

                is Resource.Success -> {
                    pictureUpdating = false
                }
            }
        }
        Box(
            modifier = Modifier
                .size(100.dp)
                .clickable {
                    if (rolePriority != CommunityRoles.Member.rolePriority && rolePriority != null) {
                        pictureOptionsExpanded = true
                    }
                }
        ) {
            if (pictureUpdating) {
                CircularWavyProgressIndicator(
                    modifier = Modifier
                        .size(50.dp)
                        .align(Alignment.Center)
                )
            }
            if (community.communityPictureUrl != null) {
                GlideImage(
                    imageModel = { community.communityPictureUrl.toUri() },
                    modifier = Modifier
                        .matchParentSize()
                        .clip(CircleShape),
                    requestBuilder = {
                        val thumbnailRequest = Glide
                            .with(context)
                            .asBitmap()
                            .load(community.communityPictureUrl.toUri())
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
                                .fillMaxWidth(0.5f)
                                .fillMaxHeight(0.5f)
                                .align(Alignment.Center)
                        )
                    },
                    failure = {
                        CircularWavyProgressIndicator(
                            modifier = Modifier
                                .matchParentSize()
                                .align(Alignment.Center)
                        )
                    },
                )
            } else {
                Image(
                    imageVector = Icons.Rounded.Groups,
                    contentDescription = "community icon",
                    modifier = Modifier
                        .matchParentSize()
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )
            }
            DropdownMenu(
                expanded = pictureOptionsExpanded,
                onDismissRequest = { pictureOptionsExpanded = false },
                tonalElevation = 3.dp,
                shadowElevation = 3.dp
            ) {
                if (community.communityPictureUrl != null) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.change_picture)) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.RotateRight,
                                contentDescription = "change photo"
                            )
                        },
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
                            pictureOptionsExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.remove_picture)) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "remove photo",
                                tint = Color.Red
                            )
                        },
                        onClick = {
                            viewModel.updateCommunityPicture(null, communityId = community.id)
                            pictureOptionsExpanded = false
                        }
                    )
                } else {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.add_community_picture)) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.AddAPhoto,
                                contentDescription = "add a photo"
                            )
                        },
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
                            pictureOptionsExpanded = false
                        }
                    )
                }
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            ListItem(
                headlineContent = {
                    Text(
                        text = community.name,
                        style = MaterialTheme.typography.headlineMedium,
                        maxLines = 3,
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                trailingContent = if (rolePriority != CommunityRoles.Member.rolePriority && rolePriority != null) {
                    {
                        IconButton(
                            onClick = {
                                onEditClick(true, community.name)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "change name"
                            )
                        }
                    }
                } else null,
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier.padding(horizontal = 5.dp)
            )

        }
    }
    Spacer(modifier = Modifier.height(10.dp))
    var expendedDescription by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .align(Alignment.CenterHorizontally)
                .clickable {
                    expendedDescription = !expendedDescription
                },
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {

            Row(Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .padding(10.dp)
                        .weight(5f)
                ) {
                    Text(
                        text = stringResource(R.string.community_description),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = community.description,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = if (!expendedDescription) 2 else Int.MAX_VALUE,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (rolePriority != CommunityRoles.Member.rolePriority && rolePriority != null) {
                    IconButton(
                        onClick = {
                            onEditClick(false, community.description)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "edit description"
                        )
                    }
                }
            }

        }
    }
    if (rolePriority != null) {

        UpcomingEvents(
            onEventListClick = onEventListClick,
            onEventClick = { event ->
                onEventClick(event)
            },
            eventList = eventList
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TextButton(
            onClick = onMembersClick,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.padding(9.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Group,
                contentDescription = "members",
                modifier = Modifier.padding(end = 3.dp)
            )
            Text(
                text = "$numberOfMembers ${stringResource(R.string.member).lowercase()}"
            )
        }
        TextButton(
            onClick = onEventListClick,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.padding(9.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Celebration,
                contentDescription = "events",
                modifier = Modifier.padding(end = 3.dp)
            )
            Text(
                text = "$numOfEvents ${stringResource(R.string.event).lowercase()}"
            )
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
}


@Composable
private fun PromoteDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            ElevatedButton(
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                onClick = onConfirm
            ) {
                Text(text = stringResource(R.string.promote))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = stringResource(R.string.cancel)
                )
            }
        },
        icon = {
            Icon(
                imageVector = Icons.Outlined.AddModerator,
                contentDescription = "promote user"
            )
        },
        text = {
            Text(
                text = stringResource(R.string.promote_dialog_text)
            )
        }
    )
}

@Composable
private fun MakeAdminDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            ElevatedButton(
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                onClick = onConfirm
            ) {
                Text(text = stringResource(R.string.make_admin))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = stringResource(R.string.cancel)
                )
            }
        },
        icon = {
            Icon(
                imageVector = Icons.Outlined.ArrowUpward,
                contentDescription = "make user admin"
            )
        },
        text = {
            Text(
                text = stringResource(R.string.make_admin_dialog_text)
            )
        }
    )
}

@Composable
fun DemoteDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            ElevatedButton(
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                onClick = onConfirm
            ) {
                Text(text = stringResource(R.string.demote))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = stringResource(R.string.cancel)
                )
            }
        },
        icon = {
            Icon(
                imageVector = Icons.Outlined.RemoveModerator,
                contentDescription = "Demote user"
            )
        },
        text = {
            Text(
                text = stringResource(R.string.demote_dialog_text)
            )
        }
    )
}

@Composable
private fun RemoveMemberDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            ElevatedButton(
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                onClick = onConfirm
            ) {
                Text(text = stringResource(R.string.remove))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = stringResource(R.string.cancel)
                )
            }
        },
        icon = {
            Icon(
                imageVector = Icons.Rounded.PersonRemove,
                contentDescription = "remove user"
            )
        },
        text = {
            Text(
                text = stringResource(R.string.remove_from_community_dialog_text)
            )
        }
    )
}


@Composable
private fun ShareDialog(communityId: String, onDismiss: () -> Unit) {
    val clipboardManager = LocalClipboardManager.current
    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "${stringResource(R.string.community_id)}:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(text = communityId, style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString(communityId))
                    }) {
                        Icon(imageVector = Icons.Outlined.ContentCopy, contentDescription = "copy")
                    }
                }
                Text(text = stringResource(R.string.share_dialog_text))
            }
        }
    }

}

@Composable
fun LeavingDialog(
    userRolePriority: Int?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {

    if (userRolePriority == CommunityRoles.Admin.rolePriority) {
        AlertDialog(
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = "cannot leave the community"
                )
            },
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.make_someone_else_admin_title)) },
            text = { Text(stringResource(R.string.make_someone_else_admin_text)) },
            confirmButton = {
                ElevatedButton(
                    onClick = onDismiss
                ) {
                    Text(stringResource(R.string.got_it))
                }
            })
    } else {
        AlertDialog(
            text = { Text(stringResource(R.string.leave_community_dialog_text)) },
            onDismissRequest = onDismiss,
            confirmButton = {
                ElevatedButton(
                    onClick = onConfirm,
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(stringResource(R.string.leave))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text(
                        text = stringResource(R.string.cancel)
                    )
                }
            })
    }
}

@Preview
@Composable
private fun CommunityPagePreview() {
    var expendedDescription by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .align(Alignment.CenterHorizontally)
                .clickable {
                    expendedDescription = !expendedDescription
                },
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Row(Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .padding(10.dp)
                        .weight(5f)
                ) {
                    Text(
                        text = "Community Description",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "DescriptionDescriptionDescriptionDescriptionDescription" +
                                "DescriptionDescriptionDescription" +
                                "DescriptionDescriptionDescriptionDescription" +
                                "DescriptionDescription",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = if (!expendedDescription) 2 else Int.MAX_VALUE,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(
                    onClick = {},
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Outlined.Edit, contentDescription = "edit description")
                }
            }

        }
    }
}
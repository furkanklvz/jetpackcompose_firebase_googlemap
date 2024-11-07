package com.klavs.bindle.uix.view.communities.communityPage

import android.Manifest
import android.net.Uri
import android.os.Build
import android.view.Gravity
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.RotateRight
import androidx.compose.material.icons.outlined.AddModerator
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.PersonAddAlt1
import androidx.compose.material.icons.outlined.RemoveModerator
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.ModeEdit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.PersonRemove
import androidx.compose.material.icons.rounded.PostAdd
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.auth.FirebaseUser
import com.klavs.bindle.data.entity.BottomNavItem
import com.klavs.bindle.data.entity.Community
import com.klavs.bindle.data.entity.CommunityRoles
import com.klavs.bindle.data.entity.JoinedCommunities
import com.klavs.bindle.data.entity.JoiningRequestForCommunity
import com.klavs.bindle.data.entity.Member
import com.klavs.bindle.resource.Resource
import com.klavs.bindle.ui.theme.Green1
import com.klavs.bindle.ui.theme.Orange2
import com.klavs.bindle.uix.view.auth.convertMillisToDate
import com.klavs.bindle.uix.view.communities.roleNameFromRoleValue
import com.klavs.bindle.uix.viewmodel.communities.CommunityPageViewModel
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


@Composable
fun CommunityPage(
    navController: NavHostController,
    communityId: String,
    viewModel: CommunityPageViewModel = hiltViewModel()
) {
    BackHandler {
        navController.navigate(BottomNavItem.Communities.route) {
            popUpTo(0) {
                inclusive = true
            }
        }
    }

    val community by viewModel.community.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val rolePriority by viewModel.userRolePriority.collectAsState()
    val amIMember by viewModel.amIMember.collectAsState()

    LaunchedEffect(rolePriority) {
        if (rolePriority != CommunityRoles.Member.rolePriority) {
            viewModel.getNumberOfRequests(communityId)
        }
    }

    var kickedAlert by remember { mutableStateOf(false) }
    if (kickedAlert) {
        Box(
            Modifier
                .fillMaxSize()
                .zIndex(10f)
        ) {
            AlertDialog(
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.WarningAmber,
                        contentDescription = "you have been kicked"
                    )
                },
                title = { Text("You are not member of this community anymore") },
                text = { Text("You have been kicked from this community or you leaved this community.") },
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                ),
                onDismissRequest = {
                    navController.navigate(BottomNavItem.Communities.route) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                }, confirmButton = {
                    ElevatedButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Text("Okay")
                    }
                })
        }
    }

    LaunchedEffect(amIMember) {
        if (!amIMember) {
            kickedAlert = true
        }
    }

    LaunchedEffect(true) {
        viewModel.getCommunity(communityId)
        viewModel.listenToMyStatus(communityId)
    }
    DisposableEffect(true) {
        onDispose {
            viewModel.currentUserJob?.cancel()
            viewModel.communityJob?.cancel()
            viewModel.myStatusJob?.cancel()
        }
    }
    when (community) {
        is Resource.Error -> {
            val toast = Toast.makeText(
                navController.context,
                "Community can not be loaded, please try later",
                Toast.LENGTH_LONG
            )
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
            navController.navigate(BottomNavItem.Communities.route) {
                popUpTo(0) {
                    inclusive = true
                }
            }
        }

        is Resource.Idle -> {}
        is Resource.Loading -> {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }

        is Resource.Success -> {
            Content(
                community = community.data!!,
                navController = navController,
                rolePriority = rolePriority
            )
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
private fun Content(
    community: Community,
    rolePriority: Int,
    viewModel: CommunityPageViewModel = hiltViewModel(),
    navController: NavHostController
) {
    var showMembersBottomSheet by remember { mutableStateOf(false) }
    val membersBottomSheetState = rememberModalBottomSheetState()
    var showRequestsBottomSheet by remember { mutableStateOf(false) }
    val requestsBottomSheetState = rememberModalBottomSheetState()
    var commentsForPost by remember { mutableStateOf<String?>(null) }
    val commentsBottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    var userToRemove by remember { mutableStateOf<String?>(null) }
    var userToBeModerator by remember { mutableStateOf<String?>(null) }
    var userToBeRemovedFromModerator by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val numberOfMembers by viewModel.numberOfMembers.collectAsState()
    val numberOfRequests by viewModel.numberOfRequests.collectAsState()
    val numOfActiveEvents by viewModel.numOfActiveEvents.collectAsState()
    var openShareDialog by remember { mutableStateOf(false) }




    Scaffold(
        floatingActionButton = {
            if (!community.onlyAdminsCanCreatePost || rolePriority != CommunityRoles.Member.rolePriority) {
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
                            "Create Post",
                            modifier = Modifier.padding(horizontal = 5.dp)
                        )
                    }
                }
            }
        },
        topBar = {
            TopAppBar(
                actions = {
                    if (rolePriority != CommunityRoles.Member.rolePriority) {
                        BadgedBox(
                            badge = {
                                if (numberOfRequests is Resource.Success && numberOfRequests.data!! > 0) {
                                    Badge {
                                        Text(
                                            text = if (numberOfRequests.data!! > 99) "99+"
                                            else numberOfRequests.data.toString()
                                        )
                                    }
                                }
                            }
                        ) {
                            IconButton(
                                onClick = {
                                    showRequestsBottomSheet = true
                                    viewModel.getRequestsWithPaging(
                                        communityId = community.id!!,
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
                            onClick = {}
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ModeEdit,
                                contentDescription = "edit"
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
                                        text = "Invite people",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                },
                                onClick = {
                                    openShareDialog = true
                                    expanded = false
                                }
                            )
                        }
                    }


                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.navigate(BottomNavItem.Communities.route) {
                                popUpTo(0) {
                                    inclusive = true
                                }
                            }
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
                        text = "Community Page",
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
            if (openShareDialog) {
                ShareDialog(communityId = community.id!!) { openShareDialog = false }
            }
            if (userToRemove != null) {
                RemoveMemberDialog(
                    uid = userToRemove!!,
                    communityId = community.id!!
                ) { userToRemove = null }
            }
            if (userToBeModerator != null) {
                PromoteDialog(
                    uid = userToBeModerator!!,
                    communityId = community.id!!
                ) { userToBeModerator = null }
            }
            if (userToBeRemovedFromModerator != null) {
                DemoteDialog(
                    uid = userToBeRemovedFromModerator!!,
                    communityId = community.id!!
                ) { userToBeRemovedFromModerator = null }
            }
            if (showMembersBottomSheet) {
                MembersBottomSheet(
                    state = membersBottomSheetState,
                    userRole = rolePriority,
                    numOfMembers = numberOfMembers.data,
                    communityId = community.id!!,
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
                    onRemoveModeratorUser = { userToBeRemovedFromModerator = it }
                )
            }
            if (commentsForPost != null) {
                CommentsBottomSheet(
                    state = commentsBottomSheetState,
                    communityId = community.id!!,
                    postId = commentsForPost!!,
                    onDismiss = { commentsForPost = null }
                )
            }
            if (showRequestsBottomSheet) {
                RequestsBottomSheet(
                    state = requestsBottomSheetState,
                    community = community,
                    numOfRequests = numberOfRequests.data,
                    onDismiss = {
                        scope.launch { requestsBottomSheetState.hide() }.invokeOnCompletion {
                            if (!requestsBottomSheetState.isVisible) {
                                showRequestsBottomSheet = false
                            }
                        }
                        viewModel.lastRequest = null
                        viewModel.deleteRequestState.value = Resource.Idle()
                        viewModel.acceptRequestState.value = Resource.Idle()
                        viewModel.getNumberOfRequests(community.id!!)
                        viewModel.getNumOfMembers(community.id)
                    })
            }

            Posts(
                community = community,
                numberOfMembers = numberOfMembers,
                numOfActiveEvents = numOfActiveEvents,
                rolePriority = rolePriority,
                onMembersClick = {
                    showMembersBottomSheet = true
                    viewModel.getMembersWithPaging(
                        communityId = community.id!!,
                        pageSize = 10
                    )
                },
                onCommentClick = { postId ->
                    commentsForPost = postId
                }
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CommunityHeadLine(
    community: Community,
    onMembersClick: () -> Unit,
    numberOfMembers: Resource<Int>,
    rolePriority: Int,
    numOfActiveEvents: Resource<Int>
) {
    val context = LocalContext.current
    val viewModel: CommunityPageViewModel = hiltViewModel()
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
            if (rolePriority != CommunityRoles.Member.rolePriority) {
                viewModel.updateCommunityPicture(uri, communityId = community.id!!)
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
                    Toast.makeText(
                        context,
                        "We cannot process your request right now, please try again later",
                        Toast.LENGTH_SHORT
                    ).show()
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
                    if (rolePriority != CommunityRoles.Member.rolePriority) {
                        pictureOptionsExpanded = true
                    }
                }
        ) {
            if (pictureUpdating){
                CircularProgressIndicator(modifier = Modifier.size(50.dp).align(Alignment.Center))
            }
            if (community.communityPictureUrl != null) {
                GlideImage(
                    imageModel = { community.communityPictureUrl.toUri() },
                    modifier = Modifier
                        .matchParentSize()
                        .clip(CircleShape),
                    loading = {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .fillMaxHeight(0.5f)
                                .align(Alignment.Center)
                        )
                    },
                    failure = {
                        CircularProgressIndicator(
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
                        text = { Text("Change Picture") },
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
                                permissionState.launchPermissionRequest()
                            }
                            pictureOptionsExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Remove Picture") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "remove photo",
                                tint = Color.Red
                            )
                        },
                        onClick = {
                            viewModel.updateCommunityPicture(null, communityId = community.id!!)
                            pictureOptionsExpanded = false
                        }
                    )
                } else {
                    DropdownMenuItem(
                        text = { Text("Add Community Picture") },
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
                                permissionState.launchPermissionRequest()
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
            Text(
                text = community.name,
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Text(
                    text = "Community Description",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = community.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = if (!expendedDescription) 2 else Int.MAX_VALUE,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
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
            if (numberOfMembers is Resource.Success) {
                Text(
                    text = "${numberOfMembers.data!!} members"
                )
            } else {
                Text(
                    text = "1 members"
                )
            }
        }
        TextButton(
            onClick = {},
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.padding(9.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Celebration,
                contentDescription = "active events",
                modifier = Modifier.padding(end = 3.dp)
            )
            if (numOfActiveEvents is Resource.Success) {
                Text(
                    text = "${numOfActiveEvents.data!!} active events"
                )
            } else {
                Text(
                    text = "0 active events"
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RequestsBottomSheet(
    state: SheetState,
    community: Community,
    numOfRequests: Int?,
    viewModel: CommunityPageViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val requests by viewModel.joiningRequests.collectAsState()
    val requestsList = remember { mutableStateListOf<JoiningRequestForCommunity>() }
    var isLoading by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.deleteRequestState.value) {
        when (val resource = viewModel.deleteRequestState.value) {
            is Resource.Error -> {
                Toast.makeText(
                    context,
                    "The request cannot be deleted right now, please check your connection.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            is Resource.Success -> {
                requestsList.remove(requestsList.find { it.uid!! == resource.data!! })
                if (requestsList.isEmpty()) {
                    viewModel.getRequestsWithPaging(
                        communityId = community.id!!,
                        pageSize = 10
                    )
                }
                Toast.makeText(context, "Request deleted", Toast.LENGTH_SHORT).show()
            }

            else -> {}
        }
    }
    LaunchedEffect(viewModel.acceptRequestState.value) {
        when (val resource = viewModel.acceptRequestState.value) {
            is Resource.Error -> {
                Toast.makeText(
                    context,
                    "We cannot process your transaction at this time, please try again later.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            is Resource.Success -> {
                val request = requestsList.find { it.uid == resource.data!! }
                val index = requestsList.indexOf(request)
                requestsList[index] = request!!.copy(accepted = true)
            }

            else -> {}
        }
    }

    ModalBottomSheet(
        sheetState = state,
        onDismissRequest = {
            onDismiss()
            requestsList.clear()
        }
    ) {
        Text(
            text = "Joining Requests",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(10.dp)
        )
        LaunchedEffect(requests) {
            when (requests) {
                is Resource.Error -> {
                    isLoading = false
                    hasError = true
                }

                is Resource.Idle -> {}
                is Resource.Loading -> {
                    isLoading = true
                }

                is Resource.Success -> {
                    isLoading = false
                    hasError = false
                    requestsList.addAll(requests.data!!)
                }
            }
        }


        LazyColumn(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            items(requestsList) { requestObject ->
                JoiningRequestRow(
                    request = requestObject,
                    onDeleteRequest = {
                        viewModel.deleteRequest(
                            communityId = community.id!!,
                            uid = requestObject.uid!!
                        )
                    },
                    onAcceptRequest = {
                        val communityModel = JoinedCommunities(
                            id = community.id!!,
                            rolePriority = CommunityRoles.Member.rolePriority
                        )
                        viewModel.acceptRequest(
                            community = communityModel,
                            requestObject = requestObject
                        )
                    }
                )
            }
            item {
                if (isLoading) {
                    CircularProgressIndicator()
                } else if (hasError) {
                    Text(
                        "Requests can not be loaded right now. Please try again.",
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    if (requestsList.isEmpty()) {
                        Row(
                            modifier = Modifier
                                .padding(vertical = 15.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "empty"
                            )
                            Text(
                                text = "There is no request to join :(",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                    if (viewModel.lastRequest != null && requestsList.size < (numOfRequests
                            ?: Int.MAX_VALUE)
                    ) {
                        TextButton(
                            modifier = Modifier.padding(4.dp),
                            onClick = {
                                viewModel.getRequestsWithPaging(
                                    communityId = community.id!!,
                                    pageSize = 10
                                )
                            }
                        ) {
                            Text(text = "See More", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

    }

}

@Composable
private fun JoiningRequestRow(
    request: JoiningRequestForCommunity,
    onDeleteRequest: () -> Unit,
    onAcceptRequest: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(50.dp)) {
                if (request.profilePictureUrl != null) {
                    GlideImage(
                        imageModel = { request.profilePictureUrl.toUri() },
                        modifier = Modifier
                            .matchParentSize()
                            .clip(CircleShape),
                        loading = {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .matchParentSize()
                                    .align(Alignment.Center)
                            )
                        }
                    )
                } else {
                    Image(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.LightGray, CircleShape)
                            .clip(
                                CircleShape
                            )
                    )
                }
            }
            Column(horizontalAlignment = Alignment.Start) {

                Text(
                    text = request.userName,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
                Text(
                    text = convertMillisToLocalizeDate(request.requestDate),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
            }

        }
        if (request.accepted) {
            ElevatedButton(
                onClick = {},
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 2.dp),
            ) {
                Text("Member", modifier = Modifier.padding(horizontal = 2.dp))
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {

                IconButton(
                    onClick = onDeleteRequest
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = null,
                        tint = Color.Red
                    )
                }
                ElevatedButton(
                    onClick = onAcceptRequest,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircleOutline,
                        contentDescription = null
                    )
                    Text(text = "Accept", modifier = Modifier.padding(horizontal = 2.dp))
                }
            }
        }
    }
}

@Composable
fun convertMillisToLocalizeDate(millis: Long): String {
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Bir LaunchedEffect kullanarak sürekli güncelleme yapıyoruz.
    LaunchedEffect(Unit) {
        while (true) {
            delay(60100) // Her dakikada güncellenir.
            currentTime = System.currentTimeMillis() // Sistemin saatini güncelle.
        }
    }

    val diff = currentTime - millis

    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)

    return when {
        days > 7 -> convertMillisToDate(millis)
        days > 0 -> "$days d ago"
        hours > 0 -> "$hours h ago"
        minutes > 0 -> "$minutes m ago"
        else -> "now"
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MembersBottomSheet(
    userRole: Int,
    state: SheetState,
    communityId: String,
    numOfMembers: Int?,
    onDismiss: () -> Unit,
    onDeleteUser: (uid: String) -> Unit,
    onAddModeratorUser: (uid: String) -> Unit,
    onRemoveModeratorUser: (uid: String) -> Unit,
    viewModel: CommunityPageViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val members by viewModel.members.collectAsState()
    val memberList = remember { mutableStateListOf<Member>() }
    var isLoading by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    if (isProcessing) {
        Box(Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }

    LaunchedEffect(viewModel.removeMemberState.value) {
        when (val resource = viewModel.removeMemberState.value) {
            is Resource.Error -> {
                isProcessing = false
                Toast.makeText(
                    context,
                    "We cannot process your transaction at this time, please try again later.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            is Resource.Idle -> {}
            is Resource.Loading -> {
                isProcessing = true
            }

            is Resource.Success -> {
                isProcessing = false
                memberList.remove(memberList.find { it.uid == resource.data!! })
                if (memberList.isEmpty()) {
                    viewModel.getMembersWithPaging(communityId = communityId, pageSize = 10)
                }
                viewModel.removeMemberState.value = Resource.Idle()
            }
        }
    }
    LaunchedEffect(viewModel.promoteMemberState.value) {
        when (val resource = viewModel.promoteMemberState.value) {
            is Resource.Error -> {
                isProcessing = false
                Toast.makeText(
                    context,
                    "We cannot process your transaction at this time, please try again later.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            is Resource.Idle -> {}
            is Resource.Loading -> {
                isProcessing = true
            }

            is Resource.Success -> {
                isProcessing = false
                val member = memberList.find { it.uid == resource.data!! }
                val index = memberList.indexOf(member)
                memberList[index] =
                    member!!.copy(rolePriority = CommunityRoles.Moderator.rolePriority)
                viewModel.promoteMemberState.value = Resource.Idle()
            }
        }
    }
    LaunchedEffect(viewModel.demoteMemberState.value) {
        when (val resource = viewModel.demoteMemberState.value) {
            is Resource.Error -> {
                isProcessing = false
                Toast.makeText(
                    context,
                    "We cannot process your transaction at this time, please try again later.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            is Resource.Idle -> {}
            is Resource.Loading -> {
                isProcessing = true
            }

            is Resource.Success -> {
                isProcessing = false
                val member = memberList.find { it.uid == resource.data!! }
                val index = memberList.indexOf(member)
                memberList[index] = member!!.copy(rolePriority = CommunityRoles.Member.rolePriority)
                viewModel.demoteMemberState.value = Resource.Idle()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = state
    ) {
        Text(
            text = "Community Members",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(10.dp)
        )
        LaunchedEffect(members) {
            when (members) {
                is Resource.Error -> {
                    isLoading = false
                    hasError = true
                }

                is Resource.Idle -> {}
                is Resource.Loading -> {
                    isLoading = true
                }

                is Resource.Success -> {
                    isLoading = false
                    hasError = false
                    memberList.addAll(members.data!!)
                }
            }
        }

        LazyColumn(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            items(memberList) {
                MemberRow(
                    member = it, userRole = userRole,
                    onDeleteUser = { onDeleteUser(it.uid) },
                    onAddModeratorUser = { onAddModeratorUser(it.uid) },
                    onRemoveModeratorUser = { onRemoveModeratorUser(it.uid) }
                )
            }
            item {
                if (isLoading) {
                    CircularProgressIndicator()
                } else if (hasError) {
                    Text(
                        "Members can not be loaded right now. Please try again.",
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    if (viewModel.lastMember != null && memberList.size < (numOfMembers
                            ?: Int.MAX_VALUE)
                    ) {
                        TextButton(
                            modifier = Modifier.padding(4.dp),
                            onClick = {
                                viewModel.getMembersWithPaging(
                                    communityId = communityId,
                                    pageSize = 10
                                )
                            }
                        ) {
                            Text(text = "See More", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

    }

}

@Composable
fun MemberRow(
    member: Member,
    userRole: Int,
    onDeleteUser: () -> Unit,
    onAddModeratorUser: () -> Unit,
    onRemoveModeratorUser: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row {
            Box(modifier = Modifier.size(50.dp)) {
                if (member.profileImageUrl != null) {
                    GlideImage(
                        imageModel = { member.profileImageUrl.toUri() },
                        modifier = Modifier
                            .matchParentSize()
                            .clip(CircleShape),
                        loading = {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .matchParentSize()
                                    .align(Alignment.Center)
                            )
                        }
                    )
                } else {
                    Image(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.LightGray, CircleShape)
                            .clip(
                                CircleShape
                            )
                    )
                }
            }
            Column(
                modifier = Modifier.padding(horizontal = 10.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = member.userName,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = roleNameFromRoleValue(member.rolePriority),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .background(
                            color = when (member.rolePriority) {
                                CommunityRoles.Admin.rolePriority -> Green1
                                CommunityRoles.Moderator.rolePriority -> Orange2
                                else -> Color.Transparent
                            },
                            shape = RoundedCornerShape(3.dp)
                        )
                        .padding(2.dp)
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            when (userRole) {
                CommunityRoles.Admin.rolePriority -> {
                    when (member.rolePriority) {
                        CommunityRoles.Member.rolePriority -> {
                            IconButton(
                                onClick = onAddModeratorUser
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AddModerator,
                                    contentDescription = "add moderator"
                                )
                            }
                            IconButton(
                                onClick = onDeleteUser
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PersonRemove,
                                    tint = MaterialTheme.colorScheme.error,
                                    contentDescription = "remove"
                                )
                            }
                        }

                        CommunityRoles.Moderator.rolePriority -> {
                            IconButton(
                                onClick = onRemoveModeratorUser
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.RemoveModerator,
                                    contentDescription = "remove moderator"
                                )
                            }
                            IconButton(
                                onClick = onDeleteUser
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PersonRemove,
                                    tint = MaterialTheme.colorScheme.error,
                                    contentDescription = "remove"
                                )
                            }
                        }

                        else -> {}
                    }

                }

                CommunityRoles.Moderator.rolePriority -> {
                    if (member.rolePriority == CommunityRoles.Member.rolePriority) {
                        IconButton(
                            onClick = onAddModeratorUser
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AddModerator,
                                contentDescription = "remove"
                            )
                        }
                        IconButton(
                            onClick = onDeleteUser
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PersonRemove,
                                tint = MaterialTheme.colorScheme.error,
                                contentDescription = "remove"
                            )
                        }
                    }
                }
            }
        }

    }
}

@Composable
private fun PromoteDialog(uid: String, communityId: String, onDismiss: () -> Unit) {
    val viewModel: CommunityPageViewModel = hiltViewModel()
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            ElevatedButton(
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                onClick = {
                    viewModel.promoteMember(communityId = communityId, uid = uid)
                    onDismiss()
                }
            ) {
                Text(text = "Promote")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Cancel"
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
                text = "Are you sure you want to promote this user to moderator?" +
                        "\nOnly admin is able to demote moderators back to members." +
                        "\n\nBy promoting the user, you will grant the following permissions:" +
                        "\n• Editing community settings" +
                        "\n• Promoting members to moderators" +
                        "\n• Creating events associated with the community if event creation restriction is on"
            )
        }
    )
}

@Composable
fun DemoteDialog(uid: String, communityId: String, onDismiss: () -> Unit) {
    val viewModel: CommunityPageViewModel = hiltViewModel()
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            ElevatedButton(
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                onClick = {
                    viewModel.demoteMember(communityId = communityId, uid = uid)
                    onDismiss()
                }
            ) {
                Text(text = "Demote")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Cancel"
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
                text = "Are you sure you want to demote this user to member?" +
                        "\nOnly admin is able to promote members back to moderators." +
                        "\n\nBy demoting the user, you will revoke the following permissions:" +
                        "\n• Editing community settings" +
                        "\n• Promoting members to moderators" +
                        "\n• Creating events associated with the community if event creation restriction is on"
            )
        }
    )
}

@Composable
private fun RemoveMemberDialog(uid: String, communityId: String, onDismiss: () -> Unit) {
    val viewModel: CommunityPageViewModel = hiltViewModel()
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            ElevatedButton(
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                onClick = {
                    viewModel.removeMember(communityId = communityId, uid = uid)
                    onDismiss()
                }
            ) {
                Text(text = "Remove")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Cancel"
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
                text = "Are you sure you want to remove this user from the community?"
            )
        }
    )
}


@Composable
private fun ShareDialog(communityId: String, onDismiss: () -> Unit) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(text = "Community ID:", style = MaterialTheme.typography.bodyMedium)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = communityId, style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString(communityId))
                        Toast.makeText(context, "ID copied", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(imageVector = Icons.Outlined.ContentCopy, contentDescription = "copy")
                    }
                }
                Text(text = "People can find this community by using this Comment ID")
            }
        }
    }

}

@Preview
@Composable
private fun CommunityPagePreview() {
    /*var pictureOptionsExpanded by remember { mutableStateOf(false) }
    Box {
        DropdownMenu(
            expanded = pictureOptionsExpanded,
            onDismissRequest = { pictureOptionsExpanded = false },
            tonalElevation = 3.dp,
            shadowElevation = 3.dp
        ) {
            if (true) {
                DropdownMenuItem(
                    text = {Text("Change Picture")},
                    trailingIcon = {Icon(imageVector = Icons.AutoMirrored.Rounded.RotateRight,
                        contentDescription = "change photo")},
                    onClick = {}
                )
                DropdownMenuItem(
                    text = {Text("Remove Picture")},
                    trailingIcon = {Icon(imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "remove photo",
                        tint = Color.Red)},
                    onClick = {}
                )
            }else{
                DropdownMenuItem(
                    text = { Text("Add Community Picture") },
                    trailingIcon = {Icon(imageVector = Icons.Rounded.AddAPhoto, contentDescription = "add a photo")},
                    onClick = {}
                )
            }
        }
    }*/


}
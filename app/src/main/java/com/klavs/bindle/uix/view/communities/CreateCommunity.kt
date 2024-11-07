package com.klavs.bindle.uix.view.communities

import android.net.Uri
import android.os.Build
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowRightAlt
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil3.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.klavs.bindle.data.entity.BottomNavItem
import com.klavs.bindle.data.entity.Community
import com.klavs.bindle.resource.Resource
import com.klavs.bindle.ui.theme.Green2
import com.klavs.bindle.uix.viewmodel.communities.CommunityViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CreateCommunity(
    navController: NavHostController,
    viewModel: CommunityViewModel = hiltViewModel()
) {
    var communityName by remember { mutableStateOf("") }
    var communityDescription by remember { mutableStateOf("") }
    var onlyAdminsCanCreateEvent by remember { mutableStateOf(true) }
    var onlyAdminsCanCreatePost by remember { mutableStateOf(false) }
    var joinWithRequest by remember { mutableStateOf(true) }
    var communityNameIsEmpty by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val permissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionState =
            rememberPermissionState(permission = android.Manifest.permission.READ_MEDIA_IMAGES)
        permissionState

    } else {
        val permissionState =
            rememberPermissionState(permission = android.Manifest.permission.READ_EXTERNAL_STORAGE)
        permissionState
    }
    val mediaIsGranted = remember {
        mutableStateOf(
            permissionState.status.isGranted
        )
    }

    val selectedImageUri = rememberSaveable {
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
                errorMessage = result.message!!
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
                        text = "Create Community",
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
                Box(modifier = Modifier.align(Alignment.Center)) {
                    CircularProgressIndicator()
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
                    title = { Text(text = "Error") },
                    text = { Text(text = errorMessage) },
                    onDismissRequest = { isError = false },
                    confirmButton = {
                        Button(onClick = { isError = false }) {
                            Text(text = "Ok")
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
                    Text(text = "Community Picture", style = MaterialTheme.typography.titleMedium)
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
                                            permissionState.launchPermissionRequest()
                                        }
                                    }
                            )
                            IconButton(
                                onClick = {
                                    if (mediaIsGranted.value) {
                                        galleryLauncher.launch("image/*")
                                    } else {
                                        permissionState.launchPermissionRequest()
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
                                            permissionState.launchPermissionRequest()
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
                            Text(text = "Remove picture")
                        }
                    }

                }
                Column(Modifier.padding(10.dp)) {
                    TextField(
                        isError = communityNameIsEmpty,
                        label = { Text(text = "Community Name") },
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
                            if (it.isNotEmpty()) {
                                communityNameIsEmpty = false
                            }
                            if (it.length <= 40) {
                                communityName = it
                            }
                        },
                        supportingText = {
                            Column {
                                if (communityNameIsEmpty) {
                                    Text("Community name can not be empty")
                                }
                                Text(text = "${communityName.length}/40")
                            }

                        }
                    )
                }
                Column(Modifier.padding(10.dp)) {
                    Text(
                        text = "Community Description",
                        style = MaterialTheme.typography.titleMedium
                    )
                    TextField(
                        placeholder = {
                            Text(
                                text = "It's helpful to define a community description. " +
                                        "For example, you can write down the rules you want members to follow.",
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
                    text = "Community Settings",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Only admins can create events associated with this community.",
                        modifier = Modifier.width(250.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = onlyAdminsCanCreateEvent,
                        onCheckedChange = {
                            onlyAdminsCanCreateEvent = !onlyAdminsCanCreateEvent
                        }
                    )
                }
                Spacer(Modifier.height(3.dp))
                if (!onlyAdminsCanCreateEvent) {
                    Text(
                        "All members of the community can create events associated with this community.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth(0.9f)
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Only admins can share post in this community.",
                        modifier = Modifier.width(250.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = onlyAdminsCanCreatePost,
                        onCheckedChange = {
                            onlyAdminsCanCreatePost = !onlyAdminsCanCreatePost
                        }
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "This community can only be joined by request.",
                        modifier = Modifier.width(250.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = joinWithRequest,
                        onCheckedChange = {
                            joinWithRequest = !joinWithRequest
                        }
                    )
                }
                Spacer(Modifier.height(3.dp))
                if (!joinWithRequest) {
                    Text(
                        "No permission is required to join this community",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth(0.9f)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "*The community settings can be edited by the community admins at any time.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
                Spacer(modifier = Modifier.height(5.dp))
                FloatingActionButton(
                    onClick = {
                        if (communityName.isEmpty()) {
                            communityNameIsEmpty = true
                        } else {
                            val community = Community(
                                name = communityName,
                                description = communityDescription,
                                onlyAdminsCanCreateEvent = onlyAdminsCanCreateEvent,
                                onlyAdminsCanCreatePost = onlyAdminsCanCreatePost,
                                requestIsRequireForJoining = joinWithRequest,
                                communityPictureUrl = selectedImageUri.value.toString()
                            )
                            viewModel.createCommunity(community = community)
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowRightAlt,
                        contentDescription = "ok"
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))

            }

        }

    }

}

@Preview
@Composable
private fun CreateCommunityPreview() {
    CreateCommunity(navController = rememberNavController())
}
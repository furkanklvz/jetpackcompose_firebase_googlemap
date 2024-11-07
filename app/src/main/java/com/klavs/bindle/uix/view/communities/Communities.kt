package com.klavs.bindle.uix.view.communities

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.klavs.bindle.data.entity.BottomNavItem
import com.klavs.bindle.data.entity.Community
import com.klavs.bindle.data.entity.CommunityRoles
import com.klavs.bindle.data.entity.JoinedCommunities
import com.klavs.bindle.resource.Resource
import com.klavs.bindle.uix.viewmodel.communities.CommunityViewModel
import com.skydoves.landscapist.glide.GlideImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Communities(
    navController: NavHostController,
    viewModel: CommunityViewModel = hiltViewModel()
) {
    val currentUser by viewModel.currentUser.collectAsState()
    var searching by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    val communities by viewModel.communities.collectAsState()
    var searchedCommunityNotFound by remember { mutableStateOf(false) }
    var searchedCommunity by remember { mutableStateOf(Community()) }
    var searchError by remember { mutableStateOf(false) }
    var searchLoading by remember { mutableStateOf(false) }
    var searchIsSuccessful by remember { mutableStateOf(false) }

    DisposableEffect(true) {
        onDispose {
            viewModel.communitiesJob?.cancel()
            viewModel.currentUserJob?.cancel()
        }
    }

    LaunchedEffect(true) {
        viewModel.getCommunities()
    }

    DisposableEffect(true) {
        onDispose {
            viewModel.searchedCommunityState.value = Resource.Idle()
        }
    }

    LaunchedEffect(key1 = viewModel.searchedCommunityState.value) {
        when (val resource = viewModel.searchedCommunityState.value) {
            is Resource.Error -> {
                searchLoading = false
                searchError = true
            }

            is Resource.Idle -> {}
            is Resource.Loading -> {
                searchError = false
                searchLoading = true
            }

            is Resource.Success -> {
                searchLoading = false
                if (resource.data == null) {
                    searchedCommunityNotFound = true
                } else {
                    searchIsSuccessful = true
                    searchedCommunity = resource.data
                }
            }
        }
    }

    /*val communities = remember {
        mutableStateListOf(
            CommunityRowItem(
                name = "TestTestTestTestTestTestTestTest",
                role = "creator"
            ),
            CommunityRowItem(
                name = "Revir",
                role = "member"
            )
        )
    }*/
    Scaffold(
        topBar = {
            TopAppBar(
                actions = {
                    if (!searching) {
                        IconButton(onClick = { searching = true }) {
                            Icon(imageVector = Icons.Rounded.Search, contentDescription = "search")
                        }
                    }
                },
                title = {
                    AnimatedVisibility(searching) {
                        TextField(
                            isError = searchedCommunityNotFound,
                            supportingText = if (searchedCommunityNotFound) {
                                { Text(text = "Community not found") }
                            } else null,
                            textStyle = MaterialTheme.typography.bodyMedium,
                            placeholder = {
                                Text(
                                    text = "Community ID:",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = {
                                    if (searchText.isNotEmpty()) {
                                        viewModel.searchCommunityById(communityId = searchText)
                                    }
                                }) {
                                    if (searchLoading) {
                                        CircularProgressIndicator()
                                    } else {
                                        Icon(
                                            imageVector = Icons.Rounded.Search,
                                            contentDescription = "search"
                                        )
                                    }
                                }
                            },
                            leadingIcon = {
                                IconButton(onClick = {
                                    searchText = ""
                                    searching = false
                                }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                        contentDescription = "back"
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth(0.95f),
                            shape = CircleShape,
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                errorIndicatorColor = Color.Transparent
                            ),
                            value = searchText,
                            onValueChange = {
                                searchText = it
                                searchedCommunityNotFound = false
                            }
                        )
                    }
                    AnimatedVisibility(!searching) {
                        Text(
                            text = BottomNavItem.Communities.label
                        )
                    }


                })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("create_community") }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.GroupAdd,
                        contentDescription = "create",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    Text(
                        text = "Create",
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            if (searchIsSuccessful) {
                SearchedCommunityDialog(
                    community = searchedCommunity,
                    onDismissRequest = {
                        searchIsSuccessful = false
                        viewModel.memberCheckJob?.cancel()
                        viewModel.requestCheckJob?.cancel()
                    }
                )
            }
            if (currentUser != null) {
                when (val communitiesResource = communities) {
                    is Resource.Error -> {
                        Text(
                            text = "Error, please try again later",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    is Resource.Idle -> {}
                    is Resource.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    is Resource.Success -> {
                        if (communitiesResource.data!!.isEmpty()) {
                            Text(
                                text = "No communities found",
                                modifier = Modifier.align(Alignment.Center),
                                style = MaterialTheme.typography.titleLarge
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                items(communitiesResource.data) { item ->
                                    CommunityRow(item = item, navController = navController)
                                    Spacer(Modifier.height(10.dp))
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "Please login to see communities",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

        }
    }
}

@Composable
fun SearchedCommunityDialog(
    community: Community,
    onDismissRequest: () -> Unit,
    viewModel: CommunityViewModel = hiltViewModel()
) {
    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(4.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = community.name,
                        style = MaterialTheme.typography.headlineMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(5.dp))
                    Box(modifier = Modifier.size(100.dp)) {
                        if (community.communityPictureUrl == null) {
                            Image(
                                imageVector = Icons.Rounded.Groups,
                                contentDescription = "picture",
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(CircleShape)
                                    .background(Color.LightGray)
                            )
                        } else {
                            GlideImage(
                                imageModel = { community.communityPictureUrl },
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
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(5.dp))
                    val numOfMembers by viewModel.numOfMembersOfSearchedCommunity.collectAsState()
                    if (numOfMembers is Resource.Success) {
                        Text(
                            text = "${numOfMembers.data!!} members",
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        Text(
                            text = "",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }


                Spacer(Modifier.height(10.dp))
                Column(modifier = Modifier.fillMaxWidth(0.8f)) {
                    Text(
                        text = community.description,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 150.dp)
                            .verticalScroll(rememberScrollState())
                    )
                }
                Spacer(Modifier.height(10.dp))
                val amIMember by viewModel.amIMember.collectAsState()
                when (amIMember) {
                    is Resource.Error -> {
                        Text(
                            text = "Error, please try again",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    is Resource.Idle -> {}
                    is Resource.Loading -> {
                        CircularProgressIndicator(Modifier.size(10.dp))
                    }

                    is Resource.Success -> {
                        if (amIMember.data!!) {
                            Button(
                                onClick = {},
                                enabled = false
                            ) {
                                Text(
                                    text = "You are member"
                                )
                            }
                        } else {
                            val didISendRequest by viewModel.didISendRequest.collectAsState()
                            when (val didISendRequestResource = didISendRequest) {
                                is Resource.Error -> {
                                    Text(
                                        text = "Error, please try again",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                is Resource.Idle -> {}
                                is Resource.Loading -> {
                                    CircularProgressIndicator()
                                }

                                is Resource.Success -> {
                                    if (didISendRequestResource.data!!) {
                                        Button(
                                            onClick = {},
                                            enabled = false
                                        ) {
                                            Text(
                                                text = "Request sent"
                                            )
                                        }
                                    } else {
                                        ElevatedButton(
                                            colors = ButtonDefaults.elevatedButtonColors(
                                                containerColor = MaterialTheme.colorScheme.primaryContainer
                                            ),
                                            onClick = {
                                                if (community.requestIsRequireForJoining) {
                                                    viewModel.sendJoinRequest(communityId = community.id!!)
                                                } else {
                                                    viewModel.joinTheCommunity(community = community)
                                                }
                                            }
                                        ) {
                                            Text(
                                                text = if (community.requestIsRequireForJoining) "Request to join" else "Join",
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

fun roleNameFromRoleValue(roleValue: Int):String {
    Log.e("roleValue", "$roleValue")
    val roles = listOf(
        CommunityRoles.Admin,
        CommunityRoles.Moderator,
        CommunityRoles.Member
    )
    return roles.find { it.rolePriority == roleValue }!!.roleName
}
@Composable
fun CommunityRow(item: JoinedCommunities, navController: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .clickable {
                navController.navigate("community_page/${item.id}")
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(50.dp)) {
                if (item.communityPictureUrl != null) {
                    GlideImage(
                        imageModel = { item.communityPictureUrl.toUri() },
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
                        }
                    )
                } else {
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
                    )
                }
            }
            Column {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .width(220.dp),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
                Text(
                    text = roleNameFromRoleValue(item.rolePriority),
                    modifier = Modifier
                        .padding(start = 10.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }

        }
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = "arrow"
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun CommunitiesPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
    ) {
        CommunityRow(
            item = JoinedCommunities(
                name = "TEMA",
                rolePriority = CommunityRoles.Moderator.rolePriority
            ),
            navController = rememberNavController()
        )
    }
}

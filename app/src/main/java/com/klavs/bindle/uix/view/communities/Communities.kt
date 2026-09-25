package com.klavs.bindle.uix.view.communities

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.YoutubeSearchedFor
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseUser
import com.klavs.bindle.R
import com.klavs.bindle.data.entity.community.Community
import com.klavs.bindle.data.entity.sealedclasses.CommunityRoles
import com.klavs.bindle.data.entity.community.JoinedCommunity
import com.klavs.bindle.resource.Resource
import com.klavs.bindle.uix.view.GlideImageLoader
import com.klavs.bindle.uix.view.communities.communityPage.LeavingDialog
import com.klavs.bindle.uix.viewmodel.NavHostViewModel
import com.klavs.bindle.uix.viewmodel.communities.CommunityViewModel
import com.klavs.bindle.util.TicketDialog
import com.klavs.bindle.util.UnverifiedAccountAlertDialog
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Communities(
    navController: NavHostController,
    currentUser: FirebaseUser?,
    navHostViewModel: NavHostViewModel,
    onBottomBarVisibilityChange: (Boolean) -> Unit,
    viewModel: CommunityViewModel
) {

    val context = LocalContext.current
    var searchText by rememberSaveable { mutableStateOf("") }
    val userResourceFlow by navHostViewModel.userResourceFlow.collectAsState()
    var showTicketDialog by remember { mutableStateOf(false) }
    var showUnverifiedAccountAlertDialog by remember { mutableStateOf(false) }
    val communitiesResource by viewModel.communities.collectAsState()
    val pinnedCommunities by viewModel.pinnedCommunities.collectAsState()
    val searchResultsResource by viewModel.searchResults.collectAsState()


    LaunchedEffect(true) {
        if (communitiesResource !is Resource.Success && currentUser != null) {
            viewModel.listenToCommunities(
                myUid = currentUser.uid
            )
            Log.e("communities", "listenToCommunities launched")
            Log.e("communities", "communitiesResource: ${communitiesResource}")
            viewModel.listenToPinnedCommunities(
                myUid = currentUser.uid
            )
        }
    }

    val focusRequester = remember { FocusRequester() }
    val searchBarExpanded by navHostViewModel.communitiesSearchBarExpanded.collectAsState()

    LaunchedEffect(searchBarExpanded) {
        onBottomBarVisibilityChange(!searchBarExpanded)
        Log.e("communities", "bottom bar enabled: ${!searchBarExpanded}")
        if (!searchBarExpanded) {
            searchText = ""
            viewModel.clearSearchResults()
        }
    }
    BackHandler(searchBarExpanded) {
        navHostViewModel.communitiesSearchBarExpanded.value = false
    }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    if (searchBarExpanded) {
                        TextField(
                            placeholder = {
                                Text(stringResource(R.string.community_search_placeholder))
                            },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Search // Klavyede "Arama" ikonunu gösterir
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    if (searchText.isNotBlank()) {
                                        viewModel.searchCommunity(
                                            searchQuery = searchText
                                        )
                                    }
                                }
                            ),
                            leadingIcon = {
                                IconButton(
                                    onClick = {
                                        navHostViewModel.communitiesSearchBarExpanded.value = false
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "close"
                                    )
                                }
                            },
                            modifier = Modifier
                                .focusRequester(focusRequester)
                                .fillMaxWidth(),
                            value = searchText,
                            onValueChange = { searchText = it },
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent
                            ),
                            shape = CircleShape
                        )
                        LaunchedEffect(true) {
                            focusRequester.requestFocus()
                        }
                    } else {
                        Text(stringResource(R.string.communities))
                    }
                },
                actions = {
                    if (!searchBarExpanded) {
                        IconButton(
                            onClick = {
                                navHostViewModel.communitiesSearchBarExpanded.value = true
                            }
                        ) {
                            Icon(imageVector = Icons.Rounded.Search, contentDescription = "search")
                        }
                    }
                }
            )

        },
        floatingActionButton = {
            if (currentUser != null) {
                AnimatedVisibility(!searchBarExpanded) {
                    FloatingActionButton(
                        onClick = {
                            if (userResourceFlow is Resource.Success && userResourceFlow.data != null) {
                                if (currentUser.isEmailVerified) {
                                    if (userResourceFlow.data!!.tickets < 3) {
                                        showTicketDialog = true
                                    } else {
                                        Log.e(
                                            "communities",
                                            "navigate to create community from button"
                                        )
                                        navController.navigate("create_community")
                                    }
                                } else {
                                    showUnverifiedAccountAlertDialog = true
                                }
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        context.getString(
                                            R.string.something_went_wrong_try_again_later
                                        )
                                    )
                                }
                            }
                        }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.GroupAdd,
                                contentDescription = "create",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                            Text(
                                text = stringResource(R.string.create),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }

                    }
                }
            }

        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            if (showUnverifiedAccountAlertDialog) {
                UnverifiedAccountAlertDialog(
                    onDismiss = { showUnverifiedAccountAlertDialog = false }
                ) {
                    navController.navigate("menu_profile")
                }
            }
            if (showTicketDialog && currentUser != null) {
                TicketDialog(
                    onDismiss = { showTicketDialog = false },
                    uid = currentUser.uid,
                    tickets = userResourceFlow.data!!.tickets,
                    paddingValues = innerPadding
                )
            }
            if (searchBarExpanded) {
                SearchContent(
                    searchResultsResource = searchResultsResource,
                    onCommunityClick = { navController.navigate("community_page/$it") }
                )
            } else {
                if (currentUser != null) {
                    Content(
                        navigateToCommunity = {
                            navController.navigate("community_page/$it") {
                            }
                        },
                        communitiesResource = communitiesResource,
                        pinnedCommunities = pinnedCommunities,
                        myUid = currentUser.uid,
                        viewModel = viewModel
                    )
                } else {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.to_see_communities_sign_in),
                            style = MaterialTheme.typography.titleMedium
                        )
                        OutlinedButton(
                            onClick = { navController.navigate("log_in") }
                        ) {
                            Text(stringResource(R.string.sign_in))
                        }
                    }

                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SearchContent(
    searchResultsResource: Resource<List<Community>>,
    onCommunityClick: (String) -> Unit
) {
    val context = LocalContext.current
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (searchResultsResource) {
            is Resource.Error -> {
                Box(Modifier.fillMaxHeight(0.6f)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SearchOff, contentDescription = "error",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(searchResultsResource.messageResource?.let { stringResource(it) }
                            ?: "")
                    }
                }
            }

            is Resource.Idle -> {
                Box(Modifier.fillMaxHeight(0.6f)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        FilledTonalIconButton(
                            onClick = {}
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Groups,
                                contentDescription = "search"
                            )
                        }

                        Text(
                            stringResource(R.string.community_search_idle_text),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            is Resource.Loading -> {
                Box(Modifier.fillMaxHeight(0.6f)) {
                    CircularWavyProgressIndicator(Modifier.align(Alignment.Center))
                }
            }

            is Resource.Success -> {
                if (searchResultsResource.data != null) {
                    if (searchResultsResource.data.isEmpty()) {
                        Box(Modifier.fillMaxHeight(0.6f)) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.align(Alignment.Center)
                            ) {
                                FilledTonalIconButton(
                                    onClick = {}
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.YoutubeSearchedFor,
                                        contentDescription = "no result"
                                    )
                                }

                                Text(
                                    stringResource(R.string.community_search_empty_text),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    } else {
                        searchResultsResource.data.forEach { community ->
                            ListItem(
                                modifier = Modifier.clickable {
                                    onCommunityClick(community.id)
                                },
                                headlineContent = {
                                    Text(community.name)
                                },
                                trailingContent = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                                        contentDescription = "expand"
                                    )
                                },
                                supportingContent = {
                                    Text(
                                        community.description,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                leadingContent = {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                    ) {
                                        if (community.communityPictureUrl.isNullOrEmpty()) {
                                            Image(
                                                imageVector = Icons.Rounded.Groups,
                                                contentDescription = community.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .matchParentSize()
                                                    .background(Color.LightGray)
                                            )
                                        } else {
                                            GlideImageLoader(
                                                url = community.communityPictureUrl,
                                                context = context,
                                                modifier = Modifier.matchParentSize()
                                            )
                                        }
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommunitySearchBar(
    searchBarExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    val textFieldState = rememberTextFieldState()

    SearchBar(
        colors = SearchBarDefaults.colors(
            containerColor = Color.Transparent
        ),
        modifier = Modifier
            .zIndex(5f),
        inputField = {
            SearchBarDefaults.InputField(
                state = textFieldState,
                onSearch = {
                },
                expanded = searchBarExpanded,
                onExpandedChange = {
                    onExpandedChange(it)
                },
                placeholder = { Text(stringResource(R.string.search_events)) },
            )

        },
        expanded = searchBarExpanded,
        onExpandedChange = {
            onExpandedChange(it)
        }
    ) {}


}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Content(
    navigateToCommunity: (String) -> Unit,
    myUid: String,
    viewModel: CommunityViewModel,
    communitiesResource: Resource<List<JoinedCommunity>>,
    pinnedCommunities: List<String>
) {
    val communityList = remember { mutableStateListOf<JoinedCommunity>() }
    var communitiesLoading by remember { mutableStateOf(true) }
    var communitiesError by remember { mutableStateOf(false) }
    var communityToLeave by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(communitiesResource) {
        when (communitiesResource) {
            is Resource.Error -> {
                communitiesLoading = false
                communitiesError = true
            }

            is Resource.Idle -> {
                communitiesError = false
            }

            is Resource.Loading -> {
                communitiesLoading = true
            }

            is Resource.Success -> {
                communitiesLoading = false
                communitiesError = false
                communityList.clear()
                communityList.addAll(communitiesResource.data!!)
                communityList.sortByDescending { it.pinned }
            }
        }
    }

    LaunchedEffect(pinnedCommunities) {
        communityList.forEachIndexed { index, joinedCommunities ->
            communityList[index] = communityList[index].copy(
                pinned = pinnedCommunities.contains(joinedCommunities.id)
            )
        }
        communityList.sortByDescending { it.pinned }
    }


    Box(Modifier.fillMaxSize()) {
        if (communityToLeave != null) {
            LeavingDialog(
                userRolePriority = communityList.find { it.id == communityToLeave }?.rolePriority,
                onConfirm = {
                    viewModel.leaveTheCommunity(
                        communityId = communityToLeave!!,
                        myUid = myUid
                    )
                    communityToLeave = null
                },
                onDismiss = { communityToLeave = null }
            )
        }
        if (communitiesLoading) {
            CircularWavyProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (communitiesError) {
            Text(
                text = stringResource(R.string.something_went_wrong_try_again_later),
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            if (communityList.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_communities_found),
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
                    items(communityList) { joinedCommunity ->
                        CommunityRow(
                            community = joinedCommunity,
                            onPinClick = {
                                viewModel.changePin(
                                    it, joinedCommunity.id,
                                    myUid = myUid
                                )
                            },
                            onLeaveClick = { communityToLeave = joinedCommunity.id },
                            navigateToCommunity = { navigateToCommunity(joinedCommunity.id) }
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }
        }
    }

}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CommunityRow(
    community: JoinedCommunity,
    onPinClick: (Boolean) -> Unit,
    onLeaveClick: () -> Unit,
    navigateToCommunity: () -> Unit
) {
    val context = LocalContext.current
    var optionsMenuExpanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth(0.96f)
            .combinedClickable(
                onClick = navigateToCommunity,
                onLongClick = { optionsMenuExpanded = true },
                onLongClickLabel = "Options"
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)) {
                if (community.communityPictureUrl != null) {
                    GlideImageLoader(
                        url = community.communityPictureUrl,
                        context = context,
                        modifier = Modifier.matchParentSize()
                    )
                } else {
                    Image(
                        imageVector = Icons.Rounded.Groups,
                        contentDescription = "picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                CircleShape
                            )
                    )
                }
            }
            Column {
                Text(
                    text = community.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .width(220.dp),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
                Text(
                    text = stringResource(getRoleNameFromRolePriority(community.rolePriority)),
                    modifier = Modifier
                        .padding(start = 10.dp),
                    style = MaterialTheme.typography.labelSmall
                )
            }

        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (community.pinned) {
                Icon(
                    imageVector = Icons.Outlined.PushPin,
                    contentDescription = "pin it",
                    modifier = Modifier
                        .padding(end = 5.dp)
                        .size(20.dp)
                )
            }
            Box {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = "arrow"
                )
                DropdownMenu(
                    expanded = optionsMenuExpanded,
                    onDismissRequest = { optionsMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (community.pinned) stringResource(R.string.unpin) else stringResource(
                                    R.string.pin
                                )
                            )
                        },
                        onClick = {
                            onPinClick(!community.pinned)
                            optionsMenuExpanded = false
                        },
                        leadingIcon = if (community.pinned) {
                            {
                                Icon(
                                    painter = painterResource(R.drawable.unpin),
                                    contentDescription = "unpin"
                                )
                            }
                        } else {
                            {
                                Icon(
                                    imageVector = Icons.Rounded.PushPin,
                                    contentDescription = "pin"
                                )
                            }
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.leave_the_community)) },
                        onClick = {
                            onLeaveClick()
                            optionsMenuExpanded = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ExitToApp,
                                contentDescription = "leave the community"
                            )
                        }
                    )
                }
            }

        }

    }
}


fun getRoleNameFromRolePriority(rolePriority: Int): Int {
    Log.e("roleValue", "$rolePriority")
    val roles = listOf(
        CommunityRoles.NotMember,
        CommunityRoles.Admin,
        CommunityRoles.Moderator,
        CommunityRoles.Member
    )
    return roles.find { it.rolePriority == rolePriority }!!.roleNameResource
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showSystemUi = true)
@Composable
private fun CommunitiesPreview() {
    var searchBarExpanded by remember { mutableStateOf(false) }
    Scaffold(topBar = {
        TopAppBar(
            title = {
                CommunitySearchBar(
                    searchBarExpanded = searchBarExpanded
                ) {
                    searchBarExpanded = it
                }
            }
        )
    }) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            SearchContent(
                searchResultsResource = Resource.Success(
                    data = listOf(
                        /*Community(name = "topluluk 1", description = "description1"),
                        Community(name = "topluluk 2", description = "description2"),
                        Community(name = "topluluk 3", description = "description3"),*/
                    )
                )
                /* searchResultsResource = Resource.Idle()*/, {}
            )
        }
    }
}

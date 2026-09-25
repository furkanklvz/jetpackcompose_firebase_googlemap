package com.klavs.bindle.uix.view

import android.Manifest
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LocalActivity
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowRight
import androidx.compose.material.icons.rounded.LocalActivity
import androidx.compose.material.icons.rounded.LocationOff
import androidx.compose.material.icons.rounded.NavigateNext
import androidx.compose.material.icons.rounded.PlaylistRemove
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.klavs.bindle.R
import com.klavs.bindle.data.entity.sealedclasses.BottomNavItem
import com.klavs.bindle.data.entity.Event
import com.klavs.bindle.data.entity.User
import com.klavs.bindle.data.entity.sealedclasses.EventType
import com.klavs.bindle.data.entity.community.Community
import com.klavs.bindle.resource.Resource
import com.klavs.bindle.ui.theme.logoFont
import com.klavs.bindle.uix.view.event.getEventIconFromValue
import com.klavs.bindle.uix.viewmodel.HomeViewModel
import com.klavs.bindle.uix.viewmodel.NavHostViewModel
import com.klavs.bindle.util.EventBottomSheet
import com.klavs.bindle.util.TicketDialog
import com.klavs.bindle.util.TimeFunctions
import com.klavs.bindle.util.UnverifiedAccountAlertDialog
import com.klavs.bindle.util.UtilFunctions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneOffset


@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun Home(
    currentUser: FirebaseUser? = null,
    navController: NavHostController,
    navHostViewModel: NavHostViewModel,
    homeViewModel: HomeViewModel
) {
    val context = LocalContext.current
    val locationPermission =
        rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)
    var locationPermissionIsGranted by remember {
        mutableStateOf(locationPermission.status.isGranted)
    }

    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(
            permission = Manifest.permission.POST_NOTIFICATIONS
        )
    } else null

    val userResource by navHostViewModel.userResourceFlow.collectAsState()
    val upcomingEventsResource by navHostViewModel.upcomingEvents.collectAsState()
    val eventsNearMeResource by homeViewModel.eventsNearMe.collectAsState()
    val currentLocationResource by homeViewModel.currentLocation.collectAsState()
    val popularCommunitiesResource by homeViewModel.popularCommunitiesResource.collectAsState()
    val popularCommunityList by homeViewModel.popularCommunityList.collectAsState()
    val lastPopularCommunityDocSnapshot by homeViewModel.lastPopularCommunityDocSnapshot.collectAsState()
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(locationPermission.status) {
        locationPermissionIsGranted = locationPermission.status.isGranted
        if (locationPermissionIsGranted) {
            if (currentLocationResource is Resource.Idle
                || currentLocationResource is Resource.Error
            ) {
                homeViewModel.getCurrentLocation()
            }
        } else {
            locationPermission.launchPermissionRequest()
        }
    }

    LaunchedEffect(notificationPermissionState) {
        if (currentUser != null && notificationPermissionState != null) {
            if (!notificationPermissionState.status.isGranted &&
                !notificationPermissionState.status.shouldShowRationale
            ) {
                notificationPermissionState.launchPermissionRequest()
            }
        }
    }

    LaunchedEffect(true) {
        if (upcomingEventsResource is Resource.Idle
            || userResource is Resource.Error
        ) {
            if (currentUser != null) {
                navHostViewModel.listenToUpcomingEvents(
                    myUid = currentUser.uid
                )
            }
        }
        if (popularCommunitiesResource is Resource.Idle
            || popularCommunitiesResource is Resource.Error
        ) {
            homeViewModel.getPopularCommunities(
                limit = 3
            )
        }
    }
    LaunchedEffect(currentLocationResource) {
        if (currentLocationResource is Resource.Success) {
            if (eventsNearMeResource is Resource.Error || eventsNearMeResource is Resource.Idle) {
                val latLng =
                    LatLng(
                        currentLocationResource.data!!.latitude,
                        currentLocationResource.data!!.longitude
                    )
                homeViewModel.getEventsNearMe(latLng)
            }
        }
    }

    HomeContent(
        userResource = userResource,
        popularCommunitiesResource = popularCommunitiesResource,
        eventsNearMeResource = eventsNearMeResource,
        upcomingEventsResource = upcomingEventsResource,
        thereIsLastDoc = lastPopularCommunityDocSnapshot != null,
        currentLocationResource = currentLocationResource,
        locationPermissionIsGranted = locationPermissionIsGranted,
        navigateToEventsPage = {
            navController.navigate(BottomNavItem.Events.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        },
        onEventClick = { navController.navigate("event_page/$it") },
        onLoginClick = { navController.navigate("log_in") },
        onExploreClick = {
            navController.navigate("map_graph") {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        },
        onSearchClicked = {
            navHostViewModel.communitiesSearchBarExpanded.value = true
            navController.navigate(BottomNavItem.Communities.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                // Avoid multiple copies of the same destination when
                // reselecting the same item
                launchSingleTop = true
                // Restore state when reselecting a previously selected item
                restoreState = true
            }
        },
        onCommunityClick = {
            navController.navigate("community_page/${it}")
        },
        tryAgain = {
            homeViewModel.getCurrentLocation()
        },
        askPermission = {
            if (!locationPermission.status.shouldShowRationale) {

                scope.launch {
                    val result = snackBarHostState.showSnackbar(
                        message = context.getString(R.string.location_permission_rationale),
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
            } else {
                locationPermission.launchPermissionRequest()
            }
        },
        navHostViewModel = navHostViewModel,
        currentUser = currentUser,
        navigateToEventPage = { navController.navigate("event_page/$it") },
        navigateToProfile = { navController.navigate("menu_profile") },
        loadMorePopularCommunities = { homeViewModel.getPopularCommunities(4) },
        popularCommunityList = popularCommunityList,
        scope = scope,
        snackBarHostState = snackBarHostState
    )

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HomeContent(
    userResource: Resource<User>,
    popularCommunityList: List<Community>,
    popularCommunitiesResource: Resource<List<Community>>,
    eventsNearMeResource: Resource<List<Event>>,
    upcomingEventsResource: Resource<List<Event>>,
    currentLocationResource: Resource<Location>,
    locationPermissionIsGranted: Boolean,
    navigateToEventsPage: () -> Unit,
    navigateToEventPage: (String) -> Unit,
    navigateToProfile: () -> Unit,
    thereIsLastDoc: Boolean,
    scope: CoroutineScope,
    snackBarHostState: SnackbarHostState,
    onEventClick: (String) -> Unit,
    onLoginClick: () -> Unit,
    loadMorePopularCommunities: () -> Unit,
    onExploreClick: () -> Unit,
    onSearchClicked: () -> Unit,
    onCommunityClick: (String) -> Unit,
    tryAgain: () -> Unit,
    askPermission: () -> Unit,
    navHostViewModel: NavHostViewModel? = null,
    currentUser: FirebaseUser? = null
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    var showTicketDialog by remember { mutableStateOf(false) }
    var showCommunitiesBottomSheet by rememberSaveable { mutableStateOf(false) }
    val communitiesBottomSheetState = rememberModalBottomSheetState()
    var showUnverifiedAccountAlertDialog by remember { mutableStateOf(false) }
    var showEventBottomSheet by remember { mutableStateOf<Event?>(null) }
    val eventBottomSheetState = rememberModalBottomSheetState()
    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = {
            SnackbarHost(snackBarHostState)
        },
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BottomAppBarDefaults.containerColor
                ),
                actions = {
                    /*FilledTonalButton(
                        onClick = { navController.navigate("greeting") }
                    ) {
                        Text("Greeting Page")
                    }*/
                    if (currentUser != null && userResource is Resource.Success) {
                        FilledTonalButton(
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                            ),
                            shape = ButtonDefaults.squareShape,
                            onClick = {
                                showTicketDialog = true
                            }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.LocalActivity,
                                    contentDescription = "tickets",
                                    modifier = Modifier
                                        .padding(end = 5.dp)
                                        .size(IconButtonDefaults.xSmallIconSize),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(userResource.data?.tickets?.toString() ?: "")
                            }
                        }
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painterResource(R.drawable.logo_no_background),
                            contentDescription = "Bindle",
                            modifier = Modifier
                                .padding(end = 10.dp)
                                .size(IconButtonDefaults.xSmallIconSize),
                            contentScale = ContentScale.Crop
                        )
                        Text(
                            stringResource(R.string.app_name), fontFamily = logoFont,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                })
        }) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(top = innerPadding.calculateTopPadding())
                .fillMaxSize()
                .background(BottomAppBarDefaults.containerColor)
        )
        {
            if (showCommunitiesBottomSheet) {
                PopularCommunitiesBottomSheet(
                    onDismiss = {
                        scope.launch {
                            communitiesBottomSheetState.hide()
                        }.invokeOnCompletion {
                            if (!communitiesBottomSheetState.isVisible) {
                                showCommunitiesBottomSheet = false
                            }
                        }
                    },
                    sheetState = communitiesBottomSheetState,
                    communitiesResource = popularCommunitiesResource,
                    communityList = popularCommunityList,
                    onCommunityClick = {
                        onCommunityClick(it.id!!)
                    },
                    loadMore = loadMorePopularCommunities,
                    thereIsLastDoc = thereIsLastDoc
                )
            }
            if (showEventBottomSheet != null) {
                EventBottomSheet(
                    onDismiss = { showEventBottomSheet = null },
                    sheetState = eventBottomSheetState,
                    event = showEventBottomSheet!!,
                    onCommunityClick = { onCommunityClick(it) },
                    navHostViewModel = navHostViewModel!!,
                    showTicketDialog = { showTicketDialog = true },
                    currentUser = currentUser,
                    navigateToEventPage = { navigateToEventPage(it) },
                    showUnverifiedAccountAlertDialog = { showUnverifiedAccountAlertDialog = true },
                    context = context,
                    onLogInClick = onLoginClick
                )
            }
            if (showTicketDialog) {
                if (currentUser != null && userResource is Resource.Success) {
                    TicketDialog(
                        onDismiss = { showTicketDialog = false },
                        uid = currentUser.uid,
                        tickets = userResource.data!!.tickets,
                        paddingValues = innerPadding
                    )
                }
            }
            if (showUnverifiedAccountAlertDialog) {
                UnverifiedAccountAlertDialog(
                    onDismiss = { showUnverifiedAccountAlertDialog = false }
                ) {
                    navigateToProfile()
                }
            }
            val popularCommunitiesIsLoading =
                popularCommunitiesResource is Resource.Loading || popularCommunitiesResource is Resource.Idle
            val upcomingEventIsLoading = upcomingEventsResource is Resource.Loading
            if (upcomingEventIsLoading
                || popularCommunitiesIsLoading
            ) {
                CircularWavyProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                )

            } else {
                Column(
                    Modifier
                        .padding(horizontal = 5.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .background(Color.Transparent),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    UpcomingEventsHome(
                        upcomingEventsResource,
                        navigateToEventsPage = navigateToEventsPage,
                        onEventClick = { onEventClick(it) },
                        onLoginClick = onLoginClick
                    )
                    EventsNearMe(
                        eventsResource = eventsNearMeResource,
                        currentLocation = currentLocationResource,
                        locationPermissionIsGranted = locationPermissionIsGranted,
                        askPermission = askPermission,
                        tryAgain = tryAgain,
                        onExploreClick = onExploreClick,
                        onEventClick = {
                            showEventBottomSheet = it
                        }
                    )
                    PopularCommunities(
                        communitiesResource = popularCommunitiesResource,
                        onSearchClicked = onSearchClicked,
                        onCommunityClick = {
                            onCommunityClick(it.id!!)
                        },
                        showPopularCommunitiesBottomSheet = {
                            showCommunitiesBottomSheet = true
                        },
                        communityList = popularCommunityList
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EventsNearMe(
    eventsResource: Resource<List<Event>>,
    currentLocation: Resource<Location>,
    locationPermissionIsGranted: Boolean,
    tryAgain: () -> Unit,
    onExploreClick: () -> Unit,
    onEventClick: (Event) -> Unit,
    askPermission: () -> Unit
) {
    val isLoading = eventsResource is Resource.Loading
            || currentLocation is Resource.Loading
            || (currentLocation is Resource.Success && eventsResource is Resource.Idle)
            || (currentLocation is Resource.Idle && eventsResource is Resource.Idle && locationPermissionIsGranted)
    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ),
        modifier = Modifier
            .padding(vertical = 10.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.LocalActivity,
                    contentDescription = "upcoming events",
                    modifier = Modifier.size(IconButtonDefaults.xSmallIconSize)
                )
                Text(
                    stringResource(R.string.events_near_me),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(start = 5.dp),
                    maxLines = 1,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis
                )
                if (locationPermissionIsGranted && currentLocation.data != null) {
                    AnimatedVisibility(visible = !isLoading) {
                        IconButton(
                            onClick = tryAgain
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.RestartAlt,
                                contentDescription = "try again",
                                modifier = Modifier.size(IconButtonDefaults.xSmallIconSize)
                            )
                        }
                    }
                }
            }

            IconButton(onClick = onExploreClick) {
                Icon(
                    imageVector = Icons.Rounded.TravelExplore,
                    contentDescription = "explore more"
                )
            }
        }
        Column(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .fillMaxWidth()
        ) {
            if (!locationPermissionIsGranted) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .align(Alignment.CenterHorizontally),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LocationOff,
                                contentDescription = "no events nearby",
                                modifier = Modifier.size(IconButtonDefaults.xSmallIconSize)
                            )
                            Text(
                                stringResource(R.string.location_permission_not_granted),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 5.dp)
                            )
                        }

                    TextButton(
                        onClick = askPermission
                    ) {
                        Text(
                            stringResource(R.string.grant_permission)
                        )
                    }
                }

            } else {
                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .padding(vertical = 20.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }

                AnimatedVisibility(!isLoading) {
                    Column(Modifier.fillMaxWidth()) {
                        if (currentLocation.data == null) {
                            Column(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .align(Alignment.CenterHorizontally),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                OutlinedCard(
                                    colors = CardDefaults.outlinedCardColors(
                                        containerColor = Color.Transparent
                                    ),
                                    shape = IconButtonDefaults.mediumPressedShape
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.ErrorOutline,
                                            contentDescription = "error",
                                            modifier = Modifier.size(IconButtonDefaults.xSmallIconSize)
                                        )
                                        Text(
                                            stringResource(R.string.location_not_detected),
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(start = 5.dp),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = tryAgain
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Refresh,
                                        contentDescription = "try again"
                                    )
                                }
                            }

                        } else {
                            when (eventsResource) {
                                is Resource.Error -> {
                                    FilledTonalButton(
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .align(Alignment.CenterHorizontally),
                                        shape = IconButtonDefaults.mediumPressedShape,
                                        onClick = {},
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer,
                                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Rounded.ErrorOutline,
                                                contentDescription = "error",
                                                modifier = Modifier.size(IconButtonDefaults.xSmallIconSize)
                                            )
                                            Text(
                                                eventsResource.messageResource?.let {
                                                    stringResource(
                                                        it
                                                    )
                                                } ?: "",
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(start = 5.dp),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                    }
                                }

                                is Resource.Success -> {
                                    val eventList = eventsResource.data!!
                                    if (eventList.isEmpty()) {
                                        Text(
                                            stringResource(R.string.no_events_nearby),
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier
                                                .padding(15.dp)
                                                .align(Alignment.CenterHorizontally),
                                        )
                                    } else {
                                        eventList.forEach { event ->
                                            ListItem(
                                                modifier = Modifier.clickable { onEventClick(event) },
                                                colors = ListItemDefaults.colors(
                                                    containerColor = Color.Transparent
                                                ),
                                                headlineContent = {
                                                    Text(event.title, fontWeight = FontWeight.Bold)
                                                },
                                                supportingContent = {
                                                    val distance =
                                                        UtilFunctions().calculateDistance(
                                                            latlng1 = LatLng(
                                                                currentLocation.data.latitude,
                                                                currentLocation.data.longitude
                                                            ),
                                                            latlng2 = LatLng(
                                                                event.latitude,
                                                                event.longitude
                                                            )
                                                        )
                                                    val text = stringResource(
                                                        R.string.distance_localized_text,
                                                        distance
                                                    )
                                                    Text(
                                                        text
                                                    )
                                                },
                                                trailingContent = {
                                                    IconButton(
                                                        onClick = { onEventClick(event) }
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.AutoMirrored.Rounded.NavigateNext,
                                                            contentDescription = ""
                                                        )
                                                    }
                                                },
                                                leadingContent = {
                                                    Icon(
                                                        imageVector = getEventIconFromValue(event.type),
                                                        contentDescription = event.title
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }

                                else -> {}
                            }
                        }
                    }

                }
            }

        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PopularCommunities(
    communitiesResource: Resource<List<Community>>,
    onSearchClicked: () -> Unit,
    communityList: List<Community>,
    showPopularCommunitiesBottomSheet: () -> Unit,
    onCommunityClick: (Community) -> Unit
) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ),
        modifier = Modifier.padding(vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Groups,
                    contentDescription = "upcoming events",
                    modifier = Modifier.size(IconButtonDefaults.xSmallIconSize)
                )
                Text(
                    stringResource(R.string.popular_communities),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(start = 5.dp),
                    maxLines = 1,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onSearchClicked) {
                Icon(
                    painter = painterResource(R.drawable.group_search),
                    contentDescription = "search"
                )
            }
        }
        when (communitiesResource) {
            is Resource.Error -> {
                FilledTonalButton(
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.CenterHorizontally),
                    shape = IconButtonDefaults.mediumPressedShape,
                    onClick = {},
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = "no upcoming event",
                            modifier = Modifier.size(IconButtonDefaults.xSmallIconSize)
                        )
                        Text(
                            communitiesResource.messageResource?.let { stringResource(it) } ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 5.dp),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                }
            }

            is Resource.Idle -> {}
            is Resource.Loading -> {}
            is Resource.Success -> {
                communityList.take(3).forEach { community ->
                    ListItem(
                        modifier = Modifier.clickable { onCommunityClick(community) },
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent
                        ),
                        headlineContent = {
                            Text(
                                community.name,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        supportingContent =
                        if (community.description.isNotBlank()) {
                            {
                                Text(
                                    community.description,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else null,
                        leadingContent = {
                            Box(
                                modifier = Modifier
                                    .size(IconButtonDefaults.mediumContainerSize())
                                    .clip(
                                        RoundedCornerShape(10.dp)
                                    )
                            ) {
                                if (community.communityPictureUrl != null) {
                                    GlideImageLoader(
                                        url = community.communityPictureUrl,
                                        context = context,
                                        modifier = Modifier.matchParentSize()
                                    )
                                } else {
                                    Image(
                                        imageVector = Icons.Rounded.Groups,
                                        contentDescription = community.name,
                                        modifier = Modifier
                                            .matchParentSize()
                                            .background(Color.LightGray),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        },
                        overlineContent = {
                            Text(
                                text = stringResource(
                                    R.string.x_members,
                                    community.numOfMembers?.toString() ?: ""
                                ) + " • " + stringResource(
                                    R.string.x_events,
                                    community.numOfEvents?.toString() ?: "0"
                                )
                            )
                        }
                    )
                }
                OutlinedButton(
                    modifier = Modifier
                        .padding(6.dp)
                        .align(Alignment.CenterHorizontally),
                    onClick = showPopularCommunitiesBottomSheet
                ) {
                    Text(stringResource(R.string.see_more))
                }

            }
        }


    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PopularCommunitiesBottomSheet(
    onDismiss: () -> Unit,
    loadMore: () -> Unit,
    thereIsLastDoc: Boolean,
    onCommunityClick: (Community) -> Unit,
    sheetState: SheetState,
    communityList: List<Community>,
    communitiesResource: Resource<List<Community>>
) {
    val context = LocalContext.current
    ModalBottomSheet(
        modifier = Modifier.padding(
            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        ),
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(Modifier.fillMaxSize()) {
            Text(
                stringResource(R.string.popular_communities),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(10.dp)
                    .align(Alignment.CenterHorizontally)
            )
            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(communityList) { community ->
                    ListItem(
                        modifier = Modifier.clickable { onCommunityClick(community) },
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent
                        ),
                        headlineContent = {
                            Text(
                                community.name,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        supportingContent =
                        if (community.description.isNotBlank()) {
                            {
                                Text(
                                    community.description,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else null,
                        leadingContent = {
                            Box(
                                modifier = Modifier
                                    .size(IconButtonDefaults.mediumContainerSize())
                                    .clip(
                                        RoundedCornerShape(10.dp)
                                    )
                            ) {
                                if (community.communityPictureUrl != null) {
                                    GlideImageLoader(
                                        url = community.communityPictureUrl,
                                        context = context,
                                        modifier = Modifier.matchParentSize()
                                    )
                                } else {
                                    Image(
                                        imageVector = Icons.Rounded.Groups,
                                        contentDescription = community.name,
                                        modifier = Modifier
                                            .matchParentSize()
                                            .background(Color.LightGray),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        },
                        overlineContent = {
                            Text(
                                text = stringResource(
                                    R.string.x_members,
                                    community.numOfMembers?.toString() ?: ""
                                ) + " • " + stringResource(
                                    R.string.x_events,
                                    community.numOfEvents?.toString() ?: "0"
                                )
                            )
                        }
                    )
                }
                if (communitiesResource is Resource.Loading) {
                    item {
                        Box(Modifier.fillMaxWidth()) {
                            CircularWavyProgressIndicator(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .align(Alignment.TopCenter)
                            )
                        }
                    }
                } else if (thereIsLastDoc) {
                    item {
                        Box(Modifier.fillMaxWidth()) {
                            OutlinedIconButton(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .align(Alignment.TopCenter),
                                onClick = loadMore
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = "load more"
                                )
                            }
                        }
                    }
                }
            }
        }

    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun UpcomingEventsHome(
    eventsResource: Resource<List<Event>>,
    onEventClick: (String) -> Unit,
    onLoginClick: () -> Unit,
    navigateToEventsPage: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ),
        modifier = Modifier
            .padding(vertical = 10.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.AccessTime,
                    contentDescription = "upcoming events",
                    modifier = Modifier.size(IconButtonDefaults.xSmallIconSize)
                )
                Text(
                    stringResource(R.string.my_upcoming_events),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(start = 5.dp),
                    maxLines = 1,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (eventsResource is Resource.Success) {
                if (eventsResource.data!!.size > 1) {
                    TextButton(
                        onClick = navigateToEventsPage
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.x_more, eventsResource.data.size - 1))
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.NavigateNext,
                                contentDescription = "go events",
                                modifier = Modifier
                                    .padding(start = 5.dp)
                            )
                        }

                    }
                } else {
                    IconButton(
                        onClick = navigateToEventsPage
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                            contentDescription = "go events"
                        )
                    }
                }
            }
        }
        when (eventsResource) {
            is Resource.Error -> {
                FilledTonalButton(
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.CenterHorizontally),
                    shape = IconButtonDefaults.mediumPressedShape,
                    onClick = {},
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = "no upcoming event",
                            modifier = Modifier.size(IconButtonDefaults.xSmallIconSize)
                        )
                        Text(
                            eventsResource.messageResource?.let { stringResource(it) } ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 5.dp),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                }

            }

            is Resource.Idle -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        stringResource(R.string.sign_in_to_see_upcoming_events),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedButton(
                        onClick = {
                            onLoginClick()
                        }
                    ) {
                        Text(stringResource(R.string.sign_in))
                    }
                }

            }

            is Resource.Loading -> {
                LinearProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 10.dp)
                )
            }

            is Resource.Success -> {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .fillMaxWidth()
                ) {
                    if (eventsResource.data!!.isNotEmpty()) {
                        val firstEvent = eventsResource.data.minByOrNull { it.date }!!
                        var remainingTime = TimeFunctions().calculateLeftTime(
                            timestamp = firstEvent.date
                        )

                        LaunchedEffect(Unit) {
                            remainingTime = TimeFunctions().calculateLeftTime(
                                timestamp = firstEvent.date
                            )
                            delay(60100)
                        }

                        ListItem(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onEventClick(firstEvent.id!!) },
                            colors = ListItemDefaults.colors(
                                containerColor = Color.Transparent
                            ),
                            headlineContent = {
                                Text(firstEvent.title)
                            },
                            supportingContent = {
                                val text = remainingTime.first.second?.let {
                                    stringResource(
                                        remainingTime.first.first,
                                        it
                                    )
                                } ?: stringResource(remainingTime.first.first)
                                val highlight = remainingTime.second
                                Text(
                                    text,
                                    color = if (highlight) MaterialTheme.colorScheme.error
                                    else Color.Unspecified
                                )
                            },
                            trailingContent = {
                                IconButton(
                                    onClick = { onEventClick(firstEvent.id!!) }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.NavigateNext,
                                        contentDescription = "go to event"
                                    )
                                }
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = getEventIconFromValue(firstEvent.type),
                                    contentDescription = firstEvent.title
                                )
                            }
                        )
                        Spacer(Modifier.height(5.dp))

                    } else {
                            Row(
                                modifier = Modifier
                                    .padding(20.dp)
                                    .align(Alignment.CenterHorizontally),
                                verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.PlaylistRemove,
                                    contentDescription = "no upcoming event",
                                    modifier = Modifier.size(IconButtonDefaults.xSmallIconSize)
                                )
                                Text(
                                    stringResource(R.string.no_upcoming_events),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 5.dp)
                                )
                            }



                    }

                }
            }
        }

    }
}

@Preview(showSystemUi = true, locale = "tr")
@Composable
fun HomePreview() {
    val upcomingEventList = listOf(
        Event(
            title = "Bisiklet Turu",
            type = EventType.Sport.value,
            date = Timestamp(
                time = LocalDateTime.of(
                    2024, 12, 25, 16, 30, 0
                ).toInstant(ZoneOffset.UTC)
            )
        ),
        Event(
            title = "Doğum Günü",
            type = EventType.Party.value,
            date = Timestamp(
                time = LocalDateTime.of(
                    2024, 12, 26, 16, 30, 0
                ).toInstant(ZoneOffset.UTC)
            )
        ),
        Event(
            title = "Kan Bağışı",
            type = EventType.Solidarity.value,
            date = Timestamp(
                time = LocalDateTime.of(
                    2024, 12, 26, 16, 30, 0
                ).toInstant(ZoneOffset.UTC)
            )
        ),
        Event(
            title = "Fidan Dikme",
            type = EventType.Nature.value,
            date = Timestamp(
                time = LocalDateTime.of(
                    2024, 12, 24, 16, 30, 0
                ).toInstant(ZoneOffset.UTC)
            )
        ),
    ).sortedBy { it.date }
    val nearEventList = listOf(
        Event(
            title = "Bisiklet Turu",
            type = EventType.Sport.value,
            date = Timestamp(
                time = LocalDateTime.of(
                    2024, 12, 25, 16, 30, 0
                ).toInstant(ZoneOffset.UTC)
            )
        ),
        Event(
            title = "Doğum Günü",
            type = EventType.Party.value,
            date = Timestamp(
                time = LocalDateTime.of(
                    2024, 12, 13, 16, 30, 0
                ).toInstant(ZoneOffset.UTC)
            )
        ),
        Event(
            title = "Kan Bağışı",
            type = EventType.Solidarity.value,
            date = Timestamp(
                time = LocalDateTime.of(
                    2024, 12, 21, 16, 30, 0
                ).toInstant(ZoneOffset.UTC)
            )
        )
    ).sortedBy { it.date }
    val suggestedCommunities = listOf(
        Community(
            name = "Bisiklet Topluluğu",
            numOfMembers = 15,
            numOfEvents = 4,
            description = "descriptiondescriptiondescriptiondescriptiondescriptiondescription" +
                    "descriptiondescriptiondescriptiondescription"
        ),
        Community(
            name = "Satranç Kulübü",
            numOfMembers = 3541,
            numOfEvents = 12,
            description = "descriptiondescriptiondescriptiondescriptiondescriptiondescription" +
                    "descriptiondescriptiondescriptiondescription"
        ),
        Community(
            name = "Dostlar Köşesi",
            numOfMembers = 542,
            numOfEvents = 26,
            description = "descriptiondescriptiondescriptiondescriptiondescriptiondescription" +
                    "descriptiondescriptiondescriptiondescription"
        ),
    )
    HomeContent(
        userResource = Resource.Success(data = User()),
        popularCommunitiesResource = Resource.Success(data = suggestedCommunities),
        eventsNearMeResource = Resource.Success(data = nearEventList),
        upcomingEventsResource = Resource.Success(data = emptyList()),
        currentLocationResource = Resource.Success(data = Location("test_provider").apply {
            this.latitude = latitude
            this.longitude = longitude
        }),
        locationPermissionIsGranted = false,
        navigateToEventsPage = {},
        onEventClick = {},
        onLoginClick = {},
        onExploreClick = {},
        onSearchClicked = {},
        onCommunityClick = {},
        tryAgain = {},
        askPermission = {},
        navigateToEventPage = {},
        navigateToProfile = {},
        loadMorePopularCommunities = {},
        popularCommunityList = suggestedCommunities,
        thereIsLastDoc = true,
        scope = rememberCoroutineScope(),
        snackBarHostState = remember { SnackbarHostState() }
    )
    /*Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(text = "test",
            modifier = Modifier.align(Alignment.End),
            style = MaterialTheme.typography.headlineLarge)
    }*/
}

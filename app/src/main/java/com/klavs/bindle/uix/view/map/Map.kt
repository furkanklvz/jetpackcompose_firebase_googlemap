package com.klavs.bindle.uix.view.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.rounded.AddLocationAlt
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.LocationDisabled
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.klavs.bindle.R
import com.klavs.bindle.util.TimeFunctions
import com.klavs.bindle.data.entity.sealedclasses.BottomNavItem
import com.klavs.bindle.data.entity.Event
import com.klavs.bindle.data.entity.User
import com.klavs.bindle.data.entity.sealedclasses.EventType
import com.klavs.bindle.resource.Resource
import com.klavs.bindle.ui.theme.Green2
import com.klavs.bindle.uix.view.GlideImageLoader
import com.klavs.bindle.uix.view.event.getEventIconFromValue
import com.klavs.bindle.uix.viewmodel.MapViewModel
import com.klavs.bindle.uix.viewmodel.NavHostViewModel
import com.klavs.bindle.util.Constants
import com.klavs.bindle.util.EventBottomSheet
import com.klavs.bindle.util.TicketDialog
import com.klavs.bindle.util.UnverifiedAccountAlertDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Date

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Map(
    navController: NavHostController,
    viewModel: MapViewModel,
    currentUser: FirebaseUser?,
    navHostViewModel: NavHostViewModel,
    onBottomBarVisibilityChange: (Boolean) -> Unit,
) {
    LaunchedEffect(navController.currentDestination) {
        onBottomBarVisibilityChange(true)
    }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val context = LocalContext.current

    val mapIsLoading = remember { mutableStateOf(true) }
    val searchResultResource by viewModel.searchResults.collectAsState()
    val locationPermission =
        rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)
    val locationPermissionIsGranted = remember {
        mutableStateOf(false)
    }


    val currentLocationIsLoading = remember { mutableStateOf(true) }
    var currentLocation by remember {
        mutableStateOf<Location?>(null)
    }
    val DEFAULT_CAMERA_LOCATION = LatLng(46.031376, 29.229242)
    val firstCameraLocation = remember {
        mutableStateOf(DEFAULT_CAMERA_LOCATION)
    }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            firstCameraLocation.value,
            3f
        )
    }

    val mapHasLaunched = rememberSaveable {
        mutableStateOf(false)
    }
    LaunchedEffect(key1 = mapIsLoading.value) {
        if (locationPermissionIsGranted.value) {
            if (!mapIsLoading.value && !mapHasLaunched.value) {
                viewModel.getCurrentLocation()
                mapHasLaunched.value = true
            }
        }
    }

    val launched = remember {
        mutableStateOf(false)
    }
    LaunchedEffect(key1 = viewModel.currentLocationState.value) {
        if (launched.value) {
            when (val resource = viewModel.currentLocationState.value) {
                is Resource.Error -> {
                    currentLocationIsLoading.value = false
                    scope.launch { resource.messageResource?.let { context.getString(it) } ?: "" }
                }

                is Resource.Idle -> {}
                is Resource.Loading -> {
                    currentLocationIsLoading.value = true
                }

                is Resource.Success -> {
                    currentLocation = resource.data
                    if (currentLocation != null) {
                        firstCameraLocation.value =
                            LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
                        cameraPositionState.animate(
                            CameraUpdateFactory.newCameraPosition(
                                CameraPosition.fromLatLngZoom(
                                    firstCameraLocation.value,
                                    13f
                                )
                            )
                        )
                        currentLocationIsLoading.value = false
                    } else {
                        scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.location_not_detected)) }
                    }
                }
            }
        } else {
            launched.value = true
        }


    }

    val uiSettings = remember {
        mutableStateOf(
            MapUiSettings(
                compassEnabled = false,
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false
            )
        )
    }
    val properties = remember {
        mutableStateOf(
            MapProperties(
                mapType = MapType.NORMAL,
                isMyLocationEnabled = locationPermissionIsGranted.value,
                maxZoomPreference = 17.1f
            )
        )
    }
    val theme = viewModel.themeState.collectAsState()
    LaunchedEffect(key1 = theme.value) {
        properties.value = properties.value.copy(
            mapStyleOptions =
            MapStyleOptions.loadRawResourceStyle(
                context,
                when (theme.value) {
                    "dark" -> R.raw.dark_map
                    "light" -> R.raw.light_map
                    else -> if (isSystemInDarkTheme)
                        R.raw.dark_map
                    else R.raw.light_map
                }
            ),
            isMyLocationEnabled = locationPermissionIsGranted.value
        )
    }
    LaunchedEffect(key1 = true) {
        if (!locationPermission.status.isGranted) {
            locationPermission.launchPermissionRequest()
        }
    }
    LaunchedEffect(key1 = locationPermission.status.isGranted) {
        locationPermissionIsGranted.value = locationPermission.status.isGranted
        if (locationPermissionIsGranted.value && cameraPositionState.position.target == DEFAULT_CAMERA_LOCATION) {
            viewModel.getCurrentLocation()
        }
        properties.value = properties.value.copy(
            isMyLocationEnabled = locationPermissionIsGranted.value
        )
    }

    if (mapIsLoading.value) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.align(Alignment.Center)) {
                CircularWavyProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }

    MapView(
        onMapLoaded = {
            mapIsLoading.value = false
        },
        searchResultResource = searchResultResource,
        cameraPositionState = cameraPositionState,
        properties = properties.value,
        uiSettings = uiSettings.value,
        locationPermissionIsGranted = locationPermissionIsGranted.value,
        locationPermission = locationPermission,
        navController = navController,
        viewModel = viewModel,
        onBottomBarVisibilityChange = { onBottomBarVisibilityChange(it) },
        currentUser = currentUser,
        onSearch = {
            viewModel.searchEvent(
                searchQuery = it
            )
        },
        navHostViewModel = navHostViewModel,
        scope = scope,
        snackbarHostState = snackbarHostState,
    )

}


@OptIn(
    ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
private fun MapView(
    onMapLoaded: () -> Unit,
    cameraPositionState: CameraPositionState,
    properties: MapProperties,
    uiSettings: MapUiSettings,
    currentUser: FirebaseUser?,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    locationPermissionIsGranted: Boolean,
    locationPermission: PermissionState,
    navController: NavHostController,
    onBottomBarVisibilityChange: (Boolean) -> Unit,
    viewModel: MapViewModel,
    navHostViewModel: NavHostViewModel,
    searchResultResource: Resource<List<Event>>,
    onSearch: (String) -> Unit

) {
    val userResource by navHostViewModel.userResourceFlow.collectAsState()
    val context = LocalContext.current
    var selectionModeIsEnable by remember { mutableStateOf(false) }
    val eventBottomSheetState = rememberModalBottomSheetState()
    var showEventBottomSheet by remember { mutableStateOf<Event?>(null) }
    val eventList = remember { mutableStateListOf<Event>() }
    var isLoading by remember { mutableStateOf(false) }
    var openFilterDialog by remember { mutableStateOf(false) }
    var showTicketDialog by remember { mutableStateOf(false) }
    var onlyPublicEvents by remember { mutableStateOf(false) }
    var showUnverifiedAccountAlertDialog by remember { mutableStateOf(false) }
    val selectedCategories = remember { mutableStateListOf<EventType>() }
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }


    LaunchedEffect(true) {
        if (viewModel.createEventState.value is Resource.Success) {
            val locationLatLng = LatLng(
                viewModel.createEventState.value.data!!.latitude,
                viewModel.createEventState.value.data!!.longitude
            )
            showEventBottomSheet = viewModel.createEventState.value.data!!
            eventList.add(viewModel.createEventState.value.data!!)
            cameraPositionState.animate(
                update = CameraUpdateFactory.newCameraPosition(
                    CameraPosition.fromLatLngZoom(
                        locationLatLng,
                        17f
                    )
                )
            )
            viewModel.createEventState.value = Resource.Idle()
        }
    }
    val eventsInCamRegion by viewModel.eventsInRegion.collectAsState()
    LaunchedEffect(eventsInCamRegion) {
        when (eventsInCamRegion) {
            is Resource.Error -> {
                isLoading = false
                scope.launch {
                    snackbarHostState.showSnackbar(eventsInCamRegion.messageResource?.let {
                        context.getString(
                            it
                        )
                    } ?: "")
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
                eventList.clear()
                eventList.addAll(eventsInCamRegion.data!!)
                if (eventList.isEmpty()) {
                    scope.launch {
                        snackbarHostState.showSnackbar(context.getString(R.string.no_events_found))
                    }
                    viewModel.resetSearchResultResource()
                }
                Log.e("map", "eventList.size: " + eventList.size)
            }
        }
    }

    var searchBarExpanded by rememberSaveable { mutableStateOf(false) }
    val textFieldState = rememberTextFieldState()
    LaunchedEffect(searchBarExpanded) {
        onBottomBarVisibilityChange(!searchBarExpanded)
    }
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .semantics { isTraversalGroup = true }) {
            if (showUnverifiedAccountAlertDialog) {
                UnverifiedAccountAlertDialog(
                    onDismiss = { showUnverifiedAccountAlertDialog = false }
                ) {
                    navController.navigate("menu_profile")
                }
            }
            if (showTicketDialog) {
                if (currentUser != null) {
                    if (userResource is Resource.Success && userResource.data != null) {
                        TicketDialog(
                            onDismiss = { showTicketDialog = false },
                            uid = userResource.data!!.uid!!,
                            tickets = userResource.data!!.tickets,
                            paddingValues = innerPadding
                        )
                    } else {
                        scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.something_went_wrong_try_again_later)) }
                        showTicketDialog = false
                    }
                } else {
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            context.getString(R.string.please_sign_in),
                            actionLabel = context.getString(R.string.sign_in),
                            duration = SnackbarDuration.Long,
                            withDismissAction = true
                        )
                        when (result) {
                            SnackbarResult.Dismissed -> {}
                            SnackbarResult.ActionPerformed -> {
                                navController.navigate("log_in")
                            }
                        }
                    }
                    showTicketDialog = false
                }
            }
            if (openFilterDialog) {
                FilterDialog(
                    startDate = startDate,
                    endDate = endDate,
                    selectedCategories = selectedCategories,
                    onlyPublicEvents = onlyPublicEvents,
                    onStartDateChange = { startDate = it },
                    onEndDateChange = { endDate = it },
                    onPublicChange = { onlyPublicEvents = it },
                    onAddSelectedCategories = { selectedCategories.add(it) },
                    onRemoveSelectedCategories = { selectedCategories.remove(it) },
                    onDismissRequest = { openFilterDialog = false },
                    onReset = {
                        selectedCategories.clear()
                        startDate = null
                        endDate = null
                        onlyPublicEvents = false
                    },
                    onConfirm = {
                        openFilterDialog = false
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newCameraPosition(
                                    CameraPosition.fromLatLngZoom(
                                        cameraPositionState.position.target,
                                        11f
                                    )
                                )
                            )
                        }.invokeOnCompletion {
                            val bounds =
                                cameraPositionState.projection?.visibleRegion?.latLngBounds
                            if (bounds != null) {
                                Log.e("map log", "startDate: ${startDate?.let { Timestamp(Date(it)) }}, endDate: ${endDate?.let { Timestamp(Date(it)) }}")
                                viewModel.getEventsInRegion(
                                    bounds = bounds,
                                    listSize = 10,
                                    selectedCategories = selectedCategories.map { it.value },
                                    startDate = startDate,
                                    endDate = endDate,
                                    onlyPublicEvents = onlyPublicEvents
                                )
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar(context.getString(R.string.camera_is_not_ready))
                                }
                            }
                        }

                    }
                )
            }
            Column(
                Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(2f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(visible = !selectionModeIsEnable) {
                    SearchBarContent(
                        textFieldState = textFieldState,
                        onSearch = { onSearch(it) },
                        searchBarExpanded = searchBarExpanded,
                        searchResultResource = searchResultResource,
                        changeSearchBarExpanded = { searchBarExpanded = it },
                        userResource = userResource,
                        showTicketDialog = { showTicketDialog = true },
                        openFilterDialog = { openFilterDialog = true },
                        selectedCategories = selectedCategories,
                        startDate = startDate,
                        addToEventList = { eventList.add(it) },
                        showEventBottomSheet = { showEventBottomSheet = it },
                        animateCamera = {
                            scope.launch {
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newCameraPosition(
                                        CameraPosition.fromLatLngZoom(
                                            it,
                                            17f
                                        )
                                    )
                                )
                            }
                        },
                        endDate = endDate,
                        clearSearchBar = {
                            textFieldState.clearText()
                            viewModel.clearSearchResults()
                        },
                        onlyPublicEvents = onlyPublicEvents
                    )
                }
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (!selectionModeIsEnable) {
                        if (!isLoading) {
                            ElevatedButton(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .zIndex(2f),
                                onClick = {
                                    val bounds =
                                        cameraPositionState.projection?.visibleRegion?.latLngBounds
                                    if (bounds != null) {
                                        viewModel.getEventsInRegion(
                                            bounds = bounds, listSize = 10,
                                            selectedCategories = selectedCategories.map { it.value },
                                            startDate = startDate,
                                            endDate = endDate,
                                            onlyPublicEvents = onlyPublicEvents
                                        )
                                    } else {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(context.getString(R.string.camera_is_not_ready))
                                        }
                                    }
                                }
                            ) {
                                Text(stringResource(R.string.search_events_in_this_region))
                            }
                        }
                    }
                    IconButton(
                        onClick = {
                            if (locationPermissionIsGranted) {
                                viewModel.getCurrentLocation()
                            } else {
                                if (!locationPermission.status.shouldShowRationale) {

                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
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
                                }else {
                                    locationPermission.launchPermissionRequest()
                                }
                            }
                        },
                        modifier = Modifier
                            .padding(
                                end = 5.dp,
                                top = if (selectionModeIsEnable) WindowInsets.statusBars
                                    .asPaddingValues()
                                    .calculateTopPadding() else 0.dp
                            )
                            .align(Alignment.CenterEnd)
                            .shadow(3.dp, shape = CircleShape)
                            .background(
                                MaterialTheme.colorScheme.surfaceContainerLow,
                                shape = CircleShape
                            )
                            .zIndex(2f)

                    ) {
                        if (locationPermissionIsGranted) {
                            Icon(
                                imageVector = Icons.Rounded.MyLocation,
                                contentDescription = "location"
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.LocationDisabled,
                                contentDescription = "location"
                            )
                        }
                    }
                }

            }



            if (isLoading) {
                CircularWavyProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .zIndex(2f)
                        .padding(top = 30.dp)
                )
            }

            if (showEventBottomSheet != null) {
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
                        navController.navigate("event_page/${showEventBottomSheet!!.id!!}")
                    },
                    onLogInClick = { navController.navigate("log_in") }
                )
            }

            if (!searchBarExpanded) {
                FloatingActionButton(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 25.dp, start = 15.dp)
                        .zIndex(2f),
                    containerColor = if (selectionModeIsEnable) MaterialTheme.colorScheme.errorContainer else FloatingActionButtonDefaults.containerColor,
                    contentColor = if (selectionModeIsEnable) MaterialTheme.colorScheme.onErrorContainer else contentColorFor(
                        FloatingActionButtonDefaults.containerColor
                    ),
                    onClick = {
                        if (currentUser != null) {
                            if (!selectionModeIsEnable && !currentUser.isEmailVerified) {
                                showUnverifiedAccountAlertDialog = true
                            } else {
                                selectionModeIsEnable = !selectionModeIsEnable
                            }

                        } else {
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    context.getString(R.string.please_sign_in_to_create_event),
                                    actionLabel = context.getString(R.string.sign_in),
                                    duration = SnackbarDuration.Long,
                                    withDismissAction = true
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    navController.navigate("log_in")
                                }
                            }
                        }
                    }) {
                    if (!selectionModeIsEnable) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.AddLocationAlt,
                                contentDescription = "add",
                                modifier = Modifier.padding(start = 6.dp)
                            )
                            Text(
                                text = stringResource(R.string.create_event),
                                modifier = Modifier.padding(horizontal = 9.dp)
                            )
                        }

                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Cancel,
                                contentDescription = "cancel",
                                modifier = Modifier.padding(start = 6.dp)
                            )
                            Text(
                                text = stringResource(R.string.cancel),
                                modifier = Modifier.padding(horizontal = 9.dp)
                            )
                        }

                    }
                }
            }

            FloatingActionButton(
                onClick = {
                    if (userResource is Resource.Success && userResource.data != null) {
                        if (userResource.data!!.tickets < 3) {
                            showTicketDialog = true
                        } else {
                            val latitude = cameraPositionState.position.target.latitude.toString()
                            val longitude = cameraPositionState.position.target.longitude.toString()
                            navController.navigate("create_event/$latitude/$longitude") {
                                viewModel.createEventState.value = Resource.Idle()
                                popUpTo(BottomNavItem.Map.route) {
                                    saveState = true
                                }
                            }
                        }
                    }

                },
                modifier = Modifier
                    .animateFloatingActionButton(
                        visible = selectionModeIsEnable,
                        alignment = Alignment.BottomEnd
                    )
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 25.dp, end = 15.dp)
                    .zIndex(2f),
                containerColor = Green2,
                contentColor = Color.Black,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "check",
                        modifier = Modifier.padding(start = 6.dp)
                    )
                    Text(
                        text = stringResource(R.string.create),
                        modifier = Modifier.padding(horizontal = 9.dp)
                    )
                }
            }
            AnimatedVisibility(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 100.dp)
                    .zIndex(2f), visible = selectionModeIsEnable
            ) {
                Text(
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    text = stringResource(R.string.select_location_by_dragging_the_map),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier

                        .background(
                            MaterialTheme.colorScheme.background.copy(alpha = 0.6f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(10.dp)
                )
            }
            if (selectionModeIsEnable) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = "",
                    modifier = Modifier
                        .zIndex(2f)
                        .offset(y = (-24).dp)
                        .size(48.dp)
                        .align(
                            Alignment.Center
                        )
                        .zIndex(2f)
                )
            }


            GoogleMap(
                onMapLoaded = onMapLoaded,
                modifier = Modifier.matchParentSize(),
                cameraPositionState = cameraPositionState,
                properties = properties,
                uiSettings = uiSettings
            ) {

                eventList.forEach { event ->
                    if (!selectionModeIsEnable) {
                        Log.e("map", "num of marker: " + eventList.size)
                        val markerState =
                            MarkerState(position = LatLng(event.latitude, event.longitude))
                        Marker(
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET),
                            state = markerState,
                            title = event.title,
                            onClick = { marker ->
                                scope.launch {
                                    marker.showInfoWindow()
                                    cameraPositionState.animate(
                                        update = CameraUpdateFactory.newCameraPosition(
                                            CameraPosition.fromLatLngZoom(
                                                LatLng(
                                                    event.latitude,
                                                    event.longitude
                                                ),
                                                17f
                                            )
                                        )
                                    )
                                }
                                showEventBottomSheet = event
                                true
                            }
                        )
                    }
                }
            }
        }
    }

}


@OptIn(
    ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class,
)
@Composable
fun FilterDialog(
    onDismissRequest: () -> Unit,
    onReset: () -> Unit,
    startDate: Long?,
    endDate: Long?,
    selectedCategories: List<EventType>,
    onStartDateChange: (Long?) -> Unit,
    onEndDateChange: (Long?) -> Unit,
    onAddSelectedCategories: (EventType) -> Unit,
    onRemoveSelectedCategories: (EventType) -> Unit,
    onConfirm: () -> Unit,
    onlyPublicEvents: Boolean,
    onPublicChange: (Boolean) -> Unit
) {
    var expandCategory by remember { mutableStateOf(true) }
    var expandDate by remember { mutableStateOf(true) }
    var showDateRangePicker by remember { mutableStateOf(false) }
    val categories = Constants.EVENT_TYPES
    var numOfParticipantsRangeError by remember { mutableStateOf(false) }
    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = startDate,
        initialSelectedEndDateMillis = endDate
    )
    val filteringIsEnable =
        !(selectedCategories.isEmpty()
                && !onlyPublicEvents
                && startDate == null
                && endDate == null)
    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        if (showDateRangePicker) {
            DatePickerDialog(
                properties = DialogProperties(
                    dismissOnClickOutside = true
                ),
                onDismissRequest = { showDateRangePicker = false },
                confirmButton = {
                    Button(
                        onClick = {
                            onStartDateChange(dateRangePickerState.selectedStartDateMillis)
                            onEndDateChange(dateRangePickerState.selectedEndDateMillis)
                            showDateRangePicker = false
                        }
                    ) {
                        Text(stringResource(R.string.select))
                    }
                }, dismissButton = {
                    TextButton(
                        onClick = { showDateRangePicker = false }
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            ) {
                DateRangePicker(
                    state = dateRangePickerState
                )
            }
        }
        Surface(
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp),
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6f)
        ) {
            Column(
                Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { expandDate = !expandDate },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Public,
                                    contentDescription = "public",
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                                Text(
                                    stringResource(R.string.only_public_events),
                                    modifier = Modifier.padding(10.dp),
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            Switch(
                                modifier = Modifier.padding(end = 5.dp),
                                checked = onlyPublicEvents,
                                onCheckedChange = { onPublicChange(it) }
                            )
                        }
                    }
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { expandCategory = !expandCategory },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Category,
                                    contentDescription = "category",
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                                Text(
                                    stringResource(R.string.event_type),
                                    modifier = Modifier.padding(10.dp),
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }

                            Icon(
                                imageVector = if (expandCategory) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = "expand"
                            )
                        }
                        AnimatedVisibility(visible = expandCategory) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                categories.forEach { category ->
                                    FilterChip(
                                        leadingIcon = {
                                            Icon(
                                                imageVector = category.icon,
                                                contentDescription = null
                                            )
                                        },
                                        selected = selectedCategories.contains(category),
                                        onClick = if (selectedCategories.contains(category)) {
                                            { onRemoveSelectedCategories(category) }
                                        } else {
                                            { onAddSelectedCategories(category) }
                                        },
                                        label = { Text(stringResource(category.labelResource)) }
                                    )
                                }
                            }

                        }
                    }
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { expandDate = !expandDate },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.DateRange,
                                    contentDescription = "date",
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                                Text(
                                    stringResource(R.string.date_range),
                                    modifier = Modifier.padding(10.dp),
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            Icon(
                                imageVector = if (expandDate) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = "expand"
                            )
                        }
                        AnimatedVisibility(
                            visible = expandDate
                        ) {
                            Column(Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 10.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        stringResource(R.string.select_date_range),
                                        style = MaterialTheme.typography.titleSmall,

                                        )
                                    if (startDate != null) {
                                        ElevatedAssistChip(
                                            colors = AssistChipDefaults.elevatedAssistChipColors(
                                                labelColor = MaterialTheme.colorScheme.error
                                            ),
                                            onClick = {
                                                onStartDateChange(null)
                                                onEndDateChange(null)
                                            },
                                            label = { Text(stringResource(R.string.clear)) }
                                        )
                                    }
                                }

                                ElevatedCard(
                                    modifier = Modifier
                                        .padding(vertical = 10.dp)
                                        .align(Alignment.CenterHorizontally),
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    onClick = { showDateRangePicker = true }
                                ) {
                                    Text((startDate?.let {
                                        TimeFunctions().convertTimestampToLocalizeDate(
                                            Timestamp(Date(it))
                                        )
                                    }
                                        ?: stringResource(R.string.start_date)) + " - " + (endDate?.let {
                                        TimeFunctions().convertTimestampToLocalizeDate(
                                            Timestamp(Date(it))
                                        )
                                    } ?: stringResource(R.string.end_date)),
                                        modifier = Modifier.padding(15.dp))

                                }
                            }


                        }
                    }
                }
                Row(
                    Modifier
                        .padding(2.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onDismissRequest
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "close"
                        )
                    }
                    Row {
                        TextButton(
                            onClick = {
                                onReset()
                                numOfParticipantsRangeError = false
                            }
                        ) {
                            Text(stringResource(R.string.clear))
                        }
                        FilledTonalIconButton(
                            enabled = filteringIsEnable,
                            onClick = {
                                onConfirm()
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.map_search),
                                contentDescription = "search"
                            )
                        }
                    }

                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    event: Event,
    onItemClick: () -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(
                event.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        },
        modifier = Modifier.clickable { onItemClick.invoke() },
        overlineContent = {
            if (!event.addressDescription.isNullOrBlank()) {
                if (event.privateInfo) {
                    Text(
                        stringResource(R.string.private_address),
                        fontStyle = FontStyle.Italic,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Text(
                        event.addressDescription,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        supportingContent = {
            Text(
                event.description,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = {

            if (event.privateInfo) {
                Text(
                    "${stringResource(R.string._private)}\n${stringResource(R.string.date)}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    TimeFunctions().convertTimestampToLocalizeDate(
                        event.date,
                        singleLine = false,
                    ),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Center
                )
            }

        },
        trailingContent = {
            Icon(
                imageVector = getEventIconFromValue(event.type),
                contentDescription = null
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SearchBarContent(
    textFieldState: TextFieldState,
    onSearch: (String) -> Unit,
    searchBarExpanded: Boolean,
    searchResultResource: Resource<List<Event>>,
    changeSearchBarExpanded: (Boolean) -> Unit,
    userResource: Resource<User>,
    showTicketDialog: () -> Unit,
    openFilterDialog: () -> Unit,
    selectedCategories: List<EventType>,
    startDate: Long?,
    onlyPublicEvents: Boolean,
    clearSearchBar: () -> Unit,
    addToEventList: (Event) -> Unit,
    showEventBottomSheet: (Event?) -> Unit,
    animateCamera: (LatLng) -> Unit,
    endDate: Long?,
) {
    val context = LocalContext.current
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    SearchBar(
        shadowElevation = 3.dp,
        modifier = Modifier
            .semantics { traversalIndex = 0f }
            .zIndex(5f),
        inputField = {
            SearchBarDefaults.InputField(
                modifier = Modifier.width(screenWidth * 0.85f),
                state = textFieldState,
                onSearch = {
                    onSearch(it)
                },
                expanded = searchBarExpanded,
                onExpandedChange = {
                    changeSearchBarExpanded(it)
                },
                placeholder = { Text(stringResource(R.string.search_events)) },
                leadingIcon = {
                    if (searchBarExpanded) {
                        IconButton(
                            onClick = { changeSearchBarExpanded(false) }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "expand off"
                            )
                        }
                    } else {

                        Box(
                            Modifier
                                .size(SearchBarDefaults.InputFieldHeight * 0.65f)
                                .clip(
                                    CircleShape
                                )
                                .clickable {
                                    showTicketDialog()
                                }
                        ) {
                            if (userResource is Resource.Success && userResource.data!!.profilePictureUrl != null) {
                                GlideImageLoader(
                                    userResource.data.profilePictureUrl!!,
                                    context,
                                    Modifier.matchParentSize()
                                )
                            } else {
                                Image(
                                    imageVector = Icons.Rounded.Person,
                                    contentDescription = "anonymous",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(Color.LightGray)
                                )
                            }
                        }

                    }
                },
                trailingIcon = {
                    if (searchBarExpanded) {
                        if (textFieldState.text.isNotEmpty()) {
                            IconButton(onClick = {
                                clearSearchBar()
                            }) {
                                Icon(
                                    Icons.Rounded.Close,
                                    contentDescription = "clear"
                                )
                            }
                        } else {
                            Icon(
                                Icons.Rounded.Search,
                                contentDescription = "search"
                            )
                        }

                    } else {
                        val filteringIsEnable = selectedCategories.isNotEmpty()
                                || startDate != null
                                || endDate != null
                                || onlyPublicEvents

                        IconButton(
                            onClick = openFilterDialog
                        ) {
                            BadgedBox(
                                badge = {
                                    if (filteringIsEnable) {
                                        Badge()
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.FilterList,
                                    contentDescription = "filter"
                                )
                            }
                        }
                    }
                },
            )
        },
        expanded = searchBarExpanded,
        onExpandedChange = {
            changeSearchBarExpanded(it)
        }
    ) {
        when (searchResultResource) {
            is Resource.Error -> {
                Box(Modifier.fillMaxSize()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.align(
                            Alignment.Center
                        ),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = "error"
                        )
                        Text(
                            searchResultResource.messageResource?.let {
                                stringResource(
                                    it
                                )
                            } ?: "",
                            style = MaterialTheme.typography.titleSmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            is Resource.Idle -> {
                Box(
                    Modifier
                        .fillMaxHeight(0.6f)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        FilledTonalIconButton(
                            onClick = {}
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Celebration,
                                contentDescription = "search"
                            )
                        }

                        Text(
                            stringResource(R.string.event_search_idle_text),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            is Resource.Loading -> {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.6f)
                ) {
                    CircularWavyProgressIndicator(
                        modifier = Modifier.align(
                            Alignment.Center
                        )
                    )
                }
            }

            is Resource.Success -> {
                if (searchResultResource.data.isNullOrEmpty()) {
                    Box(
                        Modifier
                            .fillMaxHeight(0.6f)
                            .align(Alignment.CenterHorizontally)) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SearchOff,
                                contentDescription = "no results found"
                            )
                            Text(
                                stringResource(R.string.no_events_found),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        searchResultResource.data.forEach { event ->

                            SearchResultRow(
                                event = event,
                                onItemClick = {
                                    changeSearchBarExpanded(false)
                                    val locationLatLng = LatLng(
                                        event.latitude,
                                        event.longitude
                                    )
                                    showEventBottomSheet(event)
                                    addToEventList(event)
                                    Log.e("map", "marker added")
                                    animateCamera(locationLatLng)
                                }
                            )
                        }
                    }

                }
            }
        }
    }
}

@Preview
@Composable
private fun MapPreview() {
    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.SpaceAround
    ) {
        if (true) {
            SearchBarContent(
                textFieldState = TextFieldState(""),
                onSearch = {},
                searchBarExpanded = true,
                searchResultResource = Resource.Success(
                    data = listOf(

                    )
                ),
                changeSearchBarExpanded = {},
                userResource = Resource.Success(data = User()),
                showTicketDialog = {},
                openFilterDialog = {},
                selectedCategories = emptyList(),
                startDate = null,
                addToEventList = {},
                showEventBottomSheet = {},
                animateCamera = {},
                endDate = null,
                clearSearchBar = {},
                onlyPublicEvents = true
            )
        } else {
            FilterDialog(
                onDismissRequest = {},
                onReset = {},
                startDate = 0,
                endDate = 0,
                selectedCategories = emptyList(),
                onStartDateChange = {},
                onEndDateChange = {},
                onAddSelectedCategories = {},
                onRemoveSelectedCategories = {},
                onConfirm = {},
                onlyPublicEvents = true,
                onPublicChange = {}
            )
        }
        val rowHeight = 120.dp
        /*SearchResultRow(
            Event(
                title = "title",
                description = "descriptiondescriptiondescriptiondescriptiondescriptiondescription",
                addressDescription = "addressDescriptionaddressDescriptionaddressDescriptionaddressDescription",
            ),
            onItemClick = {},
            onShowOnMapClick = {}
        )*/
    }
    /*var searchBarExpanded by rememberSaveable { mutableStateOf(false) }
    val textFieldState = rememberTextFieldState()
    Box(
        Modifier
            .fillMaxSize()
            .semantics { isTraversalGroup = true }) {

        SearchBar(
            shadowElevation = 3.dp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .semantics { traversalIndex = 0f }
                .zIndex(5f),
            inputField = {
                SearchBarDefaults.InputField(
                    modifier = Modifier.width(300.dp),
                    state = textFieldState,
                    onSearch = { },
                    expanded = searchBarExpanded,
                    onExpandedChange = {
                        searchBarExpanded = it
                    },
                    placeholder = { Text("Search Events") },
                    leadingIcon = {
                        if (searchBarExpanded) {
                            IconButton(
                                onClick = { searchBarExpanded = false }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "expand off"
                                )
                            }
                        } else {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null
                            )

                        }
                    },
                    trailingIcon = {
                        if (searchBarExpanded) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "search"
                            )
                        } else {
                            IconButton(
                                onClick = {}
                            ) {
                                Icon(
                                    Icons.Default.FilterList,
                                    contentDescription = "filter"
                                )
                            }
                        }
                    },
                )
            },
            expanded = searchBarExpanded,
            onExpandedChange = {
                searchBarExpanded = it
            }
        ) { }
    }*/
}
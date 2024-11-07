package com.klavs.bindle.uix.view.map

import android.annotation.SuppressLint
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.rounded.AddLocation
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.LocationDisabled
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.klavs.bindle.R
import com.klavs.bindle.resource.Resource
import com.klavs.bindle.ui.theme.Green2
import com.klavs.bindle.uix.view.loading.LoadingAnimation
import com.klavs.bindle.uix.viewmodel.MapViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Map(navController: NavHostController, viewModel: MapViewModel = hiltViewModel()) {

    val isSystemInDarkTheme = isSystemInDarkTheme()
    val context = LocalContext.current

    val mapIsLoading = remember { mutableStateOf(true) }

    val locationPermission =
        rememberPermissionState(permission = android.Manifest.permission.ACCESS_FINE_LOCATION)
    val locationPermissionIsGranted = remember {
        mutableStateOf(false)
    }


    val currentLocationIsLoading = remember { mutableStateOf(true) }
    val currentLocation = remember {
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
                    Toast.makeText(context, "Location data not found", Toast.LENGTH_SHORT).show()
                }

                is Resource.Idle -> {}
                is Resource.Loading -> {
                    currentLocationIsLoading.value = true
                }

                is Resource.Success -> {
                    currentLocation.value = resource.data
                    firstCameraLocation.value =
                        LatLng(currentLocation.value!!.latitude, currentLocation.value!!.longitude)
                    cameraPositionState.animate(
                        CameraUpdateFactory.newCameraPosition(
                            CameraPosition.fromLatLngZoom(
                                firstCameraLocation.value,
                                13f
                            )
                        )
                    )
                    currentLocationIsLoading.value = false

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
                isMyLocationEnabled = locationPermissionIsGranted.value
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
                LoadingAnimation(300.dp)
            }
        }
    }

    MapView(
        onMapLoaded = { mapIsLoading.value = false
                      Log.e("mappp", "map loaded")},
        cameraPositionState = cameraPositionState,
        properties = properties.value,
        uiSettings = uiSettings.value,
        locationPermissionIsGranted = locationPermissionIsGranted.value,
        locationPermission = locationPermission,
        navController= navController
    )

}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
private fun MapView(
    onMapLoaded: () -> Unit,
    cameraPositionState: CameraPositionState,
    properties: MapProperties,
    uiSettings: MapUiSettings,
    locationPermissionIsGranted: Boolean,
    locationPermission: PermissionState,
    navController: NavHostController,
    viewModel: MapViewModel = hiltViewModel()

) {
    val selectionModeIsEnable = remember { mutableStateOf(false) }
    val markerState = rememberMarkerState(position = cameraPositionState.position.target)
    var searchText by remember {
        mutableStateOf("")
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                actions = {
                    IconButton(
                        onClick = {
                            if (locationPermissionIsGranted) {
                                viewModel.getCurrentLocation()
                            } else {
                                locationPermission.launchPermissionRequest()
                            }
                        },
                        modifier = Modifier
                            .shadow(3.dp, shape = CircleShape)
                            .background(
                                MaterialTheme.colorScheme.surfaceContainerLow,
                                shape = CircleShape
                            )
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
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                title = {
                    if (!selectionModeIsEnable.value) {
                        TextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            textStyle = TextStyle(fontSize = 15.sp),
                            shape = CircleShape,
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            ),
                            placeholder = {
                                Text(
                                    text = "Search",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(50.dp)
                                .shadow(3.dp, shape = CircleShape)
                        )
                    }
                })
        }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize()) {
            FloatingActionButton(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 15.dp, start = 15.dp)
                    .zIndex(2f),
                containerColor = if (selectionModeIsEnable.value) MaterialTheme.colorScheme.errorContainer else FloatingActionButtonDefaults.containerColor,
                contentColor = if (selectionModeIsEnable.value) MaterialTheme.colorScheme.onErrorContainer else contentColorFor(
                    FloatingActionButtonDefaults.containerColor
                ),
                onClick = {
                    selectionModeIsEnable.value = !selectionModeIsEnable.value
                }) {
                if (!selectionModeIsEnable.value) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.AddLocation, contentDescription = "add",
                            modifier = Modifier.padding(start = 6.dp)
                        )
                        Text(text = "Create Event", modifier = Modifier.padding(horizontal = 9.dp))
                    }

                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Cancel,
                            contentDescription = "cancel",
                            modifier = Modifier.padding(start = 6.dp)
                        )
                        Text(text = "Cancel", modifier = Modifier.padding(horizontal = 9.dp))
                    }

                }
            }
            if (selectionModeIsEnable.value) {
                FloatingActionButton(
                    onClick = {
                        val latitude = cameraPositionState.position.target.latitude.toString()
                        val longitude = cameraPositionState.position.target.longitude.toString()
                        navController.navigate("create_event/$latitude/$longitude")
                              },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 15.dp, end = 15.dp)
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
                        Text(text = "Create", modifier = Modifier.padding(horizontal = 9.dp))
                    }
                }
                Text(
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    text = "Select Location by Dragging The Map",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = innerPadding.calculateTopPadding() + 10.dp)
                        .zIndex(2f)
                )
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = "",
                    modifier = Modifier
                        .align(
                            Alignment.Center
                        )
                        .offset(y = (-24).dp)
                        .size(48.dp)
                        .zIndex(2f)
                )
            }
            GoogleMap(
                onMapLoaded = onMapLoaded,
                modifier = Modifier.matchParentSize(),
                cameraPositionState = cameraPositionState,
                properties = properties,
                uiSettings = uiSettings
            ) {}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun MapPreview() {
    /*MapView(
        mapIsLoading = false,

        locationPermissionIsGranted = true,
    )*/
}
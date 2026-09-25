package com.klavs.bindle.uix.view.map

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.automirrored.outlined.ShortText
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.rounded.AllInclusive
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.klavs.bindle.R
import com.klavs.bindle.util.TimeFunctions
import com.klavs.bindle.data.entity.Event
import com.klavs.bindle.data.entity.sealedclasses.EventType
import com.klavs.bindle.data.entity.sealedclasses.HidingDataOptions
import com.klavs.bindle.data.entity.sealedclasses.ParticipationOptionsForEvent
import com.klavs.bindle.data.entity.community.JoinedCommunity
import com.klavs.bindle.resource.Resource
import com.klavs.bindle.ui.theme.LightRed
import com.klavs.bindle.uix.view.GlideImageLoader
import com.klavs.bindle.uix.view.communities.getRoleNameFromRolePriority
import com.klavs.bindle.uix.viewmodel.MapViewModel
import com.klavs.bindle.uix.viewmodel.NavHostViewModel
import com.klavs.bindle.util.Constants
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CreateEvent(
    navController: NavHostController,
    latitude: String,
    myUid: String,
    longitude: String,
    viewModel: MapViewModel,
    navHostViewModel: NavHostViewModel
) {

    val location = LatLng(latitude.toDouble(), longitude.toDouble())
    var isLoading by remember {
        mutableStateOf(false)
    }
    var address by remember {
        mutableStateOf("")
    }

    LaunchedEffect(key1 = true) {
        viewModel.getAddressFromLocation(location)
    }
    LaunchedEffect(key1 = viewModel.addressState.value) {
        when (val result = viewModel.addressState.value) {
            is Resource.Error -> {
                isLoading = false
                address = ""
            }

            is Resource.Idle -> {}
            is Resource.Loading -> {
                isLoading = true
            }

            is Resource.Success -> {
                isLoading = false
                address = result.data!!.getAddressLine(0)
            }
        }
    }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "back"
                    )
                }
            }, title = {
                Text(
                    text = stringResource(R.string.create_event),
                    style = MaterialTheme.typography.titleSmall
                )
            })
        }) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(top = innerPadding.calculateTopPadding())
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            if (isLoading) {
                Box(modifier = Modifier.align(Alignment.Center)) {
                    CircularWavyProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            } else {
                Content(
                    address = address,
                    location = location,
                    navController = navController,
                    viewModel = viewModel,
                    myUid = myUid,
                    navHostViewModel = navHostViewModel,
                    showSnackbar = { scope.launch { snackbarHostState.showSnackbar(it) } }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Content(
    address: String,
    location: LatLng,
    myUid: String,
    navController: NavHostController,
    showSnackbar: (String) -> Unit,
    navHostViewModel: NavHostViewModel,
    viewModel: MapViewModel
) {
    val context = LocalContext.current
    var openCommunitySelectionDialog by remember { mutableStateOf(false) }
    val linkedCommunities = remember { mutableStateListOf<JoinedCommunity>() }
    var addressDescription by remember {
        mutableStateOf(address)
    }
    var title by remember {
        mutableStateOf("")
    }
    var date by remember {
        mutableStateOf(Timestamp.now())
    }
    var type by remember {
        mutableStateOf<EventType?>(null)
    }
    var participantLimit by remember { mutableStateOf<Int?>(null) }
    var eventDescription by remember {
        mutableStateOf("")
    }
    val userResourceFlow by navHostViewModel.userResourceFlow.collectAsState()
    val communities by viewModel.joinedCommunities.collectAsState()
    var onlyByRequest by remember { mutableStateOf(true) }
    var privateEvent by remember { mutableStateOf(false) }
    var hideDate by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(linkedCommunities) {
        if (linkedCommunities.isEmpty()) {
            privateEvent = false
        }
    }

    LaunchedEffect(viewModel.createEventState.value) {
        when (val resource = viewModel.createEventState.value) {
            is Resource.Error -> {
                isLoading = false
                showSnackbar(resource.messageResource?.let { context.getString(it) } ?: "")
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

    Box {
        if (isLoading) {
            Dialog(
                onDismissRequest = {},
                properties = DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                )
            ) {
                CircularWavyProgressIndicator()
            }
        }
        if (openCommunitySelectionDialog) {
            SelectCommunityDialog(
                communitiesResource = communities,
                onDismissRequest = { openCommunitySelectionDialog = false },
                selectedCommunities = linkedCommunities,
                onSelect = {
                    linkedCommunities.add(it)
                },
                onRemove = { id ->
                    linkedCommunities.remove(linkedCommunities.find {
                        it.id == id
                    })
                }
            )
        }
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            BasicInfos(
                title = title,
                onTitleChange = {
                    if (it.length <= 30) {
                        title = it
                    }
                },
                onDateChange = {
                    date = it
                },
                date = date
            )
            Spacer(modifier = Modifier.height(10.dp))
            LocationInfos(address = addressDescription, onAddressChange = {
                if (it.length <= 100) {
                    addressDescription = it
                }
            })
            Spacer(modifier = Modifier.height(10.dp))
            val options = Constants.EVENT_TYPES
            EventTypeDatas(options = options,
                eventDescription = eventDescription,
                onEventDescriptionChange = {
                    if (it.length <= 150) {
                        eventDescription = it
                    }
                },
                onTypeChange = {
                    type = it
                })
            Spacer(modifier = Modifier.height(10.dp))
            ParticipationInfos(
                onHideDateChange = { hideDate = it },
                onParticipationChange = { onlyByRequest = it },
                onParticipantLimitChange = {
                    participantLimit = it
                },
                onlyByRequest = onlyByRequest,
                participationLimit = participantLimit,
                hideData = hideDate
            )
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .padding(6.dp)
                        .fillMaxWidth(0.96f), propagateMinConstraints = true
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.invite_your_communities),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(4.dp)
                        )
                        Text(
                            text = stringResource(R.string.invite_your_communities_description),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(4.dp)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = {
                                viewModel.getJoinedCommunities(
                                    myUid = myUid
                                )
                                openCommunitySelectionDialog = true
                            }) {
                                Icon(
                                    imageVector = Icons.Outlined.Add,
                                    contentDescription = "add",
                                    modifier = Modifier.padding(end = 5.dp)
                                )
                                Text(text = stringResource(R.string.add_community))
                            }
                        }
                        linkedCommunities.forEach { community ->
                            Spacer(modifier = Modifier.height(5.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = community.name!!,
                                    modifier = Modifier.padding(start = 18.dp)
                                )
                                TextButton(
                                    onClick = { linkedCommunities.remove(linkedCommunities.find { it.id == community.id }) }
                                ) {
                                    Text(
                                        text = stringResource(R.string.remove),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                        AnimatedVisibility(linkedCommunities.isNotEmpty()) {
                            ListItem(
                                colors = ListItemDefaults.colors(
                                    containerColor = Color.Transparent
                                ),
                                headlineContent = {
                                    Text(stringResource(R.string.private_event_switch_text))
                                },
                                supportingContent = {
                                    Text(stringResource(R.string.private_event_switch_text_description))
                                },
                                trailingContent = {
                                    Switch(
                                        checked = privateEvent,
                                        onCheckedChange = { privateEvent = it },
                                        thumbContent = {
                                            if (privateEvent) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Lock,
                                                    contentDescription = "private event",
                                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Rounded.Public,
                                                    contentDescription = "public event",
                                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                                )
                                            }
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    if (date < Timestamp.now()) {
                        showSnackbar(context.getString(R.string.date_cannot_be_in_the_past))
                    } else if (title.isBlank()) {
                        showSnackbar(context.getString(R.string.title_cannot_be_empty))
                    } else if (type == null) {
                        showSnackbar(context.getString(R.string.type_cannot_be_empty))
                    } else if (participantLimit?.let { it <= 1 } == true) {
                        showSnackbar(context.getString(R.string.participant_limit_cannot_be_one))
                    } else {
                        if (userResourceFlow is Resource.Success && (userResourceFlow.data?.tickets?:0) >= 2) {
                            val eventModel = Event(
                                ownerUid = myUid,
                                title = title.trim(),
                                type = type!!.value,
                                description = eventDescription.trim(),
                                addressDescription = addressDescription.trim(),
                                date = date,
                                onlyByRequest = onlyByRequest,
                                privateEvent = privateEvent,
                                privateInfo = hideDate,
                                linkedCommunities = linkedCommunities.map { it.id!! },
                                latitude = location.latitude,
                                longitude = location.longitude,
                                participantLimit = participantLimit
                            )
                            viewModel.createEvent(
                                eventModel,
                                myUid = myUid,
                                newTickets = userResourceFlow.data!!.tickets - 2
                            )
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(text = context.getString(R.string.create_event))
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SelectCommunityDialog(
    communitiesResource: Resource<List<JoinedCommunity>>,
    selectedCommunities: List<JoinedCommunity>,
    onSelect: (JoinedCommunity) -> Unit,
    onRemove: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val communityList = remember { mutableStateListOf<JoinedCommunity>() }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(communitiesResource) {
        when (communitiesResource) {
            is Resource.Error -> {
                isLoading = false
                Toast.makeText(
                    context,
                    communitiesResource.messageResource?.let { context.getString(it) } ?: "",
                    Toast.LENGTH_LONG
                ).show()
            }

            is Resource.Idle -> {
                isLoading = false
            }

            is Resource.Loading -> {
                isLoading = true
            }

            is Resource.Success -> {
                isLoading = false
                communityList.clear()
                communityList.addAll(
                    communitiesResource.data!!
                )
            }
        }
    }
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            modifier = Modifier
                .width(300.dp)
                .height(500.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    CircularWavyProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    if (communityList.isNotEmpty()) {
                        LazyColumn(modifier = Modifier.height(450.dp)) {
                            items(communityList) { community ->
                                val isSelected = selectedCommunities.any { it.id == community.id }
                                CommunityRow(
                                    community = community,
                                    isSelected = isSelected,
                                    onRemove = { onRemove(community.id!!) },
                                    onSelect = {
                                        if (selectedCommunities.size < 5) {
                                            onSelect(community)
                                        } else {
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.linked_community_limit_reached),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                )
                                if (community != communityList.last()) {
                                    HorizontalDivider()
                                }
                            }
                        }
                    } else {
                        Text(
                            textAlign = TextAlign.Center,
                            text = stringResource(R.string.no_membered_communities),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    OutlinedButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.align(
                            Alignment.BottomCenter
                        )
                    ) {
                        Text(text = stringResource(R.string.okay))
                    }
                }
            }


        }
    }
}

@Composable
private fun CommunityRow(
    community: JoinedCommunity,
    isSelected: Boolean,
    onRemove: () -> Unit,
    onSelect: () -> Unit
) {
    val context = LocalContext.current
    val restricted = (community.eventCreationRestriction!!) && community.rolePriority == 1
    ListItem(
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        ),
        modifier = Modifier.padding(2.dp),
        headlineContent = { Text(text = community.name!!) },
        overlineContent = { Text(text = stringResource(getRoleNameFromRolePriority(community.rolePriority))) },
        trailingContent = {
            TextButton(onClick = {
                if (isSelected) {
                    onRemove()
                } else {
                    if (!restricted) {
                        onSelect()
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.event_creation_restricted_community_warning_message),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }) {
                Text(
                    text = if (restricted) stringResource(R.string.restricted) else {
                        if (isSelected) stringResource(R.string.added) else stringResource(R.string.add)
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = if (restricted) LightRed else {
                        if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Unspecified
                    }
                )
            }
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
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
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.LightGray)
                    )
                }
            }
        }
    )
}


@Composable
fun EventTypeDatas(
    type: EventType? = null,
    options: List<EventType> = emptyList(),
    eventDescription: String,
    onEventDescriptionChange: (String) -> Unit,
    onTypeChange: (EventType) -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }
    var selectedValue by remember { mutableStateOf(type) }
    Surface(color = MaterialTheme.colorScheme.surfaceContainer, shape = RoundedCornerShape(10.dp)) {
        Box(
            modifier = Modifier
                .padding(6.dp)
                .fillMaxWidth(0.96f), propagateMinConstraints = true
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Column {
                    TextField(
                        label = { Text(text = "${stringResource(R.string.event_type)}:") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.KeyboardArrowDown,
                                contentDescription = "extend"
                            )
                        },
                        leadingIcon = selectedValue?.let { eventType ->
                            {
                                Icon(
                                    imageVector = eventType.icon,
                                    contentDescription = "type"
                                )
                            }
                        } ?: {
                            Icon(
                                imageVector = Icons.Outlined.Celebration,
                                contentDescription = "type"
                            )
                        },
                        enabled = false,
                        modifier = Modifier
                            .clickable {
                                expanded = !expanded
                            }
                            .fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            disabledContainerColor = Color.Transparent,
                            disabledTextColor = TextFieldDefaults.colors().unfocusedTextColor,
                            disabledLabelColor = TextFieldDefaults.colors().unfocusedLabelColor,
                            disabledIndicatorColor = Color.Transparent,
                            disabledLeadingIconColor = TextFieldDefaults.colors().unfocusedLeadingIconColor,
                            disabledTrailingIconColor = TextFieldDefaults.colors().unfocusedTrailingIconColor
                        ),
                        value = selectedValue?.labelResource?.let { stringResource(it) } ?: "",
                        onValueChange = {}
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    DropdownMenu(modifier = Modifier.fillMaxWidth(0.94f),
                        shape = RoundedCornerShape(10.dp),
                        expanded = expanded,
                        onDismissRequest = { expanded = false }) {
                        options.forEach {
                            DropdownMenuItem(leadingIcon = {
                                Icon(
                                    imageVector = it.icon,
                                    contentDescription = "type"
                                )
                            },
                                text = { Text(text = stringResource(it.labelResource)) },
                                onClick = {
                                    selectedValue = it
                                    onTypeChange(it)
                                    expanded = false
                                })

                        }
                    }
                }
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = eventDescription,
                    onValueChange = onEventDescriptionChange,
                    minLines = 2,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ShortText,
                            contentDescription = "description"
                        )
                    },
                    supportingText = { Text(text = "${eventDescription.length}/150") },
                    label = { Text(text = "${stringResource(R.string.event_description)}:") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }

        }
    }
}

@Composable
fun ParticipationInfos(
    onlyByRequest: Boolean,
    participationLimit: Int?,
    hideData: Boolean,
    onParticipationChange: (Boolean) -> Unit,
    onHideDateChange: (Boolean) -> Unit,
    onParticipantLimitChange: (Int?) -> Unit
) {
    val participationOptions =
        listOf(
            ParticipationOptionsForEvent.OnlyByRequest,
            ParticipationOptionsForEvent.OpenToAll
        )
    val hidingDataOptions =
        listOf(
            HidingDataOptions.HideData,
            HidingDataOptions.EveryoneCanSee
        )

    var expanded by remember { mutableStateOf(false) }
    var hideDataEnabled by remember { mutableStateOf(true) }
    var onlyByRequestSelectedValue by remember { mutableStateOf(if (onlyByRequest) ParticipationOptionsForEvent.OnlyByRequest else ParticipationOptionsForEvent.OpenToAll) }
    var hideDataExpanded by remember { mutableStateOf(false) }
    val hideDataSelectedValue = if (hideData) HidingDataOptions.HideData else HidingDataOptions.EveryoneCanSee
    LaunchedEffect(onlyByRequestSelectedValue) {
        if (onlyByRequestSelectedValue == ParticipationOptionsForEvent.OpenToAll) {
            hideDataEnabled = false
            hideDataExpanded = false
            onHideDateChange(false)
        } else {
            hideDataEnabled = true
            hideDataExpanded = false
        }
    }



    Surface(color = MaterialTheme.colorScheme.surfaceContainer, shape = RoundedCornerShape(10.dp)) {
        Box(
            modifier = Modifier
                .padding(6.dp)
                .fillMaxWidth(0.96f), propagateMinConstraints = true
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                var limitIsEnabled by remember { mutableStateOf(participationLimit != null) }
                var limit by remember { mutableStateOf(participationLimit?.toString() ?: "2") }
                ListItem(
                    trailingContent = {
                        OutlinedTextField(
                            singleLine = true,
                            textStyle = MaterialTheme.typography.titleMedium,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            enabled = limitIsEnabled,
                            shape = RoundedCornerShape(10.dp),
                            value = limit,
                            onValueChange = { value ->
                                if (value.isNotBlank()) {
                                    if (value.all { char -> char.isDigit() } && value.length < 7) {
                                        limit = value
                                        onParticipantLimitChange(limit.toInt())
                                    }
                                } else {
                                    limit = ""
                                    onParticipantLimitChange(null)
                                }
                            },
                            modifier = Modifier.width(90.dp)
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    leadingContent = {
                        Switch(
                            thumbContent = {
                                if (limitIsEnabled) {
                                    Icon(
                                        imageVector = Icons.Rounded.Group,
                                        contentDescription = "person",
                                        modifier = Modifier.size(SwitchDefaults.IconSize)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.AllInclusive,
                                        contentDescription = "limitless",
                                        modifier = Modifier.size(SwitchDefaults.IconSize)
                                    )
                                }
                            },
                            checked = limitIsEnabled,
                            onCheckedChange = {
                                limitIsEnabled = !limitIsEnabled
                                if (!limitIsEnabled) {
                                    onParticipantLimitChange(null)
                                } else {
                                    if (limit.isNotBlank()) {
                                        onParticipantLimitChange(limit.toInt())
                                    } else {
                                        onParticipantLimitChange(0)
                                    }
                                }
                            }
                        )
                    },
                    headlineContent = { Text(text = stringResource(R.string.limit_number_of_participants)) }
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.participation_settings),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(4.dp)
                )
                Text(
                    text = stringResource(R.string.participation_settings_description),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(4.dp)
                )
                Spacer(
                    modifier = Modifier.height(5.dp)
                )
                Column {
                    DropdownMenuItem(
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.KeyboardArrowDown,
                                contentDescription = "down"
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = onlyByRequestSelectedValue.imageVector,
                                contentDescription = "onlyByRequest"
                            )
                        },
                        text = {
                            Text(
                                text = stringResource(onlyByRequestSelectedValue.titleResID)
                            )
                        },
                        onClick = { expanded = !expanded })
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        participationOptions.forEach { participationOption ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(participationOption.titleResID)
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = participationOption.imageVector,
                                        contentDescription = ""
                                    )
                                },
                                onClick = {
                                    onlyByRequestSelectedValue = participationOption
                                    onParticipationChange(participationOption == ParticipationOptionsForEvent.OnlyByRequest)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.date_and_address_visibility),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(4.dp)
                )
                Text(
                    text = stringResource(R.string.date_and_address_visibility_description),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(4.dp)
                )
                Spacer(modifier = Modifier.height(5.dp))
                Column {
                    DropdownMenuItem(
                        enabled = hideDataEnabled,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.KeyboardArrowDown,
                                contentDescription = "down"
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = hideDataSelectedValue.imageVector,
                                contentDescription = ""
                            )
                        },
                        text = { Text(text = stringResource(hideDataSelectedValue.titleResID)) },
                        onClick = { hideDataExpanded = !hideDataExpanded }
                    )
                    DropdownMenu(
                        expanded = hideDataExpanded,
                        onDismissRequest = { hideDataExpanded = false }) {
                        hidingDataOptions.forEach { hidingDataOption ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(hidingDataOption.titleResID)
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = hidingDataOption.imageVector,
                                        contentDescription = ""
                                    )
                                },
                                onClick = {
                                    onHideDateChange(hidingDataOption == HidingDataOptions.HideData)
                                    hideDataExpanded = false
                                })
                        }
                    }
                }


            }
        }
    }
}

@Composable
fun LocationInfos(
    address: String, onAddressChange: (String) -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainer, shape = RoundedCornerShape(10.dp)) {
        Box(
            modifier = Modifier
                .padding(6.dp)
                .fillMaxWidth(0.96f), propagateMinConstraints = true
        ) {
            Column {
                TextField(
                    supportingText = { Text(text = "${address.length}/100") },
                    modifier = Modifier
                        .heightIn(min = 110.dp)
                        .fillMaxWidth(),
                    label = { Text(text = "${stringResource(R.string.address_description)}:") },
                    value = address,
                    onValueChange = onAddressChange,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent

                    )
                )
            }

        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicInfos(
    title: String,
    date: Timestamp,
    onTitleChange: (String) -> Unit,
    onDateChange: (Timestamp) -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        yearRange = LocalDate.now().year..LocalDate.now().year + 3,
        initialSelectedDateMillis = System.currentTimeMillis()
    )
    val timePickerState = rememberTimePickerState()
    Surface(color = MaterialTheme.colorScheme.surfaceContainer, shape = RoundedCornerShape(10.dp)) {
        Box(
            modifier = Modifier
                .padding(6.dp)
                .fillMaxWidth(0.96f), propagateMinConstraints = true
        ) {
            if (showTimePicker) {
                DatePickerDialog(
                    onDismissRequest = {
                        showTimePicker = false
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                onDateChange(
                                    TimeFunctions().mergeDateAndTime(
                                        date,
                                        timePickerState.hour,
                                        timePickerState.minute
                                    )
                                )
                                showTimePicker = false
                            }
                        ) { Text(stringResource(R.string.select)) }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showTimePicker = false
                            },
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        TimePicker(
                            state = timePickerState,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                }
            }
            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = {
                        showDatePicker = false
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                onDateChange(Timestamp(Date(datePickerState.selectedDateMillis!!)))
                                showDatePicker = false
                            }
                        ) { Text(stringResource(R.string.select)) }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showDatePicker = false
                            },
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                ) {
                    DatePicker(
                        state = datePickerState,
                        showModeToggle = false
                    )
                }
            }
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        supportingText = { Text(text = "${title.length}/30") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Label,
                                contentDescription = "title"
                            )
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text(text = "${stringResource(R.string.title)}:") },
                        singleLine = false,
                        maxLines = 3,
                        value = title,
                        onValueChange = onTitleChange,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        TextField(
                            modifier = Modifier.clickable { showDatePicker = !showDatePicker },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.DateRange,
                                    contentDescription = "date"
                                )
                            },
                            label = { Text(text = "${stringResource(R.string.date)}:") },
                            singleLine = true,
                            value = TimeFunctions().convertTimestampToDate(date),
                            onValueChange = {},
                            enabled = false,
                            colors = TextFieldDefaults.colors(
                                disabledContainerColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                disabledTextColor = TextFieldDefaults.colors().unfocusedTextColor,
                                disabledLabelColor = TextFieldDefaults.colors().unfocusedLabelColor,
                                disabledLeadingIconColor = TextFieldDefaults.colors().unfocusedLeadingIconColor
                            )
                        )
                        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                        TextField(
                            modifier = Modifier.clickable { showTimePicker = !showTimePicker },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.AccessTime,
                                    contentDescription = "time"
                                )
                            },
                            label = { Text(text = "${stringResource(R.string.time)}:") },
                            singleLine = true,
                            value = sdf.format(date.toDate()),
                            onValueChange = {},
                            enabled = false,
                            colors = TextFieldDefaults.colors(
                                disabledContainerColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                disabledTextColor = TextFieldDefaults.colors().unfocusedTextColor,
                                disabledLabelColor = TextFieldDefaults.colors().unfocusedLabelColor,
                                disabledLeadingIconColor = TextFieldDefaults.colors().unfocusedLeadingIconColor
                            )
                        )
                    }
                }
            }
        }

    }
}

@Preview
@Composable
private fun CreateEventPreview() {
    Column {
        ParticipationInfos(
            onlyByRequest = false,
            participationLimit = null,
            hideData = true,
            onParticipationChange = {},
            onHideDateChange = {}
        ) { }
    }
}
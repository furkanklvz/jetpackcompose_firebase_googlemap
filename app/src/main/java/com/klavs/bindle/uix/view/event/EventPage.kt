package com.klavs.bindle.uix.view.event

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShortText
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.LocalActivity
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PersonRemoveAlt1
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import com.klavs.bindle.R
import com.klavs.bindle.data.entity.Event
import com.klavs.bindle.data.entity.sealedclasses.BottomNavItem
import com.klavs.bindle.util.TimeFunctions
import com.klavs.bindle.data.entity.sealedclasses.EventType
import com.klavs.bindle.data.entity.RequestForEvent
import com.klavs.bindle.data.entity.User
import com.klavs.bindle.data.entity.community.Community
import com.klavs.bindle.resource.Resource
import com.klavs.bindle.ui.theme.Green2
import com.klavs.bindle.uix.view.GlideImageLoader
import com.klavs.bindle.uix.viewmodel.NavHostViewModel
import com.klavs.bindle.uix.viewmodel.event.EventsViewModel
import com.klavs.bindle.util.Constants
import com.klavs.bindle.util.TicketDialog
import com.klavs.bindle.util.UnverifiedAccountAlertDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EventPage(
    navController: NavHostController,
    eventId: String,
    currentUser: FirebaseUser,
    navHostViewModel: NavHostViewModel
) {
    val viewModel: EventsViewModel = hiltViewModel()
    val context = LocalContext.current
    val owner by viewModel.eventOwner.collectAsState()
    val scope = rememberCoroutineScope()
    val eventResource by viewModel.event.collectAsState()
    val countTheParticipants by viewModel.countTheParticipants.collectAsState()
    val numberOfRequests by viewModel.numberOfRequests.collectAsState()
    val userResourceFlow by navHostViewModel.userResourceFlow.collectAsState()
    val requestsMBSState = rememberModalBottomSheetState()
    var showRequests by remember { mutableStateOf(false) }
    var showCancelAlertDialog by remember { mutableStateOf(false) }
    var showTicketDialog by remember { mutableStateOf(false) }
    val participantsMBSState = rememberModalBottomSheetState()
    var showParticipants by remember { mutableStateOf(false) }
    var showUnverifiedAccountAlertDialog by remember { mutableStateOf(false) }
    val requestsResource by viewModel.requests.collectAsState()
    val lastRequestDoc by viewModel.lastRequestDoc.collectAsState()
    val acceptResource by viewModel.acceptResource.collectAsState()
    val rejectResource by viewModel.rejectResource.collectAsState()
    val participantsResource by viewModel.participants.collectAsState()
    val lastParticipantDoc by viewModel.lastParticipantDoc.collectAsState()
    val removeParticipantsResource by viewModel.removeParticipantResource.collectAsState()
    val communities by viewModel.linkedCommunities.collectAsState()


    LaunchedEffect(true) {
        if (eventResource is Resource.Idle || eventResource is Resource.Error) {
            viewModel.listenToEvent(eventId)
        }
        if (countTheParticipants == null) {
            viewModel.getCountTheParticipants(eventId)
        }
        if (numberOfRequests == null) {
            viewModel.getNumberOfRequests(eventId)
        }
    }
    LaunchedEffect(eventResource) {
        Log.d("eventPage", "event: ${eventResource.data}")
        if (eventResource is Resource.Success && owner !is Resource.Success) {
            viewModel.getEventOwner(eventResource.data?.ownerUid ?: "")
        }
    }
    EventPageContent(
        eventResource = eventResource,
        navigateToEventPage = { navController.navigate("event_chat/${eventId}/${countTheParticipants ?: -1}") },
        currentUser = currentUser,
        numberOfRequests = numberOfRequests,
        showRequests = {
            viewModel.getRequests(
                eventId = eventId,
                pageSize = 10
            )
            showRequests = true
        },
        navigateToEditEvent = {

            val eventJson = Gson().toJson(eventResource.data)
            val encodedJsonEvent = Uri.encode(eventJson)
            navController.navigate("edit_event/$encodedJsonEvent")

        },
        showCancelAlertDialog = { showCancelAlertDialog = it },
        onBackClick = {
            navController.popBackStack()
        },
        cancelTheEvent = {
            viewModel.cancelTheEvent(
                eventId,
                uid = currentUser.uid
            )
        },
        cancelAlertDialogIsShown = showCancelAlertDialog,
        showUnverifiedAccountAlertDialog = { showUnverifiedAccountAlertDialog = it },
        unverifiedAccountDialogIsShown = showUnverifiedAccountAlertDialog,
        navigateToProfile = { navController.navigate("menu_profile") },
        participantsIsShown = showParticipants,
        showParticipants = {
            if (it) {
                showParticipants = true
            } else {

                scope.launch { participantsMBSState.hide() }.invokeOnCompletion {
                    if (!participantsMBSState.isVisible) {
                        showParticipants = false
                    }
                }
                viewModel.lastParticipantDoc.value = null
            }
        },
        ticketDialogIsShown = showTicketDialog,
        showTicketDialog = { showTicketDialog = it },
        userResourceFlow = userResourceFlow,
        participantsResource = participantsResource,
        removeParticipantsResource = removeParticipantsResource,
        participantsMBSState = participantsMBSState,
        removeParticipant = { uid ->
            viewModel.removeParticipant(
                eventId = eventId,
                participantUid = uid
            )
        },
        loadMoreParticipants = {
            if (owner.data != null) {
                viewModel.getParticipants(
                    eventId = eventId,
                    pageSize = 10,
                    eventOwnerId = owner.data!!.uid!!
                )
            }
        },
        requestsIsShown = showRequests,
        requestsMBSState = requestsMBSState,
        dismissRequestsMBS = {
            scope.launch { requestsMBSState.hide() }.invokeOnCompletion {
                if (!requestsMBSState.isVisible) {
                    showRequests = false
                }
            }
            viewModel.lastRequestDoc.value = null
            viewModel.acceptResource.value = Resource.Idle()
            viewModel.rejectResource.value = Resource.Idle()
            viewModel.getNumberOfRequests(
                eventId = eventId
            )
            viewModel.getCountTheParticipants(
                eventId = eventId
            )
        },
        owner = owner,
        acceptReqeust = {
            if (eventResource.data != null) {
                viewModel.acceptRequest(
                    eventId = eventId,
                    uid = it,
                    ownerUid = eventResource.data!!.ownerUid
                )
            }
        },
        rejectRequest = {
            viewModel.rejectRequest(
                eventId = eventId,
                uid = it
            )
        },
        acceptResource = acceptResource,
        rejectResource = rejectResource,
        loadMoreRequests = {
            viewModel.getRequests(
                eventId = eventId,
                pageSize = 10
            )
        },
        requestsResource = requestsResource,
        countTheParticipants = countTheParticipants,
        communities = communities,
        getLinkedCommunities = {
            viewModel.getLinkedCommunities(
                eventResource.data?.linkedCommunities ?: emptyList()
            )
        },
        navigateToCommunityPage = { navController.navigate("community_page/$it") },
        thereIsLastParticipantDoc = lastParticipantDoc != null,
        thereIsLastRequestDoc = lastRequestDoc != null
    )

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EventPageContent(
    eventResource: Resource<Event>,
    navigateToEventPage: () -> Unit,
    navigateToProfile: () -> Unit,
    currentUser: FirebaseUser?,
    numberOfRequests: Int?,
    showRequests: () -> Unit,
    participantsIsShown: Boolean,
    showParticipants: (Boolean) -> Unit,
    cancelTheEvent: () -> Unit,
    ticketDialogIsShown: Boolean,
    showTicketDialog: (Boolean) -> Unit,
    userResourceFlow: Resource<User>,
    navigateToEditEvent: () -> Unit,
    onBackClick: () -> Unit,
    showCancelAlertDialog: (Boolean) -> Unit,
    cancelAlertDialogIsShown: Boolean,
    showUnverifiedAccountAlertDialog: (Boolean) -> Unit,
    unverifiedAccountDialogIsShown: Boolean,
    participantsResource: Resource<List<User>>,
    removeParticipantsResource: Resource<String>,
    participantsMBSState: SheetState,
    removeParticipant: (String) -> Unit,
    loadMoreParticipants: () -> Unit,
    requestsIsShown: Boolean,
    requestsMBSState: SheetState,
    dismissRequestsMBS: () -> Unit,
    owner: Resource<User>,
    acceptReqeust: (String) -> Unit,
    rejectRequest: (String) -> Unit,
    acceptResource: Resource<String>,
    rejectResource: Resource<String>,
    loadMoreRequests: () -> Unit,
    requestsResource: Resource<List<RequestForEvent>>,
    countTheParticipants: Int?,
    communities: Resource<List<Community>>,
    getLinkedCommunities: () -> Unit,
    navigateToCommunityPage: (String) -> Unit,
    thereIsLastParticipantDoc: Boolean,
    thereIsLastRequestDoc: Boolean
) {
    val context = LocalContext.current
    Scaffold(
        floatingActionButton = {
            if (eventResource is Resource.Success) {
                ExtendedFloatingActionButton(
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Chat,
                            contentDescription = "chat"
                        )
                    },
                    text = {
                        Text(text = stringResource(R.string.chat))
                    },
                    onClick = navigateToEventPage
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        topBar = {
            CenterAlignedTopAppBar(
                actions = {
                    if (eventResource.data != null) {
                        if (currentUser != null) {
                            if (currentUser.uid == eventResource.data.ownerUid && eventResource.data.date > Timestamp.now()) {
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
                                            showRequests()
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.PersonAdd,
                                            contentDescription = "joining requests"
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = navigateToEditEvent
                                ) {
                                    Icon(imageVector = Icons.Rounded.Edit, "edit")
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
                                                    imageVector = Icons.Rounded.Close,
                                                    contentDescription = "cancel the event",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            },
                                            text = {
                                                Text(
                                                    text = stringResource(R.string.cancel_the_event),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            },
                                            onClick = {
                                                showCancelAlertDialog(true)
                                                expanded = false
                                            }
                                        )

                                    }
                                }
                            }
                        }

                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "back"
                        )
                    }
                },
                title = {
                    Text(
                        stringResource(R.string.event_page),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            )
        }
    ) { innerPadding ->
        Box(
            Modifier
                .padding(top = innerPadding.calculateTopPadding())
                .fillMaxSize()
        ) {
            if (cancelAlertDialogIsShown) {
                CancelAlertDialog(
                    onDismiss = { showCancelAlertDialog(false) },
                    onConfirm = {
                        cancelTheEvent()
                        showCancelAlertDialog(false)
                    }
                )
            }
            if (unverifiedAccountDialogIsShown) {
                UnverifiedAccountAlertDialog(
                    onDismiss = { showUnverifiedAccountAlertDialog(false) }
                ) {
                    navigateToProfile()
                }
            }
            if (ticketDialogIsShown) {
                if (userResourceFlow is Resource.Success) {
                    if (currentUser != null) {
                        TicketDialog(
                            onDismiss = { showTicketDialog(false) },
                            uid = currentUser.uid,
                            tickets = userResourceFlow.data!!.tickets,
                            paddingValues = innerPadding
                        )
                    }
                }
            }
            if (participantsIsShown) {
                if (currentUser != null) {
                    ParticipantsMBS(
                        participantsResource = participantsResource,
                        removeParticipantsResource = removeParticipantsResource,
                        state = participantsMBSState,
                        onRemoveParticipant = { uid ->
                            removeParticipant(uid)
                        },
                        onDismiss = {
                            showParticipants(false)
                        },
                        onLoadMore = {
                            loadMoreParticipants()
                        },
                        eventOwner = owner.data,
                        myUid = currentUser.uid,
                        thereIsLastParticipantDoc = thereIsLastParticipantDoc
                    )
                }
            }
            if (requestsIsShown) {
                RequestsMBS(
                    state = requestsMBSState,
                    onDismiss = {
                        dismissRequestsMBS()
                    },
                    onAccepted = {
                        acceptReqeust(it)
                    },
                    onRejected = {
                        rejectRequest(it)
                    },
                    acceptResource = acceptResource,
                    rejectResource = rejectResource,
                    onLoadMore = {
                        loadMoreRequests()
                    },
                    requestsResource = requestsResource,
                    thereIsLastRequestDoc = thereIsLastRequestDoc
                )
            }

            when (eventResource) {
                is Resource.Error -> {
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WarningAmber,
                            contentDescription = "error"
                        )
                        Text(eventResource.messageResource?.let { stringResource(it) } ?: "")
                    }
                }

                is Resource.Idle -> {}
                is Resource.Loading -> {
                    CircularWavyProgressIndicator(Modifier.align(Alignment.Center))
                }

                is Resource.Success -> {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(5.dp)
                                .fillMaxWidth(0.9f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocalActivity,
                                contentDescription = "title"
                            )
                            Text(
                                eventResource.data?.title ?: "",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(0.9f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            HorizontalDivider(
                                modifier = Modifier
                                    .weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = eventResource.data?.let { getEventLabelFromValue(it.type) }
                                        ?.let { stringResource(it) } ?: "",
                                    modifier = Modifier.wrapContentWidth()
                                )
                                if (eventResource.data != null) {
                                    Icon(
                                        getEventIconFromValue(eventResource.data.type),
                                        contentDescription = eventResource.data.type,
                                        modifier = Modifier
                                            .padding(start = 3.dp)
                                            .size(IconButtonDefaults.xSmallIconSize)
                                    )
                                }
                            }


                        }

                        Spacer(Modifier.height(15.dp))
                        Row(
                            Modifier
                                .fillMaxWidth(0.9f)
                                .align(Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarMonth,
                                    contentDescription = "date"
                                )
                                Text(
                                    eventResource.data?.let {
                                        TimeFunctions().convertTimestampToLocalizeDate(
                                            it.date
                                        )
                                    }
                                        ?: "",
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AccessTime,
                                    contentDescription = "time"
                                )
                                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                                Text(
                                    eventResource.data?.let { sdf.format(it.date.toDate()) } ?: "",
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                        }
                        Spacer(Modifier.height(15.dp))
                        ElevatedCard(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(7.dp)
                                .clickable {
                                    if (owner.data != null) {
                                        showParticipants(true)
                                        loadMoreParticipants()
                                    }
                                },
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Group,
                                    contentDescription = "participants",
                                    modifier = Modifier.size(IconButtonDefaults.xSmallIconSize)
                                )
                                Text(
                                    "${countTheParticipants ?: ""} ${stringResource(R.string.people_participating)}",
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                        }

                        Spacer(Modifier.height(15.dp))
                        TextField(
                            textStyle = if ((eventResource.data?.addressDescription
                                    ?: "") == ""
                            ) TextStyle(
                                fontStyle = FontStyle.Italic
                            ) else LocalTextStyle.current,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .fillMaxWidth(0.9f)
                                .shadow(3.dp, RoundedCornerShape(12.dp)),
                            label = {
                                Text(
                                    "${stringResource(R.string.address)}:",
                                    modifier = Modifier.padding(6.dp)
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                focusedIndicatorColor = Color.Transparent,
                                focusedLabelColor = TextFieldDefaults.colors().unfocusedLabelColor
                            ),
                            value = if ((eventResource.data?.addressDescription
                                    ?: "") == ""
                            ) stringResource(R.string.no_address_provided) else eventResource.data?.addressDescription
                                ?: "",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = {
                                    if (eventResource.data != null) {
                                        val mapUri =
                                            Uri.parse("geo:0,0?q=${eventResource.data.latitude},${eventResource.data.longitude}(${eventResource.data.title})")
                                        val mapIntent = Intent(Intent.ACTION_VIEW, mapUri)
                                        context.startActivity(mapIntent)
                                    }

                                }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Directions,
                                        contentDescription = "direction"
                                    )
                                }

                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.LocationOn,
                                    contentDescription = "location"
                                )
                            }
                        )
                        Spacer(Modifier.height(26.dp))
                        TextField(
                            textStyle = if ((eventResource.data?.description
                                    ?: "") == ""
                            ) TextStyle(
                                fontStyle = FontStyle.Italic
                            ) else LocalTextStyle.current,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .fillMaxWidth(0.9f)
                                .shadow(3.dp, RoundedCornerShape(12.dp)),
                            label = {
                                Text(
                                    "${stringResource(R.string.description)}:",
                                    modifier = Modifier.padding(6.dp)
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                focusedIndicatorColor = Color.Transparent,
                                focusedLabelColor = TextFieldDefaults.colors().unfocusedLabelColor
                            ),
                            value = if ((eventResource.data?.description
                                    ?: "") == ""
                            ) stringResource(R.string.no_description_provided) else eventResource.data?.description
                                ?: "",
                            onValueChange = {},
                            readOnly = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ShortText,
                                    contentDescription = "description"
                                )
                            }
                        )
                        Spacer(Modifier.height(16.dp))

                        ListItem(
                            modifier = Modifier.fillMaxWidth(0.9f),
                            leadingContent = {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                ) {
                                    if (owner.data?.profilePictureUrl != null) {
                                        GlideImageLoader(
                                            owner.data.profilePictureUrl,
                                            context = context,
                                            modifier = Modifier.matchParentSize()
                                        )
                                    } else {
                                        Image(
                                            imageVector = Icons.Rounded.Person,
                                            contentDescription = owner.data?.userName,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .matchParentSize()
                                                .background(Color.LightGray)
                                        )
                                    }
                                }
                            },
                            overlineContent = {
                                Text("${stringResource(R.string.event_owner)}:")
                            },
                            headlineContent = {
                                when (owner) {
                                    is Resource.Error -> {
                                        Text(
                                            owner.messageResource?.let { stringResource(it) } ?: "",
                                            textDecoration = TextDecoration.LineThrough
                                        )
                                    }

                                    is Resource.Idle -> {}
                                    is Resource.Loading -> {
                                        LinearProgressIndicator()
                                    }

                                    is Resource.Success -> {
                                        Text(owner.data?.userName ?: "")
                                    }
                                }

                            }
                        )
                        Spacer(Modifier.height(16.dp))
                        if (eventResource.data?.linkedCommunities?.isNotEmpty() == true) {
                            var showCommunities by remember { mutableStateOf(false) }
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .padding(5.dp)
                                        .align(Alignment.CenterHorizontally)
                                        .clickable {

                                            if (eventResource.data.linkedCommunities.isNotEmpty()) {
                                                showCommunities = !showCommunities
                                                if (communities !is Resource.Success
                                                    && communities !is Resource.Loading
                                                    && showCommunities
                                                ) {
                                                    getLinkedCommunities()
                                                }
                                            }
                                        }
                                ) {
                                    Icon(
                                        Icons.Rounded.Groups, contentDescription = null,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        stringResource(
                                            R.string.event_linked_to_x_communities,
                                            eventResource.data.linkedCommunities.size.toString()
                                        ),
                                        modifier = Modifier.weight(4f),
                                        textAlign = TextAlign.Center
                                    )
                                    Icon(
                                        if (showCommunities) {
                                            Icons.Rounded.KeyboardArrowUp
                                        } else Icons.Rounded.KeyboardArrowDown,
                                        contentDescription = null,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                AnimatedVisibility(
                                    visible = showCommunities,
                                    modifier = Modifier.padding(9.dp)
                                ) {
                                    when (communities) {
                                        is Resource.Error -> {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(
                                                    5.dp
                                                )
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.WarningAmber,
                                                    contentDescription = "error"
                                                )
                                                Text(communities.messageResource?.let {
                                                    stringResource(
                                                        it
                                                    )
                                                } ?: "")
                                            }
                                        }

                                        is Resource.Idle -> {}
                                        is Resource.Loading -> {
                                            LinearProgressIndicator()
                                        }

                                        is Resource.Success -> {
                                            Column(
                                                modifier = Modifier
                                                    .align(Alignment.CenterHorizontally),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                communities.data?.forEach { community ->
                                                    Row(
                                                        Modifier.fillMaxWidth(),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(
                                                                8.dp
                                                            )
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(40.dp)
                                                                    .clip(CircleShape)
                                                            ) {
                                                                if (community.communityPictureUrl != null) {
                                                                    GlideImageLoader(
                                                                        community.communityPictureUrl,
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
                                                                            .background(
                                                                                Color.LightGray
                                                                            )
                                                                    )
                                                                }
                                                            }
                                                            Text(
                                                                community.name,
                                                                style = MaterialTheme.typography.titleSmall
                                                            )
                                                        }
                                                        IconButton(
                                                            onClick = {
                                                                navigateToCommunityPage(community.id!!)
                                                            }
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                                                                contentDescription = "expand"
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(100.dp))
                    }
                }
            }

        }
    }
}

@Composable
fun CancelAlertDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        icon = {
            Icon(imageVector = Icons.Rounded.WarningAmber, contentDescription = "warning")
        },
        text = {
            Text(stringResource(R.string.cancel_the_event_alert_dialog_text))
        },
        confirmButton = {
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                onClick = onConfirm
            ) {
                Text(stringResource(R.string.cancel_the_event))
            }
        },
        dismissButton = {
            IconButton(
                modifier = Modifier.padding(end = 15.dp),
                onClick = onDismiss
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "cancel"
                )
            }
        },
        onDismissRequest = onDismiss
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ParticipantsMBS(
    participantsResource: Resource<List<User>>,
    state: SheetState,
    eventOwner: User?,
    myUid: String,
    onRemoveParticipant: (String) -> Unit,
    onDismiss: () -> Unit,
    onLoadMore: () -> Unit,
    thereIsLastParticipantDoc: Boolean,
    removeParticipantsResource: Resource<String>
) {
    val context = LocalContext.current
    val participantList = remember { mutableStateListOf<User>() }
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }
    LaunchedEffect(participantsResource) {
        when (participantsResource) {
            is Resource.Error -> {
                isError = true
                isLoading = false
            }

            is Resource.Idle -> {}
            is Resource.Loading -> {
                isLoading = true
                isError = false
            }

            is Resource.Success -> {
                participantList.addAll(participantsResource.data!!)
                isLoading = false
                isError = false
            }
        }
    }
    LaunchedEffect(removeParticipantsResource) {
        when (removeParticipantsResource) {
            is Resource.Error -> {
                Toast.makeText(
                    context,
                    removeParticipantsResource.messageResource?.let { context.getString(it) } ?: "",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }

            is Resource.Success -> {
                participantList.remove(participantList.find { it.uid == removeParticipantsResource.data })
            }

            else -> {}
        }
    }
    ModalBottomSheet(
        modifier = Modifier.padding(
            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        ),
        onDismissRequest = {
            onDismiss()
            participantList.clear()
        },
        sheetState = state
    ) {
        Text(
            stringResource(R.string.participants),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(10.dp)
                .align(Alignment.CenterHorizontally)
        )
        if (eventOwner != null) {
            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    ParticipantRow(
                        participant = eventOwner,
                        isOwner = true,
                        amIOwner = myUid == eventOwner.uid
                    ) {}
                }

                items(participantList) { participant ->
                    AnimatedVisibility(
                        visible = participant in participantList,
                        enter = expandVertically(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        ParticipantRow(
                            participant = participant,
                            onRemoveParticipant = { onRemoveParticipant(participant.uid!!) },
                            amIOwner = myUid == eventOwner.uid
                        )
                    }

                }
                if (isError) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.ErrorOutline,
                                contentDescription = "error"
                            )
                            Text(
                                stringResource(R.string.something_went_wrong),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }
                if (isLoading) {
                    item {
                        CircularWavyProgressIndicator(
                            Modifier
                                .padding(15.dp)
                                .size(40.dp)
                        )
                    }

                } else if (thereIsLastParticipantDoc) {
                    item {
                        IconButton(
                            onClick = onLoadMore,
                            modifier = Modifier
                                .padding(15.dp)
                                .size(40.dp)
                                .border(
                                    1.dp, IconButtonDefaults.iconButtonColors().contentColor,
                                    CircleShape
                                )
                        ) {
                            Icon(imageVector = Icons.Rounded.Add, "load more")
                        }
                    }
                }

            }
        }

    }
}

@Composable
private fun ParticipantRow(
    participant: User,
    isOwner: Boolean = false,
    amIOwner: Boolean,
    onRemoveParticipant: () -> Unit
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
            ) {
                if (participant.profilePictureUrl != null) {
                    GlideImageLoader(
                        url = participant.profilePictureUrl,
                        context = context,
                        modifier = Modifier.matchParentSize()
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
            Text(
                text = participant.userName,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 10.dp)
            )


        }
        if (isOwner) {
            Text(
                stringResource(R.string.event_owner), style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(horizontal = 5.dp)
                    .background(
                        Green2,
                        RoundedCornerShape(5.dp)
                    )
                    .padding(3.dp),
                color = Green2.copy(red = 0.1f, blue = 0.1f, green = 0.33f)
            )
        } else if (amIOwner) {
            IconButton(
                onClick = onRemoveParticipant
            ) {
                Icon(
                    imageVector = Icons.Outlined.PersonRemoveAlt1,
                    contentDescription = "remove participant",
                    tint = Color.Red
                )
            }
        }


    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RequestsMBS(
    requestsResource: Resource<List<RequestForEvent>>,
    state: SheetState,
    acceptResource: Resource<String>,
    rejectResource: Resource<String>,
    thereIsLastRequestDoc: Boolean,
    onDismiss: () -> Unit,
    onAccepted: (String) -> Unit,
    onRejected: (String) -> Unit,
    onLoadMore: () -> Unit
) {
    val context = LocalContext.current
    val requestList = remember { mutableStateListOf<RequestForEvent>() }
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }
    LaunchedEffect(requestsResource) {
        when (requestsResource) {
            is Resource.Error -> {
                isError = true
                isLoading = false
            }

            is Resource.Idle -> {}
            is Resource.Loading -> {
                isLoading = true
                isError = false
            }

            is Resource.Success -> {
                requestList.addAll(requestsResource.data!!)
                isLoading = false
                isError = false
            }
        }
    }
    LaunchedEffect(acceptResource) {
        when (acceptResource) {
            is Resource.Error -> {
                Toast.makeText(
                    context,
                    acceptResource.messageResource?.let { context.getString(it) } ?: "",
                    Toast.LENGTH_SHORT
                ).show()
            }

            is Resource.Success -> {
                val index = requestList.indexOf(requestList.find { it.uid == acceptResource.data })
                if (index != -1) {
                    requestList[index] = requestList[index].copy(accepted = true)
                    Log.e("events", "accepted")
                }
            }

            else -> {}
        }
    }
    LaunchedEffect(rejectResource) {
        when (rejectResource) {
            is Resource.Error -> {
                Toast.makeText(
                    context,
                    rejectResource.messageResource?.let { context.getString(it) } ?: "",
                    Toast.LENGTH_SHORT
                ).show()
            }

            is Resource.Success -> {
                requestList.remove(requestList.find { it.uid == rejectResource.data })
                if (requestList.isEmpty()) {
                    onLoadMore()
                }

            }

            else -> {}
        }
    }
    ModalBottomSheet(
        modifier = Modifier.padding(
            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        ),
        onDismissRequest = {
            onDismiss()
            requestList.clear()
        },
        sheetState = state
    ) {
        Text(
            stringResource(R.string.participation_requests),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(10.dp)
                .align(Alignment.CenterHorizontally)
        )
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            if (!isLoading && !isError && requestList.isEmpty()) {
                item {
                    Box(Modifier.fillMaxSize()) {
                        Text(
                            stringResource(R.string.no_request_yet),
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                }
            } else {
                items(requestList) { request ->
                    RequestRow(
                        request = request,
                        onRejectRequest = { onRejected(request.uid) },
                        onAcceptRequest = { onAccepted(request.uid) }
                    )
                }
                if (isError) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.ErrorOutline,
                                contentDescription = "error"
                            )
                            Text(
                                stringResource(R.string.something_went_wrong),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }
                if (isLoading) {
                    item {
                        CircularWavyProgressIndicator(
                            Modifier
                                .padding(15.dp)
                                .size(40.dp)
                        )
                    }

                } else if (thereIsLastRequestDoc) {
                    item {
                        IconButton(
                            onClick = onLoadMore,
                            modifier = Modifier
                                .padding(15.dp)
                                .size(40.dp)
                                .border(
                                    1.dp, IconButtonDefaults.iconButtonColors().contentColor,
                                    CircleShape
                                )
                        ) {
                            Icon(imageVector = Icons.Rounded.Add, "load more")
                        }
                    }
                }
            }
        }
    }

}

fun getEventLabelFromValue(value: String): Int {
    val eventTypeList = Constants.EVENT_TYPES
    return eventTypeList.find { it.value == value }?.labelResource ?: EventType.Party.labelResource
}

fun getEventIconFromValue(value: String): ImageVector {
    val eventTypeList = Constants.EVENT_TYPES
    return eventTypeList.find { it.value == value }?.icon ?: Icons.Rounded.Celebration
}

fun getEventTypeObjectFromValue(value: String): EventType? {
    val eventTypeList = Constants.EVENT_TYPES
    return eventTypeList.find { it.value == value }
}

@Composable
private fun RequestRow(
    request: RequestForEvent,
    onRejectRequest: () -> Unit,
    onAcceptRequest: () -> Unit,
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
            ) {
                if (request.photoUrl != null) {
                    GlideImageLoader(
                        url = request.photoUrl,
                        context = context,
                        modifier = Modifier.matchParentSize()
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
                    text = request.userName ?: "",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
                var text by remember {
                    mutableStateOf(
                        TimeFunctions().convertTimestampToLocalizeTime(
                            request.timestamp,
                            context
                        )
                    )
                }
                LaunchedEffect(true) {
                    while (true) {
                        delay(60100)
                        text = TimeFunctions().convertTimestampToLocalizeTime(
                            request.timestamp,
                            context
                        )
                    }
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
            }

        }
        if (request.accepted == true) {
            ElevatedButton(
                onClick = {},
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 2.dp),
            ) {
                Text(
                    stringResource(R.string.participated),
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {

                IconButton(
                    onClick = onRejectRequest
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
                    Text(
                        text = stringResource(R.string.accept),
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun EventPagePreview() {
    EventPageContent(
        eventResource = Resource.Success(
            data = Event(
                title = "title",
                description = "description",
                addressDescription = "address",
                date = Timestamp(
                    LocalDateTime.of(
                        2025,1,1,1,1
                    ).toInstant(ZoneOffset.UTC)
                )
            )
        ),
        navigateToEventPage = {},
        navigateToProfile = {},
        currentUser = null,
        numberOfRequests = 5,
        showRequests = {},
        participantsIsShown = false,
        showParticipants = {},
        cancelTheEvent = {},
        ticketDialogIsShown = false,
        showTicketDialog = {},
        userResourceFlow = Resource.Success(data = User(uid = "0", userName = "furkan")),
        navigateToEditEvent = {},
        onBackClick = {},
        showCancelAlertDialog = {},
        cancelAlertDialogIsShown = false,
        showUnverifiedAccountAlertDialog = {},
        unverifiedAccountDialogIsShown = false,
        participantsResource = Resource.Idle(),
        removeParticipantsResource = Resource.Idle(),
        participantsMBSState = rememberModalBottomSheetState(),
        removeParticipant = {},
        loadMoreParticipants = {},
        requestsIsShown = false,
        requestsMBSState = rememberModalBottomSheetState(),
        dismissRequestsMBS = {},
        owner = Resource.Success(data = User(uid = "0",userName = "furkan")),
        acceptReqeust = {},
        rejectRequest = {},
        acceptResource = Resource.Idle(),
        rejectResource = Resource.Idle(),
        loadMoreRequests = {},
        requestsResource = Resource.Idle(),
        countTheParticipants = 5,
        communities = Resource.Idle(),
        getLinkedCommunities = {},
        navigateToCommunityPage = {},
        thereIsLastParticipantDoc = true,
        thereIsLastRequestDoc = true
    )
}
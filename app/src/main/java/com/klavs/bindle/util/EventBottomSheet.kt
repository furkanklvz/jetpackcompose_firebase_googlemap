package com.klavs.bindle.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShortText
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.klavs.bindle.R
import com.klavs.bindle.data.entity.community.Community
import com.klavs.bindle.data.entity.DetailedEvent
import com.klavs.bindle.data.entity.Event
import com.klavs.bindle.data.entity.User
import com.klavs.bindle.resource.Resource
import com.klavs.bindle.ui.theme.Green1
import com.klavs.bindle.ui.theme.Green2
import com.klavs.bindle.ui.theme.LightRed
import com.klavs.bindle.uix.view.GlideImageLoader
import com.klavs.bindle.uix.viewmodel.EventBottomSheetViewModel
import com.klavs.bindle.uix.viewmodel.NavHostViewModel
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EventBottomSheet(
    onDismiss: () -> Unit,
    sheetState: SheetState,
    event: Event,
    onCommunityClick: (String) -> Unit,
    viewModel: EventBottomSheetViewModel = hiltViewModel(),
    navHostViewModel: NavHostViewModel,
    showTicketDialog: () -> Unit,
    onLogInClick: () -> Unit,
    currentUser: FirebaseUser?,
    navigateToEventPage: (String) -> Unit,
    showUnverifiedAccountAlertDialog: () -> Unit,
    context: Context
) {
    var isLoading by remember { mutableStateOf(true) }
    var detailedEvent by remember { mutableStateOf<DetailedEvent?>(null) }
    val requestSent by viewModel.requestSent.collectAsState()
    val amIParticipated by viewModel.amIParticipating.collectAsState()
    val userResourceFlow by navHostViewModel.userResourceFlow.collectAsState()
    val linkedCommunitiesResource by viewModel.linkedCommunities.collectAsState()



    LaunchedEffect(true) {
        viewModel.getEventDetails(
            event = event,
            myUid = currentUser?.uid
        )
    }

    LaunchedEffect(viewModel.eventDetailsState.value) {
        when (val state = viewModel.eventDetailsState.value) {
            is Resource.Error -> {
                Toast.makeText(
                    context,
                    state.messageResource?.let { context.getString(it) } ?: "",
                    Toast.LENGTH_SHORT
                ).show()
                onDismiss()
                isLoading = false
            }

            is Resource.Idle -> {}
            is Resource.Loading -> {
                isLoading = true
            }

            is Resource.Success -> {
                detailedEvent = state.data!!
                isLoading = false
            }
        }
    }
    EventBottomSheetContent(
        sheetState = sheetState,
        onDismiss = {
            onDismiss()
            viewModel.listenRequestJob?.cancel()
            viewModel.listenParticipatingJob?.cancel()
            viewModel.resetLinkedCommunities()
        },
        userResourceFlow = userResourceFlow,
        myUid = currentUser?.uid,
        sendRequest = {
            viewModel.sendRequest(
                eventId = it,
                myUid = currentUser!!.uid,
                newTickets = userResourceFlow.data!!.tickets - 2
            )
        },
        isLoading = isLoading,
        requestSent = requestSent,
        amIParticipated = amIParticipated,
        detailedEvent = detailedEvent,
        getLinkedCommunities = {
            viewModel.getLinkedCommunities(
                listOfCommunityIds = detailedEvent!!.event.linkedCommunities
            )
        },
        onCommunityClick = { onCommunityClick(it) },
        isEmailVerified = currentUser?.isEmailVerified,
        showUnverifiedAccountAlertDialog = showUnverifiedAccountAlertDialog,
        showTicketDialog = showTicketDialog,
        linkedCommunitiesResource = linkedCommunitiesResource,
        participateTheEvent = {
            viewModel.participateTheEvent(
                detailedEvent!!.event.id ?: "",
                detailedEvent!!.event.ownerUid,
                myUid = currentUser!!.uid,
                newTickets = userResourceFlow.data!!.tickets - 2
            )
        },
        navigateToEventPage = { navigateToEventPage(event.id!!) },
        onLogInClick = onLogInClick
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EventBottomSheetContent(
    userResourceFlow: Resource<User>,
    myUid: String?,
    requestSent: Boolean?,
    amIParticipated: Boolean?,
    detailedEvent: DetailedEvent?,
    sheetState: SheetState,
    isLoading: Boolean,
    isEmailVerified: Boolean?,
    onCommunityClick: (String) -> Unit,
    navigateToEventPage: () -> Unit,
    sendRequest: (String) -> Unit,
    linkedCommunitiesResource: Resource<List<Community>>,
    showUnverifiedAccountAlertDialog: () -> Unit,
    participateTheEvent: () -> Unit,
    onLogInClick: () -> Unit,
    showTicketDialog: () -> Unit,
    getLinkedCommunities: () -> Unit,
    onDismiss: () -> Unit
) {
    var showConfirmRequestDialog by remember { mutableStateOf<String?>(null) }
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        modifier = Modifier.padding(
            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        )
    ) {
        Column(Modifier.fillMaxSize()) {
            if (showConfirmRequestDialog != null) {
                AlertDialog(
                    text = {
                        Text(stringResource(R.string.your_ticket_will_be_refunded))
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (userResourceFlow is Resource.Success) {
                                    if (myUid != null) {
                                        sendRequest(showConfirmRequestDialog!!)
                                    }
                                    showConfirmRequestDialog = null
                                }
                            }
                        ) {
                            Text(stringResource(R.string.send_request))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showConfirmRequestDialog = null }
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                    },
                    onDismissRequest = { showConfirmRequestDialog = null }
                )
            }
            if (isLoading) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularWavyProgressIndicator()
                }
            } else {
                EventInfos(
                    detailedEvent = detailedEvent!!,
                    onCommunityListClicked = getLinkedCommunities,
                    amIParticipated = amIParticipated,
                    requestSent = requestSent,
                    myUid = myUid,
                    onCommunityClick = { onCommunityClick(it) },
                    showConfirmRequestDialog = {
                        if (myUid != null) {
                            if (isEmailVerified!!) {
                                if (userResourceFlow is Resource.Success && userResourceFlow.data != null) {
                                    if (userResourceFlow.data.tickets < 2) {
                                        showTicketDialog()
                                    } else {
                                        showConfirmRequestDialog = it
                                    }
                                } else {
                                    /*Toast.makeText(
                                        context,
                                        context.getString(R.string.something_went_wrong_try_again_later),
                                        Toast.LENGTH_SHORT
                                    ).show()*/
                                }
                            } else {
                                showUnverifiedAccountAlertDialog()
                            }
                        }

                    },
                    onParticipate = {
                        if (myUid != null) {
                            if (isEmailVerified!!) {
                                if (userResourceFlow is Resource.Success && userResourceFlow.data != null) {
                                    if (userResourceFlow.data.tickets < 2) {
                                        showTicketDialog()
                                    } else {
                                        participateTheEvent()
                                    }
                                } else {
                                    /*Toast.makeText(
                                        context,
                                        context.getString(R.string.something_went_wrong_try_again_later),
                                        Toast.LENGTH_SHORT
                                    ).show()*/
                                }
                            } else {
                                showUnverifiedAccountAlertDialog()
                            }
                        }
                    },
                    navigateToEventPage = navigateToEventPage,
                    onLogInClick = onLogInClick,
                    linkedCommunitiesResource = linkedCommunitiesResource,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EventInfos(
    detailedEvent: DetailedEvent,
    requestSent: Boolean?,
    linkedCommunitiesResource: Resource<List<Community>>,
    myUid: String?,
    showConfirmRequestDialog: (String) -> Unit,
    onParticipate: () -> Unit,
    navigateToEventPage: () -> Unit,
    onCommunityClick: (String) -> Unit,
    onCommunityListClicked: () -> Unit,
    onLogInClick: () -> Unit,
    amIParticipated: Boolean?
) {
    val context = LocalContext.current
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        val eventTypeList = Constants.EVENT_TYPES

        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            visible = detailedEvent.event.date > Timestamp.now()
                    && myUid != null
                    && amIParticipated == true
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                onClick = { navigateToEventPage() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(5.dp)
                ) {
                    Text(
                        stringResource(R.string.your_are_participating),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.NavigateNext,
                        contentDescription = "go to event page"
                    )
                }
            }
        }
        Row(
            Modifier
                .fillMaxWidth(0.9f)
                .padding(vertical = 30.dp)
                .align(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = eventTypeList.find { it.value == detailedEvent.event.type }?.icon
                        ?: Icons.Rounded.Celebration,
                    contentDescription = "type"
                )
                Text(
                    detailedEvent.event.title,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Rounded.Groups, contentDescription = "participants count")
                Text(
                    text = "${detailedEvent.participantsCount ?: 0}/${detailedEvent.event.participantLimit ?: "∞"}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }

        }
        if (detailedEvent.event.date < Timestamp.now()) {
            Text(
                stringResource(R.string.expired),
                style = MaterialTheme.typography.titleSmall,
                color = LightRed,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(end = 15.dp)
            )
        } else {
            if (myUid == null) {
                FilledTonalButton(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(end = 20.dp),
                    onClick = onLogInClick
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            stringResource(R.string.sign_in_to_participate)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.NavigateNext,
                            contentDescription = "sign in"
                        )
                    }

                }

            } else {
                if (amIParticipated != null && requestSent != null) {
                    if (!amIParticipated) {
                        if (detailedEvent.event.onlyByRequest) {
                            if (requestSent) {
                                FilledTonalButton(
                                    modifier = Modifier.align(Alignment.CenterHorizontally),
                                    shape = RoundedCornerShape(15.dp),
                                    onClick = {},

                                    ) {
                                    Text(stringResource(R.string.request_sent))
                                }
                            } else {
                                FilledTonalButton(
                                    modifier = Modifier.align(Alignment.CenterHorizontally),
                                    shape = RoundedCornerShape(15.dp),
                                    onClick = {
                                        showConfirmRequestDialog(detailedEvent.event.id!!)
                                    }
                                ) {
                                    Text(stringResource(R.string.send_request_to_participate))
                                }
                            }
                        } else {
                            FilledTonalButton(
                                shape = RoundedCornerShape(15.dp),
                                onClick = onParticipate,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                            ) {
                                Text(stringResource(R.string.participate))
                            }
                        }
                    }
                }
            }
        }


        Surface(
            Modifier
                .padding(10.dp)
                .fillMaxWidth(), shape = RoundedCornerShape(10.dp)
        ) {
            Column {
                Text(
                    "When?", style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(10.dp)
                )
                if (detailedEvent.event.privateInfo) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = "event date",
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Column {
                            Text(
                                stringResource(R.string._private),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                stringResource(R.string.to_see_date_participate_event),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = "event date",
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            TimeFunctions().convertTimestampToDate(detailedEvent.event.date),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccessTime,
                            contentDescription = "event time",
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                        Text(
                            sdf.format(detailedEvent.event.date.toDate()),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }


            }
        }
        Surface(
            Modifier
                .padding(10.dp)
                .fillMaxWidth(), shape = RoundedCornerShape(10.dp)
        ) {
            Column {
                Text(
                    stringResource(R.string.where), style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(10.dp)
                )
                ListItem(
                    headlineContent = {
                        if (detailedEvent.event.privateInfo) {
                            Column {
                                Text(
                                    stringResource(R.string._private),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    stringResource(R.string.to_see_address_participate_event),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        } else {
                            Text(
                                detailedEvent.event.addressDescription ?: "",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = "event address"
                        )
                    },
                    trailingContent = if (detailedEvent.event.privateInfo) null else {
                        {
                            FilledTonalIconButton(
                                onClick = {
                                    val mapUri =
                                        Uri.parse("geo:0,0?q=${detailedEvent.event.latitude},${detailedEvent.event.longitude}(${detailedEvent.event.title})")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, mapUri)
                                    context.startActivity(mapIntent)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Directions,
                                    contentDescription = "direction"
                                )
                            }
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
        }
        Surface(
            Modifier
                .padding(10.dp)
                .fillMaxWidth(), shape = RoundedCornerShape(10.dp)
        ) {
            Column {
                Text(
                    stringResource(R.string.how), style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(10.dp)
                )
                ListItem(
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    leadingContent = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ShortText,
                            contentDescription = "event description"
                        )
                    },
                    headlineContent = {
                        Text(
                            detailedEvent.event.description,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                )
            }
        }
        Surface(
            Modifier
                .padding(10.dp)
                .fillMaxWidth(), shape = RoundedCornerShape(10.dp)
        ) {
            Column {
                Text(
                    stringResource(R.string.who_organizes),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(10.dp)
                )
                ListItem(
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(IconButtonDefaults.smallContainerSize())
                                .clip(CircleShape)
                        ) {
                            if (detailedEvent.owner.profilePictureUrl != null) {
                                GlideImageLoader(
                                    detailedEvent.owner.profilePictureUrl,
                                    LocalContext.current,
                                    Modifier.matchParentSize()
                                )
                            } else {
                                Image(
                                    imageVector = Icons.Rounded.Person,
                                    contentDescription = "event owner",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(Color.LightGray)
                                )
                            }
                        }
                    },
                    headlineContent = {
                        Text(
                            detailedEvent.owner.userName,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        }
        Surface(
            Modifier
                .padding(10.dp)
                .fillMaxWidth(), shape = RoundedCornerShape(10.dp)
        ) {
            var communityListExpanded by remember { mutableStateOf(false) }
            Column {
                Text(
                    stringResource(R.string.which_communities_are_linked),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(10.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Groups,
                        contentDescription = "linked communities",
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .align(Alignment.Top)
                    )
                    Column {
                        Text(
                            stringResource(
                                R.string.x_linked_community,
                                detailedEvent.event.linkedCommunities.size
                            ),
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (!communityListExpanded && detailedEvent.event.linkedCommunities.isNotEmpty()) {
                            TextButton(
                                onClick = {
                                    onCommunityListClicked()
                                    communityListExpanded = true
                                }
                            ) {
                                Text(stringResource(R.string.show))
                            }
                        }
                        if (communityListExpanded) {
                            LinkedCommunityList(
                                onCommunityClick = { onCommunityClick(it) },
                                linkedCommunitiesResource = linkedCommunitiesResource
                            )
                        }
                    }

                }
            }
        }

    }
}

@Composable
private fun LinkedCommunityList(
    onCommunityClick: (String) -> Unit,
    linkedCommunitiesResource: Resource<List<Community>>,
) {
    val context = LocalContext.current
    val linkedCommunityList = remember { mutableStateListOf<Community>() }
    var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(linkedCommunitiesResource) {
        when (linkedCommunitiesResource) {
            is Resource.Error -> {
                isLoading = false
                Toast.makeText(
                    context,
                    linkedCommunitiesResource.messageResource?.let { context.getString(it) } ?: "",
                    Toast.LENGTH_SHORT
                ).show()
            }

            is Resource.Idle -> {}
            is Resource.Loading -> {
                isLoading = true
            }

            is Resource.Success -> {
                isLoading = false
                linkedCommunityList.clear()
                linkedCommunityList.addAll(linkedCommunitiesResource.data!!)
            }
        }
    }
    if (isLoading) {
        Column(Modifier.fillMaxWidth()) {
            LinearProgressIndicator(Modifier.padding(vertical = 10.dp))
        }
    } else {
        Column {
            linkedCommunityList.forEach { community ->
                ListItem(
                    modifier = Modifier.clickable { onCommunityClick(community.id!!) },
                    trailingContent = {
                        IconButton(
                            onClick = { onCommunityClick(community.id!!) }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                                contentDescription = "expand"
                            )
                        }

                    },
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        ) {
                            if (community.communityPictureUrl != null) {
                                GlideImageLoader(
                                    community.communityPictureUrl,
                                    LocalContext.current,
                                    Modifier.matchParentSize()
                                )
                            } else {
                                Image(
                                    imageVector = Icons.Rounded.Groups,
                                    contentDescription = "event owner",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(Color.LightGray)
                                )
                            }
                        }
                    },
                    headlineContent = {
                        Text(
                            community.name, maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun EventBottomSheetPreview() {
    var requestSent by remember { mutableStateOf(false) }
    var amIParticipated by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize()) {
        EventBottomSheetContent(
            userResourceFlow = Resource.Success(data = User(uid = "0", tickets = 2)),
            myUid = null,
            requestSent = requestSent,
            amIParticipated = amIParticipated,
            detailedEvent = DetailedEvent(
                event = Event(
                    date = Timestamp(
                        LocalDateTime
                            .of(2025, 1, 1, 1, 1, 1)
                            .toInstant(ZoneOffset.UTC)
                    ),
                    title = "sample titlesample titlesample titlesample titlesample title",
                    id = "0",
                    description = "sample descriptionsample descriptiosample descriptiosample descriptio",
                    addressDescription = "sample addresssample addresssample address",
                    participantLimit = 30,
                    privateInfo = false,
                    onlyByRequest = false
                ),
                owner = User(
                    uid = "1",
                    userName = "sample ownersample ownersample ownersample ownersample owner"
                ),
                participants = emptyList(),
                participantsCount = 15
            ),
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            isLoading = false,
            isEmailVerified = true,
            onCommunityClick = {},
            navigateToEventPage = {},
            sendRequest = { requestSent = true },
            linkedCommunitiesResource = Resource.Success(data =
            listOf(Community(id = "0", name = "sample community"))
            ),
            showUnverifiedAccountAlertDialog = {},
            participateTheEvent = { amIParticipated = true },
            onLogInClick = {},
            showTicketDialog = {},
            getLinkedCommunities = {},
            onDismiss = {}
        )
    }
}
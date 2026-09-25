package com.klavs.bindle.uix.view.event

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.firebase.Timestamp
import com.klavs.bindle.R
import com.klavs.bindle.data.entity.Event
import com.klavs.bindle.data.entity.community.JoinedCommunity
import com.klavs.bindle.data.entity.sealedclasses.BottomNavItem
import com.klavs.bindle.resource.Resource
import com.klavs.bindle.uix.view.map.BasicInfos
import com.klavs.bindle.uix.view.map.EventTypeDatas
import com.klavs.bindle.uix.view.map.LocationInfos
import com.klavs.bindle.uix.view.map.ParticipationInfos
import com.klavs.bindle.uix.view.map.SelectCommunityDialog
import com.klavs.bindle.uix.viewmodel.NavHostViewModel
import com.klavs.bindle.uix.viewmodel.event.EditEventViewModel
import com.klavs.bindle.util.Constants
import com.klavs.bindle.util.TicketDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EditEvent(
    navController: NavHostController,
    myUid: String,
    event: Event,
    viewModel: EditEventViewModel,
    navHostViewModel: NavHostViewModel
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val linkedCommunitiesResource by viewModel.linkedCommunitiesResource.collectAsState()
    val joinedCommunitiesResource by viewModel.joinedCommunitiesResource.collectAsState()
    val saveEventState by viewModel.changesResource.collectAsState()
    val userResource by navHostViewModel.userResourceFlow.collectAsState()
    var showTicketDialog by remember { mutableStateOf(false) }

    LaunchedEffect(saveEventState) {
        if (saveEventState is Resource.Success) {
            navController.popBackStack()
        }
    }

    LaunchedEffect(true) {
        viewModel.changesResource.value = Resource.Idle()
        viewModel.linkedCommunitiesResource.value = Resource.Idle()
        viewModel.joinedCommunitiesResource.value = Resource.Idle()
        if (linkedCommunitiesResource is Resource.Idle) {
            viewModel.getLinkedCommunities(
                communityIds = event.linkedCommunities
            )
        }
    }
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
                    text = stringResource(R.string.edit_event),
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
            when (saveEventState) {
                is Resource.Error -> {
                    AlertDialog(text = {
                        Text(
                            text = linkedCommunitiesResource.messageResource?.let {
                                stringResource(
                                    it
                                )
                            } ?: ""
                        )
                    },
                        onDismissRequest = { navController.popBackStack() },
                        confirmButton = {
                            TextButton(onClick = { navController.popBackStack() }) {
                                Text(text = stringResource(R.string.okay))
                            }
                        })
                }

                is Resource.Loading -> {
                    CircularWavyProgressIndicator(
                        Modifier
                            .align(Alignment.Center)
                            .zIndex(2f)
                    )
                }

                else -> {

                }
            }
            when (linkedCommunitiesResource) {
                is Resource.Error -> {
                    AlertDialog(text = {
                        Text(
                            text = linkedCommunitiesResource.messageResource?.let {
                                stringResource(
                                    it
                                )
                            } ?: ""
                        )
                    },
                        onDismissRequest = { navController.popBackStack() },
                        confirmButton = {
                            TextButton(onClick = { navController.popBackStack() }) {
                                Text(text = stringResource(R.string.okay))
                            }
                        })
                }

                is Resource.Idle -> {}
                is Resource.Loading -> {
                    CircularWavyProgressIndicator(Modifier.align(Alignment.Center))
                }

                is Resource.Success -> {
                    Content(
                        myUid = myUid,
                        event = event,
                        linkedCommunities = linkedCommunitiesResource.data ?: emptyList(),
                        communities = joinedCommunitiesResource,
                        getJoinedCommunities = {
                            viewModel.getJoinedCommunities(
                                myUid = myUid
                            )
                        },
                        onSaveChanges = { updatedEvent ->
                            if (userResource is Resource.Success) {
                                if (userResource.data!!.tickets >= 1) {
                                    viewModel.saveChanges(
                                        updatedEvent,
                                        newTickets = userResource.data!!.tickets  - 1,
                                        uid = myUid
                                    )
                                } else {
                                    showTicketDialog = true
                                }
                            } else {
                                scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.something_went_wrong_try_again_later)) }
                            }
                        },
                        showSnackbar = {
                            scope.launch {
                                snackbarHostState.showSnackbar(it)
                            }
                        },
                        showTicketDialog = showTicketDialog,
                        dismissTicketDialog = { showTicketDialog = false },
                        tickets = userResource.data?.tickets ?: 0,
                        innerPadding = innerPadding
                    )
                }
            }
        }
    }
}

@Composable
private fun Content(
    linkedCommunities: List<JoinedCommunity>,
    event: Event,
    showTicketDialog: Boolean,
    dismissTicketDialog: () -> Unit,
    communities: Resource<List<JoinedCommunity>>,
    onSaveChanges: (Event) -> Unit,
    showSnackbar: (String) -> Unit,
    getJoinedCommunities: () -> Unit,
    innerPadding: PaddingValues,
    tickets: Long,
    myUid: String,
) {
    val context = LocalContext.current
    var openCommunitySelectionDialog by remember { mutableStateOf(false) }
    val mutableLinkedCommunities = remember { linkedCommunities.toMutableStateList() }
    var addressDescription by remember {
        mutableStateOf(event.addressDescription ?: "")
    }
    var title by remember {
        mutableStateOf(event.title)
    }
    var date by remember {
        mutableStateOf(event.date)
    }
    var type by remember {
        mutableStateOf(getEventTypeObjectFromValue(event.type))
    }
    var privateEvent by remember { mutableStateOf(event.privateEvent) }
    var participantLimit by remember { mutableStateOf(event.participantLimit) }
    var eventDescription by remember {
        mutableStateOf(event.description)
    }
    var onlyByRequest by remember { mutableStateOf(event.onlyByRequest) }
    var hideDate by remember { mutableStateOf(event.privateInfo) }

    LaunchedEffect(mutableLinkedCommunities) {
        if (mutableLinkedCommunities.isEmpty()) {
            privateEvent = false
        }
    }


    Box {
        if (showTicketDialog){
            TicketDialog(
                onDismiss = dismissTicketDialog,
                uid = myUid,
                tickets = tickets,
                paddingValues = innerPadding
            )
        }
        if (openCommunitySelectionDialog) {
            SelectCommunityDialog(
                communitiesResource = communities,
                onDismissRequest = { openCommunitySelectionDialog = false },
                selectedCommunities = mutableLinkedCommunities,
                onSelect = {
                    mutableLinkedCommunities.add(it)
                },
                onRemove = { id ->
                    mutableLinkedCommunities.remove(mutableLinkedCommunities.find {
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
            EventTypeDatas(
                type = type,
                options = options,
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
                onParticipationChange = {
                    onlyByRequest = it
                },
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
                                getJoinedCommunities()
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
                        mutableLinkedCommunities.forEach { community ->
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
                                    onClick = {
                                        mutableLinkedCommunities.remove(
                                            mutableLinkedCommunities.find { it.id == community.id })
                                    }
                                ) {
                                    Text(text = stringResource(R.string.remove), color = Color.Red)
                                }
                            }
                        }
                        AnimatedVisibility(mutableLinkedCommunities.isNotEmpty()) {
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
                    } else {
                        if (event.date >= Timestamp.now()) {
                            val eventModel = Event(
                                id = event.id,
                                ownerUid = myUid,
                                title = title,
                                type = type!!.value,
                                description = eventDescription,
                                addressDescription = addressDescription,
                                date = date,
                                privateEvent = privateEvent,
                                onlyByRequest = onlyByRequest,
                                privateInfo = hideDate,
                                linkedCommunities = mutableLinkedCommunities.map { it.id!! },
                                latitude = event.latitude,
                                longitude = event.longitude,
                                participantLimit = participantLimit,
                                chatRestriction = event.chatRestriction
                            )
                            onSaveChanges(eventModel)
                        } else {
                            showSnackbar(context.getString(R.string.cannot_edit_past_events))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(text = stringResource(R.string.save))
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Preview
@Composable
private fun EditEventPreview() {
    Scaffold {
        Box(
            Modifier
                .padding(it)
                .fillMaxSize(), contentAlignment = Alignment.TopCenter
        ) {
            Content(
                linkedCommunities = emptyList(),
                event = Event(),
                communities = Resource.Idle(),
                onSaveChanges = {},
                showSnackbar = {},
                getJoinedCommunities = {},
                myUid = "0",
                showTicketDialog = false,
                dismissTicketDialog = {},
                tickets = 0,
                innerPadding = PaddingValues(0.dp)
            )
        }
    }
}
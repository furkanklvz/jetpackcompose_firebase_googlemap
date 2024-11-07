package com.klavs.bindle.uix.view.map

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.automirrored.outlined.ShortText
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.LockClock
import androidx.compose.material.icons.outlined.LockPerson
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.maps.model.LatLng
import com.klavs.bindle.data.entity.EventType
import com.klavs.bindle.resource.Resource
import com.klavs.bindle.uix.view.loading.LoadingAnimation
import com.klavs.bindle.uix.viewmodel.MapViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEvent(
    navController: NavHostController, latitude: String, longitude: String,
    viewModel: MapViewModel = hiltViewModel()
) {

    val location = LatLng(latitude.toDouble(), longitude.toDouble())
    val context = LocalContext.current
    var isLoading by remember {
        mutableStateOf(false)
    }
    var isError by remember {
        mutableStateOf(false)
    }
    var errorMessage by remember {
        mutableStateOf("")
    }
    var address by remember {
        mutableStateOf("")
    }
    var isSuccess by remember {
        mutableStateOf(false)
    }
    var openCommunitySelectionDialog by remember { mutableStateOf(false) }

    var communities = remember {
        mutableStateMapOf(
            "001" to "Revir",
            "002" to "TEMA",
            "003" to "Revir",
            "004" to "TEMA",
            "005" to "Revir",
            "006" to "TEMA",
            "007" to "Revir",
            "008" to "TEMA",
            "009" to "Revir",
            "010" to "TEMA",
            "011" to "Revir",
            "012" to "TEMA",
            "013" to "Revir",
            "014" to "TEMA",
        )
    }
    val selectedCommunities = remember { mutableStateMapOf<String, String>() }


    LaunchedEffect(key1 = true) {
        viewModel.getAddressFromLocation(location)
    }
    LaunchedEffect(key1 = viewModel.addressState.value) {
        when (val result = viewModel.addressState.value) {
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
                address = result.data!!.getAddressLine(0)
            }
        }
    }


    Scaffold(topBar = {
        TopAppBar(navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "back"
                )
            }
        }, title = {
            Text(
                text = "Create Event",
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
            if (openCommunitySelectionDialog) {
                Dialog(onDismissRequest = { openCommunitySelectionDialog = false }) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        modifier = Modifier
                            .width(300.dp)
                            .height(500.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Box(modifier = Modifier.matchParentSize()) {
                            if (!communities.isEmpty()) {
                                LazyColumn(modifier = Modifier.height(450.dp)) {
                                    items(communities.keys.toList()) {
                                        val isSelected = selectedCommunities.containsKey(it)
                                        TextButton(onClick = {
                                            if (isSelected) {
                                                selectedCommunities.remove(it)
                                            } else {
                                                selectedCommunities[it] = communities[it]!!
                                            }
                                        }) {
                                            Icon(
                                                imageVector = if (isSelected) Icons.Outlined.Check else Icons.Outlined.Add,
                                                contentDescription = "select",
                                                modifier = Modifier.padding(end = 8.dp)
                                            )
                                            Text(text = communities[it]!!)
                                        }
                                    }
                                }
                            } else {
                                Text(
                                    textAlign = TextAlign.Center,
                                    text = "There is no community you are a member of",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.align(Alignment.Center))
                            }
                            OutlinedButton(
                                onClick = { openCommunitySelectionDialog = false },
                                modifier = Modifier.align(
                                    Alignment.BottomCenter
                                )
                            ) {
                                Text(text = "Okay")
                            }
                        }
                    }
                }
            }
            if (isError) {
                AlertDialog(text = { Text(text = errorMessage) },
                    onDismissRequest = { isError = false },
                    confirmButton = {
                        TextButton(onClick = { navController.popBackStack() }) {
                            Text(text = "ok")
                        }
                    })
            } else if (isLoading) {
                Box(modifier = Modifier.align(Alignment.Center)) {
                    LoadingAnimation(300.dp)
                }
            } else {
                Content(
                    address = address,
                    openCommunitySelectionDialog = { openCommunitySelectionDialog = true },
                    selectedCommunities = selectedCommunities.toMap(),
                    removeCommunity = { selectedCommunities.remove(it) }
                )
            }
        }
    }
}

@Composable
private fun Content(
    address: String,
    openCommunitySelectionDialog: () -> Unit,
    selectedCommunities: Map<String, String>,
    removeCommunity: (String) -> Unit
) {
    var addressDescription by remember {
        mutableStateOf(address)
    }
    var title by remember {
        mutableStateOf("")
    }
    var date by remember {
        mutableStateOf("")
    }
    var time by remember {
        mutableStateOf("")
    }
    var type by remember {
        mutableStateOf<EventType?>(null)
    }
    var eventDescription by remember {
        mutableStateOf("")
    }
    var onlyInvitiations by remember { mutableStateOf(true) }
    var hideDate by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        BasicInfos(title = title, date = date, time = time, onTitleChange = {
            if (it.length <= 30) {
                title = it
            }
        }, onDateChange = {}, onTimeChange = {})
        Spacer(modifier = Modifier.height(10.dp))
        LocationInfos(address = addressDescription, onAddressChange = {
            if (it.length <= 100) {
                addressDescription = it
            }
        })
        Spacer(modifier = Modifier.height(10.dp))
        val options = listOf(
            EventType.Sport,
            EventType.Cultural,
            EventType.Musical,
            EventType.Meeting,
            EventType.Organization,
            EventType.Nature,
            EventType.Education,
        )
        OtherInfos(options = options,
            eventDescription = eventDescription,
            onEventDescriptionChange = {
                if (it.length <= 150) {
                    eventDescription = it
                }
            },
            onTypeChange = {})
        Spacer(modifier = Modifier.height(10.dp))
        ParticipationInfos(onHideDateChange = { hideDate = true },
            onParticipationChange = { onlyInvitiations = it })
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
                        text = "Related Communities",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(4.dp)
                    )
                    Text(
                        text = "By adding your related communities, " +
                                "you can ensure that members in your communities are aware of your event.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(4.dp)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = openCommunitySelectionDialog) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = "add",
                                modifier = Modifier.padding(end = 5.dp)
                            )
                            Text(text = "Add Community")
                        }
                    }
                    selectedCommunities.forEach {
                        Spacer(modifier = Modifier.height(5.dp))
                        Row(verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = it.value, modifier = Modifier.padding(start = 18.dp))
                            TextButton(
                                onClick = { removeCommunity(it.key) }
                            ) {
                                Text(text = "Remove", color = Color.Red)
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = { /*TODO*/ },
            modifier = Modifier.fillMaxWidth(0.4f).align(Alignment.CenterHorizontally)) {
            Text(text = "Create Event")
        }
        Spacer(modifier = Modifier.height(10.dp))
    }
}


@Composable
private fun OtherInfos(
    options: List<EventType> = emptyList(),
    eventDescription: String,
    onEventDescriptionChange: (String) -> Unit,
    onTypeChange: (EventType) -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }
    var selectedValue by remember { mutableStateOf<EventType?>(null) }
    Surface(color = MaterialTheme.colorScheme.surfaceContainer, shape = RoundedCornerShape(10.dp)) {
        Box(
            modifier = Modifier
                .padding(6.dp)
                .fillMaxWidth(0.96f), propagateMinConstraints = true
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Column {
                    TextField(label = { Text(text = "Event Type:") }, trailingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.KeyboardArrowDown,
                            contentDescription = "extend"
                        )
                    }, leadingIcon = selectedValue?.icon ?: {
                        Icon(
                            imageVector = Icons.Outlined.Celebration,
                            contentDescription = "type"
                        )
                    }, enabled = false, modifier = Modifier
                        .clickable {
                            expanded = !expanded
                        }
                        .fillMaxWidth(), colors = TextFieldDefaults.colors(
                        disabledContainerColor = Color.Transparent,
                        disabledTextColor = TextFieldDefaults.colors().unfocusedTextColor,
                        disabledLabelColor = TextFieldDefaults.colors().unfocusedLabelColor,
                        disabledIndicatorColor = Color.Transparent,
                        disabledLeadingIconColor = TextFieldDefaults.colors().unfocusedLeadingIconColor,
                        disabledTrailingIconColor = TextFieldDefaults.colors().unfocusedTrailingIconColor
                    ), value = selectedValue?.label ?: "", onValueChange = {})
                    Spacer(modifier = Modifier.height(6.dp))
                    DropdownMenu(modifier = Modifier.fillMaxWidth(0.94f),
                        shape = RoundedCornerShape(10.dp),
                        expanded = expanded,
                        onDismissRequest = { expanded = false }) {
                        options.forEach {
                            DropdownMenuItem(leadingIcon = it.icon,
                                text = { Text(text = it.label) },
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
                    label = { Text(text = "Event Description:") },
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
private fun ParticipationInfos(
    onParticipationChange: (Boolean) -> Unit,
    onHideDateChange: (Boolean) -> Unit,
) {
    val participationOptions =
        listOf<Pair<String, @Composable () -> Unit>>("By invitation only" to {
            Icon(
                imageVector = Icons.Outlined.LockPerson, contentDescription = "lock"
            )
        }, "Open to all" to {
            Icon(
                imageVector = Icons.Outlined.Public, contentDescription = "public"
            )
        })
    val dateVisibilityOptions =
        listOf<Pair<String, @Composable () -> Unit>>("Only participation's can see" to {
            Icon(
                imageVector = Icons.Outlined.VisibilityOff, contentDescription = "lock"
            )
        }, "Everyone can see" to {
            Icon(
                imageVector = Icons.Outlined.Public, contentDescription = "public"
            )
        })

    var expanded by remember { mutableStateOf(false) }
    var selectedValue by remember { mutableStateOf(participationOptions[0]) }

    var dataVisibilityExpanded by remember { mutableStateOf(false) }
    var dateVisibilitySelectedValue by remember { mutableStateOf(dateVisibilityOptions[0]) }

    Surface(color = MaterialTheme.colorScheme.surfaceContainer, shape = RoundedCornerShape(10.dp)) {
        Box(
            modifier = Modifier
                .padding(6.dp)
                .fillMaxWidth(0.96f), propagateMinConstraints = true
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Participation Method",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(4.dp)
                )
                Text(
                    text = "You can choose how people participate in your event.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(4.dp)
                )
                Spacer(modifier = Modifier.height(5.dp))
                Column {
                    DropdownMenuItem(trailingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.KeyboardArrowDown,
                            contentDescription = "down"
                        )
                    },
                        leadingIcon = selectedValue.second,
                        text = { Text(text = selectedValue.first) },
                        onClick = { expanded = !expanded })
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        participationOptions.forEach {
                            DropdownMenuItem(text = { Text(text = it.first) },
                                leadingIcon = it.second,
                                onClick = {
                                    selectedValue = it
                                    onParticipationChange(it.first != "Open to all")
                                    expanded = false
                                })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(15.dp))
                Text(
                    text = "Date Visibility",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(4.dp)
                )
                Text(
                    text = "You can choose who can see the date of your event on the map",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(4.dp)
                )
                Spacer(modifier = Modifier.height(5.dp))
                Column {
                    DropdownMenuItem(trailingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.KeyboardArrowDown,
                            contentDescription = "down"
                        )
                    },
                        leadingIcon = dateVisibilitySelectedValue.second,
                        text = { Text(text = dateVisibilitySelectedValue.first) },
                        onClick = { dataVisibilityExpanded = !dataVisibilityExpanded })
                    DropdownMenu(expanded = dataVisibilityExpanded,
                        onDismissRequest = { dataVisibilityExpanded = false }) {
                        dateVisibilityOptions.forEach {
                            DropdownMenuItem(text = { Text(text = it.first) },
                                leadingIcon = it.second,
                                onClick = {
                                    dateVisibilitySelectedValue = it
                                    onHideDateChange(it.first != "Open to all")
                                    dataVisibilityExpanded = false
                                })
                        }
                    }
                }


            }
        }
    }
}

@Composable
private fun LocationInfos(
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
                    label = { Text(text = "Address Description:") },
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

@Composable
private fun BasicInfos(
    title: String,
    date: String,
    time: String,
    onTitleChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onTimeChange: (String) -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainer, shape = RoundedCornerShape(10.dp)) {
        Box(
            modifier = Modifier
                .padding(6.dp)
                .fillMaxWidth(0.96f), propagateMinConstraints = true
        ) {
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
                        label = { Text(text = "Title:") },
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
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.DateRange,
                                    contentDescription = "date"
                                )
                            },
                            label = { Text(text = "Date:") },
                            singleLine = true,
                            value = date,
                            onValueChange = onDateChange,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            )
                        )
                        TextField(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.AccessTime,
                                    contentDescription = "time"
                                )
                            },
                            label = { Text(text = "Time:") },
                            singleLine = true,
                            value = time,
                            onValueChange = onTimeChange,
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

    }
}

@Preview
@Composable
private fun CreateEventPreview() {
    CreateEvent(navController = rememberNavController(), "0", "0")
}
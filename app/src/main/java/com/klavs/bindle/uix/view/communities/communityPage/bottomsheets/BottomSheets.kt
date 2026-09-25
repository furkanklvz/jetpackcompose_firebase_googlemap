package com.klavs.bindle.uix.view.communities.communityPage.bottomsheets

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddModerator
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.RemoveModerator
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonRemove
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestOptions
import com.klavs.bindle.R
import com.klavs.bindle.util.TimeFunctions
import com.klavs.bindle.data.entity.community.Community
import com.klavs.bindle.data.entity.sealedclasses.CommunityRoles
import com.klavs.bindle.data.entity.RequestForCommunity
import com.klavs.bindle.data.entity.Member
import com.klavs.bindle.resource.Resource
import com.klavs.bindle.ui.theme.Green1
import com.klavs.bindle.ui.theme.Orange2
import com.klavs.bindle.uix.view.communities.getRoleNameFromRolePriority
import com.klavs.bindle.uix.viewmodel.communities.CommunityPageViewModel
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    state: SheetState,
    eventCreationRestriction: Boolean,
    postSharingRestriction: Boolean,
    participationByRequestOnly: Boolean,
    onEventCreationChange: (Boolean) -> Unit,
    onPostSharingChange: (Boolean) -> Unit,
    onParticipationByRequestChange: (Boolean) -> Unit,
    viewModel: CommunityPageViewModel,
    onDismiss: () -> Unit
) {

    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    LaunchedEffect(viewModel.updateCommunityFieldState.value) {
        when (val resource = viewModel.updateCommunityFieldState.value) {
            is Resource.Error -> {
                isLoading = false
                Toast.makeText(
                    context,
                    resource.messageResource?.let { context.getString(it) }?:"",
                    Toast.LENGTH_SHORT
                ).show()
            }

            is Resource.Loading -> {
                isLoading = true
            }

            else -> {
                isLoading = false
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = state
    ) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.community_settings),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(15.dp))
            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.event_creation_restriction),
                    modifier = Modifier.width(250.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = eventCreationRestriction,
                    onCheckedChange = {
                        if (!isLoading) {
                            onEventCreationChange(it)
                        }
                    },
                    thumbContent = {
                        if (eventCreationRestriction) {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = "Event creation restriction on",
                                modifier = Modifier.size(SwitchDefaults.IconSize)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.LockOpen,
                                contentDescription = "Event creation restriction on",
                                modifier = Modifier.size(SwitchDefaults.IconSize)
                            )
                        }
                    }
                )
            }
            Spacer(Modifier.height(3.dp))
            if (eventCreationRestriction) {
                Text(
                    stringResource(R.string.event_creation_restriction_description1),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            } else {
                Text(
                    stringResource(R.string.event_creation_restriction_description2),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.post_sharing_restriction),
                    modifier = Modifier.width(250.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = postSharingRestriction,
                    onCheckedChange = {
                        if (!isLoading) {
                            onPostSharingChange(it)
                        }
                    },
                    thumbContent = {
                        if (postSharingRestriction) {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = "post sharing restriction on",
                                modifier = Modifier.size(SwitchDefaults.IconSize)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.LockOpen,
                                contentDescription = "post sharing restriction on",
                                modifier = Modifier.size(SwitchDefaults.IconSize)
                            )
                        }
                    }
                )
            }
            Spacer(Modifier.height(3.dp))
            if (postSharingRestriction) {
                Text(
                    stringResource(R.string.post_sharing_restriction_description1),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            } else {
                Text(
                    stringResource(R.string.post_sharing_restriction_description2),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.joining_by_request_only),
                    modifier = Modifier.width(250.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = participationByRequestOnly,
                    onCheckedChange = {
                        if (!isLoading) {
                            onParticipationByRequestChange(it)
                        }
                    },
                    thumbContent = {
                        if (participationByRequestOnly) {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = "",
                                modifier = Modifier.size(SwitchDefaults.IconSize)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.LockOpen,
                                contentDescription = "",
                                modifier = Modifier.size(SwitchDefaults.IconSize)
                            )
                        }
                    }
                )
            }
            Spacer(Modifier.height(3.dp))
            if (participationByRequestOnly) {
                Text(
                    stringResource(R.string.joining_by_request_only_description1),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            } else {
                Text(
                    stringResource(R.string.joining_by_request_only_description2),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            }
            Spacer(Modifier.height(15.dp))
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RequestsBottomSheet(
    state: SheetState,
    community: Community,
    numOfRequests: Int?,
    viewModel: CommunityPageViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val requests by viewModel.joiningRequests.collectAsState()
    val requestList = remember { mutableStateListOf<RequestForCommunity>() }
    var isLoading by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.rejectRequestState.value) {
        when (val resource = viewModel.rejectRequestState.value) {
            is Resource.Error -> {
                Toast.makeText(
                    context,
                    resource.messageResource?.let { context.getString(it) }?:"",
                    Toast.LENGTH_SHORT
                ).show()
            }

            is Resource.Success -> {
                requestList.remove(requestList.find { it.uid == resource.data!! })
                if (requestList.isEmpty()) {
                    viewModel.getRequestsWithPaging(
                        communityId = community.id,
                        pageSize = 10
                    )
                }
            }
            else -> {}
        }
    }
    LaunchedEffect(viewModel.acceptRequestState.value) {
        when (val resource = viewModel.acceptRequestState.value) {
            is Resource.Error -> {
                Toast.makeText(
                    context,
                    resource.messageResource?.let { context.getString(it) }?:"",
                    Toast.LENGTH_SHORT
                ).show()
            }

            is Resource.Success -> {
                val request = requestList.find { it.uid == resource.data!! }
                val index = requestList.indexOf(request)
                requestList[index] = request!!.copy(accepted = true)
            }

            else -> {}
        }
    }

    ModalBottomSheet(
        modifier = Modifier.padding(top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()),
        sheetState = state,
        onDismissRequest = {
            onDismiss()
            requestList.clear()
        }
    ) {
        Text(
            text = stringResource(R.string.joining_requests),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(10.dp)
        )
        LaunchedEffect(requests) {
            when (requests) {
                is Resource.Error -> {
                    isLoading = false
                    hasError = true
                }

                is Resource.Idle -> {}
                is Resource.Loading -> {
                    isLoading = true
                }

                is Resource.Success -> {
                    isLoading = false
                    hasError = false
                    requestList.addAll(requests.data!!)
                }
            }
        }


        LazyColumn(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            items(requestList) { requestObject ->
                JoiningRequestRow(
                    request = requestObject,
                    onDeleteRequest = {
                        viewModel.rejectRequest(
                            communityId = community.id,
                            uid = requestObject.uid
                        )
                    },
                    onAcceptRequest = {
                        viewModel.acceptRequest(
                            communityId = community.id,
                            uid = requestObject.uid
                        )
                    }
                )
            }
            item {
                if (isLoading) {
                    CircularWavyProgressIndicator()
                } else if (hasError) {
                    Text(
                        stringResource(R.string.something_went_wrong),
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    if (requestList.isEmpty()) {
                        Row(
                            modifier = Modifier
                                .padding(vertical = 15.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "empty"
                            )
                            Text(
                                text = stringResource(R.string.no_request_to_join),
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                    if (viewModel.lastRequest != null && requestList.size < (numOfRequests
                            ?: Int.MAX_VALUE)
                    ) {
                        TextButton(
                            modifier = Modifier.padding(4.dp),
                            onClick = {
                                viewModel.getRequestsWithPaging(
                                    communityId = community.id,
                                    pageSize = 10
                                )
                            }
                        ) {
                            Text(text = stringResource(R.string.see_more), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

    }

}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun JoiningRequestRow(
    request: RequestForCommunity,
    onDeleteRequest: () -> Unit,
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
            Box(modifier = Modifier.size(50.dp)) {
                if (request.profilePictureUrl != null) {
                    GlideImage(
                        imageModel = { request.profilePictureUrl.toUri() },
                        modifier = Modifier
                            .matchParentSize()
                            .clip(CircleShape),
                        requestBuilder = {
                            val thumbnailRequest = Glide
                                .with(context)
                                .asBitmap()
                                .load(request.profilePictureUrl.toUri())
                                .apply(RequestOptions().override(100))

                            Glide
                                .with(context)
                                .asBitmap()
                                .apply(
                                    RequestOptions()
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                )
                                .thumbnail(thumbnailRequest)
                                .transition(withCrossFade())
                        },
                        loading = {
                            CircularWavyProgressIndicator(
                                modifier = Modifier
                                    .matchParentSize()
                                    .align(Alignment.Center)
                            )
                        }
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
                    text = request.userName,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
                var text by remember { mutableStateOf(TimeFunctions().convertTimestampToLocalizeTime(request.requestDate, context)) }
                LaunchedEffect(true) {
                    while(true){
                        delay(60100)
                        text = TimeFunctions().convertTimestampToLocalizeTime(request.requestDate, context)
                    }
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
            }

        }
        if (request.accepted) {
            ElevatedButton(
                onClick = {},
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 2.dp),
            ) {
                Text(stringResource(R.string.member), modifier = Modifier.padding(horizontal = 2.dp))
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {

                IconButton(
                    onClick = onDeleteRequest
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
                    Text(text = stringResource(R.string.accept), modifier = Modifier.padding(horizontal = 2.dp))
                }
            }
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MembersBottomSheet(
    userRole: Int,
    state: SheetState,
    communityId: String,
    numOfMembers: Int?,
    onDismiss: () -> Unit,
    onDeleteUser: (uid: String) -> Unit,
    onAddModeratorUser: (uid: String) -> Unit,
    onRemoveModeratorUser: (uid: String) -> Unit,
    onMakeAdminUser: (uid: String) -> Unit,
    viewModel: CommunityPageViewModel,
) {
    val context = LocalContext.current
    val members by viewModel.members.collectAsState()
    val memberList = remember { mutableStateListOf<Member>() }
    var isLoading by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    if (isProcessing) {
        Box(Modifier.fillMaxSize()) {
            CircularWavyProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }

    LaunchedEffect(viewModel.removeMemberState.value) {
        when (val resource = viewModel.removeMemberState.value) {
            is Resource.Error -> {
                isProcessing = false
                Toast.makeText(
                    context,
                    resource.messageResource?.let { context.getString(it) }?:"",
                    Toast.LENGTH_SHORT
                ).show()
            }

            is Resource.Idle -> {}
            is Resource.Loading -> {
                isProcessing = true
            }

            is Resource.Success -> {
                isProcessing = false
                memberList.remove(memberList.find { it.uid == resource.data!! })
                if (memberList.isEmpty()) {
                    viewModel.getMembersWithPaging(communityId = communityId, pageSize = 10)
                }
                viewModel.removeMemberState.value = Resource.Idle()
            }
        }
    }
    LaunchedEffect(viewModel.promoteMemberState.value) {
        when (val resource = viewModel.promoteMemberState.value) {
            is Resource.Error -> {
                isProcessing = false
                Toast.makeText(
                    context,
                    resource.messageResource?.let { context.getString(it) }?:"",
                    Toast.LENGTH_SHORT
                ).show()
            }

            is Resource.Idle -> {}
            is Resource.Loading -> {
                isProcessing = true
            }

            is Resource.Success -> {
                isProcessing = false
                val member = memberList.find { it.uid == resource.data!! }
                val index = memberList.indexOf(member)
                memberList[index] =
                    member!!.copy(rolePriority = CommunityRoles.Moderator.rolePriority)
                viewModel.promoteMemberState.value = Resource.Idle()
            }
        }
    }
    LaunchedEffect(viewModel.demoteMemberState.value) {
        when (val resource = viewModel.demoteMemberState.value) {
            is Resource.Error -> {
                isProcessing = false
                Toast.makeText(
                    context,
                    resource.messageResource?.let { context.getString(it) }?:"",
                    Toast.LENGTH_SHORT
                ).show()
            }

            is Resource.Idle -> {}
            is Resource.Loading -> {
                isProcessing = true
            }

            is Resource.Success -> {
                isProcessing = false
                val member = memberList.find { it.uid == resource.data!! }
                val index = memberList.indexOf(member)
                memberList[index] = member!!.copy(rolePriority = CommunityRoles.Member.rolePriority)
                viewModel.demoteMemberState.value = Resource.Idle()
            }
        }
    }
    LaunchedEffect(viewModel.changeAdminState.value) {
        when (val resource = viewModel.changeAdminState.value) {
            is Resource.Error -> {
                isProcessing = false
                Toast.makeText(
                    context,
                    resource.messageResource?.let { context.getString(it) }?:"",
                    Toast.LENGTH_SHORT
                ).show()
            }

            is Resource.Idle -> {}
            is Resource.Loading -> {
                isProcessing = true
            }

            is Resource.Success -> {
                isProcessing = false
                viewModel.changeAdminState.value = Resource.Idle()
                onDismiss.invoke()
            }
        }
    }

    ModalBottomSheet(
        modifier = Modifier.padding(top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()),
        onDismissRequest = onDismiss,
        sheetState = state
    ) {
        Text(
            text = stringResource(R.string.members),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(10.dp)
        )
        LaunchedEffect(members) {
            when (members) {
                is Resource.Error -> {
                    isLoading = false
                    hasError = true
                }

                is Resource.Idle -> {}
                is Resource.Loading -> {
                    isLoading = true
                }

                is Resource.Success -> {
                    isLoading = false
                    hasError = false
                    memberList.addAll(members.data!!)
                }
            }
        }

        LazyColumn(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {

            items(memberList) {
                MemberRow(
                    member = it,
                    userRole = userRole,
                    onDeleteUser = { onDeleteUser(it.uid) },
                    onAddModeratorUser = { onAddModeratorUser(it.uid) },
                    onRemoveModeratorUser = { onRemoveModeratorUser(it.uid) },
                    onMakeAdminUser = {onMakeAdminUser(it.uid)}
                )
            }
            item {
                if (isLoading) {
                    CircularWavyProgressIndicator()
                } else if (hasError) {
                    Text(
                        stringResource(R.string.something_went_wrong),
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    if (viewModel.lastMember != null && memberList.size < (numOfMembers
                            ?: Int.MAX_VALUE)
                    ) {
                        TextButton(
                            modifier = Modifier.padding(4.dp),
                            onClick = {
                                viewModel.getMembersWithPaging(
                                    communityId = communityId,
                                    pageSize = 10
                                )
                            }
                        ) {
                            Text(text = stringResource(R.string.see_more), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MemberRow(
    member: Member,
    userRole: Int,
    onDeleteUser: () -> Unit,
    onAddModeratorUser: () -> Unit,
    onRemoveModeratorUser: () -> Unit,
    onMakeAdminUser: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row {
            Box(modifier = Modifier.size(50.dp)) {
                if (member.profileImageUrl != null) {
                    GlideImage(
                        imageModel = { member.profileImageUrl.toUri() },
                        modifier = Modifier
                            .matchParentSize()
                            .clip(CircleShape),
                        requestBuilder = {
                            val context = LocalContext.current
                            val thumbnailRequest = Glide
                                .with(context)
                                .asBitmap()
                                .load(member.profileImageUrl.toUri())
                                .apply(RequestOptions().override(100))

                            Glide
                                .with(context)
                                .asBitmap()
                                .apply(
                                    RequestOptions()
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                )
                                .thumbnail(thumbnailRequest)
                                .transition(withCrossFade())
                        },
                        loading = {
                            CircularWavyProgressIndicator(
                                modifier = Modifier
                                    .matchParentSize()
                                    .align(Alignment.Center)
                            )
                        }
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
            Column(
                modifier = Modifier.padding(horizontal = 10.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = member.userName?:"",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = stringResource(getRoleNameFromRolePriority(member.rolePriority)),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .background(
                            color = when (member.rolePriority) {
                                CommunityRoles.Admin.rolePriority -> Green1
                                CommunityRoles.Moderator.rolePriority -> Orange2
                                else -> Color.Transparent
                            },
                            shape = RoundedCornerShape(3.dp)
                        )
                        .padding(2.dp)
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            when (userRole) {
                CommunityRoles.Admin.rolePriority -> {
                    when (member.rolePriority) {
                        CommunityRoles.Member.rolePriority -> {
                            IconButton(
                                onClick = onAddModeratorUser
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AddModerator,
                                    contentDescription = "add moderator"
                                )
                            }
                            IconButton(
                                onClick = onDeleteUser
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PersonRemove,
                                    tint = MaterialTheme.colorScheme.error,
                                    contentDescription = "remove"
                                )
                            }
                        }

                        CommunityRoles.Moderator.rolePriority -> {
                            IconButton(
                                onClick = onMakeAdminUser
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ArrowUpward,
                                    contentDescription = "make Admin"
                                )
                            }
                            IconButton(
                                onClick = onRemoveModeratorUser
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.RemoveModerator,
                                    contentDescription = "remove moderator"
                                )
                            }
                            IconButton(
                                onClick = onDeleteUser
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PersonRemove,
                                    tint = MaterialTheme.colorScheme.error,
                                    contentDescription = "remove"
                                )
                            }
                        }

                        else -> {}
                    }

                }

                CommunityRoles.Moderator.rolePriority -> {
                    if (member.rolePriority == CommunityRoles.Member.rolePriority) {
                        IconButton(
                            onClick = onAddModeratorUser
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AddModerator,
                                contentDescription = "remove"
                            )
                        }
                        IconButton(
                            onClick = onDeleteUser
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PersonRemove,
                                tint = MaterialTheme.colorScheme.error,
                                contentDescription = "remove"
                            )
                        }
                    }
                }
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UpdateCommunityNameOrDescriptionBottomSheet(
    isName: Boolean,
    state: SheetState,
    value: String,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    viewModel: CommunityPageViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    LaunchedEffect(viewModel.updateCommunityFieldState.value) {
        when (val resource = viewModel.updateCommunityFieldState.value) {
            is Resource.Error -> {
                isLoading = false
                Toast.makeText(context, resource.messageResource?.let { context.getString(it) }?:"", Toast.LENGTH_SHORT).show()
                viewModel.updateCommunityFieldState.value = Resource.Idle()
            }

            is Resource.Idle -> {
                isLoading = false
            }

            is Resource.Loading -> {
                isLoading = true
            }

            is Resource.Success -> {
                isLoading = false
                onDismiss()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = state
    ) {
        val focusRequester = remember { FocusRequester() }
        val characterLimit = if (isName) 40 else 400
        var text by remember { mutableStateOf(value) }
        var cannotBeEmpty by remember { mutableStateOf(false) }

        LaunchedEffect(true) {
            focusRequester.requestFocus()
        }
        TextField(
            value = text,
            trailingIcon = {
                if (isLoading) {
                    CircularWavyProgressIndicator(gapSize = TextFieldDefaults.MinHeight * 0.6f)
                } else {
                    IconButton(
                        onClick = {
                            if (isName) {
                                cannotBeEmpty = false
                                if (text.isNotBlank()) {
                                    onNameChange(text)
                                } else {
                                    cannotBeEmpty = true
                                }
                            } else {
                                onDescriptionChange(text)
                            }

                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(imageVector = Icons.Rounded.Check, contentDescription = "save")

                    }
                }
            },
            minLines = if (!isName) 3 else 1,
            isError = cannotBeEmpty,
            placeholder =
            if (!isName) {
                {
                    Text(
                        text = stringResource(R.string.community_description_placeholder),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else null,
            label = if (isName) {
                {
                    Text(
                        stringResource(R.string.change_community_name),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            } else {
                {
                    Text(
                        stringResource(R.string.change_community_description),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            },
            onValueChange = {
                cannotBeEmpty = false
                text = it
            },
            singleLine = isName,
            supportingText = {
                if (cannotBeEmpty) {
                    Text(stringResource(R.string.community_name_cannot_be_empty))
                } else {
                    Text("${text.length}/$characterLimit")
                }
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 10.dp)
                .fillMaxWidth(0.96f)
                .focusRequester(focusRequester),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent
            )
        )
    }
}

@Preview
@Composable
private fun MembersBottomSheetPreview() {
Column(Modifier.fillMaxSize()) {

}

}
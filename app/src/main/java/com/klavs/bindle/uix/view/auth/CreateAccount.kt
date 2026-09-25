package com.klavs.bindle.uix.view.auth

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.MailOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil3.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.klavs.bindle.R
import com.klavs.bindle.resource.Resource
import com.klavs.bindle.uix.viewmodel.CreateUserViewModel
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun CreateAccount(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: CreateUserViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(true) {
        viewModel.checkUniqueUsername.value = Resource.Idle()
        viewModel.checkUniqueEmail.value = Resource.Idle()
    }
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            CenterAlignedTopAppBar(colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ), navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "turn back"
                    )
                }
            },
                actions = {
                    Icon(
                        painter = painterResource(id = R.drawable.logo_no_background),
                        contentDescription = "logo",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(50.dp)
                    )
                },
                title = { Text(text = "Register") })
        }) { innerPadding ->

        val userName = rememberSaveable {
            mutableStateOf("")
        }
        val email = rememberSaveable {
            mutableStateOf("")
        }
        val emailError = remember {
            mutableStateOf<Boolean?>(null)
        }
        val userNameError = remember {
            mutableStateOf<Boolean?>(null)
        }
        val isLoading = remember {
            mutableStateOf(false)
        }
        val isError = remember {
            mutableStateOf(false)
        }
        val errorMessage = remember {
            mutableStateOf("")
        }
        val permissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionState =
                rememberPermissionState(permission = Manifest.permission.READ_MEDIA_IMAGES)
            permissionState

        } else {
            val permissionState =
                rememberPermissionState(permission = Manifest.permission.READ_EXTERNAL_STORAGE)
            permissionState
        }
        val mediaIsGranted = remember {
            mutableStateOf(
                permissionState.status.isGranted
            )
        }

        val selectedImageUri = rememberSaveable {
            mutableStateOf<Uri?>(null)
        }
        val galleryLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri.value = uri
            }
        }
        val hasLaunched = remember { mutableStateOf(false) }
        LaunchedEffect(key1 = permissionState.status.isGranted) {
            mediaIsGranted.value = permissionState.status.isGranted
            if (hasLaunched.value) {
                if (mediaIsGranted.value) {
                    galleryLauncher.launch("image/*")
                }
            } else {
                hasLaunched.value = true
            }
        }
        LaunchedEffect(key1 = true) {
            viewModel.checkUniqueUsername.value = Resource.Idle()
        }
        LaunchedEffect(key1 = viewModel.checkUniqueEmail.value) {
            when (val result = viewModel.checkUniqueEmail.value) {
                is Resource.Error -> {
                    errorMessage.value = result.messageResource?.let { context.getString(it) } ?: ""
                    isError.value = true
                    isLoading.value = false
                }

                is Resource.Loading -> {
                    isLoading.value = true

                }

                is Resource.Idle -> {}
                is Resource.Success -> {
                    isLoading.value = false
                    if (result.data!!) {
                        val encodedUri = if (selectedImageUri.value != null) {
                            Uri.encode(selectedImageUri.value?.toString())
                        } else "default"
                        val encodedEmail = Uri.encode(email.value.trim())
                        val encodedUserName = Uri.encode(userName.value.trim())
                        navController.navigate("create_user_phase_three/${encodedUri}/$encodedUserName/$encodedEmail") {
                            popUpTo("create_account") {
                                saveState = true
                            }
                        }
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                context.getString(R.string.email_already_exists)
                            )
                        }
                        emailError.value = false
                    }

                }
            }
        }
        LaunchedEffect(key1 = viewModel.checkUniqueUsername.value) {
            when (val result = viewModel.checkUniqueUsername.value) {
                is Resource.Error -> {
                    errorMessage.value = result.messageResource?.let { context.getString(it) } ?: ""
                    isError.value = true
                    isLoading.value = false
                }

                is Resource.Loading -> {
                    isLoading.value = true

                }

                is Resource.Idle -> {}
                is Resource.Success -> {
                    isLoading.value = false
                    if (result.data!!) {
                        if (email.value.isBlank()) {
                            emailError.value = true
                        } else {
                            viewModel.checkUniqueEmail(email.value.trim())
                        }

                    } else {
                        userNameError.value = false
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .padding(top = innerPadding.calculateTopPadding())
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            if (isLoading.value) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .zIndex(1f)
                    .clickable(enabled = false) {}) {
                    Box(modifier = Modifier.align(Alignment.Center)) {
                        CircularWavyProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                }
            }
            if (isError.value) {
                AlertDialog(
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = "error"
                        )
                    },
                    title = { Text(text = stringResource(R.string.error)) },
                    text = { Text(text = errorMessage.value) },
                    onDismissRequest = {
                        isError.value = false
                        navController.popBackStack()
                    },
                    confirmButton = {
                        Button(onClick = {
                            isError.value = false
                            navController.popBackStack()
                        }) {
                            Text(text = stringResource(R.string.okay))
                        }
                    },
                )
            }
            val screenHeight = LocalConfiguration.current.screenHeightDp.dp
            Column(
                Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    selectedImageUri.value?.let {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                painter = rememberAsyncImagePainter(model = it),
                                contentDescription = "picture",
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .size(screenHeight / 5)
                                    .clickable {
                                        if (mediaIsGranted.value) {
                                            galleryLauncher.launch("image/*")
                                        } else {
                                            if (!permissionState.status.shouldShowRationale) {

                                                scope.launch {
                                                    val result = snackbarHostState.showSnackbar(
                                                        message = context.getString(R.string.media_permission_rationale),
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
                                                permissionState.launchPermissionRequest()
                                            }
                                        }
                                    },
                                contentScale = ContentScale.Crop,
                            )
                            IconButton(
                                modifier = Modifier.size(IconButtonDefaults.xSmallContainerSize()),
                                onClick = { selectedImageUri.value = null }
                            ) {
                                Icon(

                                    modifier = Modifier.size(IconButtonDefaults.xSmallIconSize),
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = "remove picture",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                    } ?: Image(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "add picture",
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer),
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(screenHeight / 5)
                            .clickable {
                                if (mediaIsGranted.value) {
                                    galleryLauncher.launch("image/*")
                                } else {
                                    if (!permissionState.status.shouldShowRationale) {

                                        scope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                message = context.getString(R.string.media_permission_rationale),
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
                                        permissionState.launchPermissionRequest()
                                    }
                                }
                            }
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                CircleShape
                            ),
                        contentScale = ContentScale.Crop
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(modifier = Modifier.padding(10.dp))
                        RegisterTextField(
                            hasError = emailError.value,
                            keyboardType = KeyboardType.Email,
                            value = email.value,
                            onValueChange = {
                                email.value = it
                                emailError.value = null
                            },
                            placeholder = stringResource(R.string.email),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.MailOutline,
                                    contentDescription = "email"
                                )
                            })
                        Spacer(modifier = Modifier.padding(10.dp))

                        RegisterTextField(
                            hasError = userNameError.value,
                            value = userName.value,
                            onValueChange = {
                                userName.value = it
                                userNameError.value = null
                            },
                            placeholder = stringResource(R.string.username)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "name"
                            )
                        }
                    }
                    Spacer(modifier = Modifier.padding(10.dp))
                    Row(Modifier.fillMaxWidth(0.9f), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = {
                            if (userName.value.isBlank()) {
                                userNameError.value = true
                            } else {
                                viewModel.checkUniqueUsername(username = userName.value.trim())
                            }
                        }) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(text = stringResource(R.string.next))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.NavigateNext,
                                    contentDescription = ""
                                )
                            }
                        }
                    }
                }


            }
        }
    }
}

@Composable
fun RegisterTextField(
    keyboardType: KeyboardType = KeyboardType.Text,
    hasError: Boolean? = null,  //true: empty, false: already taken
    value: String,
    onValueChange: (String) -> Unit, placeholder: String = "",
    trailingIcon: @Composable () -> Unit = {}
) {
    val screenWith = LocalConfiguration.current.screenWidthDp.dp
    TextField(
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        isError = hasError?.let { true } ?: false,
        modifier = Modifier.width(screenWith / 1.2f),
        singleLine = true,
        supportingText = hasError?.let {
            {
                Text(
                    text = if (!hasError) stringResource(R.string.belongs_to_another_user) else stringResource(
                        R.string.field_is_required
                    ),
                    color = TextFieldDefaults.colors().errorIndicatorColor
                )
            }
        },
        value = value,
        onValueChange = { onValueChange(it) },
        leadingIcon = trailingIcon,
        label = { Text(text = placeholder) },
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(10.dp)
    )
}

@Preview
@Composable
fun CreateAccountPreview() {
    //CreateAccount(navController = rememberNavController(), true)
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CreateAccount(navController = rememberNavController())
    }

}
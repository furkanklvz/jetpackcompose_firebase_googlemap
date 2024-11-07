package com.klavs.bindle.uix.view.auth

import android.net.Uri
import android.os.Build
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
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
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
import com.klavs.bindle.R
import com.klavs.bindle.resource.Resource
import com.klavs.bindle.uix.view.loading.LoadingAnimation
import com.klavs.bindle.uix.viewmodel.CreateUserViewModel

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun CreateAccount(navController: NavHostController, isTest: Boolean = false) {
    Scaffold(topBar = {
        CenterAlignedTopAppBar(colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ), navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    painter = painterResource(id = R.drawable.rounded_arrow_back_24),
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
        val viewModel: CreateUserViewModel = hiltViewModel()
        val realName = rememberSaveable {
            mutableStateOf("")
        }
        val userName = rememberSaveable {
            mutableStateOf("")
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
                rememberPermissionState(permission = android.Manifest.permission.READ_MEDIA_IMAGES)
            permissionState

        } else {
            val permissionState =
                rememberPermissionState(permission = android.Manifest.permission.READ_EXTERNAL_STORAGE)
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
        val haslaunched = remember { mutableStateOf(false) }
        LaunchedEffect(key1 = permissionState.status.isGranted) {
            mediaIsGranted.value = permissionState.status.isGranted
            if (haslaunched.value) {
                if (mediaIsGranted.value) {
                    galleryLauncher.launch("image/*")
                }
            } else {
                haslaunched.value = true
            }
        }
        LaunchedEffect(key1 = true) {
            viewModel.checkUniqueUsername.value = Resource.Idle()
        }
        LaunchedEffect(key1 = viewModel.checkUniqueUsername.value) {
            when (val result = viewModel.checkUniqueUsername.value) {
                is Resource.Error -> {
                    errorMessage.value = result.message!!
                    isError.value = true
                    isLoading.value = false
                }

                is Resource.Loading -> {
                    isLoading.value = true

                }

                is Resource.Idle -> {}
                is Resource.Success -> {
                    val encodedUri = if (selectedImageUri.value != null) {
                        Uri.encode(selectedImageUri.value.toString())
                    } else "default"
                    isLoading.value = false
                    if (result.data!!) {
                        navController.navigate("create_user_phase_two/${encodedUri}/${realName.value.trim()}/${userName.value.trim()}") {
                            popUpTo("create_account") {
                                saveState = true
                            }
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
                        LoadingAnimation(size = 250.dp)
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
                    title = { Text(text = "Error") },
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
                            Text(text = "Ok")
                        }
                    },
                )
            }
            val screenWith = LocalConfiguration.current.screenWidthDp.dp
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
                                        permissionState.launchPermissionRequest()
                                        if (mediaIsGranted.value) {
                                            galleryLauncher.launch("image/*")
                                        }
                                    }
                                },
                            contentScale = ContentScale.Crop,
                        )
                    } ?: Image(
                        painter = painterResource(id = R.drawable.default_profile),
                        contentDescription = "add picture",
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer),
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(screenHeight / 5)
                            .clickable {
                                if (mediaIsGranted.value) {
                                    galleryLauncher.launch("image/*")
                                } else {
                                    permissionState.launchPermissionRequest()
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
                        Text(
                            text = "What is your name?",
                            fontSize = MaterialTheme.typography.titleLarge.fontSize
                        )
                        Spacer(modifier = Modifier.padding(15.dp))
                        RegisterTextField(
                            value = realName.value,
                            onValueChange = { realName.value = it },
                            placeholder = "Real name (Optional)"
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.rounded_person_24),
                                contentDescription = "name"
                            )
                        }
                        Spacer(modifier = Modifier.padding(10.dp))

                        RegisterTextField(
                            hasError = userNameError.value,
                            value = userName.value,
                            onValueChange = {
                                userName.value = it
                                userNameError.value = null
                            },
                            placeholder = "User name"
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.rounded_person_24),
                                contentDescription = "name"
                            )
                        }
                    }
                    Spacer(modifier = Modifier.padding(10.dp))
                    Row(Modifier.fillMaxWidth(0.9f), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = {
                            if (userName.value.isEmpty()) {
                                userNameError.value = true
                            } else {
                                viewModel.checkUniqueUsername(username = userName.value.trim())
                            }
                        }) {
                            Row(
                                modifier = Modifier.width(150.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Next")
                                Icon(
                                    painter = painterResource(id = R.drawable.rounded_keyboard_arrow_right_24),
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
                    text = if (!hasError) "This value is already taken" else "This field is required",
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
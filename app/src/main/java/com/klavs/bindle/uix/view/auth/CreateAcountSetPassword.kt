package com.klavs.bindle.uix.view.auth

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.klavs.bindle.R
import com.klavs.bindle.data.entity.BottomNavItem
import com.klavs.bindle.resource.Resource
import com.klavs.bindle.uix.view.loading.LoadingAnimation
import com.klavs.bindle.uix.viewmodel.CreateUserViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CreateUserSetPasword(
    navController: NavHostController,
    profilePictureUri: String,
    realName: String,
    userName: String,
    email: String,
    phoneNumber: String,
    gender: String,
    birthDay: Long,
    viewModel: CreateUserViewModel = hiltViewModel()
) {
    Scaffold(topBar = {
        CenterAlignedTopAppBar(
            actions = {
                Icon(
                    painter = painterResource(id = R.drawable.logo_no_background),
                    contentDescription = "logo",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(50.dp)
                )
            }, navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "turn back"
                    )
                }
            }, title = { Text(text = "Set a password") })
    }) { innerPadding ->
        val password = remember {
            mutableStateOf("")
        }
        val passwordError = remember { mutableStateOf(true) }
        val passwordAgain = remember {
            mutableStateOf("")
        }
        val passwordDifferent = remember { mutableStateOf(true) }

        val isLoading = remember {
            mutableStateOf(false)
        }
        val isError = remember {
            mutableStateOf(false)
        }
        val errorMessage = remember {
            mutableStateOf("")
        }
        LaunchedEffect(key1 = viewModel.registerResponse.value) {
            when (val result = viewModel.registerResponse.value) {
                is Resource.Error -> {
                    isError.value = true
                    errorMessage.value = result.message ?: "unknown error"
                    isLoading.value = false
                }

                is Resource.Loading -> {
                    isLoading.value = true

                }

                is Resource.Idle -> {}
                is Resource.Success -> {
                    isLoading.value = false
                    navController.navigate(BottomNavItem.Home.route) {
                        popUpTo(navController.currentDestination?.route ?: "") {
                            inclusive = true
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            if (isLoading.value){
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent).zIndex(1f).clickable(enabled = false){}) {
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
                    onDismissRequest = { isError.value = false },
                    confirmButton = {
                        Button(onClick = { isError.value = false }) {
                            Text(text = "Ok")
                        }
                    },
                )
            }
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(35.dp))
                PasswordTextField(
                    error = passwordError.value,
                    value = password.value,
                    onValueChange = {
                        password.value = it
                        password.value = password.value.replace(" ", "")
                        if (password.value.length < 6) {
                            passwordError.value = true
                        } else {
                            passwordError.value = false
                        }
                    },
                    label = "Password"
                )
                Spacer(modifier = Modifier.height(45.dp))
                PasswordAgainTextField(
                    isPasswordValid = !passwordError.value,
                    passwordsDifferent = passwordDifferent.value,
                    value = passwordAgain.value,
                    onValueChange = {
                        passwordAgain.value = it
                        passwordAgain.value = passwordAgain.value.replace(" ", "")
                        if (password.value != passwordAgain.value) {
                            passwordDifferent.value = true
                        } else {
                            passwordDifferent.value = false
                        }
                    },
                    label = "Password (Again)"
                )
                Spacer(modifier = Modifier.height(45.dp))

                Row(Modifier.fillMaxWidth(0.9f), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = {
                        if (!passwordError.value && !passwordDifferent.value) {
                            val userInfos = hashMapOf<String, Any?>(
                                "userName" to userName,
                                "realName" to realName,
                                "email" to email,
                                "phoneNumber" to phoneNumber,
                                "profilePictureUrl" to profilePictureUri,
                                "password" to password.value,
                                "gender" to gender,
                                "birthDay" to birthDay
                            )
                            viewModel.registerUser(userInfos = userInfos)
                        }
                    }) {
                        Row(
                            modifier = Modifier.width(150.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Register")
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

@Composable
fun PasswordTextField(
    iconIsEnable: Boolean = true,
    isResettingPassword: Boolean = false,
    error: Boolean = false,
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    val screenWith = LocalConfiguration.current.screenWidthDp.dp
    val isVisible = remember {
        mutableStateOf(false)
    }
    val visualTransformation = remember {
        mutableStateOf(VisualTransformation.None)
    }
    LaunchedEffect(key1 = isVisible.value) {
        if (isVisible.value) {
            visualTransformation.value = VisualTransformation.None
        } else {
            visualTransformation.value = PasswordVisualTransformation()
        }
    }


    TextField(
        leadingIcon = if (iconIsEnable) {
            { Icon(imageVector = Icons.Rounded.Key, contentDescription = "password") }
        } else null,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        visualTransformation = visualTransformation.value,
        supportingText = if (isResettingPassword) null else {
            {
                if (error) {
                    Text(
                        text = "Your password must be at least 6 characters",
                        color = TextFieldDefaults.colors().errorIndicatorColor
                    )
                } else {
                    Text(
                        text = "Valid"
                    )
                }
            }
        },
        trailingIcon =
        {
            Icon(painter = painterResource(id = if (isVisible.value) R.drawable.rounded_visibility_24 else R.drawable.rounded_visibility_off_24),
                contentDescription = "visibility",
                modifier = Modifier.clickable { isVisible.value = !isVisible.value })
        },
        modifier = Modifier.width(screenWith / 1.2f),
        shape = RoundedCornerShape(10.dp),
        value = value,
        onValueChange = { onValueChange(it) },
        label = { Text(text = label) }
    )
}

@Composable
fun PasswordAgainTextField(
    iconIsEnable: Boolean = true,
    isPasswordValid: Boolean,
    passwordsDifferent: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    val screenWith = LocalConfiguration.current.screenWidthDp.dp
    val isVisible = remember {
        mutableStateOf(false)
    }
    val visualTransformation = remember {
        mutableStateOf(VisualTransformation.None)
    }
    LaunchedEffect(key1 = isVisible.value) {
        if (isVisible.value) {
            visualTransformation.value = VisualTransformation.None
        } else {
            visualTransformation.value = PasswordVisualTransformation()
        }
    }


    TextField(
        leadingIcon = if (iconIsEnable) {
            { Icon(imageVector = Icons.Rounded.Key, contentDescription = "password") }
        } else null,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        visualTransformation = visualTransformation.value,
        supportingText = {
            if (isPasswordValid) {
                if (passwordsDifferent) {
                    Text(
                        text = "Passwords are different",
                        color = TextFieldDefaults.colors().errorIndicatorColor
                    )
                } else {
                    Text(
                        text = "Passwords are same"
                    )
                }
            }
        },
        trailingIcon =
        {
            Icon(painter = painterResource(id = if (isVisible.value) R.drawable.rounded_visibility_24 else R.drawable.rounded_visibility_off_24),
                contentDescription = "visibility",
                modifier = Modifier.clickable { isVisible.value = !isVisible.value })
        },

        modifier = Modifier.width(screenWith / 1.2f),
        shape = RoundedCornerShape(10.dp),
        value = value,
        onValueChange = { onValueChange(it) },
        label = { Text(text = label) }
    )
}


@Preview
@Composable
fun CreateUserSetPaswordPreview() {
    CreateUserSetPasword(
        navController = rememberNavController(),
        profilePictureUri = "deafult",
        realName = "Furkan Kılavuz",
        userName = "furkanklvz",
        email = "mail",
        phoneNumber = "555",
        gender = "male",
        birthDay = 0L
    )
}
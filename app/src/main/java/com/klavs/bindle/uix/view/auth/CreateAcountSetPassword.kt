package com.klavs.bindle.uix.view.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.klavs.bindle.R
import com.klavs.bindle.data.entity.User
import com.klavs.bindle.resource.Resource
import com.klavs.bindle.uix.viewmodel.CreateUserViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CreateUserSetPassword(
    navController: NavHostController,
    profilePictureUri: String,
    userName: String,
    email: String,
    viewModel: CreateUserViewModel = hiltViewModel()
) {
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
    LaunchedEffect(key1 = viewModel.registerResponse.value) {
        when (val result = viewModel.registerResponse.value) {
            is Resource.Error -> {
                isError = true
                errorMessage = result.messageResource?.let { context.getString(it) } ?: ""
                isLoading = false
            }

            is Resource.Loading -> {
                isLoading = true

            }

            is Resource.Idle -> {}
            is Resource.Success -> {
                isLoading = false
                navController.navigate("greeting") {
                    popUpTo(0) {
                        inclusive = true
                    }
                }
            }
        }
    }
    CreateUserSetPasswordContent(
        onBackPressed = { navController.popBackStack() },
        isLoading = isLoading,
        isError = isError,
        errorMessage = errorMessage,
        onRegisterClick = {

            val user = User(
                userName = userName,
                email = email,
                profilePictureUrl = profilePictureUri,
                password = it,
                acceptedTermsAndPrivacyPolicy = true,
            )
            viewModel.registerUser(user = user)

        },
        closeErrorDialog = { isError = false }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CreateUserSetPasswordContent(
    onBackPressed: () -> Unit,
    onRegisterClick: (String) -> Unit,
    isLoading: Boolean = false,
    isError: Boolean = false,
    closeErrorDialog: () -> Unit,
    errorMessage: String = "",
) {
    var password by remember {
        mutableStateOf("")
    }
    var passwordError by remember { mutableStateOf(true) }
    var termsOfServiceExpanded by remember { mutableStateOf(false) }
    var privacyPolicyExpanded by remember { mutableStateOf(false) }
    var passwordAgain by remember {
        mutableStateOf("")
    }
    var passwordDifferent by remember { mutableStateOf(true) }
    var termsOfServiceChecked by remember { mutableStateOf(false) }
    var privacyPolicyChecked by remember { mutableStateOf(false) }

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
                IconButton(onClick = onBackPressed) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "turn back"
                    )
                }
            }, title = { Text(text = stringResource(R.string.set_a_password)) })
    }) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            if (termsOfServiceExpanded) {
                AlertDialog(
                    icon = {
                        Icon(imageVector = Icons.Rounded.PrivacyTip, contentDescription = "")
                    },
                    onDismissRequest = { termsOfServiceExpanded = false },
                    dismissButton = {
                        IconButton(
                            onClick = { termsOfServiceExpanded = false }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "dismiss"
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                termsOfServiceChecked = true
                                termsOfServiceExpanded = false
                            }
                        ) {
                            Text(stringResource(R.string.accept))
                        }
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.terms_of_service_label)
                        )


                    },
                    text =
                    {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight(0.8f)
                                .verticalScroll(
                                    rememberScrollState()
                                )
                        ) {
                            Text(
                                text = stringResource(R.string.terms_of_service)
                            )
                        }

                    }
                )
            }
            if (privacyPolicyExpanded) {
                AlertDialog(
                    icon = {
                        Icon(imageVector = Icons.Rounded.PrivacyTip, contentDescription = "")
                    },
                    onDismissRequest = { privacyPolicyExpanded = false },
                    dismissButton = {
                        IconButton(
                            onClick = { privacyPolicyExpanded = false }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "dismiss"
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                privacyPolicyChecked = true
                                privacyPolicyExpanded = false
                            }
                        ) {
                            Text(stringResource(R.string.accept))
                        }
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.privacy_policy_label)
                        )


                    },
                    text =
                    {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight(0.8f)
                                .verticalScroll(
                                    rememberScrollState()
                                )
                        ) {
                            Text(
                                text = stringResource(R.string.privacy_policy)
                            )
                        }

                    }
                )
            }
            if (isLoading) {
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
            if (isError) {
                AlertDialog(
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = "error"
                        )
                    },
                    title = { Text(text = stringResource(R.string.error)) },
                    text = { Text(text = errorMessage) },
                    onDismissRequest = closeErrorDialog,
                    confirmButton = {
                        Button(onClick = closeErrorDialog) {
                            Text(text = stringResource(R.string.okay))
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
                    error = passwordError,
                    value = password,
                    onValueChange = {
                        password = it
                        password = password.replace(" ", "")
                        passwordError = password.length < 6
                    },
                    label = stringResource(R.string.password)
                )
                Spacer(modifier = Modifier.height(45.dp))
                PasswordAgainTextField(
                    isPasswordValid = !passwordError,
                    passwordsDifferent = passwordDifferent,
                    value = passwordAgain,
                    onValueChange = {
                        passwordAgain = it
                        passwordAgain = passwordAgain.replace(" ", "")
                        passwordDifferent = password != passwordAgain
                    },
                    label = stringResource(R.string.password_again)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Column(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .align(Alignment.Start)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = termsOfServiceChecked,
                            onCheckedChange = {termsOfServiceChecked = it}
                        )
                        val termsOfServiceText = buildAnnotatedString {
                            append(stringResource(R.string.read_and_accept_terms_of_service_append_one))
                            withStyle(
                                style = SpanStyle(
                                    color = ButtonDefaults.textButtonColors().contentColor,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append(stringResource(R.string.terms_of_service_label))
                            }
                            append(stringResource(R.string.read_and_accept_terms_of_service_append_two))
                        }
                        Text(
                            termsOfServiceText,
                            modifier = Modifier.clickable {
                                termsOfServiceExpanded = true
                            },
                            style = MaterialTheme.typography.bodySmall
                        )


                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = privacyPolicyChecked,
                            onCheckedChange = {privacyPolicyChecked = it}
                        )
                        val privacyPolicyText = buildAnnotatedString {
                            append(stringResource(R.string.read_and_accept_privacy_policy_append_one))
                            withStyle(
                                style = SpanStyle(
                                    color = ButtonDefaults.textButtonColors().contentColor,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append(stringResource(R.string.privacy_policy_label))
                            }
                            append(stringResource(R.string.read_and_accept_privacy_policy_append_two))
                        }
                        Text(
                            privacyPolicyText,
                            modifier = Modifier.clickable {
                                privacyPolicyExpanded = true
                            },
                            style = MaterialTheme.typography.bodySmall
                        )


                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    Modifier.fillMaxWidth(0.9f),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = {
                        if (!passwordError && !passwordDifferent) {
                            if (termsOfServiceChecked && privacyPolicyChecked) {
                                onRegisterClick(password)
                            }
                        }
                    }) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(text = stringResource(R.string.register))
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                                contentDescription = "",
                                modifier = Modifier.size(IconButtonDefaults.xSmallIconSize)
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
                        text = stringResource(R.string.password_must_be_at_least_6_characters),
                        color = TextFieldDefaults.colors().errorIndicatorColor
                    )
                } else {
                    Text(
                        text = stringResource(R.string.valid)
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
                        text = stringResource(R.string.passwords_are_different),
                        color = TextFieldDefaults.colors().errorIndicatorColor
                    )
                } else {
                    Text(
                        text = stringResource(R.string.passwords_are_same)
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


@Preview(locale = "tr", showSystemUi = true)
@Composable
fun CreateUserSetPasswordPreview() {
    CreateUserSetPasswordContent(
        onBackPressed = {},
        onRegisterClick = {},
        isLoading = false,
        isError = false,
        closeErrorDialog = {},
        errorMessage = ""
    )
}
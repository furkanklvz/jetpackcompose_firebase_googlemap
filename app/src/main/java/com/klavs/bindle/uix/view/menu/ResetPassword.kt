package com.klavs.bindle.uix.view.menu

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.klavs.bindle.resource.Resource
import com.klavs.bindle.uix.view.auth.PasswordAgainTextField
import com.klavs.bindle.uix.view.auth.PasswordTextField
import com.klavs.bindle.uix.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPassword(navController: NavHostController, viewModel: ProfileViewModel = hiltViewModel()) {
    val currentPassword = remember {
        mutableStateOf("")
    }
    val newPassword = remember {
        mutableStateOf("")
    }
    val newPasswordAgain = remember {
        mutableStateOf("")
    }
    val passwordIsValid = remember {
        mutableStateOf(false)
    }
    val forgotPasswordDialogIsEnable = remember {
        mutableStateOf(false)
    }
    val currentPasswordIsWrong = remember {
        mutableStateOf(false)
    }
    val error = remember {
        mutableStateOf(false)
    }
    val errorMessage = remember {
        mutableStateOf("")
    }
    val emailSent = remember {
        mutableStateOf(false)
    }
    Scaffold(topBar = {
        CenterAlignedTopAppBar(navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "turn back"
                )
            }
        }, title = { Text(text = "Reset Your Password") })
    }) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding() + 20.dp)
        ) {
            LaunchedEffect(key1 = viewModel.resetPasswordState.value) {
                when (val resource = viewModel.resetPasswordState.value) {
                    is Resource.Error -> {
                        error.value = true
                        errorMessage.value = resource.message!!
                    }

                    is Resource.Idle -> {}
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        if (resource.data!!) {
                            Toast.makeText(
                                navController.context,
                                "Password reset successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.popBackStack()
                        } else {
                            currentPasswordIsWrong.value = true
                        }
                    }
                }
            }
            if (error.value) {
                AlertDialog(
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.WarningAmber,
                            contentDescription = "error"
                        )
                    },
                    title = { Text(text = "Error") },
                    text = { Text(text = errorMessage.value) },
                    onDismissRequest = { error.value = false },
                    confirmButton = {
                        Button(onClick = { error.value = false }) {
                            Text(text = "Okay")
                        }
                    })
            }
            if (currentPasswordIsWrong.value) {
                AlertDialog(
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.WarningAmber,
                            contentDescription = "error"
                        )
                    },
                    title = { Text(text = "Wrong Password") },
                    text = { Text(text = "Please check your current password and try again") },
                    onDismissRequest = { currentPasswordIsWrong.value = false },
                    confirmButton = {
                        Button(onClick = { currentPasswordIsWrong.value = false }) {
                            Text(text = "Okay")
                        }
                    })
            }

            if (forgotPasswordDialogIsEnable.value) {
                AlertDialog(
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.LockReset,
                            contentDescription = "reset password"
                        )
                    },
                    title = { Text(text = "Reset password") },
                    text = {
                        Column(
                            Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "We will send a link to your e-mail address to reset your password")
                        }

                    },
                    onDismissRequest = { forgotPasswordDialogIsEnable.value = false },
                    confirmButton = {

                        Button(enabled = if (emailSent.value) false else true, onClick = {
                            viewModel.sendResetPasswordEmail()
                            emailSent.value = true
                        }) {
                            Text(text = if (emailSent.value) "Link sent. Please check your e-mail" else "Send link to reset the password")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { forgotPasswordDialogIsEnable.value = false }) {
                            Text(text = "Cancel")
                        }
                    })
            }
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                PasswordTextField(
                    iconIsEnable = false,
                    isResettingPassword = true,
                    value = currentPassword.value,
                    onValueChange = { currentPassword.value = it },
                    label = "Current Password"
                )
                Spacer(modifier = Modifier.height(40.dp))
                PasswordTextField(
                    iconIsEnable = false,
                    error = !passwordIsValid.value,
                    value = newPassword.value,
                    onValueChange = {
                        newPassword.value = it
                        if (newPassword.value.length < 6) {
                            passwordIsValid.value = false
                        } else {
                            passwordIsValid.value = true
                        }
                    },
                    label = "New Password"
                )
                Spacer(modifier = Modifier.height(20.dp))

                PasswordAgainTextField(
                    iconIsEnable = false,
                    isPasswordValid = passwordIsValid.value,
                    value = newPasswordAgain.value,
                    passwordsDifferent = newPassword.value != newPasswordAgain.value,
                    onValueChange = { newPasswordAgain.value = it },
                    label = "New Password (Again)"
                )
                Row(Modifier.fillMaxWidth(0.9f), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { forgotPasswordDialogIsEnable.value = true }) {
                        Text(text = "Forgot password?")
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))

                OutlinedButton(onClick = {
                    if (passwordIsValid.value && newPassword.value == newPasswordAgain.value && viewModel.resetPasswordState.value !is Resource.Loading) {
                        viewModel.resetPassword(
                            currentPassword = currentPassword.value,
                            newPassword.value
                        )
                    }
                })
                {
                    Text(text = "Change your password")
                }
            }
        }

    }
}

@Preview
@Composable
private fun ResetPasswordPreview() {
    ResetPassword(navController = rememberNavController())
}
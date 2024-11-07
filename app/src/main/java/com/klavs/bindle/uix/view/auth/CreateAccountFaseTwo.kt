package com.klavs.bindle.uix.view.auth

import android.net.Uri
import android.util.Log
import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.klavs.bindle.R
import com.klavs.bindle.resource.Resource
import com.klavs.bindle.uix.view.loading.LoadingAnimation
import com.klavs.bindle.uix.viewmodel.CreateUserViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CreateUserPhaseTwo(
    navController: NavHostController,
    profilePictureUri: String,
    realName: String,
    userName: String
) {
    Scaffold(topBar = {
        CenterAlignedTopAppBar(colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ), title = { Text(text = "Contact Information") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.rounded_arrow_back_24),
                        contentDescription = "back"
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
            })
    }) { innerPadding ->
        val isLoading = remember {
            mutableStateOf(false)
        }
        val isError = remember {
            mutableStateOf(false)
        }
        val errorMessage = remember {
            mutableStateOf("")
        }
        val email = rememberSaveable {
            mutableStateOf("")
        }
        val emailError = remember {
            mutableStateOf<Boolean?>(null)
        }
        val phoneNumber = rememberSaveable {
            mutableStateOf("")
        }
        val options = listOf("Male", "Female", "Other", "Prefer not to say")
        val gender = rememberSaveable {
            mutableStateOf(options.last())
        }
        val showDatePicker = remember { mutableStateOf(false) }
        val datePickerState = rememberDatePickerState(
            yearRange = 1920..LocalDate.now().year - 15,
            initialSelectedDateMillis = 946684800000L
        )
        val selectedDate = datePickerState.selectedDateMillis?.let {
            convertMillisToDate(it)
        } ?: ""
        val viewModel: CreateUserViewModel = hiltViewModel()
        LaunchedEffect(key1 = true) {
            viewModel.checkUniqueEmail.value = Resource.Idle()
        }
        LaunchedEffect(key1 = viewModel.checkUniqueEmail.value) {
            when (val result = viewModel.checkUniqueEmail.value) {
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
                    isLoading.value = false
                    val encodedUri = if (profilePictureUri.equals("default")) "default" else Uri.encode(profilePictureUri)
                    if (result.data!!) {
                        navController.navigate("create_user_phase_three/$encodedUri/$realName/$userName/${email.value.trim()}/${phoneNumber.value.trim()}/${gender.value}/${datePickerState.selectedDateMillis ?: 0L}")

                    } else {
                        Toast.makeText(navController.context, "already taken", Toast.LENGTH_SHORT)
                            .show()
                        emailError.value = false
                    }

                }
            }
        }
        val screenWith = LocalConfiguration.current.screenWidthDp.dp
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainer)
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
            if (showDatePicker.value) {
                Popup(
                    onDismissRequest = { showDatePicker.value = false },
                    alignment = Alignment.Center,
                    properties = PopupProperties(
                        dismissOnBackPress = true
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .shadow(elevation = 4.dp)
                            .background(Color.Transparent)
                            .zIndex(1f)
                    ) {
                        DatePicker(
                            state = datePickerState,
                            showModeToggle = false
                        )
                    }
                }
            }
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RegisterTextField(
                    hasError = emailError.value,
                    keyboardType = KeyboardType.Email,
                    value = email.value,
                    onValueChange = {
                        email.value = it
                        emailError.value = null
                    },
                    placeholder = "E-Mail",
                    trailingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.mail),
                            contentDescription = "email"
                        )
                    })
                Spacer(modifier = Modifier.height(15.dp))
                RegisterTextField(
                    keyboardType = KeyboardType.Phone,
                    value = phoneNumber.value,
                    onValueChange = { phoneNumber.value = it },
                    placeholder = "Phone Number (Optional)",
                    trailingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.phone),
                            contentDescription = "phone number"
                        )
                    })
                Spacer(modifier = Modifier.height(15.dp))

                GenderSpinner(options = options,onValueChange = {gender.value = it
                                                                Log.e( "gender",gender.value)}, leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.gender),
                        contentDescription = "gender"
                    )
                })
                Spacer(modifier = Modifier.height(15.dp))

                DateTextField(
                    selectedDate = selectedDate,
                    onClick = { showDatePicker.value = !showDatePicker.value },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.calendar),
                            contentDescription = "select date"
                        )
                    }
                )
                Spacer(modifier = Modifier.height(15.dp))

                Row(Modifier.fillMaxWidth(0.9f), horizontalArrangement = Arrangement.End) {
                    OutlinedButton({
                        if (email.value.isEmpty()) {
                            emailError.value = true
                        } else {
                            viewModel.checkUniqueEmail(email.value.trim())
                        }
                    }) {
                        Row(
                            modifier = Modifier.width(150.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Next")
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
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
fun DateTextField(
    selectedDate: String,
    onClick: () -> Unit,
    trailingIcon: @Composable () -> Unit = {}
) {
    val screenWith = LocalConfiguration.current.screenWidthDp.dp
    TextField(
        enabled = false,
        readOnly = true,
        modifier = Modifier
            .width(screenWith / 1.2f)
            .clickable { onClick() },
        singleLine = true,
        value = if (selectedDate.isEmpty()) "01/01/2000" else selectedDate,
        onValueChange = { },
        trailingIcon = trailingIcon,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            disabledTextColor = TextFieldDefaults.colors().focusedTextColor,
            disabledLabelColor = TextFieldDefaults.colors().focusedLabelColor,
            disabledTrailingIconColor = TextFieldDefaults.colors().focusedTrailingIconColor,
            disabledContainerColor = TextFieldDefaults.colors().focusedContainerColor
        ),
        shape = RoundedCornerShape(10.dp),
        label = { Text(text = "Birth Day") }
    )
}

@Composable
fun GenderSpinner(
    options: List<String>,
    onValueChange: (String) -> Unit,
    leadingIcon: @Composable () -> Unit = {}
) {

    val expanded = remember { mutableStateOf(false) }
    val selectedOptionText = remember { mutableStateOf(options.last()) }
    val screenWith = LocalConfiguration.current.screenWidthDp.dp
    Column {
        TextField(
            enabled = false,
            readOnly = true,
            modifier = Modifier
                .width(screenWith / 1.2f)
                .clickable { expanded.value = !expanded.value },
            singleLine = true,
            value = selectedOptionText.value,
            onValueChange = { },
            leadingIcon = leadingIcon,
            trailingIcon = {
                Icon(imageVector = Icons.Filled.KeyboardArrowDown, contentDescription = "")
            },
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                disabledTextColor = TextFieldDefaults.colors().focusedTextColor,
                disabledLabelColor = TextFieldDefaults.colors().focusedLabelColor,
                disabledLeadingIconColor = TextFieldDefaults.colors().focusedLeadingIconColor,
                disabledContainerColor = TextFieldDefaults.colors().focusedContainerColor
            ),
            shape = RoundedCornerShape(10.dp),
            label = { Text(text = "Gender") }
        )
        DropdownMenu(
            modifier = Modifier.width(screenWith / 1.2f),
            shape = RoundedCornerShape(10.dp),
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false }
        ) {
            options.forEach {
                DropdownMenuItem(
                    text = { Text(text = it) },
                    onClick = {
                        onValueChange(it)
                        selectedOptionText.value = it
                        expanded.value = false
                    })
            }
        }
    }

}

fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}

@Preview
@Composable
fun CreateUserFaseTwoPreview() {
    CreateUserPhaseTwo(
        profilePictureUri = "deafult",
        navController = rememberNavController(),
        realName = "Furkan K",
        userName = "klavs"
    )
}
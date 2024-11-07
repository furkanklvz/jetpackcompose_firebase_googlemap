package com.klavs.bindle.uix.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.klavs.bindle.data.entity.BottomNavItem
import com.klavs.bindle.uix.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(navController: NavHostController,viewModel: HomeViewModel = hiltViewModel()) {
    val currentUser by viewModel.currentUser.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text(text = BottomNavItem.Home.label) }) }) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            Column(
                modifier = Modifier.align(
                    Alignment.Center
                )
            ) {
                Text(
                    text = if (currentUser == null) "Signed out" else "Hello ${currentUser!!.displayName}",
                    fontSize = MaterialTheme.typography.titleLarge.fontSize
                )
            }

        }

    }
}

@Preview
@Composable
fun HomePreview() {
    Home(navController = rememberNavController())
}

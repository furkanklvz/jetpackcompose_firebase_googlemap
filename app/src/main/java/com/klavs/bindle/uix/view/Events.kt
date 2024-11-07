package com.klavs.bindle.uix.view

import android.annotation.SuppressLint
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.klavs.bindle.data.entity.BottomNavItem

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Events(navController: NavHostController){
    Scaffold(topBar = { TopAppBar(title = { Text(text = BottomNavItem.Events.label) }) }) {

    }
}
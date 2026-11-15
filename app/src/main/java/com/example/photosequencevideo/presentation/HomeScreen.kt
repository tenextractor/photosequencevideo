package com.example.photosequencevideo.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.photosequencevideo.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onNavToCreateVideo: () -> Unit, onNavToMyVideos: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.app_name)) })
        }
    ) {
        innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            ListItem(
                headlineContent = { Text("Create video") },
                modifier = Modifier.clickable(onClick = onNavToCreateVideo)
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text("Credits") },
                modifier = Modifier.clickable(onClick = onNavToMyVideos)
            )
        }
    }
}
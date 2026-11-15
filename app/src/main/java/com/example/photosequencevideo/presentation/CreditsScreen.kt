package com.example.photosequencevideo.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditsScreen() {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Credits") })
        }
    ) {
        innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            ListItem(
                headlineContent = { Text("Music credits") },
                supportingContent = { Text("Blippy Trance - Kevin MacLeod (incompetech.com)\n" +
                        "Licensed under Creative Commons: By Attribution 4.0\n" +
                        "https://creativecommons.org/licenses/by/4.0/") }
            )
        }
    }
}
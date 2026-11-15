package com.example.photosequencevideo.presentation

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateVideoScreen() {
    val context = LocalContext.current
    val selectedPhotos = remember { mutableStateListOf<Uri>() }

    val pickMultipleMedia =
        rememberLauncherForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia(5)) { uris ->
            when {
                uris.isEmpty() -> Unit
                uris.size < 3 -> {
                    selectedPhotos.clear()
                    Toast.makeText(
                        context, "Selection failed. Please select at least 3 photos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    selectedPhotos.clear()
                    selectedPhotos.addAll(uris)
                    Toast.makeText(
                        context, "Photos selected successfully!", Toast.LENGTH_SHORT
                    ).show()
                    Log.i("selected pictures", "$selectedPhotos")
                }
            }
        }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Create video") })
        }
    ) {
        innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            ListItem(
                headlineContent = { Text("Select photos") },
                supportingContent = { Text(
                    if (selectedPhotos.isEmpty()) "3 to 5 photos required"
                    else "Photo selection done"
                )},
                modifier = Modifier.clickable {
                    pickMultipleMedia.launch(PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )
            // PREVIEW CARD HERE
            ListItem(
                headlineContent = { Text("Export video") }
            )
        }
    }
}

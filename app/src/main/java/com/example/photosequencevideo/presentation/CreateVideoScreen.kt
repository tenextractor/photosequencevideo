package com.example.photosequencevideo.presentation

import android.content.Intent
import android.net.Uri
import android.view.SurfaceView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.photosequencevideo.SlideshowService.SlideShowServiceState
import com.example.photosequencevideo.SlideshowViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateVideoScreen(viewModel: SlideshowViewModel = viewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state = viewModel.state

    val pickMultipleMedia =
        rememberLauncherForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia(5)) { uris ->
            when {
                uris.isEmpty() -> Unit
                uris.size < 3 -> Toast.makeText(
                    context, "Selection failed. Please select at least 3 photos",
                    Toast.LENGTH_SHORT
                ).show()
                else -> {
                    // Picker grants are tied to this Activity; persist them so the ViewModel's
                    // URIs stay readable after configuration changes / process recreation.
                    uris.forEach {
                        context.contentResolver.takePersistableUriPermission(
                            it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    }
                    viewModel.process(uris)
                }
            }
        }

    val saveVideo =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("video/mp4")) { uri ->
            if (uri != null) scope.launch {
                val ok = viewModel.exportTo(uri)
                Toast.makeText(
                    context, if (ok) "Video exported!" else "Export failed", Toast.LENGTH_SHORT
                ).show()
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
                    if (state == SlideShowServiceState.NOT_STARTED) "3 to 5 photos required"
                    else "Photo selection done"
                )},
                modifier = Modifier.clickable {
                    pickMultipleMedia.launch(PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )
            OutlinedCard(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    when (state) {
                        SlideShowServiceState.NOT_STARTED -> Text("Preview")
                        SlideShowServiceState.PROCESSING -> CircularProgressIndicator()
                        SlideShowServiceState.DONE -> SlideshowPreview(viewModel.outputFile)
                    }
                }
            }
            val exportEnabled = state == SlideShowServiceState.DONE
            ListItem(
                headlineContent = {
                    Text(
                        "Export video",
                        color = if (exportEnabled) LocalContentColor.current
                        else LocalContentColor.current.copy(alpha = 0.38f)
                    )
                },
                supportingContent = when (state) {
                    SlideShowServiceState.NOT_STARTED -> { { Text("Photos must be picked to export") } }
                    SlideShowServiceState.PROCESSING -> { { Text("Processing...") } }
                    SlideShowServiceState.DONE -> null
                },
                modifier = Modifier.clickable(enabled = exportEnabled) {
                    saveVideo.launch("slideshow_${System.currentTimeMillis()}.mp4")
                }
            )
        }
    }
}

/** Plays the rendered video file once. Only shown in the DONE state, so the file is complete. */
@Composable
private fun SlideshowPreview(file: File) {
    val context = LocalContext.current
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.fromFile(file)))
            prepare()
            play()
        }
    }
    DisposableEffect(Unit) { onDispose { player.release() } }
    AndroidView(
        factory = { SurfaceView(it).also(player::setVideoSurfaceView) },
        modifier = Modifier.fillMaxSize()
    )
}

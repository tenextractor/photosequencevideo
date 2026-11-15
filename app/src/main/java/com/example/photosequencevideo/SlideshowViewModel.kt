@file:OptIn(UnstableApi::class)

package com.example.photosequencevideo

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import com.example.photosequencevideo.SlideshowService.SlideShowServiceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class SlideshowViewModel(app: Application) : AndroidViewModel(app) {
    /** The rendered video; only valid in the DONE state. */
    val outputFile = File(app.cacheDir, "slideshow.mp4")
    private var transformer: Transformer? = null

    var state by mutableStateOf(SlideShowServiceState.NOT_STARTED)
        private set

    /** Builds a new random slideshow and renders it to a cache file. */
    fun process(photos: List<Uri>) {
        transformer?.cancel()
        outputFile.delete()
        val composition = SlideshowService.buildComposition(getApplication(), photos)
        state = SlideShowServiceState.PROCESSING

        transformer = Transformer.Builder(getApplication())
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    state = SlideShowServiceState.DONE
                }

                override fun onError(
                    composition: Composition,
                    exportResult: ExportResult,
                    exportException: ExportException,
                ) {
                    Log.e("SlideshowViewModel", "Processing failed", exportException)
                    state = SlideShowServiceState.NOT_STARTED
                }
            })
            .build()
            .also { it.start(composition, outputFile.path) }
    }

    /** Copies the rendered video to [destination]. Returns true on success. */
    suspend fun exportTo(destination: Uri): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            getApplication<Application>().contentResolver.openOutputStream(destination)!!.use { out ->
                outputFile.inputStream().use { it.copyTo(out) }
            }
        }.isSuccess
    }

    override fun onCleared() {
        transformer?.cancel()
    }
}

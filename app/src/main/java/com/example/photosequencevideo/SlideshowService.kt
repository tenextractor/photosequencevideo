@file:OptIn(UnstableApi::class)

package com.example.photosequencevideo

import android.content.Context
import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.OverlaySettings
import androidx.media3.common.VideoCompositorSettings
import androidx.media3.common.util.Size
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Presentation
import androidx.media3.effect.StaticOverlaySettings
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.Effects

/** Stateless builder for the slideshow [Composition]. */
object SlideshowService {
    const val STATIC_SECONDS = 2
    const val TRANSITION_SECONDS = 2
    val AUDIO = R.raw.blippy_trance_cut

    private const val WIDTH = 1280
    private const val HEIGHT = 720
    private const val FRAME_RATE = 30

    enum class TransitionType {
        DISSOLVE, SLIDE_UP, SLIDE_DOWN, SLIDE_LEFT, SLIDE_RIGHT
    }

    enum class SlideShowServiceState {
        NOT_STARTED, PROCESSING, DONE
    }

    /**
     * Timeline: photo k is static for STATIC_SECONDS, then transitions into photo k+1 over
     * TRANSITION_SECONDS. Photos alternate between two video sequences (layers) so that
     * consecutive photos overlap during a transition; the compositor blends the two layers.
     */
    fun buildComposition(context: Context, photos: List<Uri>): Composition {
        val staticUs = STATIC_SECONDS * 1_000_000L
        val transitionUs = TRANSITION_SECONDS * 1_000_000L
        val periodUs = staticUs + transitionUs
        val n = photos.size
        val totalUs = n * staticUs + (n - 1) * transitionUs
        val transitions = List(n - 1) {
            if (it == 0) TransitionType.DISSOLVE else TransitionType.entries.random()
        }

        val videoSequences = (0..1).map { layer ->
            val builder = EditedMediaItemSequence.Builder(setOf(C.TRACK_TYPE_VIDEO))
            var cursorUs = 0L
            for (i in layer until n step 2) {
                val startUs = if (i == 0) 0L else i * periodUs - transitionUs
                val endUs = if (i == n - 1) totalUs else (i + 1) * periodUs
                if (startUs > cursorUs) builder.addGap(startUs - cursorUs)
                builder.addItem(imageItem(photos[i], endUs - startUs))
                cursorUs = endUs
            }
            if (totalUs > cursorUs) builder.addGap(totalUs - cursorUs)
            builder.build()
        }

        val audio = EditedMediaItem.Builder(
            MediaItem.Builder()
                .setUri("android.resource://${context.packageName}/$AUDIO")
                .setClippingConfiguration(
                    MediaItem.ClippingConfiguration.Builder()
                        .setEndPositionMs(totalUs / 1000)
                        .build()
                )
                .build()
        ).build()

        return Composition.Builder(videoSequences + EditedMediaItemSequence.withAudioFrom(listOf(audio)))
            .setVideoCompositorSettings(TransitionCompositorSettings(transitions, staticUs, transitionUs))
            .build()
    }

    private fun imageItem(uri: Uri, durationUs: Long) =
        EditedMediaItem.Builder(
            MediaItem.Builder().setUri(uri).setImageDurationMs(durationUs / 1000).build()
        )
            .setFrameRate(FRAME_RATE)
            .setEffects(
                Effects(
                    /* audioProcessors= */ listOf(),
                    listOf(Presentation.createForWidthAndHeight(WIDTH, HEIGHT, Presentation.LAYOUT_SCALE_TO_FIT))
                )
            )
            .build()

    /** Animates the two layers per frame. Input 0 (primary) is drawn on top of input 1. */
    private class TransitionCompositorSettings(
        private val transitions: List<TransitionType>,
        private val staticUs: Long,
        private val transitionUs: Long,
    ) : VideoCompositorSettings {

        override fun getOutputSize(inputSizes: List<Size>) = Size(WIDTH, HEIGHT)

        override fun getOverlaySettings(inputId: Int, presentationTimeUs: Long): OverlaySettings {
            val periodUs = staticUs + transitionUs
            val k = (presentationTimeUs / periodUs).toInt().coerceAtMost(transitions.size)
            val localUs = presentationTimeUs - k * periodUs
            val isOutgoing = inputId == k % 2 // this layer currently shows photo k

            if (k == transitions.size || localUs < staticUs) {
                return layer(alpha = if (isOutgoing) 1f else 0f)
            }

            val p = (localUs - staticUs).toFloat() / transitionUs
            val (dx, dy) = when (transitions[k]) {
                TransitionType.DISSOLVE -> {
                    val topAlpha = if (isOutgoing) 1f - p else p
                    return layer(alpha = if (inputId == 0) topAlpha else 1f)
                }
                TransitionType.SLIDE_UP -> 0f to 1f
                TransitionType.SLIDE_DOWN -> 0f to -1f
                TransitionType.SLIDE_LEFT -> -1f to 0f
                TransitionType.SLIDE_RIGHT -> 1f to 0f
            }
            // Outgoing moves 0 -> 1 frame in (dx, dy); incoming moves -1 -> 0 frame.
            val shift = if (isOutgoing) p else p - 1f
            return layer(x = 2f * dx * shift, y = 2f * dy * shift)
        }

        /** Offset (x, y) is in NDC, so 2 = one full frame. */
        private fun layer(alpha: Float = 1f, x: Float = 0f, y: Float = 0f) =
            StaticOverlaySettings.Builder()
                .setAlphaScale(alpha)
                .setBackgroundFrameAnchor(x / 2f, y / 2f)
                .setOverlayFrameAnchor(-x / 2f, -y / 2f)
                .build()
    }
}

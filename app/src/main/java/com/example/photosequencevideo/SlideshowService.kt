package com.example.photosequencevideo

object SlideshowService {
    val STATIC_SECONDS = 2
    val TRANSITION_SECONDS = 2
    val AUDIO = R.raw.blippy_trance_cut

    enum class TransitionType {
        DISSOLVE, SLIDE_UP, SLIDE_DOWN, SLIDE_LEFT, SLIDE_RIGHT
    }

    enum class SlideShowServiceState {
        NOT_STARTED, PROCESSING, DONE
    }
}

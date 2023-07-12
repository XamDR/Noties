package io.github.xamdr.noties.ui.media

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import io.github.xamdr.noties.ui.helpers.Constants

class MediaViewerViewModel(private val savedState: SavedStateHandle) : ViewModel() {

	var isFullScreen: Boolean =
		savedState.get<Boolean>(Constants.BUNDLE_VIDEO_FULL_SCREEN) ?: false
		set(value) {
			field = value
			savedState[Constants.BUNDLE_VIDEO_FULL_SCREEN] = value
		}
}
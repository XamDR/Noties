package io.github.xamdr.noties.ui.media

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MediaViewerViewModel @Inject constructor() : ViewModel() {

	var currentPosition = 0
}
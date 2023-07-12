package io.github.xamdr.noties.ui.media

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VideoState(
	var playbackPosition: Long = 0,
	var playWhenReady: Boolean = true) : Parcelable

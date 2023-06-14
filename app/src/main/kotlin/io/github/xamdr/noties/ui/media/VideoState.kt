package io.github.xamdr.noties.ui.media

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VideoState(
	val currentPosition: Long = 0,
	val playWhenReady: Boolean = false,
	val isFullScreen: Boolean = false,
	val isBuffering: Boolean = true,
	val ended: Boolean = false) : Parcelable

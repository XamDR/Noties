package io.github.xamdr.noties.ui.media

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Android
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.Listener
import androidx.media3.common.util.RepeatModeUtil
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import io.github.xamdr.noties.R
import io.github.xamdr.noties.domain.model.MediaItem
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.helpers.findActivity
import io.github.xamdr.noties.ui.helpers.showToast
import io.github.xamdr.noties.ui.theme.NotiesTheme
import timber.log.Timber
import androidx.media3.common.MediaItem as ExoMediaItem

//private const val MIN_BUFFER_MS = 1000
//private const val MAX_BUFFER_MS = 2000
//private const val BUFFER_FOR_PLAYBACK_MS = 1000
//private const val BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS = 1000

@SuppressLint("OpaqueUnitKey")
@OptIn(UnstableApi::class)
@Composable
fun VideoScreen(
	item: MediaItem,
	playWhenReady: Boolean,
	window: Window,
	onFullScreen: () -> Unit,
) {
	val context = LocalContext.current
	val activity = context.findActivity() ?: return
	val exoPlayer = rememberPlayer(item = item, playWhenReady = playWhenReady)
	var isPlaying by rememberSaveable { mutableStateOf(playWhenReady) }

	fun toggleFullScreenAndOrientation() {
		onFullScreen()
		toggleScreenOrientationIfNecessary(exoPlayer, context, activity)
	}

	DisposableEffect(
		Box {
			AndroidView(
				factory = { context ->
					PlayerView(context).apply {
						player = exoPlayer
						layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
						artworkDisplayMode = PlayerView.ARTWORK_DISPLAY_MODE_OFF
						setShowNextButton(false)
						setShowPreviousButton(false)
						setRepeatToggleModes(RepeatModeUtil.REPEAT_TOGGLE_MODE_ONE)
						setFullscreenButtonClickListener { toggleFullScreenAndOrientation() }
					}
				}
			)
		}
	) {
		val listener = object : Listener {
			override fun onEvents(player: Player, events: Player.Events) {
				super.onEvents(player, events)
				isPlaying = player.isPlaying
			}

			override fun onPlayerError(error: PlaybackException) {
				super.onPlayerError(error)
				Timber.e(error)
				context.showToast(R.string.error_video_playback)
			}
		}
		exoPlayer.addListener(listener)
		onDispose {
			exoPlayer.removeListener(listener)
			exoPlayer.release()
		}
	}

	val lifecycleOwner = LocalLifecycleOwner.current

	DisposableEffect(key1 = lifecycleOwner) {
		val observer = LifecycleEventObserver { _, event ->
			if (event == Lifecycle.Event.ON_RESUME && isPlaying) {
				exoPlayer.play()
				window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
			}
			else if (event == Lifecycle.Event.ON_PAUSE) {
				exoPlayer.pause()
				window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
			}
		}
		lifecycleOwner.lifecycle.addObserver(observer)
		onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
	}
}

@OptIn(UnstableApi::class)
@Composable
private fun rememberPlayer(item: MediaItem, playWhenReady: Boolean): ExoPlayer {
	val context = LocalContext.current
	val exoPlayer = remember(key1 = context) {
		ExoPlayer.Builder(context).build().apply {
			setMediaItem(ExoMediaItem.fromUri(item.uri))
			prepare()
			setPlayWhenReady(playWhenReady)
		}
	}
	return exoPlayer
}

@OptIn(UnstableApi::class)
private fun toggleScreenOrientationIfNecessary(exoPlayer: ExoPlayer, context: Context, activity: Activity) {
	exoPlayer.videoFormat?.let { format ->
		val width = format.width
		val height = format.height
		if (width > height) {
			toggleScreenOrientation(context, activity)
		}
	}
}

@Composable
private fun VideoScreen() {
	Box {
		Image(
			imageVector = Icons.Outlined.Android,
			contentDescription = null,
			modifier = Modifier
				.fillMaxSize()
				.padding(8.dp),
			alignment = Alignment.Center,
			contentScale = ContentScale.Fit
		)
		AndroidView(
			factory = { context -> PlayerView(context) },
			modifier = Modifier
				.fillMaxSize()
				.padding(8.dp)
		)
	}
}

@DevicePreviews
@Composable
private fun VideoScreenPreview() = NotiesTheme { VideoScreen() }


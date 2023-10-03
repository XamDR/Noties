package io.github.xamdr.noties.ui.media

import android.annotation.SuppressLint
import android.view.Window
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.ImageNotSupported
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
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.github.xamdr.noties.R
import io.github.xamdr.noties.domain.model.MediaItem
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.helpers.showToast
import io.github.xamdr.noties.ui.theme.NotiesTheme
import androidx.media3.common.MediaItem as ExoMediaItem

//private const val MIN_BUFFER_MS = 1000
//private const val MAX_BUFFER_MS = 2000
//private const val BUFFER_FOR_PLAYBACK_MS = 1000
//private const val BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS = 1000

@SuppressLint("OpaqueUnitKey")
@Composable
fun VideoScreen(
	item: MediaItem,
	playWhenReady: Boolean,
	window: Window
) {
	val context = LocalContext.current
	val exoPlayer = rememberPlayer(item = item, playWhenReady = playWhenReady)
	var isThumbnailVisible by rememberSaveable { mutableStateOf(value = false) }
	var isPlayerViewVisible by rememberSaveable { mutableStateOf(value = false) }

	DisposableEffect(
		Box {
			if (isThumbnailVisible) {
				AsyncImage(
					model = ImageRequest.Builder(LocalContext.current)
						.data(item.metadata.thumbnail ?: Icons.Outlined.ImageNotSupported)
						.build(),
					contentDescription = null,
					modifier = Modifier
						.fillMaxSize()
						.padding(8.dp),
					alignment = Alignment.Center,
					contentScale = ContentScale.Fit
				)
			}
			if (isPlayerViewVisible) {
				AndroidView(
					modifier = Modifier
						.fillMaxSize()
						.padding(8.dp),
					factory = { context ->
						PlayerView(context).apply { player = exoPlayer }
					}
				)
			}
		}
	) {
		val listener = object : Listener {
			override fun onPlaybackStateChanged(playbackState: Int) {
				super.onPlaybackStateChanged(playbackState)
				isThumbnailVisible = playbackState <= Player.STATE_BUFFERING && isPlayerViewVisible.not()
				isPlayerViewVisible = playbackState > Player.STATE_IDLE && isThumbnailVisible.not()
			}

			override fun onPlayerError(error: PlaybackException) {
				super.onPlayerError(error)
				context.showToast(R.string.error_video_playback)
			}
		}
		exoPlayer.addListener(listener)
		onDispose { clearPlayer(exoPlayer, listener) }
	}

	val lifecycleOwner = LocalLifecycleOwner.current

	DisposableEffect(key1 = lifecycleOwner) {
		val observer = LifecycleEventObserver { _, event ->
			when (event) {
				Lifecycle.Event.ON_RESUME -> {
					exoPlayer.play()
					window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
				}
				Lifecycle.Event.ON_PAUSE -> {
					exoPlayer.pause()
					window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
				}
				else -> {}
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
//	val mediaSourceFactory = ProgressiveMediaSource.Factory { ContentDataSource(context) }
//	val loadControl = DefaultLoadControl.Builder()
//		.setBufferDurationsMs(
//			MIN_BUFFER_MS,
//			MAX_BUFFER_MS,
//			BUFFER_FOR_PLAYBACK_MS,
//			BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
//		)
//		.build()

	val exoPlayer = remember(key1 = context) {
		ExoPlayer.Builder(context).build().apply {
			setMediaItem(ExoMediaItem.fromUri(item.uri))
			prepare()
			setPlayWhenReady(playWhenReady)
		}
	}
	return exoPlayer
}

private fun clearPlayer(player: ExoPlayer, listener: Listener) {
	player.apply {
		removeListener(listener)
		release()
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


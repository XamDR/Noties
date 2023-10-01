package io.github.xamdr.noties.ui.media

import android.view.Window
import android.view.WindowManager
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
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.github.xamdr.noties.R
import io.github.xamdr.noties.domain.model.MediaItem
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.helpers.showToast
import io.github.xamdr.noties.ui.theme.NotiesTheme
import timber.log.Timber
import androidx.media3.common.MediaItem as ExoMediaItem

@Composable
fun VideoScreen(item: MediaItem, player: ExoPlayer?, videoState: VideoState, window: Window) {
	val context = LocalContext.current
	val lifecycleOwner = LocalLifecycleOwner.current
	var isThumbnailVisible by rememberSaveable { mutableStateOf(value = false) }
	var isPlayerViewVisible by rememberSaveable { mutableStateOf(value = false) }

	DisposableEffect(key1 = lifecycleOwner) {
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

		val observer = LifecycleEventObserver { _, event ->
			if (event == Lifecycle.Event.ON_RESUME) {
				Timber.d("ON_RESUME")
				player?.let {
					setPlayer(
						player = it,
						item = item,
						playbackPosition = videoState.playbackPosition,
						playWhenReady = videoState.playWhenReady,
						listener = listener
					)
				}
				window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
			}
			else if (event == Lifecycle.Event.ON_PAUSE) {
				Timber.d("ON_PAUSE")
				player?.let { clearPlayer(player = it, listener = listener) }
				window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
			}
		}
		lifecycleOwner.lifecycle.addObserver(observer)

		onDispose {
			lifecycleOwner.lifecycle.removeObserver(observer)
		}
	}
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
				factory = { context ->
					PlayerView(context).apply { this.player = player }
				},
				modifier = Modifier
					.fillMaxSize()
					.padding(8.dp),
				onReset = {

				},
				update = {

				}
			)
		}
	}
}

private fun setPlayer(
	player: ExoPlayer,
	item: MediaItem,
	playbackPosition: Long,
	playWhenReady: Boolean,
	listener: Listener
) {
	player.apply {
		setMediaItem(ExoMediaItem.fromUri(item.uri))
		seekTo(playbackPosition)
		prepare()
		setPlayWhenReady(playWhenReady)
		addListener(listener)
	}
}

private fun clearPlayer(player: ExoPlayer, listener: Listener) {
	player.apply {
		pause()
		removeListener(listener)
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


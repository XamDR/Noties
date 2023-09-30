package io.github.xamdr.noties.ui.media

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.util.Clock
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.ContentDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.DefaultAnalyticsCollector
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import io.github.xamdr.noties.data.entity.media.MediaType
import io.github.xamdr.noties.domain.model.MediaItem
import io.github.xamdr.noties.ui.helpers.*
import io.github.xamdr.noties.ui.theme.NotiesTheme
import timber.log.Timber

class MediaViewerActivity : FragmentActivity() {

	var player: ExoPlayer? = null
	private val items by lazy(LazyThreadSafetyMode.NONE) {
		intent.getParcelableArrayListCompat(Constants.BUNDLE_ITEMS, MediaItem::class.java)
	}
	private val position by lazy(LazyThreadSafetyMode.NONE) {
		intent.getIntExtra(Constants.BUNDLE_POSITION, 0)
	}
	private val itemsToDelete = mutableListOf<MediaItem>()
	private val videoState = VideoState()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent { MediaViewerActivityContent() }
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		outState.putParcelableArrayList(Constants.BUNDLE_ITEMS_DELETE, ArrayList(itemsToDelete))
	}

	override fun onRestoreInstanceState(savedInstanceState: Bundle) {
		super.onRestoreInstanceState(savedInstanceState)
		val restoredItemsToDelete = savedInstanceState.getParcelableArrayListCompat(
			Constants.BUNDLE_ITEMS_DELETE,
			MediaItem::class.java
		)
		itemsToDelete.addAll(restoredItemsToDelete)
	}

	override fun onStart() {
		super.onStart()
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			initializePlayer()
		}
	}

	override fun onStop() {
		super.onStop()
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			releasePlayer()
		}
	}

	override fun onResume() {
		super.onResume()
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
			initializePlayer()
		}
	}

	override fun onPause() {
		super.onPause()
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
			releasePlayer()
		}
	}

	@Composable
	private fun MediaViewerActivityContent() {
		NotiesTheme {
			MediaViewerScreen(
				items = items,
				startIndex = position,
				window = window,
				player = player,
				videoState = videoState,
				onNavigationIconClick = ::navigateUp
			)
		}
	}

	private fun initializePlayer() {
		if (items.any { it.mediaType == MediaType.Video }) {
			buildPlayer()
			Timber.d("ExoPlayer instance initialized")
		}
	}

	@OptIn(UnstableApi::class)
	private fun buildPlayer() {
		val mediaSourceFactory = ProgressiveMediaSource.Factory { ContentDataSource(this) }
		val loadControl = DefaultLoadControl.Builder()
			.setBufferDurationsMs(
				MIN_BUFFER_MS,
				MAX_BUFFER_MS,
				BUFFER_FOR_PLAYBACK_MS,
				BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
			)
			.build()
		player = ExoPlayer.Builder(this)
			.setRenderersFactory(DefaultRenderersFactory(this))
			.setMediaSourceFactory(mediaSourceFactory)
			.setTrackSelector(DefaultTrackSelector(this))
			.setLoadControl(loadControl)
			.setBandwidthMeter(DefaultBandwidthMeter.getSingletonInstance(this))
			.setAnalyticsCollector(DefaultAnalyticsCollector(Clock.DEFAULT))
			.setSeekForwardIncrementMs(10000)
			.setSeekBackIncrementMs(10000)
			.build()
	}

	private fun releasePlayer() {
		if (player != null) {
			player?.release()
			player = null
			Timber.d("ExoPlayer instance released")
		}
	}

	private fun navigateUp() {
		onBackPressedDispatcher.addCallback(this) {
			val intent = Intent().apply {
				putExtra(Constants.BUNDLE_ITEMS_DELETE, ArrayList(itemsToDelete))
			}
			setResult(Activity.RESULT_OK, intent)
			finish()
		}
	}

	private companion object {
		private const val MIN_BUFFER_MS = 1000
		private const val MAX_BUFFER_MS = 2000
		private const val BUFFER_FOR_PLAYBACK_MS = 1000
		private const val BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS = 1000
	}
}
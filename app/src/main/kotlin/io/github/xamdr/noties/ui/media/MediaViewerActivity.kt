package io.github.xamdr.noties.ui.media

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.addCallback
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
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
import io.github.xamdr.noties.databinding.ActivityMediaViewerBinding
import io.github.xamdr.noties.domain.model.MediaItem
import io.github.xamdr.noties.ui.helpers.*
import androidx.media3.common.MediaItem as ExoMediaItem

class MediaViewerActivity : AppCompatActivity() {

	private val binding by lazy(LazyThreadSafetyMode.NONE) { ActivityMediaViewerBinding.inflate(layoutInflater) }
	private val items by lazy(LazyThreadSafetyMode.NONE) {
		intent.getParcelableArrayListCompat(Constants.BUNDLE_ITEMS, MediaItem::class.java)
	}
	private val position by lazy(LazyThreadSafetyMode.NONE) {
		intent.getIntExtra(Constants.BUNDLE_POSITION, 0)
	}
	private lateinit var pageChangedCallback: PageChangedCallback
	private val itemsToDelete = mutableListOf<MediaItem>()
	private lateinit var mediaStateAdapter: MediaStateAdapter
	var player: ExoPlayer? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(binding.root)
		setSupportActionBar(binding.toolbar)
		supportActionBar?.setDisplayHomeAsUpEnabled(true)
		setupViewPager()
		navigateUp()
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
		pageChangedCallback = PageChangedCallback(this, items.size).apply {
			onPageSelected(position)
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			buildPlayer()
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
		binding.pager.registerOnPageChangeCallback(pageChangedCallback)
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
			buildPlayer()
		}
	}

	override fun onPause() {
		super.onPause()
		binding.pager.unregisterOnPageChangeCallback(pageChangedCallback)
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
			releasePlayer()
		}
	}

	fun setMediaItem(item: MediaItem) {
		val uri = item.uri ?: return
		player?.apply {
			setMediaItem(ExoMediaItem.fromUri(uri))
			prepare()
			play()
		}
	}

	fun deleteMediaItem() = mediaStateAdapter.removeFragment(binding.pager.currentItem)

	private fun setupViewPager() {
		mediaStateAdapter = MediaStateAdapter(this, items, this::onItemRemoved)
		binding.pager.apply {
			adapter = mediaStateAdapter
			setCurrentItem(position, false)
		}
	}

	@OptIn(UnstableApi::class)
	private fun buildPlayer() {
		val mediaSourceFactory = ProgressiveMediaSource.Factory { ContentDataSource(this) }
		val loadControl = DefaultLoadControl.Builder()
			.setBufferDurationsMs(MIN_BUFFER_MS, MAX_BUFFER_MS, BUFFER_FOR_PLAYBACK_MS, BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS)
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
		player?.release()
		player = null
	}

	private fun onItemRemoved(position: Int) {
		val itemToDelete = items[position]
		val newItems = items - itemToDelete
		if (newItems.isNotEmpty()) {
			pageChangedCallback.apply {
				size = newItems.size
				onPageSelected(position)
			}
		}
		itemsToDelete.add(itemToDelete)
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
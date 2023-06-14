package io.github.xamdr.noties.ui.media

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import androidx.annotation.OptIn
import androidx.core.os.bundleOf
import androidx.core.view.*
import androidx.fragment.app.Fragment
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import io.github.xamdr.noties.data.entity.media.MediaType
import io.github.xamdr.noties.databinding.FragmentMediaVideoViewerBinding
import io.github.xamdr.noties.domain.model.MediaItem
import io.github.xamdr.noties.ui.helpers.*
import io.github.xamdr.noties.ui.helpers.media.ImageLoader
import timber.log.Timber

class VideoMediaViewerFragment : Fragment() {

	private var _binding: FragmentMediaVideoViewerBinding? = null
	private val binding get() = _binding!!
	private val item by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getParcelableCompat(Constants.BUNDLE_MEDIA_ITEM, MediaItem::class.java)
	}
	private val fullScreenHelper = FullScreenHelper(
		onEnterFullScreen = { supportActionBar?.hide() },
		onExitFullScreen = { supportActionBar?.show() }
	)
	private lateinit var exoPlayer: ExoPlayer
	private val listener = PlayerStateChangedListener()
	private lateinit var settingsObserver: OrientationSettingsObserver
	private val videoState = VideoState()
	private var shouldShowThumbnail = true
	private var isFullScreen = false
	private var videoEnded = false

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		settingsObserver = OrientationSettingsObserver(Handler(Looper.getMainLooper()), requireActivity())
		requireActivity().contentResolver.registerContentObserver(
			Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION),
			false,
			settingsObserver
		)
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		_binding = FragmentMediaVideoViewerBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onDestroy() {
		super.onDestroy()
		requireActivity().contentResolver.unregisterContentObserver(settingsObserver)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		if (item.mediaType == MediaType.Video) {
			setupPlayerView()
			ImageLoader.load(binding.thumbnail, item.metadata.thumbnail, 800) {
				parentFragment?.startPostponedEnterTransition()
			}
		}
		else {
			Timber.d("The mediaType of %s is: %s", item, item.mediaType)
		}
		binding.playerView.setFullscreenButtonClickListener { toggleFullScreen() }
		onBackPressed {
			restoreDefaultOrientation()
			findNavController().popBackStack()
		}
	}

	override fun onStart() {
		super.onStart()
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			initializePlayer()
			window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
		}
	}

	override fun onStop() {
		super.onStop()
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			pausePlayback()
			window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
		}
	}

	override fun onPause() {
		super.onPause()
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
			pausePlayback()
			window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
		}
	}

	override fun onResume() {
		super.onResume()
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
			initializePlayer()
			window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
		}
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		val newVideoState = videoState.copy(
			currentPosition = exoPlayer.contentPosition,
			playWhenReady = exoPlayer.playWhenReady,
			isFullScreen = isFullScreen,
			isBuffering = shouldShowThumbnail,
			ended = videoEnded
		)
		outState.putParcelable(Constants.BUNDLE_VIDEO_STATE, newVideoState)
	}

	override fun onViewStateRestored(savedInstanceState: Bundle?) {
		super.onViewStateRestored(savedInstanceState)
		if (savedInstanceState != null) {
			val videoState = savedInstanceState.getParcelableCompat(Constants.BUNDLE_VIDEO_STATE, VideoState::class.java)
			updateViewUI(videoState.isFullScreen)
			binding.playerView.post {
				exoPlayer.seekTo(videoState.currentPosition)
				if (!videoState.ended) {
					fullScreenHelper.toggleSystemBarsVisibility(binding.root, window)
					exoPlayer.playWhenReady = videoState.playWhenReady
				}
				else {
					exoPlayer.playWhenReady = false
				}
			}
		}
	}

	@OptIn(UnstableApi::class)
	private fun setupPlayerView() {
		exoPlayer = (requireParentFragment() as MediaViewerFragment).player ?: return
		binding.playerView.apply {
			player = exoPlayer
			setShowNextButton(false)
			setShowPreviousButton(false)
		}
	}

	private fun initializePlayer() {
		val src = item.uri ?: return
		(requireParentFragment() as MediaViewerFragment).addMediaSource(exoPlayer, src, listener)
	}

	@OptIn(UnstableApi::class)
	private fun toggleFullScreen() {
		isFullScreen = !isFullScreen
		if (isFullScreen) fullScreenHelper.toggleSystemBarsVisibility(binding.root, window)
		fullScreenHelper.toggleSystemBarsVisibility(binding.root, window)
		val videoFormat = exoPlayer.videoFormat
		if (videoFormat != null) {
			val width = videoFormat.width
			val height = videoFormat.height
			if (width > height) {
				val isLandscape = requireContext().isLandscape()
				val newOrientation = if (isLandscape) ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
					else ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
				requireActivity().requestedOrientation = newOrientation
			}
		}
	}

	@SuppressLint("PrivateResource")
	private fun updateViewUI(isFullScreen: Boolean) {
		this.isFullScreen = isFullScreen
		binding.playerView.findViewById<ImageButton>(androidx.media3.ui.R.id.exo_fullscreen).apply {
			setImageResource(if (isFullScreen) androidx.media3.ui.R.drawable.exo_ic_fullscreen_exit
			else androidx.media3.ui.R.drawable.exo_ic_fullscreen_enter)
		}
	}

	private fun pausePlayback() {
		exoPlayer.pause()
		exoPlayer.removeListener(listener)
	}

	private fun restoreDefaultOrientation() {
		requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
	}

	private inner class PlayerStateChangedListener : Player.Listener {
		override fun onPlaybackStateChanged(playbackState: Int) {
			super.onPlaybackStateChanged(playbackState)
			binding.thumbnail.isVisible = playbackState == Player.STATE_BUFFERING && shouldShowThumbnail
			binding.playerView.isVisible = playbackState != Player.STATE_IDLE && !shouldShowThumbnail
			videoEnded = playbackState == Player.STATE_ENDED
			shouldShowThumbnail = false
		}
	}

	companion object {
		fun newInstance(item: MediaItem) = VideoMediaViewerFragment().apply {
			arguments = bundleOf(Constants.BUNDLE_MEDIA_ITEM to item)
		}
	}
}
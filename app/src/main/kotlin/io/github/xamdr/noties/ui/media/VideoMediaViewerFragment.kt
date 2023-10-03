package io.github.xamdr.noties.ui.media

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.*
import android.widget.ImageButton
import androidx.annotation.OptIn
import androidx.core.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import io.github.xamdr.noties.R
import io.github.xamdr.noties.databinding.FragmentMediaVideoViewerBinding
import io.github.xamdr.noties.ui.helpers.*
import timber.log.Timber

class VideoMediaViewerFragment : Fragment(), Player.Listener {

	private var _binding: FragmentMediaVideoViewerBinding? = null
	private val binding get() = _binding!!
	private val viewModel by activityViewModels<MediaViewerViewModel>()
	private lateinit var exoPlayer: ExoPlayer
	private lateinit var settingsObserver: OrientationSettingsObserver
	private val videoState = VideoState()

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
		binding.playerView.setFullscreenButtonClickListener { toggleFullScreenAndOrientation() }
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		val videoState =
		if (::exoPlayer.isInitialized) {
			VideoState(
				playbackPosition = exoPlayer.contentPosition,
				playWhenReady = exoPlayer.playWhenReady
			)
		}
		else {
			VideoState()
		}
		outState.putParcelable(Constants.BUNDLE_VIDEO_STATE, videoState)
	}

	override fun onViewStateRestored(savedInstanceState: Bundle?) {
		super.onViewStateRestored(savedInstanceState)
		if (savedInstanceState != null) {
			val savedVideoState = savedInstanceState.getParcelableCompat(Constants.BUNDLE_VIDEO_STATE, VideoState::class.java)
			videoState.apply {
				playbackPosition = savedVideoState.playbackPosition
				playWhenReady = savedVideoState.playWhenReady
			}
		}
	}

	override fun onPlaybackStateChanged(playbackState: Int) {
		super.onPlaybackStateChanged(playbackState)
		binding.thumbnail.isVisible = playbackState <= Player.STATE_BUFFERING && !binding.playerView.isVisible
		binding.playerView.isVisible = playbackState > Player.STATE_IDLE && !binding.thumbnail.isVisible
	}

	override fun onPlayerError(error: PlaybackException) {
		super.onPlayerError(error)
		binding.root.showSnackbar(R.string.error_video_playback)
		binding.thumbnail.setImageResource(R.drawable.ic_image_not_supported)
	}

	private fun toggleFullScreenAndOrientation() {
		viewModel.isFullScreen = !viewModel.isFullScreen
		toggleFullScreen(viewModel.isFullScreen)
		toggleScreenOrientationIfNecessary()
	}

	@OptIn(UnstableApi::class)
	private fun toggleScreenOrientationIfNecessary() {
		exoPlayer.videoFormat?.let { format ->
			val width = format.width
			val height = format.height
			if (width > height) {
				val newOrientation = if (requireContext().isLandscape()) ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
					else ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
				requireActivity().requestedOrientation = newOrientation
			}
		}
	}

	@SuppressLint("PrivateResource")
	private fun toggleFullScreen(isFullScreen: Boolean) {
		val fullScreenButton = binding.playerView.findViewById<ImageButton>(androidx.media3.ui.R.id.exo_fullscreen)
		if (isFullScreen) {
			Timber.d("Enter full screen")
//			fullScreenHelper.enterFullScreen(binding.root, window)
			fullScreenButton.setImageResource(androidx.media3.ui.R.drawable.exo_ic_fullscreen_exit)
		}
		else {
			Timber.d("Exit full screen")
//			fullScreenHelper.exitFullScreen(binding.root, window)
			fullScreenButton.setImageResource(androidx.media3.ui.R.drawable.exo_ic_fullscreen_enter)
		}
	}
}
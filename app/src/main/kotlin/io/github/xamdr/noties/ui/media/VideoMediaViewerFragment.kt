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
import androidx.core.os.bundleOf
import androidx.core.view.*
import androidx.fragment.app.activityViewModels
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import io.github.xamdr.noties.R
import io.github.xamdr.noties.data.entity.media.MediaType
import io.github.xamdr.noties.databinding.FragmentMediaVideoViewerBinding
import io.github.xamdr.noties.domain.model.MediaItem
import io.github.xamdr.noties.ui.helpers.*
import io.github.xamdr.noties.ui.helpers.media.ImageLoader

class VideoMediaViewerFragment : MediaViewerFragment(), Player.Listener {

	private var _binding: FragmentMediaVideoViewerBinding? = null
	private val binding get() = _binding!!
	private val viewModel by activityViewModels<MediaViewerViewModel>()
	private val menuProvider = VideoMediaMenuProvider()
	private lateinit var exoPlayer: ExoPlayer
	private lateinit var settingsObserver: OrientationSettingsObserver

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
			ImageLoader.load(binding.thumbnail, item.metadata.thumbnail, 800)
		}
		binding.playerView.setFullscreenButtonClickListener { toggleFullScreenAndOrientation() }
	}

	override fun onResume() {
		super.onResume()
		addMenuProvider(menuProvider)
		setPlayer()
		toggleFullScreen(viewModel.isFullScreen)
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
	}

	override fun onPause() {
		super.onPause()
		removeMenuProvider(menuProvider)
		clearPlayer()
		window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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

	@OptIn(UnstableApi::class)
	private fun setPlayer() {
		exoPlayer = (requireActivity() as MediaViewerActivity).player ?: return
		binding.playerView.player = exoPlayer
		exoPlayer.addListener(this)
		(requireActivity() as MediaViewerActivity).setMediaItem(item)
	}

	private fun clearPlayer() {
		exoPlayer.pause()
		exoPlayer.removeListener(this)
		binding.playerView.player = null
	}

	@OptIn(UnstableApi::class)
	private fun toggleFullScreenAndOrientation() {
		viewModel.isFullScreen = !viewModel.isFullScreen
		toggleFullScreen(viewModel.isFullScreen)
		toggleOrientationIfNecessary()
	}

	@OptIn(UnstableApi::class)
	private fun toggleOrientationIfNecessary() {
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
			fullScreenHelper.enterFullScreen(binding.root, window)
			fullScreenButton.setImageResource(androidx.media3.ui.R.drawable.exo_ic_fullscreen_exit)
		}
		else {
			fullScreenHelper.exitFullScreen(binding.root, window)
			fullScreenButton.setImageResource(androidx.media3.ui.R.drawable.exo_ic_fullscreen_enter)
		}
	}

	private inner class VideoMediaMenuProvider : MenuProvider {
		override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
			menuInflater.inflate(R.menu.menu_media_video_viewer, menu)
		}

		override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
			return when (menuItem.itemId) {
				android.R.id.home -> {
					onBackPressed(); true
				}
				R.id.share -> {
					shareMediaItem(); true
				}
				R.id.download -> {
					downloadMediaItem(); true
				}
				R.id.delete -> {
					true
				}
				else -> false
			}
		}
	}

	companion object {
		fun newInstance(item: MediaItem) = VideoMediaViewerFragment().apply {
			arguments = bundleOf(Constants.BUNDLE_MEDIA_ITEM to item)
		}
	}
}
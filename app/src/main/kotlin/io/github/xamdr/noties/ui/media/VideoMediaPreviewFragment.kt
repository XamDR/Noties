package io.github.xamdr.noties.ui.media

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.core.os.bundleOf
import androidx.core.view.*
import androidx.fragment.app.Fragment
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import io.github.xamdr.noties.data.entity.media.MediaType
import io.github.xamdr.noties.databinding.FragmentMediaPreviewVideoBinding
import io.github.xamdr.noties.domain.model.MediaItem
import io.github.xamdr.noties.ui.helpers.*
import io.github.xamdr.noties.ui.helpers.media.ImageLoader
import timber.log.Timber

class VideoMediaPreviewFragment : Fragment() {

	private var _binding: FragmentMediaPreviewVideoBinding? = null
	private val binding get() = _binding!!
	private val item by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getParcelableCompat(Constants.BUNDLE_MEDIA_ITEM, MediaItem::class.java)
	}
	private lateinit var exoPlayer: ExoPlayer
	private val listener = PlayerStateChangedListener()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		_binding = FragmentMediaPreviewVideoBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		releasePlayer()
		_binding = null
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
		binding.playerView.onClick { toggleSystemBarsVisibility(it) }
		binding.playerView.setFullscreenButtonClickListener { toggleVideoRotation() }
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

	override fun onPause() {
		super.onPause()
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
			releasePlayer()
		}
	}

	override fun onResume() {
		super.onResume()
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
			initializePlayer()
		}
	}

	private fun toggleSystemBarsVisibility(view: View) {
		val windowInsetsController = WindowCompat.getInsetsController(window, view)
		windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE
		ViewCompat.getRootWindowInsets(view)?.let { windowInsets ->
			val systemInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
			// We use systemInsets.top to check if the status bar is visible,
			// and systemInsets.bottom to check if the navigation bar is visible
			if (systemInsets.top > 0 || systemInsets.bottom > 0) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
					WindowCompat.setDecorFitsSystemWindows(window, false)
				}
				windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
				supportActionBar?.hide()
			}
			else {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
					WindowCompat.setDecorFitsSystemWindows(window, true)
				}
				windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
				supportActionBar?.show()
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

	private fun releasePlayer() {
		exoPlayer.removeListener(listener)
	}

	private fun toggleVideoRotation() {

	}

	private inner class PlayerStateChangedListener : Player.Listener {
		override fun onPlaybackStateChanged(playbackState: Int) {
			binding.thumbnail.isVisible = playbackState == Player.STATE_IDLE || playbackState == Player.STATE_BUFFERING
			binding.playerView.isVisible = playbackState == Player.STATE_READY || playbackState == Player.STATE_ENDED
		}
	}

	companion object {
		fun newInstance(item: MediaItem) = VideoMediaPreviewFragment().apply {
			arguments = bundleOf(Constants.BUNDLE_MEDIA_ITEM to item)
		}
	}
}
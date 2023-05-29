package io.github.xamdr.noties.ui.media

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import io.github.xamdr.noties.data.entity.media.MediaType
import io.github.xamdr.noties.databinding.FragmentMediaPreviewVideoBinding
import io.github.xamdr.noties.domain.model.MediaItem
import io.github.xamdr.noties.ui.helpers.Constants
import io.github.xamdr.noties.ui.helpers.getParcelableCompat
import io.github.xamdr.noties.ui.helpers.supportActionBar
import io.github.xamdr.noties.ui.helpers.window
import timber.log.Timber

class VideoMediaPreviewFragment : Fragment() {

	private var _binding: FragmentMediaPreviewVideoBinding? = null
	private val binding get() = _binding!!
	private val item by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getParcelableCompat(Constants.BUNDLE_MEDIA_ITEM, MediaItem::class.java)
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		_binding = FragmentMediaPreviewVideoBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		releaseMediaPlayer()
		_binding = null
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		if (item.mediaType == MediaType.Video) {
			initializeMediaPlayer()
		}
		else {
			Timber.d("The mediaType of %s is: %s", item, item.mediaType)
		}
		binding.video.setOnClickListener { toggleSystemBarsVisibility(it) }
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

	private fun initializeMediaPlayer() {
		binding.video.apply {
			setVideoURI(item.uri)
			setMediaController(MediaController(requireContext()))
			requestFocus()
			setOnPreparedListener { start() }
		}
	}

	private fun releaseMediaPlayer() {
		binding.video.stopPlayback()
	}

	companion object {
		fun newInstance(item: MediaItem) = VideoMediaPreviewFragment().apply {
			arguments = bundleOf(Constants.BUNDLE_MEDIA_ITEM to item)
		}
	}
}
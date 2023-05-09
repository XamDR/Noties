package io.github.xamdr.noties.ui.media

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import io.github.xamdr.noties.databinding.FragmentMediaPreviewImageBinding
import io.github.xamdr.noties.domain.model.Image
import io.github.xamdr.noties.ui.helpers.Constants
import io.github.xamdr.noties.ui.helpers.getParcelableCompat
import io.github.xamdr.noties.ui.helpers.supportActionBar
import io.github.xamdr.noties.ui.helpers.window
import io.github.xamdr.noties.ui.image.ImageLoader

class MediaPreviewFragment : Fragment() {

	private var _binding: FragmentMediaPreviewImageBinding? = null
	private val binding get() = _binding!!
	private val item by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getParcelableCompat(Constants.BUNDLE_IMAGE, Image::class.java)
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		_binding = FragmentMediaPreviewImageBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		ImageLoader.load(binding.image, item.uri, 800) {
			parentFragment?.startPostponedEnterTransition()
		}
		binding.image.setOnClickListener { toggleSystemBarsVisibility(it) }
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

	companion object {
		fun newInstance(item: Image) = MediaPreviewFragment().apply {
			arguments = bundleOf(Constants.BUNDLE_IMAGE to item)
		}
	}
}
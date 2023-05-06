package io.github.xamdr.noties.ui.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import io.github.xamdr.noties.databinding.FragmentMediaPreviewImageBinding
import io.github.xamdr.noties.domain.model.Image
import io.github.xamdr.noties.ui.helpers.Constants
import io.github.xamdr.noties.ui.helpers.getParcelableCompat
import io.github.xamdr.noties.ui.helpers.supportActionBar
import io.github.xamdr.noties.ui.image.ImageLoader

class MediaPreviewFragment : Fragment() {

	private var _binding: FragmentMediaPreviewImageBinding? = null
	private val binding get() = _binding!!
	private val item by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getParcelableCompat(Constants.BUNDLE_IMAGE, Image::class.java)
	}
	private var fullScreen = false

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
		if (fullScreen) showSystemBars(view) else hideSystemBars(view)
		fullScreen = !fullScreen
		toggleActionBarVisibility(view)
	}

	private fun hideSystemBars(view: View) {
		WindowCompat.getInsetsController(requireActivity().window, view).apply {
			systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE
			hide(WindowInsetsCompat.Type.systemBars())
		}
	}

	private fun showSystemBars(view: View) {
		WindowCompat.getInsetsController(requireActivity().window, view).apply {
			systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE
			show(WindowInsetsCompat.Type.systemBars())
		}
	}

	@Suppress("DEPRECATION")
	private fun toggleActionBarVisibility(view: View) {
		view.setOnSystemUiVisibilityChangeListener { visibility ->
			if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
				supportActionBar?.show()
			}
			else {
				supportActionBar?.hide()
			}
		}
	}

	companion object {
		fun newInstance(item: Image) = MediaPreviewFragment().apply {
			arguments = bundleOf(Constants.BUNDLE_IMAGE to item)
		}
	}
}
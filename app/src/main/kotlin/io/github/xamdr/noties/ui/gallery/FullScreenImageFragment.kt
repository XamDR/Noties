package io.github.xamdr.noties.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat.getInsetsController
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import io.github.xamdr.noties.databinding.FragmentFullScreenImageBinding
import io.github.xamdr.noties.domain.model.Image
import io.github.xamdr.noties.ui.helpers.getSerializableCompat
import io.github.xamdr.noties.ui.helpers.supportActionBar

class FullScreenImageFragment : Fragment() {

	private var _binding: FragmentFullScreenImageBinding? = null
	private val binding get() = _binding!!
	private val image by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getSerializableCompat(KEY, Image::class.java)
	}
	private var fullScreen = false

	override fun onCreateView(inflater: LayoutInflater,
	                          container: ViewGroup?,
	                          savedInstanceState: Bundle?): View {
		_binding = FragmentFullScreenImageBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.image.setImageURI(image.uri)
		binding.image.setOnClickListener { toggleSystemBarsVisibility(it) }
	}

	private fun toggleSystemBarsVisibility(view: View) {
		if (fullScreen) showSystemBars(view) else hideSystemBars(view)
		fullScreen = !fullScreen
		toggleActionBarVisibility(view)
	}

	private fun hideSystemBars(view: View) {
		getInsetsController(requireActivity().window, view).apply {
			systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE
			hide(WindowInsetsCompat.Type.systemBars())
		}
	}

	private fun showSystemBars(view: View) {
		getInsetsController(requireActivity().window, view).apply {
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
		private const val KEY = "image"

		fun newInstance(image: Image) = FullScreenImageFragment().apply {
			arguments = bundleOf(KEY to image)
		}
	}
}
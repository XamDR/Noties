package net.azurewebsites.eznotes.ui.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import net.azurewebsites.eznotes.core.MediaItemEntity
import net.azurewebsites.eznotes.databinding.FragmentFullScreenImageBinding

class FullScreenImageFragment : Fragment() {

	private var _binding: FragmentFullScreenImageBinding? = null
	private val binding get() = _binding!!
	private var mediaItem: MediaItemEntity? = null
	private var fullScreen = false

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		mediaItem = arguments?.getParcelable("media_item")
	}

	override fun onCreateView(inflater: LayoutInflater,
	                          container: ViewGroup?,
	                          savedInstanceState: Bundle?): View {
		_binding = FragmentFullScreenImageBinding.inflate(inflater, container, false).apply {
			mediaItem = this@FullScreenImageFragment.mediaItem
		}
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.image.setOnClickListener { toggleSystemBarsVisibility(it) }
	}

	private fun toggleSystemBarsVisibility(view: View) {
		if (fullScreen) showSystemBars(view) else hideSystemBars(view)
		fullScreen = !fullScreen
		toggleActionBarVisibility(view)
	}

	private fun hideSystemBars(view: View) {
		ViewCompat.getWindowInsetsController(view)?.apply {
			systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE
			hide(WindowInsetsCompat.Type.systemBars())
		}
	}

	private fun showSystemBars(view: View) {
		ViewCompat.getWindowInsetsController(view)?.apply {
			systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE
			show(WindowInsetsCompat.Type.systemBars())
		}
	}

	@Suppress("DEPRECATION")
	private fun toggleActionBarVisibility(view: View) {
		view.setOnSystemUiVisibilityChangeListener { visibility ->
			if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
				(activity as? AppCompatActivity)?.supportActionBar?.show()
			}
			else {
				(activity as? AppCompatActivity)?.supportActionBar?.hide()
			}
		}
	}

	companion object {
		fun newInstance(mediaItem: MediaItemEntity) = FullScreenImageFragment().apply {
			arguments = bundleOf("media_item" to mediaItem)
		}
	}
}
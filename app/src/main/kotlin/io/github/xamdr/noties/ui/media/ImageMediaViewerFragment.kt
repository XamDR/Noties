package io.github.xamdr.noties.ui.media

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import io.github.xamdr.noties.R
import io.github.xamdr.noties.data.entity.media.MediaType
import io.github.xamdr.noties.databinding.FragmentMediaImageViewerBinding
import io.github.xamdr.noties.domain.model.MediaItem
import io.github.xamdr.noties.ui.helpers.*
import io.github.xamdr.noties.ui.helpers.media.ImageLoader

class ImageMediaViewerFragment : MediaViewerFragment() {

	private var _binding: FragmentMediaImageViewerBinding? = null
	private val binding get() = _binding!!
	private val menuProvider = ImageMediaMenuProvider()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		_binding = FragmentMediaImageViewerBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		if (item.mediaType == MediaType.Image) {
			ImageLoader.load(binding.image, item.uri, 800)
		}
		binding.image.setOnClickListener { toggleFullScreen() }
	}

	override fun onResume() {
		super.onResume()
		addMenuProvider(menuProvider)
	}

	override fun onPause() {
		super.onPause()
		removeMenuProvider(menuProvider)
	}

	private fun toggleFullScreen() {
		fullScreenHelper.toggleSystemBarsVisibility(binding.root, window)
	}

	private inner class ImageMediaMenuProvider : MenuProvider {
		override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
			menuInflater.inflate(R.menu.menu_media_image_viewer, menu)
		}

		override fun onMenuItemSelected(menuItem: MenuItem) = when (menuItem.itemId) {
			android.R.id.home -> {
				onBackPressed(); true
			}
			R.id.share -> {
				shareMediaItem(); true
			}
			R.id.toggle_orientation -> {
				toggleScreenOrientation(); true
			}
			R.id.copy -> {
				copyImageToClipboard(); true
			}
			R.id.download -> {
				downloadMediaItem(); true
			}
			R.id.print -> {
				printImage(); true
			}
			R.id.delete -> {
				deleteMediaItem(); true
			}
			R.id.set_as -> {
				setImageAs(); true
			}
			else -> false
		}
	}

	companion object {
		fun newInstance(item: MediaItem) = ImageMediaViewerFragment().apply {
			arguments = bundleOf(Constants.BUNDLE_MEDIA_ITEM to item)
		}
	}
}
package io.github.xamdr.noties.ui.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import io.github.xamdr.noties.data.entity.media.MediaType
import io.github.xamdr.noties.databinding.FragmentMediaImageViewerBinding
import io.github.xamdr.noties.domain.model.MediaItem
import io.github.xamdr.noties.ui.helpers.Constants
import io.github.xamdr.noties.ui.helpers.getParcelableCompat
import io.github.xamdr.noties.ui.helpers.media.ImageLoader
import io.github.xamdr.noties.ui.helpers.supportActionBar
import io.github.xamdr.noties.ui.helpers.window
import timber.log.Timber

class ImageMediaViewerFragment : Fragment() {

	private var _binding: FragmentMediaImageViewerBinding? = null
	private val binding get() = _binding!!
	private val item by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getParcelableCompat(Constants.BUNDLE_MEDIA_ITEM, MediaItem::class.java)
	}
	private val fullScreenHelper = FullScreenHelper(
		onEnterFullScreen = { supportActionBar?.hide() },
		onExitFullScreen = { supportActionBar?.show() }
	)

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
			ImageLoader.load(binding.image, item.uri, 800) {
				parentFragment?.startPostponedEnterTransition()
			}
		}
		else {
			Timber.d("The mediaType of %s is: %s", item, item.mediaType)
		}
		binding.image.setOnClickListener { fullScreenHelper.toggleSystemBarsVisibility(it, window) }
	}

	companion object {
		fun newInstance(item: MediaItem) = ImageMediaViewerFragment().apply {
			arguments = bundleOf(Constants.BUNDLE_MEDIA_ITEM to item)
		}
	}
}
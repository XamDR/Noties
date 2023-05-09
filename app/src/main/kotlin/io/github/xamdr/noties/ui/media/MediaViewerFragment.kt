package io.github.xamdr.noties.ui.media

import android.os.Bundle
import android.view.*
import androidx.core.app.ShareCompat
import androidx.core.app.SharedElementCallback
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import dagger.hilt.android.AndroidEntryPoint
import io.github.xamdr.noties.R
import io.github.xamdr.noties.databinding.FragmentMediaViewerBinding
import io.github.xamdr.noties.domain.model.Image
import io.github.xamdr.noties.ui.helpers.*

@AndroidEntryPoint
class MediaViewerFragment : Fragment() {

	private var _binding: FragmentMediaViewerBinding? = null
	private val binding get() = _binding!!
	private val sharedViewModel by hiltNavGraphViewModels<MediaViewerViewModel>(R.id.nav_editor)
	private val images by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getParcelableArrayListCompat(Constants.BUNDLE_IMAGES, Image::class.java)
	}
	private lateinit var mediaStateAdapter: MediaStateAdapter
	private lateinit var pageSelectedCallback: PageSelectedCallback
	private val menuProvider = MediaMenuProvider()
	private val itemsToDelete = mutableListOf<Image>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		mediaStateAdapter = MediaStateAdapter(this, images, this::onItemRemoved)
		pageSelectedCallback = PageSelectedCallback(images.size)
		if (savedInstanceState != null) {
			val restoredItemsToDelete = savedInstanceState.getParcelableArrayListCompat(
				Constants.BUNDLE_IMAGES,
				Image::class.java
			)
			itemsToDelete.addAll(restoredItemsToDelete)
		}
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		_binding = FragmentMediaViewerBinding.inflate(inflater, container, false)
		addMenuProvider(menuProvider, viewLifecycleOwner)
		prepareSharedElementEnterTransition()
		postponeEnterTransition()
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setupViewPager()
		pageSelectedCallback.onPageSelected(sharedViewModel.currentPosition)
		onBackPressed {
			setNavigationResult(Constants.BUNDLE_IMAGES, ArrayList(itemsToDelete))
			findNavController().popBackStack()
		}
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		outState.putParcelableArrayList(Constants.BUNDLE_IMAGES, ArrayList(itemsToDelete))
	}

	override fun onPause() {
		super.onPause()
		binding.pager.unregisterOnPageChangeCallback(pageSelectedCallback)
	}

	override fun onResume() {
		super.onResume()
		binding.pager.registerOnPageChangeCallback(pageSelectedCallback)
	}

	private fun prepareSharedElementEnterTransition() {
		sharedElementEnterTransition = inflateTransition(R.transition.shared_image_transition)
		setEnterSharedElementCallback(object : SharedElementCallback() {
			override fun onMapSharedElements(names: MutableList<String>?, sharedElements: MutableMap<String, View>?) {
				if (sharedViewModel.currentPosition != 0) {
					val currentFragment = childFragmentManager.findFragmentByTag(
						"f${mediaStateAdapter.getItemId(sharedViewModel.currentPosition)}") ?: return
					val view = currentFragment.view ?: return
					if (!names.isNullOrEmpty() && !sharedElements.isNullOrEmpty()) {
						sharedElements[names[0]] = view.findViewById(R.id.image)
					}
				}
			}
		})
	}

	private fun setupViewPager() {
		binding.pager.apply {
			adapter = mediaStateAdapter
//			setPageTransformer(ZoomOutPageTransformer())
			setCurrentItem(sharedViewModel.currentPosition, false)
		}
	}

	private fun shareMediaItem(position: Int) {
		val uri = images[position].uri ?: return
		ShareCompat.IntentBuilder(requireContext())
			.setType(Constants.MIME_TYPE_IMAGE)
			.addStream(uri)
			.setChooserTitle(R.string.share_item)
			.startChooser()
	}

	private fun copyMediaItem(position: Int) {
		val uri = images[position].uri ?: return
		requireContext().copyUriToClipboard(R.string.image_item, uri, R.string.image_copied_msg)
	}

	private fun downloadMediaItem(position: Int) {
		val uri = images[position].uri ?: return
		TODO("Save $uri to disk :)")
	}

	private fun onItemRemoved(position: Int) {
		val itemToDelete = images[position]
		val newImages = images - itemToDelete
		if (newImages.isNotEmpty()) {
			pageSelectedCallback.size = newImages.size
			pageSelectedCallback.onPageSelected(position)
		}
		else {
			sharedViewModel.currentPosition = 0
		}
		itemsToDelete.add(itemToDelete)
	}

	private inner class MediaMenuProvider : MenuProvider {
		override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
			menuInflater.inflate(R.menu.menu_image_full_screen, menu)
		}

		override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
			return when (menuItem.itemId) {
				android.R.id.home -> {
					onBackPressed(); true
				}
				R.id.share -> {
					shareMediaItem(binding.pager.currentItem); true
				}
				R.id.copy -> {
					copyMediaItem(binding.pager.currentItem); true
				}
				R.id.download -> {
					downloadMediaItem(binding.pager.currentItem); true
				}
				R.id.delete -> {
					mediaStateAdapter.removeFragment(binding.pager.currentItem); true
				}
				else -> false
			}
		}
	}

	private inner class PageSelectedCallback(var size: Int) : ViewPager2.OnPageChangeCallback() {

		override fun onPageSelected(position: Int) {
			supportActionBar?.title = "${position + 1}/$size"
			sharedViewModel.currentPosition = position
		}
	}
}
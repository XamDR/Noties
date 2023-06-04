package io.github.xamdr.noties.ui.media

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.annotation.OptIn
import androidx.core.app.ShareCompat
import androidx.core.app.SharedElementCallback
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.media3.common.Player
import androidx.media3.common.MediaItem as ExoPlayerMediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import dagger.hilt.android.AndroidEntryPoint
import io.github.xamdr.noties.R
import io.github.xamdr.noties.databinding.FragmentMediaViewerBinding
import io.github.xamdr.noties.domain.model.MediaItem
import io.github.xamdr.noties.ui.helpers.*

@AndroidEntryPoint
class MediaViewerFragment : Fragment() {

	private var _binding: FragmentMediaViewerBinding? = null
	private val binding get() = _binding!!
	private val sharedViewModel by hiltNavGraphViewModels<MediaViewerViewModel>(R.id.nav_editor)
	private val items by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getParcelableArrayListCompat(Constants.BUNDLE_ITEMS, MediaItem::class.java)
	}
	private lateinit var mediaStateAdapter: MediaStateAdapter
	private lateinit var pageSelectedCallback: PageSelectedCallback
	private val menuProvider = MediaMenuProvider()
	private val itemsToDelete = mutableListOf<MediaItem>()
	var player: ExoPlayer? = null
	private lateinit var mediaSourceFactory: DefaultDataSource.Factory

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		mediaStateAdapter = MediaStateAdapter(this, items, this::onItemRemoved)
		pageSelectedCallback = PageSelectedCallback(items.size)
		player = ExoPlayer.Builder(requireContext()).build()
		mediaSourceFactory = DefaultDataSource.Factory(requireContext())
		if (savedInstanceState != null) {
			val restoredItemsToDelete = savedInstanceState.getParcelableArrayListCompat(
				Constants.BUNDLE_ITEMS_DELETE,
				MediaItem::class.java
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
			setNavigationResult(Constants.BUNDLE_ITEMS_DELETE, ArrayList(itemsToDelete))
			findNavController().popBackStack()
		}
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		outState.putParcelableArrayList(Constants.BUNDLE_ITEMS_DELETE, ArrayList(itemsToDelete))
	}

	override fun onStop() {
		super.onStop()
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			releasePlayer()
		}
	}

	override fun onPause() {
		super.onPause()
		binding.pager.unregisterOnPageChangeCallback(pageSelectedCallback)
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
			releasePlayer()
		}
	}

	override fun onResume() {
		super.onResume()
		binding.pager.registerOnPageChangeCallback(pageSelectedCallback)
	}

	@OptIn(UnstableApi::class)
	fun addMediaSource(player: ExoPlayer, src: Uri, listener: Player.Listener) {
		val mediaSource = ProgressiveMediaSource.Factory(mediaSourceFactory).createMediaSource(ExoPlayerMediaItem.fromUri(src))
		player.apply {
			setMediaSource(mediaSource)
			addListener(listener)
			prepare()
			play()
		}
	}

	private fun prepareSharedElementEnterTransition() {
		sharedElementEnterTransition = inflateTransition(R.transition.shared_image_transition)
		setEnterSharedElementCallback(object : SharedElementCallback() {
			override fun onMapSharedElements(names: MutableList<String>?, sharedElements: MutableMap<String, View>?) {
				if (sharedViewModel.currentPosition != 0) {
					val currentFragment = mediaStateAdapter.findFragmentByPosition(sharedViewModel.currentPosition) ?: return
					val view = currentFragment.view ?: return
					if (!names.isNullOrEmpty() && !sharedElements.isNullOrEmpty()) {
						sharedElements[names[0]] = view.findViewById(R.id.image) ?: view.findViewById(R.id.thumbnail)
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
		val uri = items[position].uri ?: return
		ShareCompat.IntentBuilder(requireContext())
			.setType(Constants.MIME_TYPE_IMAGE)
			.addStream(uri)
			.setChooserTitle(R.string.share_item)
			.startChooser()
	}

	private fun copyMediaItem(position: Int) {
		val uri = items[position].uri ?: return
		requireContext().copyUriToClipboard(R.string.image_item, uri, R.string.image_copied_msg)
	}

	private fun downloadMediaItem(position: Int) {
		val uri = items[position].uri ?: return
		TODO("Save $uri to disk :)")
	}

	private fun onItemRemoved(position: Int) {
		val itemToDelete = items[position]
		val newImages = items - itemToDelete
		if (newImages.isNotEmpty()) {
			pageSelectedCallback.size = newImages.size
			pageSelectedCallback.onPageSelected(position)
		}
		else {
			sharedViewModel.currentPosition = 0
		}
		itemsToDelete.add(itemToDelete)
	}

	private fun releasePlayer() {
		player?.release()
		player = null
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
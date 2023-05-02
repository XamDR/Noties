package io.github.xamdr.noties.ui.editor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform
import dagger.hilt.android.AndroidEntryPoint
import io.github.xamdr.noties.R
import io.github.xamdr.noties.databinding.FragmentEditorBinding
import io.github.xamdr.noties.domain.model.Image
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.ui.editor.todos.DragDropCallback
import io.github.xamdr.noties.ui.helpers.*
import io.github.xamdr.noties.ui.image.BitmapCache
import io.github.xamdr.noties.ui.image.ImageAdapter
import io.github.xamdr.noties.ui.image.ImageStorageManager
import timber.log.Timber
import com.google.android.material.R as Material

@AndroidEntryPoint
class EditorFragment : Fragment(), NoteContentListener, EditorMenuListener {

	private var _binding: FragmentEditorBinding? = null
	private val binding get() = _binding!!
	private val viewModel by viewModels<EditorViewModel>()
	private val noteId by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getLong(Constants.BUNDLE_NOTE_ID, 0L)
	}
//	private val tagId by lazy(LazyThreadSafetyMode.NONE) {
//		requireArguments().getInt(Constants.BUNDLE_TAG_ID, 0)
//	}
	private lateinit var note: Note
	private lateinit var textAdapter: EditorTextAdapter
	private lateinit var concatAdapter: ConcatAdapter
	private val imageAdapter = ImageAdapter(this::navigateToGallery)
	private val menuProvider = EditorMenuProvider()
	private val itemTouchHelper = ItemTouchHelper(DragDropCallback())

	private val pickeMediaLauncher = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
		addImages(uris)
	}

	override fun onCreateView(inflater: LayoutInflater,
							  container: ViewGroup?,
							  savedInstanceState: Bundle?): View {
		_binding = FragmentEditorBinding.inflate(inflater, container, false)
		initTransitions(noteId)
		addMenuProvider(menuProvider, viewLifecycleOwner)
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setupNote()
		setupListeners()
		onBackPressed { if (::note.isInitialized) saveNote(note) }
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		viewModel.saveState(note)
	}

	override fun onNoteTextChanged(text: String) {
		note = note.copy(text = text)
	}

	override fun onNoteTitleChanged(title: String) {
		note = note.copy(title = title)
	}

	override fun onLinkClicked(url: String) {
		binding.root.showSnackbarWithAction(url, actionText = R.string.open_url) {
			startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
		}
	}

	override fun onAttachMediaFile() = pickeMediaLauncher.launch(arrayOf("image/*"))

	private fun navigateUp() {
		binding.root.hideSoftKeyboard()
		requireActivity().onBackPressedDispatcher.onBackPressed()
	}

	private fun setupNote() {
		launch {
			note = viewModel.getNote(noteId)
			textAdapter = EditorTextAdapter(note, this@EditorFragment)
			concatAdapter = ConcatAdapter(imageAdapter, textAdapter)
			setupRecyclerView()
			if (note.images.isNotEmpty()) {
				imageAdapter.submitList(note.images)
			}
		}
	}

	private fun setupRecyclerView() {
		binding.content.apply {
			adapter = concatAdapter
			(layoutManager as GridLayoutManager).spanSizeLookup =
				ConcatSpanSizeLookup(Constants.SPAN_COUNT) { concatAdapter.adapters }
			addItemTouchHelper(itemTouchHelper)
		}
	}

	private fun setupListeners() {
		binding.add.setOnClickListener {
			val menuDialog = EditorMenuFragment().apply {
				setEditorMenuListener(this@EditorFragment)
			}
			showDialog(menuDialog, Constants.MENU_DIALOG_TAG)
		}
	}

	private fun initTransitions(noteId: Long) {
		if (noteId == 0L) {
			enterTransition = MaterialContainerTransform().apply {
				startView = requireActivity().findViewById(R.id.fab)
				addTarget(binding.root)
				endContainerColor = requireContext().getThemeColor(R.attr.colorSurface)
				setAllContainerColors(MaterialColors.getColor(binding.root, Material.attr.colorSurface))
				setPathMotion(MaterialArcMotion())
				duration = resources.getInteger(R.integer.motion_duration_large).toLong()
				interpolator = FastOutSlowInInterpolator()
				fadeMode = MaterialContainerTransform.FADE_MODE_IN
			}
		}
		else {
			binding.root.transitionName = noteId.toString()
			sharedElementEnterTransition = MaterialContainerTransform().apply {
				drawingViewId = R.id.nav_host_fragment
				duration = resources.getInteger(R.integer.motion_duration_large).toLong()
				interpolator = FastOutSlowInInterpolator()
				fadeMode = MaterialContainerTransform.FADE_MODE_IN
				setAllContainerColors(MaterialColors.getColor(binding.root, Material.attr.colorSurface))
			}
		}
	}

	private fun saveNote(note: Note) {
		launch {
			when (viewModel.saveNote(note, noteId)) {
				NoteAction.DeleteEmptyNote -> {
					ImageStorageManager.deleteImages(requireContext(), note.images)
					binding.root.showSnackbar(R.string.empty_note_deleted).showOnTop()
				}
				NoteAction.InsertNote -> binding.root.showSnackbar(R.string.note_saved).showOnTop()
				NoteAction.NoAction -> {}
				NoteAction.UpdateNote -> binding.root.showSnackbar(R.string.note_updated).showOnTop()
			}
			findNavController().popBackStack()
		}
	}

	private fun addImages(uris: List<Uri>) {
		if (uris.isEmpty()) return
		launch {
			val images = mutableListOf<Image>()
			for (uri in uris) {
				val newUri = UriHelper.copyUri(requireContext(), uri)
				val image = Image(
					uri = newUri,
					mimeType = requireContext().getUriMimeType(newUri),
					noteId = note.id
				)
				images.add(image)
			}
			note = note.copy(images = note.images + images)
			Timber.d("Images: %s", note.images)
			imageAdapter.submitList(note.images)
		}
	}

	private fun navigateToGallery(images: List<Image>, position: Int) {
		BitmapCache.Instance.clear()
		val args = bundleOf(
			Constants.BUNDLE_IMAGES to images,
			Constants.BUNDLE_POSITION to position
		)
		findNavController().tryNavigate(R.id.action_editor_gallery, args)
	}

	private inner class EditorMenuProvider : MenuProvider {
		override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
			menuInflater.inflate(R.menu.menu_editor, menu)
		}

		override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
			return when (menuItem.itemId) {
				android.R.id.home -> {
					navigateUp(); true
				}
				else -> false
			}
		}
	}
}
package io.github.xamdr.noties.ui.editor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.core.view.doOnPreDraw
import androidx.documentfile.provider.DocumentFile
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
import io.github.xamdr.noties.data.entity.media.MediaType
import io.github.xamdr.noties.databinding.FragmentEditorBinding
import io.github.xamdr.noties.domain.model.MediaItem
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.ui.editor.media.MediaItemAdapter
import io.github.xamdr.noties.ui.editor.media.RecordVideoLauncher
import io.github.xamdr.noties.ui.editor.media.TakePictureLauncher
import io.github.xamdr.noties.ui.editor.todos.DragDropCallback
import io.github.xamdr.noties.ui.helpers.*
import io.github.xamdr.noties.ui.helpers.media.MediaHelper
import io.github.xamdr.noties.ui.helpers.media.MediaStorageManager
import timber.log.Timber
import java.io.FileNotFoundException
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
	private val mediaItemAdapter = MediaItemAdapter(this::navigateToMediaViewer)
	private val menuProvider = EditorMenuProvider()
	private val itemTouchHelper = ItemTouchHelper(DragDropCallback())
	private val pickeMediaLauncher = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
		addMediaItems(uris)
	}
	private lateinit var takePictureLauncher: TakePictureLauncher
	private lateinit var recordVideoLauncher: RecordVideoLauncher
	private val openFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
		readFileContent(uri)
	}
	private var fileUri: Uri? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		takePictureLauncher = TakePictureLauncher(
			requireContext(),
			activityResultRegistry,
			onSuccess = { cameraUri -> addMediaItems(listOf(cameraUri)) },
			onError = { binding.root.showSnackbar(R.string.error_take_picture) }
		)
		recordVideoLauncher = RecordVideoLauncher(
			requireContext(),
			activityResultRegistry,
			onSuccess = { videoUri -> addMediaItems(listOf(videoUri)) },
			onError = { binding.root.showSnackbar(R.string.error_take_video) }
		)
		lifecycle.addObserver(takePictureLauncher)
		lifecycle.addObserver(recordVideoLauncher)
	}

	override fun onCreateView(inflater: LayoutInflater,
							  container: ViewGroup?,
							  savedInstanceState: Bundle?): View {
		_binding = FragmentEditorBinding.inflate(inflater, container, false)
		initTransitions(noteId)
		postponeEnterTransition()
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
		if (::note.isInitialized && note.text.length <= Constants.MAX_TEXT_LIMIT) {
			viewModel.saveState(note)
		}
		else {
			Timber.d("Text is too big to be saved into bundle.")
			fileUri?.let { outState.putParcelable(Constants.BUNDLE_FILE_URI, it) }
		}
	}

	override fun onViewStateRestored(savedInstanceState: Bundle?) {
		super.onViewStateRestored(savedInstanceState)
		if (savedInstanceState != null && savedInstanceState.containsKey(Constants.BUNDLE_FILE_URI)) {
			fileUri = savedInstanceState.getParcelableCompat(Constants.BUNDLE_FILE_URI, Uri::class.java)
			readFileContent(fileUri)
		}
	}

	override fun onNoteTextChanged(text: String) {
		note = note.copy(text = text)
		requireActivity().invalidateMenu()
	}

	override fun onNoteTitleChanged(title: String) {
		note = note.copy(title = title)
	}

	override fun onLinkClicked(url: String) {
		binding.root.showSnackbarWithAction(url, actionText = R.string.open_url) {
			startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
		}
	}

	override fun onAttachMediaFiles() = pickeMediaLauncher.launch(arrayOf("image/*", "video/*"))

	override fun onTakePicture() = takePictureLauncher.launch()

	override fun onTakeVideo() = recordVideoLauncher.launch()

	private fun navigateUp() {
		binding.root.hideSoftKeyboard()
		onBackPressed()
	}

	private fun setupNote() {
		launch {
			if (!::note.isInitialized) {
				note = viewModel.getNote(noteId)
			}
//			getNavigationResult<ArrayList<MediaItem>>(Constants.BUNDLE_ITEMS_DELETE)?.let { itemsToDelete ->
//				note = note.copy(items = note.items - itemsToDelete.toSet())
//				requireActivity().invalidateMenu()
//			}
			textAdapter = EditorTextAdapter(note, this@EditorFragment).apply {
				setOnContentReceivedListener { uri -> addMediaItems(listOf(uri)) }
			}
			concatAdapter = ConcatAdapter(mediaItemAdapter, textAdapter)
			setupViews(note)
		}
	}

	private fun setupViews(note: Note) {
		binding.rvContent.apply {
			adapter = concatAdapter
			(layoutManager as GridLayoutManager).spanSizeLookup =
				ConcatSpanSizeLookup(Constants.SPAN_COUNT) { concatAdapter.adapters }
			addItemTouchHelper(itemTouchHelper)
		}
		mediaItemAdapter.submitList(note.items)
		binding.txtModificationDate.text = DateTimeHelper.formatCurrentDateTime(note.modificationDate)
		binding.root.doOnPreDraw { startPostponedEnterTransition() }
		addMenuProvider(menuProvider, viewLifecycleOwner)
	}

	private fun setupListeners() {
		binding.btnAdd.setOnClickListener {
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
			Timber.d("Note: %s", note)
			when (viewModel.saveNote(note, noteId)) {
				NoteAction.DeleteEmptyNote -> {
					MediaStorageManager.deleteItems(requireContext(), note.items)
					binding.root.showSnackbar(R.string.empty_note_deleted).showOnTop()
				}
				NoteAction.InsertNote -> binding.root.showSnackbar(R.string.note_saved).showOnTop()
				NoteAction.NoAction -> {}
				NoteAction.UpdateNote -> {
//					getNavigationResult<ArrayList<MediaItem>>(Constants.BUNDLE_ITEMS_DELETE)?.let { itemsToDelete ->
//						MediaStorageManager.deleteItems(requireContext(), itemsToDelete)
//						viewModel.deleteItems(itemsToDelete)
//					}

					binding.root.showSnackbar(R.string.note_updated).showOnTop()
				}
			}
			findNavController().popBackStack()
		}
	}

	private fun addMediaItems(uris: List<Uri>) {
		if (uris.isEmpty()) return
		launch {
			binding.progressIndicator.show()
			val items = mutableListOf<MediaItem>()
			for (uri in uris) {
				val newUri = MediaHelper.copyUri(requireContext(), uri)
				val mimeType = MediaHelper.getMediaMimeType(requireContext(), newUri)
				val item: MediaItem
				if (MediaHelper.isImage(requireContext(), newUri)) {
					item = MediaItem(
						uri = newUri,
						mimeType = mimeType,
						mediaType = MediaType.Image,
						noteId = note.id
					)
				}
				else {
					val metadata = MediaHelper.getMediaItemMetadata(requireContext(), newUri)
					item = MediaItem(
						uri = newUri,
						mimeType = mimeType,
						mediaType = MediaType.Video,
						metadata = metadata,
						noteId = note.id
					)
				}
				items.add(item)
			}
			note = note.copy(items = note.items + items)
			mediaItemAdapter.submitList(note.items)
			binding.progressIndicator.hide()
			requireActivity().invalidateMenu()
		}
	}

	private fun navigateToMediaViewer(view: View, position: Int) {
		BitmapCache.Instance.clear()
		val args = bundleOf(
			Constants.BUNDLE_ITEMS to note.items,
			Constants.BUNDLE_POSITION to position
		)
		findNavController().tryNavigate(R.id.action_editor_media_viewer, args)
	}

	override fun onNoteContentLoading() = ProgressDialogHelper.show(requireContext(), getString(R.string.loading_text))

	override fun onNoteContentLoaded() = ProgressDialogHelper.dismiss()

	private fun readFileContent(uri: Uri?) {
		if (uri != null) {
			try {
				launch {
					fileUri = uri
					val file = DocumentFile.fromSingleUri(requireContext(), uri)
					val text = UriHelper.readTextFromUri(requireContext(), uri)
					note = note.copy(title = file?.simpleName ?: String.Empty, text = text)
					textAdapter.submitNote(note)
					requireActivity().invalidateMenu()
				}
			}
			catch (e: FileNotFoundException) {
				Timber.e(e)
				binding.root.showSnackbar(R.string.error_open_file)
			}
		}
	}

	private inner class EditorMenuProvider : MenuProvider {
		override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
			menuInflater.inflate(R.menu.menu_editor, menu)
		}

		override fun onPrepareMenu(menu: Menu) {
			val menuItem = menu.findItem(R.id.share_content)
			menuItem.isVisible = !note.isEmpty()
			menuItem.isEnabled = !note.isEmpty()
		}

		override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
			return when (menuItem.itemId) {
				android.R.id.home -> {
					navigateUp(); true
				}
				R.id.share_content -> {
					ShareHelper.shareContent(requireContext(), note); true
				}
				R.id.open_file -> {
					binding.root.hideSoftKeyboard()
					openFileLauncher.launch(arrayOf(Constants.MIME_TYPE_TEXT)); true
				}
				else -> false
			}
		}
	}
}
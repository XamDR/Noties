package io.github.xamdr.noties.ui.editor

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.View.OnLayoutChangeListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.SharedElementCallback
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.core.view.doOnPreDraw
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
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
import io.github.xamdr.noties.ui.image.BitmapHelper
import io.github.xamdr.noties.ui.image.ImageAdapter
import io.github.xamdr.noties.ui.image.ImageStorageManager
import io.github.xamdr.noties.ui.media.MediaViewerViewModel
import timber.log.Timber
import java.io.FileNotFoundException
import com.google.android.material.R as Material

@AndroidEntryPoint
class EditorFragment : Fragment(), NoteContentListener, EditorMenuListener {

	private var _binding: FragmentEditorBinding? = null
	private val binding get() = _binding!!
	private val viewModel by viewModels<EditorViewModel>()
	private val sharedViewModel by hiltNavGraphViewModels<MediaViewerViewModel>(R.id.nav_editor)
	private val noteId by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getLong(Constants.BUNDLE_NOTE_ID, 0L)
	}
//	private val tagId by lazy(LazyThreadSafetyMode.NONE) {
//		requireArguments().getInt(Constants.BUNDLE_TAG_ID, 0)
//	}
	private lateinit var note: Note
	private lateinit var textAdapter: EditorTextAdapter
	private lateinit var concatAdapter: ConcatAdapter
	private val imageAdapter = ImageAdapter(this::navigateToMediaViewer)
	private val menuProvider = EditorMenuProvider()
	private val itemTouchHelper = ItemTouchHelper(DragDropCallback())
	private val pickeMediaLauncher = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
		addImages(uris)
	}
	private lateinit var cameraUri: Uri
	private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
		savePicture(success)
	}
	private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
		onPermissionRequested(granted)
	}
	private val openFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
		readFileContent(uri)
	}
	private var fileUri: Uri? = null

	override fun onCreateView(inflater: LayoutInflater,
							  container: ViewGroup?,
							  savedInstanceState: Bundle?): View {
		_binding = FragmentEditorBinding.inflate(inflater, container, false)
		initTransitions(noteId)
		prepareSharedElementExitTransition()
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
		if (note.text.length <= 65536) {
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

	override fun onAttachMediaFile() = pickeMediaLauncher.launch(arrayOf("image/*"))

	override fun onTakePicture() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			takePicture()
		}
		else {
			if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
				PackageManager.PERMISSION_GRANTED) {
				PermissionRationaleDialog.createFor(
					requireContext(),
					R.string.write_external_storage_permission_rationale,
					R.drawable.ic_external_storage
				)
				.setNegativeButton(R.string.not_now_button, null)
				.setPositiveButton(R.string.continue_button) { _, _ ->
					requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
				}.show()
			}
			else {
				takePicture()
			}
		}
	}

	private fun navigateUp() {
		binding.root.hideSoftKeyboard()
		onBackPressed()
	}

	private fun setupNote() {
		launch {
			if (!::note.isInitialized) {
				note = viewModel.getNote(noteId)
			}
			getNavigationResult<ArrayList<Image>>(Constants.BUNDLE_IMAGES)?.let { itemsToDelete ->
				note = note.copy(images = note.images - itemsToDelete.toSet())
				requireActivity().invalidateMenu()
			}
			textAdapter = EditorTextAdapter(note, this@EditorFragment).apply {
				setOnContentReceivedListener { uri -> addImages(listOf(uri)) }
			}
			concatAdapter = ConcatAdapter(imageAdapter, textAdapter)
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
		imageAdapter.submitList(note.images)
		binding.txtModificationDate.text = DateTimeHelper.formatCurrentDateTime(note.modificationDate)
		binding.root.doOnPreDraw { startPostponedEnterTransition() }
		if (sharedViewModel.currentPosition == 0) {
			supportActionBar?.setTitle(R.string.editor_fragment_label)
		}
		addMenuProvider(menuProvider, viewLifecycleOwner)
	}

	private fun setupListeners() {
		binding.btnAdd.setOnClickListener {
			val menuDialog = EditorMenuFragment().apply {
				setEditorMenuListener(this@EditorFragment)
			}
			showDialog(menuDialog, Constants.MENU_DIALOG_TAG)
		}
		binding.rvContent.addOnLayoutChangeListener(object : OnLayoutChangeListener {
			override fun onLayoutChange(
				v: View?,
				left: Int,
				top: Int,
				right: Int,
				bottom: Int,
				oldLeft: Int,
				oldTop: Int,
				oldRight: Int,
				oldBottom: Int
			) {
				binding.rvContent.removeOnLayoutChangeListener(this)
				val layoutManager = binding.rvContent.layoutManager ?: return
				val viewAtPosition = layoutManager.findViewByPosition(sharedViewModel.currentPosition)
				if (viewAtPosition == null ||
					layoutManager.isViewPartiallyVisible(viewAtPosition, false, true)) {
					binding.rvContent.post { layoutManager.scrollToPosition(sharedViewModel.currentPosition) }
				}
			}
		})
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
					ImageStorageManager.deleteImages(requireContext(), note.images)
					binding.root.showSnackbar(R.string.empty_note_deleted).showOnTop()
				}
				NoteAction.InsertNote -> binding.root.showSnackbar(R.string.note_saved).showOnTop()
				NoteAction.NoAction -> {}
				NoteAction.UpdateNote -> {
					getNavigationResult<ArrayList<Image>>(Constants.BUNDLE_IMAGES)?.let { itemsToDelete ->
						ImageStorageManager.deleteImages(requireContext(), itemsToDelete)
						viewModel.deleteImages(itemsToDelete)
					}
					binding.root.showSnackbar(R.string.note_updated).showOnTop()
				}
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
			imageAdapter.submitList(note.images)
			requireActivity().invalidateMenu()
		}
	}

	private fun takePicture() {
		val savedUri = BitmapHelper.savePicture(requireContext()) ?: return
		cameraUri = savedUri
		takePictureLauncher.launch(cameraUri)
	}

	private fun savePicture(success: Boolean) {
		if (success && ::cameraUri.isInitialized) {
			addImages(listOf(cameraUri))
		}
		else {
			binding.root.showSnackbar(R.string.error_take_picture)
		}
	}

	private fun onPermissionRequested(granted: Boolean) {
		if (granted) {
			takePicture()
		}
		else {
			binding.root.showSnackbar(R.string.permission_denied)
		}
	}

	private fun prepareSharedElementExitTransition() {
		setExitSharedElementCallback(object : SharedElementCallback() {
			override fun onMapSharedElements(names: MutableList<String>?, sharedElements: MutableMap<String, View>?) {
				if (sharedViewModel.currentPosition != RecyclerView.NO_POSITION) {
					val viewHolder = binding.rvContent.findViewHolderForAdapterPosition(sharedViewModel.currentPosition)
					if (viewHolder?.itemView == null) return
					if (!names.isNullOrEmpty() && !sharedElements.isNullOrEmpty()) {
						sharedElements[names[0]] = viewHolder.itemView.findViewById(R.id.image)
					}
				}
			}
		})
	}

	private fun navigateToMediaViewer(view: View, position: Int) {
		BitmapCache.Instance.clear()
		sharedViewModel.currentPosition = position
		val args = bundleOf(Constants.BUNDLE_IMAGES to note.images)
		val item = note.images[position]
		val extras = FragmentNavigatorExtras(view to item.id.toString())
		findNavController().tryNavigate(R.id.action_editor_media_viewer, args, null, extras)
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
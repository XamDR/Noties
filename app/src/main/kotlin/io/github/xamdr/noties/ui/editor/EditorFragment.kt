package io.github.xamdr.noties.ui.editor

import android.Manifest
import android.app.Activity
import android.app.KeyguardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.transition.MaterialContainerTransform
import dagger.hilt.android.AndroidEntryPoint
import io.github.xamdr.noties.R
import io.github.xamdr.noties.core.ImageEntity
import io.github.xamdr.noties.core.Note
import io.github.xamdr.noties.core.Todo
import io.github.xamdr.noties.databinding.FragmentEditorBinding
import io.github.xamdr.noties.ui.editor.todos.DragDropCallback
import io.github.xamdr.noties.ui.editor.todos.TodoItemAdapter
import io.github.xamdr.noties.ui.helpers.*
import io.github.xamdr.noties.ui.image.*
import io.github.xamdr.noties.ui.notes.NotesFragment
import io.github.xamdr.noties.ui.reminders.DateTimePickerDialogFragment
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.time.ZonedDateTime

@AndroidEntryPoint
class EditorFragment : Fragment(), AttachImagesListener, LinkClickedListener,
	ImageItemContextMenuListener, ToolbarItemMenuListener, OpenFileListener {

	private var _binding: FragmentEditorBinding? = null
	private val binding get() = _binding!!
	private val viewModel by viewModels<EditorViewModel>()
	private val notebookId by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getInt(NotesFragment.ID, 1)
	}
	private val imageAdapter = ImageAdapter(this)
	private lateinit var textAdapter: EditorTextAdapter
	private lateinit var todoItemAdapter: TodoItemAdapter
	private lateinit var concatAdapter: ConcatAdapter
	private val pickImagesLauncher = registerForActivityResult(
		ActivityResultContracts.OpenMultipleDocuments(),
		PickImagesCallback(this)
	)
	private val deviceCredentialLauncher = registerForActivityResult(
		ActivityResultContracts.StartActivityForResult()
	) { result -> activityResultCallback(result) }

	private val requestPermissionLauncher = registerForActivityResult(
		ActivityResultContracts.RequestPermission(),
		RequestExternalPermissionCallback { takePicture() }.apply {
			setOnPermissionDeniedListener { binding.root.showSnackbar(R.string.permission_denied) }
		}
	)
	private val takePictureLauncher = registerForActivityResult(
		ActivityResultContracts.TakePicture()
	) { success -> takePictureCallback(success) }

	private lateinit var tempUri: Uri
	private lateinit var menuItemClickListener: MenuItemClickListener
	private val openFileLauncher = registerForActivityResult(
		ActivityResultContracts.OpenDocument(),
		OpenFileCallback(this)
	)
	private val itemTouchHelper = ItemTouchHelper(DragDropCallback())

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		menuItemClickListener = MenuItemClickListener(this, viewModel.note)
		initializeAdapters()
		addChildHeadlessFragments()
		window.setStatusBarColor(viewModel.entity.color)
	}

	override fun onCreateView(inflater: LayoutInflater,
							  container: ViewGroup?,
							  savedInstanceState: Bundle?): View {
		_binding = FragmentEditorBinding.inflate(inflater, container, false).apply {
//			fragment = this@EditorFragment
//			vm = viewModel
		}
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		checkIfNoteIsProtected()
		initTransition()
		onBackPressed()
		binding.topToolbar.setOnMenuItemClickListener(menuItemClickListener)
		setupRecyclerView()
		updateToolbarsUI()
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		viewModel.saveState()
	}

	override fun addImages(uris: List<Uri>) {
		viewLifecycleOwner.lifecycleScope.launch {
			val fragment = childFragmentManager.findFragmentByTag(ADD_IMAGES_TAG) as AddImagesFragment
			fragment.addImages(uris)
			imageAdapter.submitList(viewModel.note.images)
			binding.topToolbar.findItem(R.id.delete_images).isVisible = true
		}
	}

	override fun onLinkClicked(url: String) {
		binding.root.showSnackbar(url, action = R.string.open_url) {
			startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
		}
	}

	override fun copyImage(position: Int) {
		val uri = viewModel.note.images[position].uri
		if (uri != null) {
			requireContext().copyUriToClipboard(R.string.image_item, uri, R.string.image_copied_msg)
		}
	}

	override fun addAltText(position: Int) {
		val image = viewModel.note.images[position]
		val imageDescriptionDialog = ImageDescriptionDialogFragment.newInstance(image).apply {
			setOnAltTextAddedListener { context?.showToast(R.string.alt_text_updated) }
		}
		showDialog(imageDescriptionDialog, ALT_TEXT_DIALOG_TAG)
	}

	override fun deleteImage(position: Int) {
		val imageToBeDeleted = viewModel.note.images[position]
		deleteImage(imageToBeDeleted)
		imageAdapter.submitList(viewModel.note.images)
		if (viewModel.note.images.isEmpty()) {
			binding.topToolbar.findItem(R.id.delete_images).isVisible = false
		}
	}

	override fun shareContent() {
		val fragment = childFragmentManager.findFragmentByTag(SHARE_CONTENT_TAG) as ShareContentFragment
		fragment.shareContent()
	}

	override fun lockNote() {
		viewModel.updateNote(viewModel.entity.copy(isProtected = true))
		binding.root.showSnackbar(R.string.note_locked)
	}

	override fun unlockNote() {
		viewModel.updateNote(viewModel.entity.copy(isProtected = false))
		binding.root.showSnackbar(R.string.note_unlocked)
	}

	override fun pinNote() {
		viewModel.updateNote(viewModel.entity.copy(isPinned = true))
		binding.root.showSnackbar(R.string.note_pinned)
	}

	override fun unpinNote() {
		viewModel.updateNote(viewModel.entity.copy(isPinned = false))
		binding.root.showSnackbar(R.string.note_unpinned)
	}

	override fun showDeleteImagesDialog() {
		val deleteImagesDialog = DeleteImagesDialogFragment().apply {
			setOnDeleteImagesListener { deleteAllImages() }
		}
		showDialog(deleteImagesDialog, DELETE_IMAGES_DIALOG_TAG)
	}

	override fun openTextFile() = openFileLauncher.launch(arrayOf(MIME_TYPE_TEXT))

	override fun readFileContent(uri: Uri?) {
		try {
			if (uri != null) {
				val inputStream = requireContext().contentResolver.openInputStream(uri)
				val file = DocumentFile.fromSingleUri(requireContext(), uri)
				inputStream?.bufferedReader()?.use { reader ->
					binding.noteTitle.setText(file?.simpleName)
					viewModel.updateNote(viewModel.entity.copy(text = reader.readText()))
				}
				textAdapter.notifyItemChanged(0)
			}
		}
		catch (e: FileNotFoundException) {
			printError(IO, e.message)
			context?.showToast(R.string.error_open_file)
		}
	}

	override fun hideTodoList() {
		if (concatAdapter.removeAdapter(todoItemAdapter)) {
			viewModel.updateNote(viewModel.entity.copy(
				text = todoItemAdapter.joinToString(),
				isTodoList = false
			))
			initializeTextAdapter()
			concatAdapter.addAdapter(textAdapter)
			binding.topToolbar.findItem(R.id.hide_todos).isVisible = false
			binding.topToolbar.findItem(R.id.open_file).isVisible = true
		}
	}

	override fun showBottomSheetColorDialog() {
		val colorDialog = EditorColorFragment().apply {
			setOnColorSelectedListener { color -> binding.root.setBackgroundColor(color) }
		}
		showDialog(colorDialog, COLOR_DIALOG_TAG)
	}

	override fun showBottomSheetMenuDialog() {
		val menuDialog = EditorMenuFragment().apply {
			setOnActivityResultListener { pickImagesLauncher.launch(arrayOf(MIME_TYPE_IMAGE)) }
			setOnTakePictureListener { takePictureOrRequestPermission() }
			setOnMakeTodoListListener { makeTodoList() }
			setOnShowDateTimePickerListener { showDateTimePickerDialog() }
		}
		showDialog(menuDialog, MENU_DIALOG_TAG)
	}

	fun navigateUp() {
		binding.root.hideSoftKeyboard()
		requireActivity().onBackPressed()
	}

	fun afterTextChanged(s: Editable) {
		viewModel.updateNote(viewModel.entity.copy(title = s.toString()))
	}

	private fun checkIfNoteIsProtected() {
		if (viewModel.entity.isProtected) {
			binding.root.isVisible = false
			requestConfirmeDeviceCredential()
		}
	}

	private fun initTransition() {
		enterTransition = MaterialContainerTransform().apply {
			startView = requireActivity().findViewById(R.id.fab)
			endView = binding.root
			endContainerColor = requireContext().getThemeColor(R.attr.colorSurface)
			scrimColor = Color.TRANSPARENT
		}
	}

	private fun setupRecyclerView() {
		binding.content.apply {
			adapter = concatAdapter
			(layoutManager as GridLayoutManager).spanSizeLookup =
				ConcatSpanSizeLookup(SPAN_COUNT) { concatAdapter.adapters }
			addItemTouchHelper(itemTouchHelper)
		}
	}

	private fun updateToolbarsUI() {
		if (viewModel.note.images.isNotEmpty()) {
			imageAdapter.submitList(viewModel.note.images)
			binding.topToolbar.findItem(R.id.delete_images).isVisible = true
		}
		if (viewModel.entity.isTodoList) {
			binding.topToolbar.findItem(R.id.hide_todos).isVisible = true
			binding.topToolbar.findItem(R.id.open_file).isVisible = false
		}
		if (viewModel.entity.isProtected) {
			binding.topToolbar.findItem(R.id.lock_note).apply {
				setIcon(R.drawable.ic_unlock_note)
				setTitle(R.string.unlock_note)
			}
		}
		if (viewModel.entity.isPinned) {
			binding.topToolbar.findItem(R.id.pin_note).apply {
				setIcon(R.drawable.ic_unpin_note)
				setTitle(R.string.unpin_note)
			}
		}
	}

	private fun onBackPressed() {
		requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
			insertOrUpdateNote(viewModel.note, viewModel.tempNote, notebookId)
			findNavController().popBackStack()
			window.setStatusBarColor(null)
		}
	}

	private fun insertOrUpdateNote(note: Note, tempNote: Note, notebookId: Int) {
		if (note.isNonEmpty()) {
			if (note.entity.isTodoList) {
				note.entity = note.entity.copy(text = todoItemAdapter.convertItemsToString())
			}
			if (note != tempNote) {
				note.entity = note.entity.copy(
					modificationDate = ZonedDateTime.now(),
					urls = extractUrls(note.entity.text)
				)
				if (note.entity.id == 0L) {
					note.entity = note.entity.copy(notebookId = notebookId)
					viewModel.insertNote(note) { context?.showToast(R.string.note_saved) }
				}
				else {
					viewModel.updateNote(note) { context?.showToast(R.string.note_updated) }
				}
			}
		}
		else if (note.entity.id != 0L) {
			setNoteToBeDeleted(note)
		}
	}

	@Suppress("DEPRECATION")
	private fun requestConfirmeDeviceCredential() {
		val keyguardManager = requireContext().getSystemService<KeyguardManager>() ?: return
		val intent = keyguardManager.createConfirmDeviceCredentialIntent(
			getString(R.string.confirme_device_credential_title),
			getString(R.string.confirme_device_credential_desc)
		)
		deviceCredentialLauncher.launch(intent)
	}

	private fun activityResultCallback(result: ActivityResult) {
		if (result.resultCode == Activity.RESULT_OK) {
			binding.root.isVisible = true
		}
		else {
			findNavController().popBackStack()
			binding.root.showSnackbar(R.string.error_auth)
		}
	}

	private fun takePictureCallback(result: Boolean) {
		if (result && ::tempUri.isInitialized) {
			addImages(listOf(tempUri))
		}
		else {
			context?.showToast(R.string.error_take_picture)
		}
	}

	private fun takePictureOrRequestPermission() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			takePicture()
		}
		else {
			if (ContextCompat.checkSelfPermission(
					requireContext(),
					Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
				showRationaleDialog()
			}
			else {
				takePicture()
			}
		}
	}

	private fun takePicture() {
		val savedUri = BitmapHelper.savePicture(requireContext()) ?: return
		tempUri = savedUri
		takePictureLauncher.launch(tempUri)
	}

	private fun showRationaleDialog() {
		PermissionRationaleDialog.createFor(
			requireContext(),
			R.string.write_external_storage_permission_rationale,
			R.drawable.ic_external_storage
		).setNegativeButton(R.string.not_now_button, null)
			.setPositiveButton(R.string.continue_button) { _, _ ->
				requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
			}.show()
	}

	private fun deleteAllImages() {
		viewModel.note.images.forEach { deleteImage(it) }
		imageAdapter.submitList(viewModel.note.images)
		binding.topToolbar.findItem(R.id.delete_images).isVisible = false
	}

	private fun deleteImage(image: ImageEntity) {
		viewModel.note.images -= image
		val images = listOf(image)
		val result = ImageStorageManager.deleteImages(requireContext(), images)
		printDebug(IMAGE_STORE_MANAGER, result)

		if (image.id != 0) {
			viewModel.deleteImages(images)
		}
	}

	private fun setNoteToBeDeleted(note: Note) {
		if (note.entity.id != 0L) {
			setFragmentResult(
				REQUEST_KEY,
				bundleOf(EditorViewModel.NOTE to note)
			)
		}
	}

	private fun makeTodoList() {
		if (concatAdapter.removeAdapter(textAdapter)) {
			initializeTodoItemAdapter()
			concatAdapter.addAdapter(todoItemAdapter)
			viewModel.updateNote(viewModel.entity.copy(isTodoList = true))
			binding.topToolbar.findItem(R.id.hide_todos).isVisible = true
			binding.topToolbar.findItem(R.id.open_file).isVisible = false
		}
	}

	private fun showDateTimePickerDialog() {
		val dateTimePickerDialog = DateTimePickerDialogFragment()
		showDialog(dateTimePickerDialog, DATE_TIME_PICKER_DIALOG_TAG)
	}

	private fun addChildHeadlessFragments() {
		if (childFragmentManager.findFragmentByTag(ADD_IMAGES_TAG) == null &&
			childFragmentManager.findFragmentByTag(SHARE_CONTENT_TAG) == null) {
			childFragmentManager.beginTransaction()
				.add(AddImagesFragment(), ADD_IMAGES_TAG)
				.add(ShareContentFragment(), SHARE_CONTENT_TAG)
				.commit()
		}
	}

	private fun initializeAdapters() {
		concatAdapter = if (viewModel.entity.isTodoList) {
			initializeTodoItemAdapter()
			ConcatAdapter(imageAdapter, todoItemAdapter)
		}
		else {
			initializeTextAdapter()
			ConcatAdapter(imageAdapter, textAdapter)
		}
	}

	private fun initializeTextAdapter() {
		textAdapter = EditorTextAdapter(viewModel.note, this).apply {
			setOnContentReceivedListener { uri -> addImages(listOf(uri)) }
		}
	}

	private fun initializeTodoItemAdapter() {
		val todoList = viewModel.note.toTodoList()
		todoItemAdapter = TodoItemAdapter(
			(todoList + Todo.Footer).toMutableList(),
			itemTouchHelper
		)
	}

	companion object {
		const val REQUEST_KEY = "deletion"
		const val SPAN_COUNT = 2
		const val MIME_TYPE_IMAGE = "image/*"
		const val MIME_TYPE_TEXT = "text/plain"
		private const val MENU_DIALOG_TAG = "MENU_DIALOG"
		private const val COLOR_DIALOG_TAG = "COLOR_DIALOG"
		private const val DATE_TIME_PICKER_DIALOG_TAG = "DATE_TIME_PICKER"
		private const val ALT_TEXT_DIALOG_TAG = "ALT_TEXT_DIALOG"
		private const val DELETE_IMAGES_DIALOG_TAG = "DELETE_IMAGES"
		private const val ADD_IMAGES_TAG = "ADD_IMAGES"
		private const val SHARE_CONTENT_TAG = "SHARE_CONTENT"
		private const val IMAGE_STORE_MANAGER = "ImageStoreManager"
		private const val IO = "IO"
	}
}
package net.azurewebsites.noties.ui.editor

import android.Manifest
import android.app.Activity
import android.app.KeyguardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
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
import kotlinx.coroutines.launch
import net.azurewebsites.noties.R
import net.azurewebsites.noties.core.DataItem
import net.azurewebsites.noties.core.ImageEntity
import net.azurewebsites.noties.core.Note
import net.azurewebsites.noties.databinding.FragmentEditorBinding
import net.azurewebsites.noties.ui.editor.todolist.DragDropCallback
import net.azurewebsites.noties.ui.editor.todolist.TodoItemAdapter
import net.azurewebsites.noties.ui.helpers.*
import net.azurewebsites.noties.ui.image.*
import net.azurewebsites.noties.ui.notes.NotesFragment
import net.azurewebsites.noties.ui.urls.JsoupHelper
import java.io.FileNotFoundException
import kotlin.collections.ArrayList

@AndroidEntryPoint
class EditorFragment : Fragment(), AttachImagesListener, LinkClickedListener,
	ImageItemContextMenuListener, ToolbarItemMenuListener, OpenFileListener {

	private var _binding: FragmentEditorBinding? = null
	private val binding get() = _binding!!
	private val viewModel by viewModels<EditorViewModel>()
	private val notebookId by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getInt(NotesFragment.ID, 1)
	}
	private val pickImagesLauncher = registerForActivityResult(
		ActivityResultContracts.OpenMultipleDocuments(),
		PickImagesCallback(this)
	)
	private val imageAdapter = ImageAdapter(this)
	private lateinit var editorTextAdapter: EditorTextAdapter
	private lateinit var concatAdapter: ConcatAdapter
	private val note by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getParcelable(NOTE) ?: Note()
	}
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
	private val menuItemClickListener = MenuItemClickListener(this)
	private val openFileLauncher = registerForActivityResult(
		ActivityResultContracts.OpenDocument(),
		OpenFileCallback(this)
	)
	private val itemTouchHelper = ItemTouchHelper(DragDropCallback())

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		if (savedInstanceState == null) {
			viewModel.note = note
		}
		editorTextAdapter = EditorTextAdapter(viewModel.note, this).apply {
			setOnContentReceivedListener { uri -> addImages(listOf(uri)) }
		}
		concatAdapter = ConcatAdapter(imageAdapter, editorTextAdapter)
	}

	override fun onCreateView(inflater: LayoutInflater,
							  container: ViewGroup?,
							  savedInstanceState: Bundle?): View {
		_binding = FragmentEditorBinding.inflate(inflater, container, false).apply {
			fragment = this@EditorFragment
			vm = viewModel
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
		binding.editorToolbar.setOnMenuItemClickListener(menuItemClickListener)
		setupRecyclerView()
		imageAdapter.submitList(viewModel.note.images)
	}

	override fun addImages(uris: List<Uri>) {
		viewLifecycleOwner.lifecycleScope.launch {
			viewModel.addImages(requireContext(), uris)
			imageAdapter.submitList(viewModel.note.images)
		}
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		viewModel.saveState()
	}

	override fun onLinkClicked(url: String) {
		viewLifecycleOwner.lifecycleScope.launch {
			val urlTitle = JsoupHelper.extractTitle(url) ?: url
			binding.root.showSnackbar(urlTitle, action = R.string.open_url) {
				startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
			}
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
	}

	override fun shareContent() {
		if (viewModel.note.isNonEmpty()) {
			if (viewModel.note.images.isEmpty()) {
				shareText(viewModel.note.entity.text)
			}
			else {
				shareImagesAndText(
					viewModel.note.images.map { it.uri },
					viewModel.note.entity.text
				)
			}
		}
		else {
			context?.showToast(R.string.empty_note_share)
		}
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
					viewModel.note.entity.text = reader.readText()
				}
				editorTextAdapter.notifyItemChanged(0)
			}
		}
		catch (e: FileNotFoundException) {
			printError(TAG, e.message)
			context?.showToast(R.string.error_open_file)
		}
	}

	fun showBottomSheetMenu() {
		val menuDialog = EditorMenuFragment().apply {
			setOnActivityResultListener { pickImagesLauncher.launch(arrayOf(MIME_TYPE_IMAGE)) }
			setOnTakePictureListener { takePictureOrRequestPermission() }
			setOnMakeTodoListListener { makeTodoList() }
		}
		showDialog(menuDialog, MENU_DIALOG_TAG)
	}

	fun navigateUp() {
		binding.root.hideSoftKeyboard()
		requireActivity().onBackPressed()
	}

	private fun checkIfNoteIsProtected() {
		if (note.entity.isProtected) {
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

	private fun onBackPressed() {
		requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
			viewLifecycleOwner.lifecycleScope.launch {
				when (viewModel.insertorUpdateNote(notebookId)) {
					Result.NoteSaved -> context?.showToast(R.string.note_saved)
					Result.NoteUpdated -> context?.showToast(R.string.note_updated)
					Result.EmptyNote -> setNoteToBeDeleted(viewModel.note)
					else -> {}
				}
				findNavController().popBackStack()
			}
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

	private fun shareText(text: String) {
		val shareIntent = Intent().apply {
			action = Intent.ACTION_SEND
			putExtra(Intent.EXTRA_TEXT, text)
			type = MIME_TYPE_TEXT
		}
		startActivity(Intent.createChooser(shareIntent, getString(R.string.chooser_dialog_title)))
	}

	private fun shareImagesAndText(images: List<Uri?>, text: String) {
		val shareIntent = Intent().apply {
			action = Intent.ACTION_SEND_MULTIPLE
			putExtra(Intent.EXTRA_TEXT, text)
			putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(images))
			type = MIME_TYPE_IMAGE
			addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
		}
		startActivity(Intent.createChooser(shareIntent, getString(R.string.chooser_dialog_title)))
	}

	private fun deleteAllImages() {
		viewModel.note.images.forEach { deleteImage(it) }
		imageAdapter.submitList(viewModel.note.images)
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
				bundleOf(NOTE to note)
			)
		}
	}

	private fun makeTodoList() {
		if (concatAdapter.removeAdapter(editorTextAdapter)) {
			val todoList = viewModel.note.toTodoList()
			val todoItemAdapter = TodoItemAdapter(
				(todoList + DataItem.Footer).toMutableList(),
				itemTouchHelper
			)
			concatAdapter.addAdapter(todoItemAdapter)
		}
	}

	companion object {
		const val NOTE = "note"
		const val REQUEST_KEY = "deletion"
		const val SPAN_COUNT = 2
		private const val MENU_DIALOG_TAG = "MENU_DIALOG"
		private const val ALT_TEXT_DIALOG_TAG = "ALT_TEXT_DIALOG"
		private const val DELETE_IMAGES_DIALOG_TAG = "DELETE_IMAGES"
		private const val IMAGE_STORE_MANAGER = "ImageStoreManager"
		private const val MIME_TYPE_IMAGE = "image/*"
		private const val MIME_TYPE_TEXT = "text/plain"
		private const val TAG = "IO"
	}
}
package net.azurewebsites.noties.ui.editor

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialContainerTransform
import kotlinx.coroutines.launch
import net.azurewebsites.noties.R
import net.azurewebsites.noties.databinding.FragmentEditorBinding
import net.azurewebsites.noties.domain.ImageEntity
import net.azurewebsites.noties.domain.Note
import net.azurewebsites.noties.domain.NoteEntity
import net.azurewebsites.noties.ui.helpers.*
import net.azurewebsites.noties.ui.media.MediaItemAdapter
import net.azurewebsites.noties.ui.media.MediaStorageManager
import java.io.File
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class EditorFragment : Fragment() {

	private var _binding: FragmentEditorBinding? = null
	private val binding get() = _binding!!
	private val viewModel by viewModels<EditorViewModel>()
	private lateinit var tempNote: Note
	private lateinit var note: Note
	private lateinit var textEditorMediaItemAdapter: EditorMediaItemAdapter
	private lateinit var textEditorContentAdapter: EditorContentAdapter
	private lateinit var player: ExoPlayer
	private var directoryId: Int = 1

	override fun onAttach(context: Context) {
		super.onAttach(context)
		player = ExoPlayer.Builder(context).build()
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		initTransition()
		note = requireArguments().getParcelable(NOTE) ?: Note()
		tempNote = note.clone()
		directoryId = requireArguments().getInt("id")
		if (savedInstanceState != null) {
			val restoredNote = savedInstanceState.getParcelable(NOTE_BUNDLE) ?: Note()
			note = restoredNote.clone()
		}
		textEditorMediaItemAdapter = EditorMediaItemAdapter(MediaItemAdapter())
		textEditorContentAdapter = EditorContentAdapter(note.entity)
		printDebug(NOTE, note)
		printDebug(DIRECTORY_ID, directoryId)
	}

	override fun onCreateView(inflater: LayoutInflater,
							  container: ViewGroup?,
							  savedInstanceState: Bundle?): View {
		_binding = FragmentEditorBinding.inflate(inflater, container, false).apply {
			note = this@EditorFragment.note.entity
		}
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.noteContent.apply {
			transitionName = note.entity.id.toString()
			adapter = ConcatAdapter(
				textEditorMediaItemAdapter,
				textEditorContentAdapter
			)
			recycledViewPool.setMaxRecycledViews(R.layout.fragment_editor_content, 0)
		}
		textEditorMediaItemAdapter.submitList(note.images)
		textEditorMediaItemAdapter.setOnCopyItemListener { position -> copyMediaItem(position) }
		textEditorMediaItemAdapter.setOnDeleteItemListener { position -> deleteMediaItem(position) }
		textEditorMediaItemAdapter.setOnDeleteAllListener { showDeletionWarningDialog() }
		textEditorMediaItemAdapter.setOnAltTextListener { position, contentDescription ->
			updateMediaItemDescription(position, contentDescription)
		}
		textEditorContentAdapter.setOnContentReceivedListener { uri -> addMediaItems(listOf(uri)) }
		textEditorContentAdapter.setOnEmptyContentListener {
			if (note.images.isEmpty()) {
				binding.noteContent.hideSoftKeyboard()
			}
		}
	}

	override fun onStart() {
		super.onStart()
		navigateUp()
		showSoftKeyboard()
		onMenuItemClick()
		onBackPressed()
		showMenuBottomSheet()
		setFragmentResultListener("uris") { _, bundle ->
			val uris = bundle.getParcelableArrayList<Uri>("uris") ?: emptyList()
			addMediaItems(uris)
		}
		setFragmentResultListener("data") { _, bundle ->
			val recognizedText = bundle.getString("recognized_text")
			recognizedText?.let { setSpeechText(it) }
			val audioUri = bundle.getParcelable<Uri>("audio_uri")
			audioUri?.let { addAudioMediaItem(it) }
		}
	}

	private fun addAudioMediaItem(uri: Uri) {
		val mediaItem = ImageEntity(
			uri = uri,
			mimeType = context?.getUriMimeType(uri),
			noteId = note.entity.id
		)
		note.images += mediaItem
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		outState.putParcelable(NOTE_BUNDLE, note)
	}

	private fun initTransition() {
		sharedElementEnterTransition = MaterialContainerTransform().apply {
			fadeMode = MaterialContainerTransform.FADE_MODE_CROSS
			fadeProgressThresholds = MaterialContainerTransform.ProgressThresholds(0f, 1f)
			scrimColor = Color.TRANSPARENT
		}
	}

	private fun onMenuItemClick() {
		binding.editorToolbar.setOnMenuItemClickListener {
			when (it.itemId) {
				R.id.share_content -> {
					shareContent(); true
				}
				else -> false
			}
		}
	}

	private fun navigateUp() {
		binding.editorToolbar.setNavigationOnClickListener {
			it.hideSoftKeyboard()
			activity?.onBackPressed()
		}
	}

	private fun onBackPressed() {
		activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
			insertOrUpdateNote(note)
			findNavController().popBackStack()
		}
	}

	private fun showMenuBottomSheet() {
		binding.add.setOnClickListener {
			it.hideSoftKeyboard()
			val menuDialog = EditorMenuFragment().apply {
				setPermissionDeniedListener { binding.root.showSnackbar(R.string.permission_denied) }
			}
			menuDialog.show(parentFragmentManager, MENU_TAG)
		}
	}

	private fun insertOrUpdateNote(note: Note) {
		if (note.entity.text.isNotEmpty() || note.images.isNotEmpty()) {
			if (note != tempNote) {
				if (note.entity.id == 0L) {
					val newNote = createNote(note.entity.title, note.entity.text, note.images, directoryId)
					viewModel.insertNote(directoryId, newNote, note.images)
					context?.showToast(R.string.note_saved)
				}
				else {
					val newNote = createNote(note.entity.title, note.entity.text, note.images, note.entity.directoryId, note.entity.id)
					val updatedNote = note.clone(
						entity = newNote.entity,
						images = newNote.images
					)
					viewModel.updateNote(updatedNote)
					context?.showToast(R.string.note_updated)
				}
			}
		}
		else if (note.entity.id != 0L) {
			setFragmentResult("deletion", bundleOf("note" to note))
		}
	}

	private fun createNote(title: String?, text: String, images: List<ImageEntity>, directoryId: Int, id: Long = 0): Note {
		return Note(
			entity = NoteEntity(
				id = id,
				title = title,
				text = text,
				updateDate = ZonedDateTime.now(),
				urls = extractUrls(text),
				previewImage = images.firstOrNull { it.mimeType?.startsWith("image") == true }?.uri,
				directoryId = directoryId
			),
			images = images
		)
	}

	private fun addMediaItems(uris: List<Uri>) {
		viewLifecycleOwner.lifecycleScope.launch {
			for (uri in uris) {
				val newUri = copyUri(uri)
				val mediaItem = ImageEntity(
					uri = newUri,
					mimeType = context?.getUriMimeType(newUri),
					noteId = note.entity.id
				)
				note.images += mediaItem
			}
			textEditorMediaItemAdapter.submitList(note.images.filter {
				it.mimeType?.startsWith("audio") == false
			})
		}
	}

	private suspend fun copyUri(uri: Uri): Uri {
		val sufix = (0..999).random()
		val extension = context?.getUriExtension(uri) ?: "jpg"
		val fileName = "IMG_${DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now())}_$sufix.$extension"
		val fullPath = MediaStorageManager.saveToInternalStorage(requireContext(), uri, fileName)
		val file = File(fullPath)
		return FileProvider.getUriForFile(requireContext(), AUTHORITY, file)
	}


	private fun copyMediaItem(position: Int) {
		val uri = note.images[position].uri
		if (uri != null) {
			requireContext().copyUriToClipboard(R.string.label_item, uri, R.string.item_copied_msg)
		}
	}

	private fun deleteMediaItem(position: Int) {
		val itemToBeDeleted = note.images[position]
		deleteMediaItem(itemToBeDeleted)
		textEditorMediaItemAdapter.submitList(note.images)
	}

	private fun deleteAllMediaItems() {
		note.images.forEach { deleteMediaItem(it) }
		textEditorMediaItemAdapter.submitList(note.images)
	}

	private fun deleteMediaItem(image: ImageEntity) {
		note.images -= image
		val fileName = DocumentFile.fromSingleUri(requireContext(), image.uri!!)?.name!!
		val result = MediaStorageManager.deleteImageFromInternalStorage(requireContext(), fileName)
		printDebug("MediaStoreManager", result)

		if (image.id != 0) {
			viewModel.deleteMediaItem(image)
		}
	}

	private fun updateMediaItemDescription(position: Int, contentDescription: String) {
		val originalMediaItem = note.images[position]
		val updatedMediaItem = originalMediaItem.copy(description = contentDescription)
		viewModel.updateMediaItem(updatedMediaItem)
		context?.showToast(R.string.alt_text_updated)
	}

	private fun setSpeechText(recognizedText: String) {
		val newContent = if (note.entity.text.isEmpty()) recognizedText else "${note.entity.text}\n$recognizedText"
		note.entity.text = newContent
		textEditorContentAdapter.notifyItemChanged(0)
	}

	private fun shareContent() {
		if (note.entity.text.isNotBlank() || note.images.isNotEmpty()) {
			val shareIntent = Intent()
			if (note.images.isEmpty()) {
				shareIntent.apply {
					action = Intent.ACTION_SEND
					putExtra(Intent.EXTRA_TEXT, note.entity.text)
					type = "text/plain"
				}
			}
			else {
				shareIntent.apply {
					action = Intent.ACTION_SEND_MULTIPLE
					putExtra(Intent.EXTRA_TEXT, note.entity.text)
					putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(note.images.map { it.uri }))
					type = "image/*"
					addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
				}
			}
			startActivity(Intent.createChooser(shareIntent, getString(R.string.chooser_dialog_title)))
		}
		else {
			context?.showToast(R.string.empty_note_share)
		}
	}

	private fun showDeletionWarningDialog() {
		MaterialAlertDialogBuilder(requireContext(), R.style.MyThemeOverlay_MaterialAlertDialog)
			.setMessage(R.string.delete_all_items_question)
			.setNegativeButton(R.string.cancel_button, null)
			.setPositiveButton(R.string.ok_button) { _, _ -> deleteAllMediaItems() }
			.show()
	}

	private fun showSoftKeyboard() {
		if (note.entity.id	== 0L) {
			binding.noteContent.showSoftKeyboard()
		}
	}

	companion object {
		const val NOTE = "note"
		private const val NOTE_BUNDLE = "note_bundle"
		private const val DIRECTORY_ID = "DIRECTORY_ID"
		private const val AUTHORITY = "net.azurewebsites.eznotes"
		private const val MENU_TAG = "MENU"
	}
}
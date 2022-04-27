package net.azurewebsites.eznotes.ui.editor

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
import net.azurewebsites.eznotes.R
import net.azurewebsites.eznotes.core.MediaItemEntity
import net.azurewebsites.eznotes.core.Note
import net.azurewebsites.eznotes.core.NoteEntity
import net.azurewebsites.eznotes.databinding.FragmentTextEditorBinding
import net.azurewebsites.eznotes.ui.audio.AudioAdapter
import net.azurewebsites.eznotes.ui.helpers.*
import net.azurewebsites.eznotes.ui.media.MediaItemAdapter
import net.azurewebsites.eznotes.ui.media.MediaStorageManager
import java.io.File
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class TextEditorFragment : Fragment() {

	private var _binding: FragmentTextEditorBinding? = null
	private val binding get() = _binding!!
	private val viewModel by viewModels<TextEditorViewModel>()
	private lateinit var tempNote: Note
	private lateinit var note: Note
	private lateinit var textEditorMediaItemAdapter: TextEditorMediaItemAdapter
	private lateinit var textEditorContentAdapter: TextEditorContentAdapter
	private lateinit var textEditorAudioMediaItemAdapter: TextEditorAudioMediaItemAdapter
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
		textEditorMediaItemAdapter = TextEditorMediaItemAdapter(MediaItemAdapter())
		textEditorContentAdapter = TextEditorContentAdapter(note.entity)
		textEditorAudioMediaItemAdapter = TextEditorAudioMediaItemAdapter(AudioAdapter(player))
		printDebug(NOTE, note)
		printDebug(DIRECTORY_ID, directoryId)
	}

	override fun onCreateView(inflater: LayoutInflater,
							  container: ViewGroup?,
							  savedInstanceState: Bundle?): View {
		_binding = FragmentTextEditorBinding.inflate(inflater, container, false).apply {
			note = this@TextEditorFragment.note.entity
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
				textEditorContentAdapter,
				textEditorAudioMediaItemAdapter
			)
			recycledViewPool.setMaxRecycledViews(R.layout.fragment_text_editor_content, 0)
		}
		textEditorAudioMediaItemAdapter.submitList(note.mediaItems.filter {
			it.mimeType?.startsWith("audio") == true
		})
		textEditorMediaItemAdapter.submitList(note.mediaItems.filter {
			it.mimeType?.startsWith("audio") == false
		})
		textEditorMediaItemAdapter.setOnCopyItemListener { position -> copyMediaItem(position) }
		textEditorMediaItemAdapter.setOnDeleteItemListener { position -> deleteMediaItem(position) }
		textEditorMediaItemAdapter.setOnDeleteAllListener { showDeletionWarningDialog() }
		textEditorMediaItemAdapter.setOnAltTextListener { position, contentDescription ->
			updateMediaItemDescription(position, contentDescription)
		}
		textEditorContentAdapter.setOnContentReceivedListener { uri -> addMediaItems(listOf(uri)) }
		textEditorContentAdapter.setOnEmptyContentListener {
			if (note.mediaItems.isEmpty()) {
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
		val mediaItem = MediaItemEntity(
			uri = uri,
			mimeType = context?.getUriMimeType(uri),
			noteId = note.entity.id
		)
		note.mediaItems += mediaItem
		textEditorAudioMediaItemAdapter.submitList(listOf(mediaItem))
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
			val menuDialog = TextEditorMenuFragment().apply {
				setPermissionDeniedListener { binding.root.showSnackbar(R.string.permission_denied) }
			}
			menuDialog.show(parentFragmentManager, MENU_TAG)
		}
	}

	private fun insertOrUpdateNote(note: Note) {
		if (note.entity.text.isNotEmpty() || note.mediaItems.isNotEmpty()) {
			if (note != tempNote) {
				if (note.entity.id == 0L) {
					val newNote = createNote(note.entity.title, note.entity.text, note.mediaItems, directoryId)
					viewModel.insertNote(directoryId, newNote, note.mediaItems)
					context?.showToast(R.string.note_saved)
				}
				else {
					val newNote = createNote(note.entity.title, note.entity.text, note.mediaItems, note.entity.directoryId, note.entity.id)
					val updatedNote = note.clone(
						entity = newNote.entity,
						mediaItems = newNote.mediaItems
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

	private fun createNote(title: String?, text: String, mediaItems: List<MediaItemEntity>, directoryId: Int, id: Long = 0): Note {
		return Note(
			entity = NoteEntity(
				id = id,
				title = title,
				text = text,
				updateDate = ZonedDateTime.now(),
				urls = extractUrls(text),
				previewImage = mediaItems.firstOrNull { it.mimeType?.startsWith("image") == true }?.uri,
				directoryId = directoryId
			),
			mediaItems = mediaItems
		)
	}

	private fun addMediaItems(uris: List<Uri>) {
		viewLifecycleOwner.lifecycleScope.launch {
			for (uri in uris) {
				val newUri = copyUri(uri)
				val mediaItem = MediaItemEntity(
					uri = newUri,
					mimeType = context?.getUriMimeType(newUri),
					noteId = note.entity.id
				)
				note.mediaItems += mediaItem
			}
			textEditorMediaItemAdapter.submitList(note.mediaItems.filter {
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
		val uri = note.mediaItems[position].uri
		if (uri != null) {
			requireContext().copyUriToClipboard(R.string.label_item, uri, R.string.item_copied_msg)
		}
	}

	private fun deleteMediaItem(position: Int) {
		val itemToBeDeleted = note.mediaItems[position]
		deleteMediaItem(itemToBeDeleted)
		textEditorMediaItemAdapter.submitList(note.mediaItems)
	}

	private fun deleteAllMediaItems() {
		note.mediaItems.forEach { deleteMediaItem(it) }
		textEditorMediaItemAdapter.submitList(note.mediaItems)
	}

	private fun deleteMediaItem(mediaItem: MediaItemEntity) {
		note.mediaItems -= mediaItem
		val fileName = DocumentFile.fromSingleUri(requireContext(), mediaItem.uri!!)?.name!!
		val result = MediaStorageManager.deleteImageFromInternalStorage(requireContext(), fileName)
		printDebug("MediaStoreManager", result)

		if (mediaItem.id != 0) {
			viewModel.deleteMediaItem(mediaItem)
		}
	}

	private fun updateMediaItemDescription(position: Int, contentDescription: String) {
		val originalMediaItem = note.mediaItems[position]
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
		if (note.entity.text.isNotBlank() || note.mediaItems.isNotEmpty()) {
			val shareIntent = Intent()
			if (note.mediaItems.isEmpty()) {
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
					putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(note.mediaItems.map { it.uri }))
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
package net.azurewebsites.eznotes.ui.notes

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale
import net.azurewebsites.eznotes.R
import net.azurewebsites.eznotes.core.MediaItemEntity
import net.azurewebsites.eznotes.core.Note
import net.azurewebsites.eznotes.databinding.FragmentNoteListBinding
import net.azurewebsites.eznotes.ui.folders.FolderListViewModel
import net.azurewebsites.eznotes.ui.helpers.*
import net.azurewebsites.eznotes.ui.media.MediaStorageManager
import net.azurewebsites.eznotes.ui.notes.urls.UrlListDialogFragment
import net.azurewebsites.eznotes.ui.settings.UserPreferences
import net.azurewebsites.eznotes.util.LayoutType

class NoteListFragment : Fragment(), OnFabClickListener {

	private var _binding: FragmentNoteListBinding? = null
	private val binding get() = _binding!!
	private val viewModel by viewModels<NoteListViewModel>()
	private val parentViewModel by activityViewModels<FolderListViewModel>()
	private val noteAdapter = NoteAdapter()
	private lateinit var userPreferences: UserPreferences
	private var directoryId = 0

	override fun onAttach(context: Context) {
		super.onAttach(context)
		userPreferences = UserPreferences(context)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setHasOptionsMenu(true)
		exitTransition = MaterialElevationScale(false)
		reenterTransition = MaterialElevationScale(true)
	}

	override fun onCreateView(inflater: LayoutInflater,
							  container: ViewGroup?,
							  savedInstanceState: Bundle?): View {
		_binding = FragmentNoteListBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
		inflater.inflate(R.menu.menu_note_list, menu)
	}

//	override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
//		R.id.action_settings -> {
//			true
//		}
//		else -> false
//	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setupRecyclerView()
		submitListAndUpdateToolbarTitle()
	}

	override fun onStart() {
		super.onStart()
		noteAdapter.setOnChipClickedListener { urls -> showUrls(urls) }
		noteAdapter.setOnNotesDeletedListener { notes, hasUndo ->
			viewModel.deleteNotes(directoryId, notes)
			submitList(directoryId)
			if (hasUndo) showUndoSnackbar(notes.single())
		}
		mainActivity.setOnFabClickListener { onClick() }
		setFragmentResultListener("deletion") { _, bundle ->
			val noteToBeDeleted = bundle.getParcelable<Note>("note")
			deleteEmptyNote(noteToBeDeleted)
		}
	}

	override fun onClick() {
		val args = bundleOf("id" to directoryId)
		findNavController().safeNavigate(R.id.action_notes_to_editor, args)
	}

	private fun showUndoSnackbar(note: Note) {
		binding.root.showSnackbar(R.string.deleted_note, action = R.string.undo) {
			viewModel.restoreNote(directoryId, note, note.mediaItems)
			submitList(directoryId)
		}.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
			override fun onDismissed(transientBottomBar: Snackbar, event: Int) {
				super.onDismissed(transientBottomBar, event)
				if (event != DISMISS_EVENT_ACTION) {
					deleteMediaItems(note.mediaItems)
				}
				transientBottomBar.removeCallback(this)
			}
		})
	}

	private fun deleteMediaItems(mediaItems: List<MediaItemEntity>) {
		for (mediaItem in mediaItems) {
			val fileName = DocumentFile.fromSingleUri(requireContext(), mediaItem.uri!!)?.name!!
			val result = MediaStorageManager.deleteImageFromInternalStorage(requireContext(), fileName)
			printDebug("ImageStoreManager", result)
		}
	}

	private fun setupRecyclerView() {
		binding.recyclerView.apply {
			adapter = noteAdapter
			(layoutManager as StaggeredGridLayoutManager).spanCount =
				UserPreferences(requireContext()).layoutType?.spanCount ?: LayoutType.Linear.spanCount
		}
		postponeEnterTransition()
	}

	private fun submitListAndUpdateToolbarTitle() {
		parentViewModel.currentDirectory.observe(viewLifecycleOwner) {
			mainActivity.supportActionBar?.title = it.name.ifEmpty { userPreferences.defaultDirectoryName }
			directoryId = if (it.id == 0) 1 else it.id
			submitList(directoryId)
		}
	}

	private fun submitList(directoryId: Int) {
		UserPreferences(requireContext()).sortMode?.let {
			viewModel.sortNotes(directoryId, it).observe(viewLifecycleOwner) { notes ->
				binding.empty.isVisible = notes.isEmpty()
				noteAdapter.submitList(notes)
				binding.root.doOnPreDraw { startPostponedEnterTransition() }
			}
		}
	}

	private fun showUrls(urls: List<String>) {
		val urlListDialog = UrlListDialogFragment.newInstance(urls.toTypedArray())
		urlListDialog.show(parentFragmentManager, TAG)
	}

	private fun deleteEmptyNote(note: Note?) {
		if (note != null) {
			viewModel.deleteNotes(directoryId, listOf(note))
			binding.root.showSnackbar(R.string.empty_note_deleted)
		}
	}

	companion object {
		private const val TAG = "URL_LIST_DIALOG"
	}
}
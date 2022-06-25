package net.azurewebsites.noties.ui.notes

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.transition.MaterialElevationScale
import dagger.hilt.android.AndroidEntryPoint
import net.azurewebsites.noties.R
import net.azurewebsites.noties.core.Note
import net.azurewebsites.noties.core.NoteEntity
import net.azurewebsites.noties.core.NotebookEntity
import net.azurewebsites.noties.databinding.FragmentNotesBinding
import net.azurewebsites.noties.ui.MainActivity
import net.azurewebsites.noties.ui.editor.EditorFragment
import net.azurewebsites.noties.ui.helpers.*
import net.azurewebsites.noties.ui.image.ImageStorageManager
import net.azurewebsites.noties.ui.notebooks.NotebooksFragment
import net.azurewebsites.noties.ui.notes.selection.*
import net.azurewebsites.noties.ui.settings.PreferenceStorage
import net.azurewebsites.noties.ui.urls.UrlsDialogFragment
import javax.inject.Inject

@AndroidEntryPoint
class NotesFragment : Fragment(), SwipeToDeleteListener, RecyclerViewActionModeListener, NotesMenuListener {

	private var _binding: FragmentNotesBinding? = null
	private val binding get() = _binding!!
	private val viewModel by viewModels<NotesViewModel>()
	private val noteAdapter = NoteAdapter(this)
	@Inject lateinit var userPreferences: PreferenceStorage
	private val notebook by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getParcelable(NotebooksFragment.NOTEBOOK) ?: NotebookEntity()
	}
	private val menuProvider = NotesMenuProvider(this)
	private lateinit var selectionTracker: SelectionTracker<Note>
	private lateinit var actionModeCallback: RecyclerViewActionModeCallback
	private lateinit var selectionObserver: SelectionObserver

	override fun onAttach(context: Context) {
		super.onAttach(context)
		(context as MainActivity).setOnFabClickListener { navigateToEditor() }
		actionModeCallback = RecyclerViewActionModeCallback(noteAdapter)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enterTransition = inflateTransition(R.transition.slide_from_bottom)
		exitTransition = MaterialElevationScale(false)
		reenterTransition = MaterialElevationScale(true)
	}

	override fun onCreateView(inflater: LayoutInflater,
							  container: ViewGroup?,
							  savedInstanceState: Bundle?): View {
		_binding = FragmentNotesBinding.inflate(inflater, container, false)
		addMenuProvider(menuProvider, viewLifecycleOwner)
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setupRecyclerView()
		submitListAndUpdateToolbar()
		buildTracker(savedInstanceState)
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		if (::selectionTracker.isInitialized) {
			selectionTracker.onSaveInstanceState(outState)
		}
		outState.putBoolean(ACTION_MODE, actionModeCallback.isVisible)
	}

	override fun onViewStateRestored(savedInstanceState: Bundle?) {
		super.onViewStateRestored(savedInstanceState)
		if (savedInstanceState != null && savedInstanceState.getBoolean(ACTION_MODE)) {
			showActionMode()
		}
	}

	override fun onStart() {
		super.onStart()
		setupAdapterListeners()
		getNoteToBeDeleted()
	}

	override fun moveNoteToTrash(note: NoteEntity) {
		viewModel.moveNoteToTrash(note) { showUndoSnackbar(it) }
	}

	override fun showDeleteNotesDialog(notes: List<Note>) {
		val deleteNotesDialog = DeleteNotesDialogFragment.newInstance(notes).apply {
			setOnNotesDeletedListener {
				selectionObserver.actionMode?.finish()
				for (note in notes) {
					ImageStorageManager.deleteImages(this@NotesFragment.requireContext(), note.images)
				}
			}
		}
		showDialog(deleteNotesDialog, DELETE_NOTES)
	}

	override fun showSortNotesDialog() {
		val sortNotesDialog = SortNotesDialogFragment().apply {
			setOnSortNotesListener {
				mode -> submitList(notebook.id, mode)
				// We need to save this mode to preferences
			}
		}
		showDialog(sortNotesDialog, SORT_NOTES)
	}

	private fun navigateToEditor() {
		val args = bundleOf(ID to notebook.id)
		findNavController().tryNavigate(R.id.action_notes_to_editor, args)
	}

	private fun setupRecyclerView() {
		binding.recyclerView.apply {
			adapter = noteAdapter
			(layoutManager as StaggeredGridLayoutManager).spanCount = 1
			addItemTouchHelper(ItemTouchHelper(SwipeToDeleteCallback(noteAdapter)))
		}
		postponeEnterTransition()
	}

	private fun submitListAndUpdateToolbar() {
		supportActionBar?.title = notebook.name.ifEmpty { getString(R.string.notes_fragment_label) }
		submitList(notebook.id, SortMode.LastEdit)
	}

	private fun submitList(notebookId: Int, mode: SortMode) {
		viewModel.sortNotes(notebookId, mode).observe(viewLifecycleOwner) {
			noteAdapter.submitList(it)
			binding.root.doOnPreDraw { startPostponedEnterTransition() }
		}
	}

	private fun showUndoSnackbar(note: NoteEntity) {
		binding.root.showSnackbar(R.string.deleted_note, action = R.string.undo) {
			viewModel.restoreNote(note)
//			submitList(notebook.id, SortMode.LastEdit)
		}
	}

	private fun showUrlsDialog(urls: List<String>) {
		val urlsDialog = UrlsDialogFragment.newInstance(urls.toTypedArray())
		showDialog(urlsDialog, TAG)
	}

	private fun buildTracker(savedInstanceState: Bundle?) {
		selectionTracker = SelectionTracker.Builder(
			SELECTION_ID,
			binding.recyclerView,
			NoteItemKeyProvider(noteAdapter),
			NoteItemDetailsLookup(binding.recyclerView),
			StorageStrategy.createParcelableStorage(Note::class.java)
		).withSelectionPredicate(SelectionPredicates.createSelectAnything()).build().apply {
			onRestoreInstanceState(savedInstanceState)
			selectionObserver = SelectionObserver(requireContext(), actionModeCallback, this)
			addObserver(selectionObserver)
		}
		noteAdapter.tracker = selectionTracker
	}

	private fun showActionMode() {
		selectionObserver.actionMode = startActionMode(actionModeCallback)
		val numSelectedItems = selectionTracker.selection.size()
		val title = resources.getQuantityString(
			R.plurals.notes_selected,
			numSelectedItems,
			numSelectedItems
		)
		selectionObserver.actionMode?.title = title
	}

	private fun setupAdapterListeners() {
		noteAdapter.setOnShowUrlsListener { urls -> showUrlsDialog(urls) }
		noteAdapter.setOnDeleteNotesListener { notes -> showDeleteNotesDialog(notes) }
		noteAdapter.setOnLockNotesListener { notes ->
			viewModel.toggleLockedStatusForNotes(notes) { selectionObserver.actionMode?.finish() }
		}
	}

	private fun getNoteToBeDeleted() {
		setFragmentResultListener(EditorFragment.REQUEST_KEY) { _, bundle ->
			val noteToBeDeleted = bundle.getParcelable<Note>(EditorFragment.NOTE)
			deleteEmptyNote(noteToBeDeleted)
		}
	}

	private fun deleteEmptyNote(note: Note?) {
		if (note != null) {
			viewModel.deleteNotes(listOf(note)) {
				ImageStorageManager.deleteImages(requireContext(), note.images)
				binding.root.showSnackbar(R.string.empty_note_deleted)
			}
		}
	}

	companion object {
		const val ID = "id"
		private const val TAG = "URLS_DIALOG"
		private const val SELECTION_ID = "note_selection"
		private const val ACTION_MODE = "action_mode"
		private const val DELETE_NOTES = "DELETE_NOTES_DIALOG"
		private const val SORT_NOTES = "SORT_NOTES_DIALOG"
	}
}
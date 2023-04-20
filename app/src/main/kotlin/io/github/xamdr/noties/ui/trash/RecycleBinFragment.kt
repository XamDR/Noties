package io.github.xamdr.noties.ui.trash

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import dagger.hilt.android.AndroidEntryPoint
import io.github.xamdr.noties.R
import io.github.xamdr.noties.data.entity.note.DatabaseNoteEntity
import io.github.xamdr.noties.databinding.FragmentRecycleBinBinding
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.ui.helpers.*
import io.github.xamdr.noties.ui.image.ImageStorageManager
import io.github.xamdr.noties.ui.notes.NoteAdapter
import io.github.xamdr.noties.ui.notes.SwipeToDeleteListener
import io.github.xamdr.noties.ui.notes.selection.*

@AndroidEntryPoint
class RecycleBinFragment : Fragment(), SwipeToDeleteListener, RecycleBinMenuListener, RecyclerViewActionModeListener {

	private var _binding: FragmentRecycleBinBinding? = null
	private val binding get() = _binding!!
	private val viewModel by viewModels<RecycleBinViewModel>()
	private val noteAdapter = NoteAdapter()
	private val menuProvider = RecycleBinMenuProvider(this)
	private lateinit var selectionTracker: SelectionTracker<Note>
	private lateinit var actionModeCallback: RecyclerViewActionModeCallback
	private lateinit var selectionObserver: SelectionObserver

	override fun onAttach(context: Context) {
		super.onAttach(context)
		actionModeCallback = RecyclerViewActionModeCallback(noteAdapter)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enterTransition = inflateTransition(R.transition.slide_from_bottom)
	}

	override fun onCreateView(inflater: LayoutInflater,
	                          container: ViewGroup?,
	                          savedInstanceState: Bundle?): View {
		_binding = FragmentRecycleBinBinding.inflate(inflater, container, false)
		addMenuProvider(menuProvider, viewLifecycleOwner)
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.recyclerView.apply { adapter = noteAdapter }
		observeTrashedNotes()
		buildTracker(savedInstanceState)
	}

	override fun onStart() {
		super.onStart()
		setupAdapterListeners()
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		selectionTracker.onSaveInstanceState(outState)
		outState.putBoolean(ACTION_MODE, actionModeCallback.isVisible)
	}

	override fun onViewStateRestored(savedInstanceState: Bundle?) {
		super.onViewStateRestored(savedInstanceState)
		if (savedInstanceState != null && savedInstanceState.getBoolean(ACTION_MODE)) {
			showActionMode()
		}
	}

	override fun moveNoteToTrash(note: DatabaseNoteEntity) {}

	override fun showEmptyRecycleBinDialog() {
		val emptyRecycleBinDialog = EmptyRecycleBinDialogFragment().apply {
			setOnRecycleBinEmptyListener { deleteImages(viewModel.notes) }
		}
		showDialog(emptyRecycleBinDialog, TAG)
	}

	override fun showDeleteNotesDialog(notes: List<Note>) {
		val deleteNotesDialog = DeleteNotesDialogFragment.newInstance(notes).apply {
			setOnNotesDeletedListener {
				selectionObserver.actionMode?.finish()
				deleteImages(notes)
			}
		}
		showDialog(deleteNotesDialog, DELETE_NOTES)
	}

	private fun observeTrashedNotes() {
//		viewModel.getTrashedNotes().observe(viewLifecycleOwner) { notes: List<Note> ->
//			noteAdapter.submitList(notes)
//			if (notes.isEmpty()) removeMenuProvider(menuProvider)
//		}
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
		selectionObserver.actionMode?.title = numSelectedItems.toString()
	}

	private fun setupAdapterListeners() {
		noteAdapter.setOnDeleteNotesListener { notes: List<Note> -> showDeleteNotesDialog(notes) }
		noteAdapter.setOnRestoreNotesListener { notes: List<Note> -> viewModel.restoreNotes(notes) }
	}

	private fun deleteImages(notes: List<Note>) {
		for (note in notes) {
			ImageStorageManager.deleteImages(requireContext(), note.images)
		}
	}

	private companion object {
		private const val TAG = "EMPTY_RECYCLE_BIN_DIALOG"
		private const val DELETE_NOTES = "DELETE_NOTES_DIALOG"
		private const val SELECTION_ID = "trashed_note_selection"
		private const val ACTION_MODE = "action_mode"
	}
}
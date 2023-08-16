package io.github.xamdr.noties.ui.notes

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.MenuItemCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.transition.MaterialElevationScale
import dagger.hilt.android.AndroidEntryPoint
import io.github.xamdr.noties.R
import io.github.xamdr.noties.databinding.FragmentNotesBinding
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.ui.helpers.Constants
import io.github.xamdr.noties.ui.helpers.addItemTouchHelper
import io.github.xamdr.noties.ui.helpers.findItem
import io.github.xamdr.noties.ui.helpers.inflateTransition
import io.github.xamdr.noties.ui.helpers.launch
import io.github.xamdr.noties.ui.helpers.media.MediaStorageManager
import io.github.xamdr.noties.ui.helpers.showDialog
import io.github.xamdr.noties.ui.helpers.showSnackbarWithActionSuspend
import io.github.xamdr.noties.ui.helpers.startActionMode
import io.github.xamdr.noties.ui.helpers.supportActionBar
import io.github.xamdr.noties.ui.helpers.tryNavigate
import io.github.xamdr.noties.ui.notes.selection.DeleteNotesDialogFragment
import io.github.xamdr.noties.ui.notes.selection.NoteItemDetailsLookup
import io.github.xamdr.noties.ui.notes.selection.NoteItemKeyProvider
import io.github.xamdr.noties.ui.notes.selection.RecyclerViewActionModeCallback
import io.github.xamdr.noties.ui.settings.PreferenceStorage
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class NotesFragment : Fragment(), Toolbar.OnMenuItemClickListener {

	private var _binding: FragmentNotesBinding? = null
	private val binding get() = _binding!!
	private val viewModel by viewModels<NotesViewModel>()
	private val noteAdapter = NoteAdapter(this::navigateToEditor, this::moveNoteToTrash)
	@Inject lateinit var preferenceStorage: PreferenceStorage
	private lateinit var selectionTracker: SelectionTracker<Note>
	private var actionMode: ActionMode? = null
	private lateinit var actionModeCallback: RecyclerViewActionModeCallback

	override fun onAttach(context: Context) {
		super.onAttach(context)
		actionModeCallback = RecyclerViewActionModeCallback(noteAdapter).apply {
			onActionModeDone = this@NotesFragment::onActionModeDone
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enterTransition = inflateTransition(R.transition.slide_from_bottom)
	}

	override fun onCreateView(inflater: LayoutInflater,
							  container: ViewGroup?,
							  savedInstanceState: Bundle?): View {
		_binding = FragmentNotesBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setupRecyclerView()
		submitList(String.Empty)
		buildTracker(savedInstanceState)
//		binding.searchBar.setupWithNavController(findNavController(), requireActivity().findViewById(R.id.drawer_layout))
		binding.searchBar.setOnMenuItemClickListener(this)
		binding.fab.setOnClickListener { navigateToEditor(note = Note()) }
		updateToolbar()
	}

	override fun onStart() {
		super.onStart()
		noteAdapter.setOnDeleteNotesListener(this::showDeleteNotesDialog)
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		if (::selectionTracker.isInitialized) {
			selectionTracker.onSaveInstanceState(outState)
		}
		outState.putBoolean(Constants.BUNDLE_ACTION_MODE, actionModeCallback.isVisible)
	}

	override fun onViewStateRestored(savedInstanceState: Bundle?) {
		super.onViewStateRestored(savedInstanceState)
		if (savedInstanceState != null && savedInstanceState.getBoolean(Constants.BUNDLE_ACTION_MODE)) {
			showActionMode()
		}
	}

	override fun onMenuItemClick(item: MenuItem) = when (item.itemId) {
		R.id.action_change_layout -> {
			setLayoutPreference(LayoutType.valueOf(preferenceStorage.layoutType), item); true
		}
		else -> false
	}

//	private fun navigateToTags() {
//		exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
//			duration = resources.getInteger(R.integer.motion_duration_large).toLong()
//		}
//		reenterTransition =  MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
//			duration = resources.getInteger(R.integer.motion_duration_large).toLong()
//		}
//		findNavController().tryNavigate(R.id.action_notes_to_tags)
//	}

	private fun setLayoutPreference(layoutType: LayoutType, menuItem: MenuItem) {
		val layoutManager = binding.recyclerView.layoutManager as StaggeredGridLayoutManager
		when (layoutType) {
			LayoutType.Linear -> {
				layoutManager.spanCount = 2
				preferenceStorage.layoutType = LayoutType.Grid.name
				menuItem.apply {
					setIcon(R.drawable.ic_view_linear_layout)
					MenuItemCompat.setContentDescription(this, getString(R.string.linear_layout_view))
				}
			}
			LayoutType.Grid -> {
				layoutManager.spanCount = 1
				preferenceStorage.layoutType = LayoutType.Linear.name
				menuItem.apply {
					setIcon(R.drawable.ic_view_grid_layout)
					MenuItemCompat.setContentDescription(this, getString(R.string.grid_layout_view))
				}
			}
		}
	}

	private fun updateToolbar() {
		binding.searchBar.findItem(R.id.action_change_layout).apply {
			when (LayoutType.valueOf(preferenceStorage.layoutType)) {
				LayoutType.Linear -> {
					setIcon(R.drawable.ic_view_grid_layout)
					MenuItemCompat.setContentDescription(this, getString(R.string.grid_layout_view))
				}
				LayoutType.Grid -> {
					setIcon(R.drawable.ic_view_linear_layout)
					MenuItemCompat.setContentDescription(this, getString(R.string.linear_layout_view))
				}
			}
		}
	}

	private fun navigateToEditor(view: View? = null, note: Note) {
		actionMode?.finish()
		exitTransition = MaterialElevationScale(false).apply {
			duration = resources.getInteger(R.integer.motion_duration_large).toLong()
		}
		reenterTransition = MaterialElevationScale(true).apply {
			duration = resources.getInteger(R.integer.motion_duration_large).toLong()
		}
		val args = bundleOf(Constants.BUNDLE_NOTE_ID to note.id)
		val extras = if (view != null) FragmentNavigatorExtras(view to note.id.toString()) else null
		findNavController().tryNavigate(R.id.action_notes_to_editor, args, null, extras)
	}

	private fun setupRecyclerView() {
		val layoutType = LayoutType.valueOf(preferenceStorage.layoutType)
		binding.recyclerView.apply {
			adapter = noteAdapter
			(layoutManager as StaggeredGridLayoutManager).spanCount = when (layoutType) {
				LayoutType.Linear -> 1
				LayoutType.Grid -> 2
			}
			setEmptyView(binding.emptyView)
			addItemTouchHelper(ItemTouchHelper(SwipeToDeleteCallback(noteAdapter)))
		}
		postponeEnterTransition()
	}

	private fun submitList(tagName: String) {
		viewModel.getNotesByTag(tagName).observe(viewLifecycleOwner) { notes ->
			buildUIForNotes()
			noteAdapter.submitList(notes)
			binding.root.doOnPreDraw { startPostponedEnterTransition() }
		}
	}

	private fun buildUIForNotes() {
		binding.searchBar.isVisible = true
		binding.fab.isVisible = true
		binding.emptyView.setText(R.string.empty_notes_message)
	}

	private fun buildTracker(savedInstanceState: Bundle?) {
		selectionTracker = SelectionTracker.Builder(
			Constants.BUNDLE_SELECTION,
			binding.recyclerView,
			NoteItemKeyProvider(noteAdapter),
			NoteItemDetailsLookup(binding.recyclerView),
			StorageStrategy.createParcelableStorage(Note::class.java)
		).withSelectionPredicate(SelectionPredicates.createSelectAnything()).build().apply {
			onRestoreInstanceState(savedInstanceState)
			addObserver(object : SelectionTracker.SelectionObserver<Note>() {
				override fun onSelectionChanged() {
					onSelectionChanged(selectionTracker)
				}
			})
		}
		noteAdapter.tracker = selectionTracker
	}

	private fun onSelectionChanged(selectionTracker: SelectionTracker<*>) {
		val numSelectedItems = selectionTracker.selection.size()
		Timber.d("Number selected items: %s", numSelectedItems)
		if (selectionTracker.hasSelection() && actionMode == null) {
			binding.searchBar.isVisible = false
			supportActionBar?.show()
			actionMode = startActionMode(actionModeCallback)
			actionMode?.title = numSelectedItems.toString()
		}
		else if (!selectionTracker.hasSelection() && actionMode != null) {
			actionMode?.finish()
			actionMode = null
		}
		else {
			actionMode?.title = numSelectedItems.toString()
			actionMode?.invalidate()
		}
	}

	private fun showActionMode() {
		actionMode = startActionMode(actionModeCallback)
		val numSelectedItems = selectionTracker.selection.size()
		actionMode?.title = numSelectedItems.toString()
	}

	private fun onActionModeDone() {
		supportActionBar?.hide()
		binding.searchBar.isVisible = true
	}

	private fun showDeleteNotesDialog(notes: List<Note>) {
		val dialog = DeleteNotesDialogFragment.newInstance(notes).apply {
			setOnNotesDeletedListener { notes ->
				actionMode?.finish()
				this@NotesFragment.launch {
					viewModel.deleteNotes(notes)
					deleteImages(notes)
				}
			}
		}
		showDialog(dialog, Constants.DELETE_NOTES_DIALOG_TAG)
	}

	private fun moveNoteToTrash(note: Note) {
		launch {
			val result = viewModel.moveNotesToTrash(listOf(note))
			binding.root.showSnackbarWithActionSuspend(R.string.deleted_note, actionText = R.string.undo) {
				viewModel.restoreNotes(result)
			}
		}
	}

	private fun deleteImages(notes: List<Note>) {
		for (note in notes) {
			MediaStorageManager.deleteItems(requireContext(), note.items)
		}
	}
}
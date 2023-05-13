package io.github.xamdr.noties.ui.notes

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
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
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint
import io.github.xamdr.noties.R
import io.github.xamdr.noties.databinding.FragmentNotesBinding
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.domain.model.Tag
import io.github.xamdr.noties.ui.helpers.*
import io.github.xamdr.noties.ui.image.ImageStorageManager
import io.github.xamdr.noties.ui.notes.recyclebin.EmptyRecycleBinDialogFragment
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
	private val tag by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getParcelableCompat(Constants.BUNDLE_TAG_ID, Tag::class.java)
	}
	private lateinit var selectionTracker: SelectionTracker<Note>
	private var actionMode: ActionMode? = null
	private lateinit var actionModeCallback: RecyclerViewActionModeCallback
	private val isRecycleBin by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getBoolean(Constants.BUNDLE_RECYCLE_BIN, false)
	}
	private val menuProvider = RecycleBinMenuProvider()
	private lateinit var trashedNotes: List<Note>

	override fun onAttach(context: Context) {
		super.onAttach(context)
		actionModeCallback = RecyclerViewActionModeCallback(noteAdapter).apply {
			setOnActionModeDoneListener(this@NotesFragment::onActionModeDone)
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
		submitList(tag.name)
		buildTracker(savedInstanceState)
		binding.searchBar.setOnMenuItemClickListener(this)
		binding.fab.setOnClickListener { navigateToEditor(note = Note()) }
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
		R.id.nav_tags -> {
			navigateToTags(); true
		}
//		R.id.change_notes_layout -> {
//			changeNotesLayout(LayoutType.valueOf(preferenceStorage.layoutType)); true
//		}
		R.id.action_layout_linear -> {
			setLayoutPreference(LayoutType.Linear, item); true
		}
		R.id.action_layout_grid -> {
			setLayoutPreference(LayoutType.Grid, item); true
		}
		R.id.nav_trash -> {
			val args = bundleOf(Constants.BUNDLE_RECYCLE_BIN to true)
			findNavController().tryNavigate(R.id.action_notes_to_self, args); true
		}
		R.id.nav_settings -> {
			findNavController().tryNavigate(R.id.action_notes_to_settings); true
		}
		else -> false
	}

	private fun navigateToTags() {
		exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
			duration = resources.getInteger(R.integer.motion_duration_large).toLong()
		}
		reenterTransition =  MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
			duration = resources.getInteger(R.integer.motion_duration_large).toLong()
		}
		findNavController().tryNavigate(R.id.action_notes_to_tags)
	}

	private fun setLayoutPreference(layoutType: LayoutType, menuItem: MenuItem) {
		val layoutManager = binding.recyclerView.layoutManager as StaggeredGridLayoutManager
		when (layoutType) {
			LayoutType.Linear -> {
				layoutManager.spanCount = 2
				preferenceStorage.layoutType = LayoutType.Grid.name
			}
			LayoutType.Grid -> {
				layoutManager.spanCount = 1
				preferenceStorage.layoutType = LayoutType.Linear.name
			}
		}
		menuItem.isChecked = true
	}

	private fun navigateToEditor(view: View? = null, note: Note) {
		actionMode?.finish()
		exitTransition = MaterialElevationScale(false).apply {
			duration = resources.getInteger(R.integer.motion_duration_large).toLong()
		}
		reenterTransition = MaterialElevationScale(true).apply {
			duration = resources.getInteger(R.integer.motion_duration_large).toLong()
		}
		val editorTitle = if (isRecycleBin) String.Empty else getString(R.string.editor_fragment_label)
		val args = bundleOf(
			Constants.BUNDLE_NOTE_ID to note.id,
			Constants.ARG_TITLE to editorTitle
		)
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
		if (isRecycleBin) {
			viewModel.getTrashedNotes().observe(viewLifecycleOwner) { trashedNotes ->
				this.trashedNotes = trashedNotes
				buildUIForTrashedNotes(trashedNotes)
				noteAdapter.submitList(trashedNotes)
				binding.root.doOnPreDraw { startPostponedEnterTransition() }
			}
		}
		else {
			viewModel.getNotesByTag(tagName).observe(viewLifecycleOwner) { notes ->
				if (tagName.isNotEmpty()) supportActionBar?.title = tag.name
				buildUIForNotes()
				noteAdapter.submitList(notes)
				binding.root.doOnPreDraw { startPostponedEnterTransition() }
			}
		}
	}

	private fun buildUIForNotes() {
		binding.searchBar.isVisible = true
		binding.fab.isVisible = true
		binding.emptyView.setText(R.string.empty_notes_message)
	}

	private fun buildUIForTrashedNotes(trashedNotes: List<Note>) {
		binding.searchBar.isVisible = false
		binding.fab.isVisible = false
		supportActionBar?.show(getString(R.string.recycle_bin_fragment_label))
		if (trashedNotes.isNotEmpty()) addMenuProvider(menuProvider, viewLifecycleOwner)
		binding.emptyView.setText(R.string.empty_recycle_bin)
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
		binding.searchBar.isVisible = false // Bug here
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

	private fun showEmptyRecycleBinDialog() {
		val dialog = EmptyRecycleBinDialogFragment().apply {
			setOnRecycleBinEmptyListener {
				this@NotesFragment.launch {
					if (::trashedNotes.isInitialized) {
						viewModel.emptyRecycleBin(trashedNotes)
						deleteImages(trashedNotes)
						this@NotesFragment.removeMenuProvider(menuProvider)
					}
				}
			}
		}
		showDialog(dialog, Constants.EMPTY_RECYCLE_BIN_DIALOG_TAG)
	}

	private fun deleteImages(notes: List<Note>) {
		for (note in notes) {
			ImageStorageManager.deleteImages(requireContext(), note.images)
		}
	}

	private inner class RecycleBinMenuProvider : MenuProvider {
		override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
			menuInflater.inflate(R.menu.menu_recycle_bin, menu)
		}

		override fun onMenuItemSelected(menuItem: MenuItem) = when (menuItem.itemId) {
			R.id.empty_recycle_bin -> {
				showEmptyRecycleBinDialog(); true
			}
			else -> false
		}
	}
}
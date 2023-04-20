package io.github.xamdr.noties.ui.notes

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
import io.github.xamdr.noties.ui.notes.selection.NoteItemDetailsLookup
import io.github.xamdr.noties.ui.notes.selection.NoteItemKeyProvider
import io.github.xamdr.noties.ui.notes.selection.RecyclerViewActionModeCallback
import io.github.xamdr.noties.ui.notes.selection.SelectionObserver
import io.github.xamdr.noties.ui.settings.PreferenceStorage
import javax.inject.Inject

@AndroidEntryPoint
class NotesFragment : Fragment(), Toolbar.OnMenuItemClickListener {

	private var _binding: FragmentNotesBinding? = null
	private val binding get() = _binding!!
	private val viewModel by viewModels<NotesViewModel>()
	private val noteAdapter = NoteAdapter()
	@Inject lateinit var preferenceStorage: PreferenceStorage
	private val tag by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getSerializableCompat(Constants.BUNDLE_TAG_ID, Tag::class.java)
	}
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
		binding.fab.setOnClickListener { navigateToEditor() }
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
		R.id.change_notes_layout -> {
			changeNotesLayout(LayoutType.valueOf(preferenceStorage.layoutType)); true
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

	private fun changeNotesLayout(layoutType: LayoutType) {
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
		requireActivity().invalidateMenu()
	}

	private fun navigateToEditor() {
		selectionObserver.actionMode?.finish()
		exitTransition = MaterialElevationScale(false).apply {
			duration = resources.getInteger(R.integer.motion_duration_large).toLong()
		}
		reenterTransition = MaterialElevationScale(true).apply {
			duration = resources.getInteger(R.integer.motion_duration_large).toLong()
		}
		val args = bundleOf(Constants.BUNDLE_TAG_ID to tag.id)
		findNavController().tryNavigate(R.id.action_notes_to_editor, args)
	}

	private fun setupRecyclerView() {
		val layoutType = LayoutType.valueOf(preferenceStorage.layoutType)
		binding.recyclerView.apply {
			adapter = noteAdapter
			(layoutManager as StaggeredGridLayoutManager).spanCount = when (layoutType) {
				LayoutType.Linear -> 1
				LayoutType.Grid -> 2
			}
			addItemTouchHelper(ItemTouchHelper(SwipeToDeleteCallback(noteAdapter)))
		}
		postponeEnterTransition()
	}

	private fun submitList(tagName: String) {
		viewModel.getNotesByTag(tagName).observe(viewLifecycleOwner) { notes ->
			if (tagName.isNotEmpty()) supportActionBar?.title = tag.name
			noteAdapter.submitList(notes)
			binding.root.doOnPreDraw { startPostponedEnterTransition() }
		}
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
}
package net.azurewebsites.noties.ui.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.transition.MaterialElevationScale
import dagger.hilt.android.AndroidEntryPoint
import net.azurewebsites.noties.R
import net.azurewebsites.noties.core.FolderEntity
import net.azurewebsites.noties.core.NoteEntity
import net.azurewebsites.noties.databinding.FragmentNotesBinding
import net.azurewebsites.noties.ui.folders.FoldersFragment
import net.azurewebsites.noties.ui.helpers.*
import net.azurewebsites.noties.ui.settings.PreferenceStorage
import javax.inject.Inject

@AndroidEntryPoint
class NotesFragment : Fragment(), SwipeToDeleteListener {

	private var _binding: FragmentNotesBinding? = null
	private val binding get() = _binding!!
	private val viewModel by viewModels<NotesViewModel>()
	private val noteAdapter = NoteAdapter(this)
	@Inject lateinit var userPreferences: PreferenceStorage
	private val folder by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getParcelable(FoldersFragment.FOLDER) ?: FolderEntity()
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
		_binding = FragmentNotesBinding.inflate(inflater, container, false).apply {
			fragment = this@NotesFragment
		}
		addMenuProvider(NotesMenuProvider(), viewLifecycleOwner)
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
	}

	fun createNote() {
		val args = bundleOf(ID to folder.id)
		findNavController().tryNavigate(R.id.action_notes_to_editor, args)
	}

	private fun setupRecyclerView() {
		binding.recyclerView.apply {
			setEmptyView(binding.emptyView)
			adapter = noteAdapter
			(layoutManager as StaggeredGridLayoutManager).spanCount = 1
			addItemTouchHelper(ItemTouchHelper(SwipeToDeleteCallback(noteAdapter)))
		}
		postponeEnterTransition()
	}

	private fun submitListAndUpdateToolbar() {
		supportActionBar?.title = folder.name.ifEmpty { getString(R.string.notes_fragment_label) }
		submitList(folder.id)
	}

	private fun submitList(folderId: Int) {
		viewModel.sortNotes(folderId, SortMode.LastEdit).observe(viewLifecycleOwner) {
			noteAdapter.submitList(it)
			binding.root.doOnPreDraw { startPostponedEnterTransition() }
		}
	}

	override fun moveNoteToTrash(note: NoteEntity) {
		viewModel.moveNoteToTrash(note) { showUndoSnackbar(it) }
	}

	private fun showUndoSnackbar(note: NoteEntity) {
		binding.root.showSnackbar(R.string.deleted_note, action = R.string.undo) {
			viewModel.restoreNote(note, folder.id)
			submitList(folder.id)
		}
	}

	companion object {
		const val ID = "id"
	}
}
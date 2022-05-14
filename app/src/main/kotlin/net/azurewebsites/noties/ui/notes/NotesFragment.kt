package net.azurewebsites.noties.ui.notes

import android.os.Bundle
import android.view.*
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.transition.MaterialElevationScale
import dagger.hilt.android.AndroidEntryPoint
import net.azurewebsites.noties.R
import net.azurewebsites.noties.databinding.FragmentNotesBinding
import net.azurewebsites.noties.ui.folders.FoldersViewModel
import net.azurewebsites.noties.ui.helpers.inflateTransition
import net.azurewebsites.noties.ui.helpers.mainActivity
import net.azurewebsites.noties.ui.settings.PreferenceStorage
import javax.inject.Inject

@AndroidEntryPoint
class NotesFragment : Fragment() {

	private var _binding: FragmentNotesBinding? = null
	private val binding get() = _binding!!
	private val viewModel by viewModels<NotesViewModel>()
	private val parentViewModel by activityViewModels<FoldersViewModel>()
	private val noteAdapter = NoteAdapter()
	@Inject lateinit var userPreferences: PreferenceStorage
	private var directoryId = 0

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setHasOptionsMenu(true)
		enterTransition = inflateTransition(R.transition.slide_from_bottom)
		exitTransition = MaterialElevationScale(false)
		reenterTransition = MaterialElevationScale(true)
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

	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
		inflater.inflate(R.menu.menu_note_list, menu)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setupRecyclerView()
		submitListAndUpdateToolbar()
	}

	private fun setupRecyclerView() {
		binding.recyclerView.apply {
			adapter = noteAdapter
			(layoutManager as StaggeredGridLayoutManager).spanCount = 1
		}
		postponeEnterTransition()
	}

	private fun submitListAndUpdateToolbar() {
		parentViewModel.selectedFolder.observe(viewLifecycleOwner) {
			mainActivity.supportActionBar?.title = it.name.ifEmpty { userPreferences.defaultFolderName }
			directoryId = if (it.id == 0) 1 else it.id
			submitList(directoryId)
		}
	}

	private fun submitList(directoryId: Int) {
		viewModel.sortNotes(directoryId, SortMode.LastEdit).observe(viewLifecycleOwner) { notes ->
			binding.empty.isVisible = notes.isEmpty()
			noteAdapter.submitList(notes)
			binding.root.doOnPreDraw { startPostponedEnterTransition() }
		}
	}

	private companion object {
//		private const val TAG = "URL_LIST_DIALOG"
	}
}
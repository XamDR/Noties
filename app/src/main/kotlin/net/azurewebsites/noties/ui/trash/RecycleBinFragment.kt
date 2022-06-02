package net.azurewebsites.noties.ui.trash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import net.azurewebsites.noties.core.NoteEntity
import net.azurewebsites.noties.databinding.FragmentRecycleBinBinding
import net.azurewebsites.noties.ui.helpers.addMenuProvider
import net.azurewebsites.noties.ui.helpers.removeMenuProvider
import net.azurewebsites.noties.ui.helpers.showDialog
import net.azurewebsites.noties.ui.notes.NoteAdapter
import net.azurewebsites.noties.ui.notes.SwipeToDeleteListener

@AndroidEntryPoint
class RecycleBinFragment : Fragment(), SwipeToDeleteListener, RecycleBinMenuListener {

	private var _binding: FragmentRecycleBinBinding? = null
	private val binding get() = _binding!!
	private val viewModel by viewModels<RecycleBinViewModel>()
	private val noteAdapter = NoteAdapter(this)
	private val menuProvider = RecycleBinMenuProvider(this)

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
		binding.recyclerView.apply {
			adapter = noteAdapter
		}
		observeTrashedNotes()
	}

	override fun moveNoteToTrash(note: NoteEntity) {}

	override fun showEmptyRecycleBinDialog() {
		val emptyRecycleBinDialog = EmptyRecycleBinDialogFragment()
		showDialog(emptyRecycleBinDialog, TAG)
	}

	private fun observeTrashedNotes() {
		viewModel.getTrashedNotes().observe(viewLifecycleOwner) {
			noteAdapter.submitList(it)
			if (it.isEmpty()) removeMenuProvider(menuProvider)
		}
	}

	private companion object {
		private const val TAG = "EMPTY_RECYCLE_BIN_DIALOG"
	}
}
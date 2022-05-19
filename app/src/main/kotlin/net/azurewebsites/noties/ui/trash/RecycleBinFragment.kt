package net.azurewebsites.noties.ui.trash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import net.azurewebsites.noties.databinding.FragmentRecycleBinBinding
import net.azurewebsites.noties.ui.notes.NoteAdapter

@AndroidEntryPoint
class RecycleBinFragment : Fragment() {

	private var _binding: FragmentRecycleBinBinding? = null
	private val binding get() = _binding!!
	private val viewModel by viewModels<RecycleBinViewModel>()
	private val noteAdapter = NoteAdapter()

	override fun onCreateView(inflater: LayoutInflater,
	                          container: ViewGroup?,
	                          savedInstanceState: Bundle?): View {
		_binding = FragmentRecycleBinBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		binding.recyclerView.adapter = null
		_binding = null
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.recyclerView.apply {
			setEmptyView(binding.emptyView)
			adapter = noteAdapter
		}
		viewModel.getTrashedNotes().observe(viewLifecycleOwner) { noteAdapter.submitList(it) }
	}
}
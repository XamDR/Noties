package net.azurewebsites.noties.ui.editor

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialContainerTransform
import dagger.hilt.android.AndroidEntryPoint
import net.azurewebsites.noties.R
import net.azurewebsites.noties.databinding.FragmentEditorBinding
import net.azurewebsites.noties.ui.helpers.getThemeColor
import net.azurewebsites.noties.ui.helpers.hideSoftKeyboard
import net.azurewebsites.noties.ui.helpers.showSoftKeyboard

@AndroidEntryPoint
class EditorFragment : Fragment() {

	private var _binding: FragmentEditorBinding? = null
	private val binding get() = _binding!!
	private val viewModel by viewModels<EditorViewModel>()
	private val directoryId by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getInt("id", 1)
	}

	override fun onCreateView(inflater: LayoutInflater,
							  container: ViewGroup?,
							  savedInstanceState: Bundle?): View {
		_binding = FragmentEditorBinding.inflate(inflater, container, false).apply {
			vm = viewModel
		}
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initTransition()
		navigateUp()
		onBackPressed()
	}

	override fun onStart() {
		super.onStart()
		showSoftKeyboard()
	}

	private fun initTransition() {
		enterTransition = MaterialContainerTransform().apply {
			startView = requireActivity().findViewById(R.id.fab)
			endView = binding.root
			endContainerColor = requireContext().getThemeColor(R.attr.colorSurface)
			scrimColor = Color.TRANSPARENT
		}
	}

	private fun navigateUp() {
		binding.editorToolbar.setNavigationOnClickListener {
			it.hideSoftKeyboard()
			requireActivity().onBackPressed()
		}
	}

	private fun onBackPressed() {
		requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
			insertOrUpdateNote()
			findNavController().popBackStack()
		}
	}

	private fun insertOrUpdateNote() {
		if (viewModel.note.value.text.isNotEmpty()) {
			val note = viewModel.createNote(
				title = viewModel.note.value.title,
				text = viewModel.note.value.text,
				images = listOf(),
				folderId = directoryId
			)
			viewModel.insertNote(note)
		}
	}

	private fun showSoftKeyboard() {
		if (viewModel.note.value.id	== 0) {
			binding.content.showSoftKeyboard()
		}
	}

	companion object {
		const val NOTE = "note"
	}
}
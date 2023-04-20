package io.github.xamdr.noties.ui.editor

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.xamdr.noties.R
import io.github.xamdr.noties.databinding.DialogFragmentColorBinding
import io.github.xamdr.noties.ui.helpers.*

class EditorColorFragment : BottomSheetDialogFragment() {

	private var _binding: DialogFragmentColorBinding? = null
	private val binding get() = _binding!!
	private val viewModel by viewModels<EditorViewModel>({ requireParentFragment() })
	private lateinit var colorAdapter: ColorAdapter
	private val colors = mutableListOf<Int?>(null)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		colors.addAll(requireContext().getIntArray(R.array.colors_editor).toList())
		colorAdapter = ColorAdapter(colors).apply {
			setOnColorSelectedListener { position -> setEditorBackgroundColor(position) }
		}
	}

	override fun onCreateView(inflater: LayoutInflater,
	                          container: ViewGroup?,
	                          savedInstanceState: Bundle?): View {
		_binding = DialogFragmentColorBinding.inflate(inflater, container, false).apply {
//			vm = viewModel
		}
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.list.adapter = colorAdapter
		colorAdapter.selectedPosition = colors.indexOf(viewModel.note.color)
		binding.list.smoothScrollToPosition(colorAdapter.selectedPosition)
	}

	@SuppressLint("NotifyDataSetChanged")
	private fun setEditorBackgroundColor(position: Int) {
		val selectedColor = colors[position]
		viewModel.updateNote(viewModel.note.copy(color = selectedColor))
		binding.root.setBackgroundColor(selectedColor)
		window.setStatusBarColor(selectedColor)
		colorAdapter.apply {
			selectedPosition = colors.indexOf(viewModel.note.color)
			notifyDataSetChanged() // I don't like this, but it works
		}
		onColorSelectedCallback(selectedColor)
	}

	fun setOnColorSelectedListener(callback: (color: Int?) -> Unit) {
		onColorSelectedCallback = callback
	}

	private var onColorSelectedCallback: (color: Int?) -> Unit = {}
}
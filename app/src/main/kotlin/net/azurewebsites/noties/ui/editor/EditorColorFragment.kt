package net.azurewebsites.noties.ui.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import net.azurewebsites.noties.R
import net.azurewebsites.noties.databinding.DialogFragmentColorBinding
import net.azurewebsites.noties.ui.helpers.ColorAdapter
import net.azurewebsites.noties.ui.helpers.getIntArray
import net.azurewebsites.noties.ui.helpers.toColorInt

class EditorColorFragment : BottomSheetDialogFragment() {

	private var _binding: DialogFragmentColorBinding? = null
	private val binding get() = _binding!!
	private val viewModel by viewModels<EditorViewModel>({ requireParentFragment() })
	private lateinit var colorAdapter: ColorAdapter
	private lateinit var colors: List<Int>

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		colors = requireContext().getIntArray(R.array.colors).toList()
		colorAdapter = ColorAdapter(colors).apply {
			setOnColorSelectedListener { position -> setEditorBackgroundColor(position) }
		}
	}

	override fun onCreateView(inflater: LayoutInflater,
	                          container: ViewGroup?,
	                          savedInstanceState: Bundle?): View {
		_binding = DialogFragmentColorBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.list.adapter = colorAdapter
	}

	private fun setEditorBackgroundColor(position: Int) {
		val selectedColor = colors[position]
		viewModel.updateNote(viewModel.entity.copy(color = selectedColor))
		binding.root.setBackgroundColor(selectedColor.toColorInt()) // This makes the imageView invisible lol
		onColorSelectedCallback(selectedColor)
	}

	fun setOnColorSelectedListener(callback: (color: Int) -> Unit) {
		onColorSelectedCallback = callback
	}

	private var onColorSelectedCallback: (color: Int) -> Unit = {}
}
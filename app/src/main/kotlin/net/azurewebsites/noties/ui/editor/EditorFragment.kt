package net.azurewebsites.noties.ui.editor

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import com.google.android.material.transition.MaterialContainerTransform
import dagger.hilt.android.AndroidEntryPoint
import net.azurewebsites.noties.R
import net.azurewebsites.noties.databinding.FragmentEditorBinding
import net.azurewebsites.noties.ui.helpers.*
import net.azurewebsites.noties.ui.media.ImageAdapter

@AndroidEntryPoint
class EditorFragment : Fragment(), AttachImagesListener {

	private var _binding: FragmentEditorBinding? = null
	private val binding get() = _binding!!
	private val viewModel by viewModels<EditorViewModel>()
	private val directoryId by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getInt("id", 1)
	}
	private val pickImagesLauncher = registerForActivityResult(
		ActivityResultContracts.OpenMultipleDocuments(),
		PickImagesCallback(this)
	)
	private val editorImageAdapter = EditorImageAdapter(ImageAdapter())
	private lateinit var editorContentAdapter: EditorContentAdapter

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		editorContentAdapter = EditorContentAdapter(viewModel)
	}

	override fun onCreateView(inflater: LayoutInflater,
							  container: ViewGroup?,
							  savedInstanceState: Bundle?): View {
		_binding = FragmentEditorBinding.inflate(inflater, container, false).apply {
			vm = viewModel
			fragment = this@EditorFragment
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
		binding.content.adapter = ConcatAdapter(editorImageAdapter, editorContentAdapter)
		viewModel.images.observe(viewLifecycleOwner) { editorImageAdapter.submitList(it) }
	}

	override fun onStart() {
		super.onStart()
		showSoftKeyboard()
	}

	override fun addImages(uris: List<Uri>) {
		viewModel.addImages(requireContext(), uris)
		viewModel.images.observe(viewLifecycleOwner) { editorImageAdapter.submitList(it) }
	}

	fun showBottomSheetMenu() {
		val menuDialog = EditorMenuFragment().apply {
			setOnActivityResultListener { pickImagesLauncher.launch(arrayOf("image/*")) }
		}
		showDialog(menuDialog, TAG)
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
			viewModel.insertNote(directoryId)
			findNavController().popBackStack()
		}
	}

	private fun showSoftKeyboard() {
		if (viewModel.note.value.id == 0L) {
			binding.content.showSoftKeyboard()
		}
	}

	companion object {
		const val NOTE = "note"
		private const val TAG = "MENU_DIALOG"
	}
}
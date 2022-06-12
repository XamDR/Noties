package net.azurewebsites.noties.ui.editor

import android.content.Intent
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import com.google.android.material.transition.MaterialContainerTransform
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import net.azurewebsites.noties.R
import net.azurewebsites.noties.core.Note
import net.azurewebsites.noties.databinding.FragmentEditorBinding
import net.azurewebsites.noties.ui.helpers.*
import net.azurewebsites.noties.ui.media.ImageAdapter
import net.azurewebsites.noties.ui.notes.NotesFragment
import net.azurewebsites.noties.ui.urls.JsoupHelper

@AndroidEntryPoint
class EditorFragment : Fragment(), AttachImagesListener, LinkClickedListener {

	private var _binding: FragmentEditorBinding? = null
	private val binding get() = _binding!!
	private val viewModel by viewModels<EditorViewModel>()
	private val notebookId by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getInt(NotesFragment.ID, 1)
	}
	private val pickImagesLauncher = registerForActivityResult(
		ActivityResultContracts.OpenMultipleDocuments(),
		PickImagesCallback(this)
	)
	private val editorImageAdapter = EditorImageAdapter(ImageAdapter())
	private lateinit var editorContentAdapter: EditorContentAdapter
	private var note: Note? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		if (savedInstanceState == null) {
			note = arguments?.getParcelable<Note>(NOTE)?.also {
				viewModel.note.value = it
				viewModel.tempNote.value = viewModel.note.value.clone()
			}
		}
		printDebug("NOTE", note)
		editorContentAdapter = EditorContentAdapter(viewModel, this)
	}

	override fun onCreateView(inflater: LayoutInflater,
							  container: ViewGroup?,
							  savedInstanceState: Bundle?): View {
		_binding = FragmentEditorBinding.inflate(inflater, container, false).apply {
			vm = viewModel
			fragment = this@EditorFragment
			lifecycleOwner = viewLifecycleOwner
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
		onBackPressed()
		binding.content.adapter = ConcatAdapter(editorImageAdapter, editorContentAdapter)
		viewModel.getImages().observe(viewLifecycleOwner) { editorImageAdapter.submitList(it) }
	}

	override fun addImages(uris: List<Uri>) {
		viewLifecycleOwner.lifecycleScope.launch {
			viewModel.addImages(requireContext(), uris)
			viewModel.getImages().observe(viewLifecycleOwner) { editorImageAdapter.submitList(it) }
		}
	}

	override fun onLinkClicked(url: String) {
		viewLifecycleOwner.lifecycleScope.launch {
			val urlTitle = JsoupHelper.extractTitle(url) ?: url
			binding.root.showSnackbar(urlTitle, action = R.string.open_url) {
				startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
			}
		}
	}

	fun showBottomSheetMenu() {
		val menuDialog = EditorMenuFragment().apply {
			setOnActivityResultListener { pickImagesLauncher.launch(arrayOf("image/*")) }
		}
		showDialog(menuDialog, TAG)
	}

	fun navigateUp() {
		binding.root.hideSoftKeyboard()
		requireActivity().onBackPressed()
	}

	private fun initTransition() {
		enterTransition = MaterialContainerTransform().apply {
			startView = requireActivity().findViewById(R.id.fab)
			endView = binding.root
			endContainerColor = requireContext().getThemeColor(R.attr.colorSurface)
			scrimColor = Color.TRANSPARENT
		}
	}

	private fun onBackPressed() {
		requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
			viewLifecycleOwner.lifecycleScope.launch {
				when (viewModel.insertorUpdateNote(notebookId)) {
					Result.SuccesfulInsert -> context?.showToast(R.string.note_saved)
					Result.SuccesfulUpdate -> context?.showToast(R.string.note_updated)
					else -> {}
				}
				findNavController().popBackStack()
			}
		}
	}

	companion object {
		const val NOTE = "note"
		private const val TAG = "MENU_DIALOG"
	}
}
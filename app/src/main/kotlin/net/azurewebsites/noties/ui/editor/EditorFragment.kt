package net.azurewebsites.noties.ui.editor

import android.Manifest
import android.app.Activity
import android.app.KeyguardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
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
import net.azurewebsites.noties.ui.image.BitmapHelper
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
	private val note by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getParcelable(NOTE) ?: Note()
	}
	private val deviceCredentialLauncher = registerForActivityResult(
		ActivityResultContracts.StartActivityForResult()
	) { result -> activityResultCallback(result) }

	private val requestPermissionLauncher = registerForActivityResult(
		ActivityResultContracts.RequestPermission(),
		RequestExternalPermissionCallback { takePicture() }.apply {
			setOnPermissionDeniedListener { binding.root.showSnackbar(R.string.permission_denied) }
		}
	)
	private val takePictureLauncher = registerForActivityResult(
		ActivityResultContracts.TakePicture()
	) { success -> takePictureCallback(success) }

	private lateinit var tempUri: Uri

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		if (savedInstanceState == null) {
			viewModel.note.value = note
			viewModel.tempNote.value = viewModel.note.value.clone()
		}
		editorContentAdapter = EditorContentAdapter(viewModel, this).apply {
			setOnContentReceivedListener { uri -> addImages(listOf(uri)) }
		}
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
		checkIfNoteIsProtected()
		initTransition()
		onBackPressed()
		binding.content.adapter = ConcatAdapter(editorImageAdapter, editorContentAdapter)
		viewModel.images.observe(viewLifecycleOwner) { editorImageAdapter.submitList(it) }
	}

	override fun addImages(uris: List<Uri>) {
		viewLifecycleOwner.lifecycleScope.launch {
			viewModel.addImages(requireContext(), uris)
			viewModel.images.observe(viewLifecycleOwner) { editorImageAdapter.submitList(it) }
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
			setOnTakePictureListener { takePictureOrRequestPermission() }
		}
		showDialog(menuDialog, TAG)
	}

	fun navigateUp() {
		binding.root.hideSoftKeyboard()
		requireActivity().onBackPressed()
	}

	private fun checkIfNoteIsProtected() {
		if (note.entity.isProtected) {
			binding.root.isVisible = false
			requestConfirmeDeviceCredential()
		}
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

	@Suppress("DEPRECATION")
	private fun requestConfirmeDeviceCredential() {
		val keyguardManager = requireContext().getSystemService<KeyguardManager>() ?: return
		val intent = keyguardManager.createConfirmDeviceCredentialIntent(
			getString(R.string.confirme_device_credential_title),
			getString(R.string.confirme_device_credential_desc)
		)
		deviceCredentialLauncher.launch(intent)
	}

	private fun activityResultCallback(result: ActivityResult) {
		if (result.resultCode == Activity.RESULT_OK) {
			binding.root.isVisible = true
		}
		else {
			findNavController().popBackStack()
			binding.root.showSnackbar(R.string.error_auth)
		}
	}

	private fun takePictureCallback(result: Boolean) {
		if (result && ::tempUri.isInitialized) {
			addImages(listOf(tempUri))
		}
		else {
			context?.showToast(R.string.error_take_picture)
		}
	}

	private fun takePictureOrRequestPermission() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			takePicture()
		}
		else {
			if (ContextCompat.checkSelfPermission(
					requireContext(),
					Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
				showRationaleDialog()
			}
			else {
				takePicture()
			}
		}
	}

	private fun takePicture() {
		val savedUri = BitmapHelper.savePicture(requireContext()) ?: return
		tempUri = savedUri
		takePictureLauncher.launch(tempUri)
	}

	private fun showRationaleDialog() {
		PermissionRationaleDialog.createFor(
			requireContext(),
			R.string.write_external_storage_permission_rationale,
			R.drawable.ic_external_storage
		).setNegativeButton(R.string.not_now_button, null)
			.setPositiveButton(R.string.continue_button) { _, _ ->
				requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
			}.show()
	}

	companion object {
		const val NOTE = "note"
		private const val TAG = "MENU_DIALOG"
	}
}
package net.azurewebsites.eznotes.ui.editor

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import net.azurewebsites.eznotes.R
import net.azurewebsites.eznotes.databinding.FragmentEditorMenuBottomSheetDialogBinding
import net.azurewebsites.eznotes.ui.audio.SpeechRecognizerFragment
import net.azurewebsites.eznotes.ui.helpers.PermissionRationaleDialog
import net.azurewebsites.eznotes.ui.helpers.printDebug
import net.azurewebsites.eznotes.ui.helpers.showToast
import net.azurewebsites.eznotes.ui.image.BitmapHelper

class TextEditorMenuFragment : BottomSheetDialogFragment() {

	private var _binding: FragmentEditorMenuBottomSheetDialogBinding? = null
	private val binding get() = _binding!!
	private val mediaLauncher = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
		if (!uris.isNullOrEmpty()) sendUris(uris)
	}
	private lateinit var tempUri: Uri
	private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
		if (success && ::tempUri.isInitialized) {
			printDebug("URI", tempUri)
			sendUris(listOf(tempUri))
		}
		else {
			context?.showToast(R.string.error_take_picture)
		}
	}
	private val writeExternalStoragePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
		if (granted) {
			takePicture()
		}
		else {
			permissionDeniedCallback.invoke()
			dismiss()
		}
	}
	private val recordAudioPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
		if (granted) {
			showRecordingFragment()
		}
		else {
			permissionDeniedCallback.invoke()
			dismiss()
		}
	}

	override fun onCreateView(inflater: LayoutInflater,
							  container: ViewGroup?,
							  savedInstanceState: Bundle?): View {
		_binding = FragmentEditorMenuBottomSheetDialogBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.attachMedia.setOnClickListener { mediaLauncher.launch(arrayOf("image/*")) }
		binding.takePicture.setOnClickListener { takePictureOrRequestPermission() }
		binding.talkToWrite.setOnClickListener {
			checkDangerousPermission(
				permission = Manifest.permission.RECORD_AUDIO,
				messageRes = R.string.audio_recording_permission_rationale,
				drawableRes = R.drawable.ic_talk_to_write
			) {
				recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
			}
		}
	}

	fun setPermissionDeniedListener(callback: () -> Unit) {
		permissionDeniedCallback = callback
	}

	private fun sendUris(uris: List<Uri>) {
		setFragmentResult("uris", bundleOf("uris" to ArrayList(uris)))
		dismiss()
	}

	private fun takePictureOrRequestPermission() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			takePicture()
		}
		else {
			checkDangerousPermission(
				permission = Manifest.permission.WRITE_EXTERNAL_STORAGE,
				messageRes = R.string.write_external_storage_permission_rationale,
				drawableRes = R.drawable.ic_folder
			) {
				writeExternalStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
			}
		}
	}

	private fun takePicture() {
		val savedUri = BitmapHelper.savePicture(requireContext()) ?: return
		tempUri = savedUri
		takePictureLauncher.launch(tempUri)
	}

	private fun showRecordingFragment() {
		val speechRecordingDialog = SpeechRecognizerFragment()
		speechRecordingDialog.show(parentFragmentManager, SPEECH_RECOGNIZER_TAG)
		dismiss()
	}

	private fun checkDangerousPermission(
		permission: String,
		@StringRes messageRes: Int,
		@DrawableRes drawableRes: Int, action: () -> Unit
	) {
		if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
			showRationaleDialog(messageRes, drawableRes, action)
		}
		else {
			action()
		}
	}

	private fun showRationaleDialog(@StringRes messageRes: Int, @DrawableRes drawableRes: Int, action: () -> Unit) {
		PermissionRationaleDialog.createFor(requireContext(), getString(messageRes), drawableRes)
			.setNegativeButton(R.string.not_now_button) { _, _ -> dismiss() }
			.setPositiveButton(R.string.continue_button) { _, _ -> action() }
			.show()
	}

	private var permissionDeniedCallback: () -> Unit = {}

	companion object {
		private const val SPEECH_RECOGNIZER_TAG = "SPEECH_RECOGNIZER"
	}
}
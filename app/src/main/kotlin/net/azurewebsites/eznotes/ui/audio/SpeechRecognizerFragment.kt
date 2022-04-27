package net.azurewebsites.eznotes.ui.audio

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import net.azurewebsites.eznotes.R
import net.azurewebsites.eznotes.databinding.FragmentSpeechRecognizerBinding
import net.azurewebsites.eznotes.ui.helpers.getCurrentLocale
import net.azurewebsites.eznotes.ui.helpers.printDebug
import net.azurewebsites.eznotes.ui.helpers.showToast
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SpeechRecognizerFragment : BottomSheetDialogFragment() {

	private var _binding: FragmentSpeechRecognizerBinding? = null
	private val binding get() = _binding!!
//	private val manager = SpeechRecognizerManager()
	private lateinit var recorder: AudioRecorder
	private var recognizedText: String? = null
	private lateinit var filePath: String
	private lateinit var languageTag: String
	private var isRecording = false
	private var audioUri: Uri? = null
	private val handler = Handler(Looper.getMainLooper())
	private var seconds = 0

	override fun onAttach(context: Context) {
		super.onAttach(context)
		languageTag = context.getCurrentLocale().toLanguageTag()
		createAudioFile()
		initAudioRecorder()
	}

//	override fun onCreate(savedInstanceState: Bundle?) {
//		super.onCreate(savedInstanceState)
//		manager.setOnStartTextRecognitionListener { binding.recording.setText(R.string.stop_button) }
//		manager.setOnStopTextRecognitionListener { requireContext().showToast(R.string.recording_finished) }
//		manager.setOnTextRecognizedListener { text -> recognizedText = text }
//	}

	override fun onCreateView(inflater: LayoutInflater,
	                          container: ViewGroup?,
	                          savedInstanceState: Bundle?): View {
		_binding = FragmentSpeechRecognizerBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
//		manager.close()
		_binding = null
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.recording.setOnClickListener { recognizeTextAndRecordAudio() }

	}

	private fun recognizeTextAndRecordAudio() {
//		manager.recognizeText(languageTag, isRecording)
		binding.recording.setText(R.string.stop_button)
		recorder.recordAudio(isRecording)
		isRecording = !isRecording

		if (!isRecording) {
			setData()
			requireContext().showToast(R.string.recording_finished)
			dismiss()
		}
	}

	@Suppress("DEPRECATION")
	private fun initAudioRecorder() {
		if (::filePath.isInitialized) {
			recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
				AudioRecorder(MediaRecorder(requireContext()), filePath)
			}
			else {
				AudioRecorder(MediaRecorder(), filePath)
			}
		}
	}

	private fun createAudioFile() {
		val audiosDir = File(context?.filesDir, "audios")
		val sufix = (0..999).random()
		val fileName = "AUD_${DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now())}_$sufix.amr"
		filePath = File(audiosDir, fileName).absolutePath
	}

	private fun setData() {
		val audioFile = File(filePath)
		audioUri = FileProvider.getUriForFile(requireContext(), AUTHORITY, audioFile)
		printDebug(TAG, audioUri)
//		printDebug(TAG, recognizedText)
		setFragmentResult(DATA, bundleOf(
			TEXT_KEY to recognizedText,
			URI_KEY to audioUri
		))
	}

	companion object {
		private const val TAG = "Speech_Recognizer_Fragment"
		private const val AUTHORITY = "net.azurewebsites.eznotes"
		private const val DATA = "data"
		private const val TEXT_KEY = "recognized_text"
		private const val URI_KEY = "audio_uri"
	}
}
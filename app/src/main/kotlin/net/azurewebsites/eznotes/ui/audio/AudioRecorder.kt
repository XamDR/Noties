package net.azurewebsites.eznotes.ui.audio

import android.media.MediaRecorder
import net.azurewebsites.eznotes.ui.helpers.printError

class AudioRecorder(private val recorder: MediaRecorder, filePath: String) {

	init {
		recorder.apply {
			setAudioSource(MediaRecorder.AudioSource.MIC)
			setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
			setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
			setOutputFile(filePath)
		}
	}

	fun recordAudio(isListening: Boolean) {
		if (isListening) {
			stopAudioRecording()
		}
		else {
			startAudioRecording()
		}
	}

	private fun startAudioRecording() {
		try {
			recorder.prepare()
			recorder.start()
		}
		catch (e: Exception) {
			printError(TAG, e.message)
		}
	}

	private fun stopAudioRecording() {
		recorder.apply {
			stop()
			release()
		}
	}

	companion object {
		private const val TAG = "RECORDING_MANAGER"
	}
}
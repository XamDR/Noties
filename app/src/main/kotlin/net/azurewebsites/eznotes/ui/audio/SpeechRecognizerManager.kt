package net.azurewebsites.eznotes.ui.audio

import android.os.Handler
import android.os.Looper
import com.microsoft.cognitiveservices.speech.ResultReason
import com.microsoft.cognitiveservices.speech.SpeechConfig
import com.microsoft.cognitiveservices.speech.SpeechRecognizer
import com.microsoft.cognitiveservices.speech.audio.AudioConfig
import net.azurewebsites.eznotes.ui.helpers.printError
import java.util.concurrent.Executors
import java.util.concurrent.Future

class SpeechRecognizerManager {

	private val audioConfig = AudioConfig.fromDefaultMicrophoneInput()
	private val executorService = Executors.newCachedThreadPool()
	private val handler = Handler(Looper.getMainLooper())
	private var recognizer: SpeechRecognizer? = null

	fun recognizeText(languageTag: String, isListening: Boolean) {
		try {
			SpeechConfig.fromSubscription(SPEECH_SUBSCRIPTION_KEY, SERVICE_REGION).use {
				it.speechRecognitionLanguage = languageTag
				recognizer = SpeechRecognizer(it, audioConfig).apply {
					recognizing.addEventListener { _, args ->
						val result = args.result
						if (result.reason == ResultReason.RecognizingSpeech) {
							onTextRecognizedCallback.invoke(result.text)
						}
					}
					if (isListening) {
						stopTextRecognition(this)
					}
					else {
						startTextRecognition(this)
					}
				}
			}
		}
		catch (e: Exception) {
			printError(TAG, e.message)
		}
	}

	fun setOnTextRecognizedListener(callback: (text: String) -> Unit) {
		onTextRecognizedCallback = callback
	}

	fun setOnStartTextRecognitionListener(callback: () -> Unit) {
		onStartTextRecognitionCallback = callback
	}

	fun setOnStopTextRecognitionListener(callback: () -> Unit) {
		onStopTextRecognitionCallback = callback
	}

	fun close() {
		recognizer?.close()
		recognizer = null
	}

	private fun startTextRecognition(speechRecognizer: SpeechRecognizer) {
		val task = speechRecognizer.startContinuousRecognitionAsync()
		setOnTaskCompletedListener(task) {
			handler.post {
				onStartTextRecognitionCallback.invoke()
			}
		}
	}

	private fun stopTextRecognition(speechRecognizer: SpeechRecognizer) {
		val task = speechRecognizer.stopContinuousRecognitionAsync()
		setOnTaskCompletedListener(task) {
			handler.post {
				onStopTextRecognitionCallback.invoke()
			}
		}
	}

	private fun <T> setOnTaskCompletedListener(task: Future<T>, listener: (T) -> Unit) {
		executorService.submit {
			val result = task.get()
			listener(result)
		}
	}

	private var onTextRecognizedCallback: (text: String) -> Unit = {}
	private var onStartTextRecognitionCallback: () -> Unit = {}
	private var onStopTextRecognitionCallback: () -> Unit = {}

	companion object {
		private const val TAG = "SPEECH_RECOGNIZER_MANAGER"
		private const val SPEECH_SUBSCRIPTION_KEY = "ad9273eb5788487f812206532e264bc7"
		private const val SERVICE_REGION = "brazilsouth"
	}
}
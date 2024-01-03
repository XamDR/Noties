package io.github.xamdr.noties.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.github.xamdr.noties.R
import io.github.xamdr.noties.databinding.ActivityMainBinding
import io.github.xamdr.noties.domain.model.MediaItem
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.ui.helpers.Constants
import io.github.xamdr.noties.ui.helpers.UriHelper
import io.github.xamdr.noties.ui.helpers.findNavController
import io.github.xamdr.noties.ui.helpers.getParcelableArrayListCompat
import io.github.xamdr.noties.ui.helpers.getParcelableExtraCompat
import io.github.xamdr.noties.ui.helpers.media.MediaHelper
import io.github.xamdr.noties.ui.helpers.showToast
import io.github.xamdr.noties.ui.helpers.simpleName
import io.github.xamdr.noties.ui.helpers.tryNavigate
import io.github.xamdr.noties.ui.settings.PreferenceStorage
import io.github.xamdr.noties.ui.settings.setNightMode
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

	@Inject lateinit var preferenceStorage: PreferenceStorage
	private val binding by lazy(LazyThreadSafetyMode.NONE) { ActivityMainBinding.inflate(layoutInflater) }
	private val navController by lazy(LazyThreadSafetyMode.NONE) { findNavController(R.id.nav_host_fragment) }

	override fun onCreate(savedInstanceState: Bundle?) {
		installSplashScreen()
		super.onCreate(savedInstanceState)
		setNightMode(preferenceStorage.appTheme)
		setContentView(binding.root)
		setupNavigation()
		handleIntent()
	}

	private fun setupNavigation() {
		navController.graph = navController.navInflater.inflate(R.navigation.nav_graph).apply {
			setStartDestination(
				if (preferenceStorage.isOnboardingCompleted) R.id.nav_all_notes else R.id.nav_welcome
			)
		}
	}

	private fun handleIntent() {
		if (intent != null) {
			if (intent.hasExtra(Constants.BUNDLE_NOTE_ID)) {
				val noteId = intent.getLongExtra(Constants.BUNDLE_NOTE_ID, 0L)
				val args = bundleOf(Constants.BUNDLE_NOTE_ID to noteId)
				navController.tryNavigate(R.id.action_notes_to_editor, args)
			}
			else {
				when (intent.action) {
					Intent.ACTION_SEND -> lifecycleScope.launch { handleSharedContent(intent) }
					Intent.ACTION_SEND_MULTIPLE -> lifecycleScope.launch { handleSharedMultipleContent(intent) }
					Intent.ACTION_VIEW -> lifecycleScope.launch { openFile(intent) }
				}
			}
		}
	}

	private suspend fun handleSharedContent(intent: Intent) {
		val type = intent.type
		if (type != null && type == Constants.MIME_TYPE_TEXT) {
			val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
			val note = Note(text = sharedText ?: String.Empty)
			val args = bundleOf(Constants.BUNDLE_NOTE to note)
			navController.tryNavigate(R.id.action_notes_to_editor, args)
		}
		else if (type != null && type.startsWith(Constants.MIME_TYPE_IMAGE.removeSuffix("*"))) {
			val uri = intent.getParcelableExtraCompat(Intent.EXTRA_STREAM, Uri::class.java)
			val newUri = MediaHelper.copyUri(this, uri)
			val mimeType = contentResolver.getType(newUri)
			val image = MediaItem(uri = newUri, mimeType = mimeType)
			val note = Note(items = listOf(image))
			val args = bundleOf(Constants.BUNDLE_NOTE to note)
			navController.tryNavigate(R.id.action_notes_to_editor, args)
		}
	}

	private suspend fun handleSharedMultipleContent(intent: Intent) {
		val type = intent.type
		if (type != null && type.startsWith(Constants.MIME_TYPE_IMAGE.removeSuffix("*"))) {
			val uris = intent.getParcelableArrayListCompat(Intent.EXTRA_STREAM, Uri::class.java)
			val images = uris
				.map { MediaHelper.copyUri(this, it) }
				.map { MediaItem(uri = it, mimeType = contentResolver.getType(it)) }
			val note = Note(items = images)
			val args = bundleOf(Constants.BUNDLE_NOTE to note)
			navController.tryNavigate(R.id.action_notes_to_editor, args)
		}
		else if (type == "*/*") {
			val clipData = intent.clipData ?: return
			val images = mutableListOf<MediaItem>()
			var finalText = String.Empty
			var finalTitle = String.Empty
			for (index in 0..< clipData.itemCount) {
				val uri = clipData.getItemAt(index).uri
				val mimeType = contentResolver.getType(uri)
				if (mimeType != null) {
					if (mimeType.startsWith(Constants.MIME_TYPE_IMAGE.removeSuffix("*"))) {
						val newUri = MediaHelper.copyUri(this, uri)
						val image = MediaItem(uri = newUri, mimeType = mimeType)
						images.add(image)
					}
					else if (mimeType == Constants.MIME_TYPE_TEXT) {
						val text = UriHelper.readTextFromUri(this, uri)
						val title = DocumentFile.fromSingleUri(this, uri)?.simpleName
						finalText = finalText.replace(finalText, text)
						finalTitle = finalTitle.replace(finalTitle, title ?: String.Empty)
					}
				}
			}
			val note = Note(text = finalText, items = images, title = finalTitle)
			val args = bundleOf(Constants.BUNDLE_NOTE to note)
			navController.tryNavigate(R.id.action_notes_to_editor, args)
		}
	}

	private suspend fun openFile(intent: Intent) {
		val data = intent.data ?: return
		val text = UriHelper.readTextFromUri(this, data)
		val file = DocumentFile.fromSingleUri(this, data)
		if (text.isNotEmpty()) {
			val note = Note(text = text, title = file?.simpleName ?: String.Empty)
			val args = bundleOf(Constants.BUNDLE_NOTE to note)
			navController.tryNavigate(R.id.action_notes_to_editor, args)
		}
		else {
			showToast(R.string.error_open_file)
		}
	}
}
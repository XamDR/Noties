package io.github.xamdr.noties.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.github.xamdr.noties.R
import io.github.xamdr.noties.databinding.ActivityMainBinding
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.ui.helpers.Constants
import io.github.xamdr.noties.ui.helpers.UriHelper
import io.github.xamdr.noties.ui.helpers.findNavController
import io.github.xamdr.noties.ui.helpers.showToast
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
					Intent.ACTION_SEND -> {
						if (intent.type == Constants.MIME_TYPE_TEXT) {
							val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return
							val note = Note(text = sharedText)
							val args = bundleOf(
								Constants.BUNDLE_NOTE_ID to note.id,
								Constants.BUNDLE_NOTE_TEXT to note.text
							)
							navController.tryNavigate(R.id.action_notes_to_editor, args)
						}
					}
					Intent.ACTION_VIEW -> {
						val data = intent.data ?: return
						lifecycleScope.launch {
							val text = UriHelper.readTextFromUri(this@MainActivity, data)
							if (text.isNotEmpty()) {
								val note = Note(text = text)
								val args = bundleOf(
									Constants.BUNDLE_NOTE_ID to note.id,
									Constants.BUNDLE_NOTE_TEXT to note.text
								)
								navController.tryNavigate(R.id.action_notes_to_editor, args)
							}
							else {
								showToast(R.string.error_open_file)
							}
						}
					}
				}
			}
		}
	}
}
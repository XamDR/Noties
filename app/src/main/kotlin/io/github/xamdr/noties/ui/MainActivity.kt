package io.github.xamdr.noties.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.AppBarLayout.LayoutParams
import dagger.hilt.android.AndroidEntryPoint
import io.github.xamdr.noties.R
import io.github.xamdr.noties.databinding.ActivityMainBinding
import io.github.xamdr.noties.ui.helpers.findNavController
import io.github.xamdr.noties.ui.helpers.setNightMode
import io.github.xamdr.noties.ui.settings.PreferenceStorage
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener {

	private val binding by lazy(LazyThreadSafetyMode.NONE) { ActivityMainBinding.inflate(layoutInflater) }
	private val navController by lazy(LazyThreadSafetyMode.NONE) { findNavController(R.id.nav_host_fragment) }
	@Inject lateinit var preferenceStorage: PreferenceStorage

	override fun onCreate(savedInstanceState: Bundle?) {
		installSplashScreen()
		super.onCreate(savedInstanceState)
		setNightMode()
		setContentView(binding.root)
		setSupportActionBar(binding.toolbar)
		setupNavigation()
//		handleIntent()
	}

//	private fun handleIntent() {
//		if (intent.action == Intent.ACTION_SEND) {
//			if (intent.type == "text/plain") {
//				val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return
//				val entity = DatabaseNoteEntity(
//					text = sharedText,
//					modificationDate = ZonedDateTime.now(),
//					urls = extractUrls(sharedText),
//					notebookId = 1
//				)
//				val args = bundleOf(EditorViewModel.NOTE to Note(entity = entity))
//				navController.tryNavigate(R.id.action_notes_to_editor, args)
//			}
//		}
//		else {
//			val uri = intent.data ?: return
//			val entity = readUriContent(this, uri)
//			if (entity != null) {
//				val args = bundleOf(EditorViewModel.NOTE to Note(entity = entity))
//				navController.tryNavigate(R.id.action_notes_to_editor, args)
//			}
//			else {
//				showToast(R.string.error_open_file)
//			}
//		}
//	}

	override fun onStart() {
		super.onStart()
		navController.addOnDestinationChangedListener(this)
	}

	override fun onStop() {
		super.onStop()
		navController.removeOnDestinationChangedListener(this)
	}

	override fun onPostCreate(savedInstanceState: Bundle?) {
		super.onPostCreate(savedInstanceState)
		navController.currentDestination?.label?.let {
			if (it.isNotEmpty()) binding.toolbar.title = it
		}
	}

	override fun onDestinationChanged(
		controller: NavController,
		destination: NavDestination,
		arguments: Bundle?)
	{
		if (arguments != null) {
			binding.toolbar.isVisible = arguments.getBoolean("ShowToolbar")
		}
//		when (destination.id) {
//			R.id.nav_notes, R.id.nav_trash -> toggleToolbarScrollFlags(isEnabled = true)
//			else -> toggleToolbarScrollFlags(isEnabled = false)
//		}
	}

	private fun setupNavigation() {
		val appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_notes))
		navController.graph = navController.navInflater.inflate(R.navigation.nav_graph).apply {
			setStartDestination(
				if (preferenceStorage.isOnboardingCompleted) R.id.nav_notes else R.id.nav_welcome
			)
		}
		binding.toolbar.setupWithNavController(navController, appBarConfiguration)
	}

	private fun toggleToolbarScrollFlags(isEnabled: Boolean) {
		val params = binding.toolbar.layoutParams as LayoutParams
		params.scrollFlags = if (isEnabled) {
			LayoutParams.SCROLL_FLAG_SCROLL or LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
		} else 0
	}

	companion object {
		const val CHANNEL_ID = "NOTIES_CHANNEL"
	}
}
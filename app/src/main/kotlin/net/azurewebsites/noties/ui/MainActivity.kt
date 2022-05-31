package net.azurewebsites.noties.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.AppBarLayout.LayoutParams
import dagger.hilt.android.AndroidEntryPoint
import net.azurewebsites.noties.R
import net.azurewebsites.noties.core.FolderEntity
import net.azurewebsites.noties.databinding.ActivityMainBinding
import net.azurewebsites.noties.ui.folders.FoldersViewModel
import net.azurewebsites.noties.ui.helpers.findNavController
import net.azurewebsites.noties.ui.helpers.setNightMode
import net.azurewebsites.noties.ui.helpers.tryNavigate
import net.azurewebsites.noties.ui.settings.PreferenceStorage
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener {

	private val binding by lazy(LazyThreadSafetyMode.NONE) { ActivityMainBinding.inflate(layoutInflater) }
	private val navController by lazy(LazyThreadSafetyMode.NONE) { findNavController(R.id.nav_host_fragment) }
	private val viewModel by viewModels<FoldersViewModel>()
	@Inject lateinit var userPreferences: PreferenceStorage
	private var currentFolder: FolderEntity? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		installSplashScreen()
		super.onCreate(savedInstanceState)
		setNightMode()
		setContentView(binding.root)
		setSupportActionBar(binding.toolbar)
		setupNavigation()
		createDefaultFolders()
	}

	override fun onStart() {
		super.onStart()
		navController.addOnDestinationChangedListener(this)
	}

	override fun onStop() {
		super.onStop()
		navController.removeOnDestinationChangedListener(this)
	}

	override fun onDestinationChanged(
		controller: NavController,
		destination: NavDestination,
		arguments: Bundle?)
	{
		if (arguments != null) {
			binding.toolbar.isVisible = arguments.getBoolean("ShowToolbar")
		}
		if (destination.id == R.id.nav_notes) {
			toggleToolbarScrollFlags(isEnabled = true)
		}
		else {
			toggleToolbarScrollFlags(isEnabled = false)
		}
	}

	fun navigateToEditor() {
		val args = bundleOf("id" to currentFolder?.id)
		navController.tryNavigate(R.id.action_notes_to_editor, args)
	}

	private fun setupNavigation() {
		val appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_folders), binding.drawerLayout)
		navController.graph = navController.navInflater.inflate(R.navigation.nav_graph).apply {
			setStartDestination(if (userPreferences.isOnboardingCompleted) R.id.nav_folders else R.id.nav_welcome)
		}
		binding.toolbar.setupWithNavController(navController, appBarConfiguration)
		binding.navView.setupWithNavController(navController)
	}

	private fun createDefaultFolders() {
		if (!userPreferences.isOnboardingCompleted) {
			val defaultFolder = FolderEntity(name = userPreferences.defaultFolderName)
			viewModel.insertFolder(defaultFolder)
			val trashFolder = FolderEntity(id = -1, name = "Recycle Bin")
			viewModel.insertFolder(trashFolder)
		}
	}

	private fun toggleToolbarScrollFlags(isEnabled: Boolean) {
		val params = binding.toolbar.layoutParams as LayoutParams
		params.scrollFlags = if (isEnabled) {
			LayoutParams.SCROLL_FLAG_SCROLL or LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
		} else 0
	}
}
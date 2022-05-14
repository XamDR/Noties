package net.azurewebsites.noties.ui

import android.app.Activity
import android.app.KeyguardManager
import android.os.Bundle
import android.view.Menu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.getSystemService
import androidx.core.os.bundleOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.core.view.isNotEmpty
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.AppBarLayout.LayoutParams
import dagger.hilt.android.AndroidEntryPoint
import net.azurewebsites.noties.R
import net.azurewebsites.noties.core.Folder
import net.azurewebsites.noties.core.FolderEntity
import net.azurewebsites.noties.databinding.ActivityMainBinding
import net.azurewebsites.noties.ui.folders.FoldersViewModel
import net.azurewebsites.noties.ui.helpers.findNavController
import net.azurewebsites.noties.ui.helpers.setNightMode
import net.azurewebsites.noties.ui.helpers.showSnackbar
import net.azurewebsites.noties.ui.helpers.tryNavigate
import net.azurewebsites.noties.ui.notes.FabScrollingBehavior
import net.azurewebsites.noties.ui.settings.PreferenceStorage
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener {

	private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
	private val navController by lazy { findNavController(R.id.nav_host_fragment) }
	private val viewModel by viewModels<FoldersViewModel>()
	@Inject lateinit var userPreferences: PreferenceStorage
	private var currentFolder: FolderEntity? = null
	private val deviceCredentialLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
		if (result.resultCode == Activity.RESULT_OK) {
			currentFolder?.let { viewModel.updateCurrentFolder(it) }
		}
		else {
			binding.root.showSnackbar(R.string.error_auth)
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		installSplashScreen()
		super.onCreate(savedInstanceState)
		setNightMode()
		binding.activity = this
		setContentView(binding.root)
		setSupportActionBar(binding.toolbar)
		setupNavigation()
		createDefaultFolder()
	}

	override fun onStart() {
		super.onStart()
		navController.addOnDestinationChangedListener(this)
		viewModel.folders.observe(this) { updateNavDrawer(it) }
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
			if (arguments.getBoolean("ShowFab")) binding.fab.show() else binding.fab.hide()
		}
		if (destination.id == R.id.nav_notes) {
			toggleToolbarScrollFlags(isEnabled = true)
			toggleFabBehavior(isEnabled = true)
		}
		else {
			toggleToolbarScrollFlags(isEnabled = false)
			toggleFabBehavior(isEnabled = false)
		}
	}

	fun navigateToEditor() {
		val args = bundleOf("id" to currentFolder?.id)
		navController.tryNavigate(R.id.action_notes_to_editor, args)
	}

	private fun setupNavigation() {
		val appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_notes), binding.drawerLayout)
		navController.graph = navController.navInflater.inflate(R.navigation.nav_graph).apply {
			setStartDestination(if (userPreferences.isOnboardingCompleted) R.id.nav_notes else R.id.nav_welcome)
		}
		binding.toolbar.setupWithNavController(navController, appBarConfiguration)
		binding.navView.setupWithNavController(navController)
	}

	private fun updateNavDrawer(folders: List<Folder>) {
		val item = binding.navView.menu.getItem(0)
		if (item.subMenu.isNotEmpty()) item.subMenu.clear()

		for (folder in folders) {
			item.subMenu.add(R.id.group_folders, Menu.NONE, Menu.NONE, folder.entity.name)
				.setIcon(if (folder.entity.id == 1) R.drawable.ic_folder_general else R.drawable.ic_folder)
				.setOnMenuItemClickListener { filterNotesByFolder(folder.entity); true }
		}
		item.subMenu.add(R.id.group_folders, R.id.nav_folders, 999, getString(R.string.view_folders))
			.setIcon(R.drawable.ic_edit_folder_name)
	}

	private fun createDefaultFolder() {
		if (!userPreferences.isOnboardingCompleted) {
			val defaultFolder = FolderEntity(name = userPreferences.defaultFolderName)
			viewModel.insertFolder(defaultFolder)
		}
	}

	@Suppress("DEPRECATION")
	private fun requestConfirmeDeviceCredential() {
		val keyguardManager = getSystemService<KeyguardManager>() ?: return
		val intent = keyguardManager.createConfirmDeviceCredentialIntent(
			getString(R.string.confirme_device_credential_title),
			getString(R.string.confirme_device_credential_desc)
		)
		deviceCredentialLauncher.launch(intent)
	}

	private fun filterNotesByFolder(folder: FolderEntity) {
		binding.drawerLayout.closeDrawer(GravityCompat.START, true)
		if (currentFolder != folder) {
			currentFolder = folder

			if (folder.isProtected) {
				requestConfirmeDeviceCredential()
			}
			else {
				viewModel.updateCurrentFolder(folder)
			}
		}
	}

	private fun toggleToolbarScrollFlags(isEnabled: Boolean) {
		val params = binding.toolbar.layoutParams as LayoutParams
		params.scrollFlags = if (isEnabled) {
			LayoutParams.SCROLL_FLAG_SCROLL or LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
		} else 0
	}

	private fun toggleFabBehavior(isEnabled: Boolean) {
		val params = binding.fab.layoutParams as CoordinatorLayout.LayoutParams
		params.behavior = if (isEnabled) FabScrollingBehavior() else null
	}
}
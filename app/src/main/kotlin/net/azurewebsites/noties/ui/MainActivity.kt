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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.core.view.isNotEmpty
import androidx.core.view.isVisible
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.AppBarLayout.LayoutParams
import dagger.hilt.android.AndroidEntryPoint
import net.azurewebsites.noties.R
import net.azurewebsites.noties.domain.FolderEntity
import net.azurewebsites.noties.databinding.ActivityMainBinding
import net.azurewebsites.noties.ui.folders.FoldersViewModel
import net.azurewebsites.noties.ui.helpers.setNightMode
import net.azurewebsites.noties.ui.helpers.showSnackbar
import net.azurewebsites.noties.ui.notes.FabScrollingBehavior
import net.azurewebsites.noties.ui.settings.PreferenceStorage
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

	private lateinit var binding: ActivityMainBinding
	private val viewModel by viewModels<FoldersViewModel>()
	private lateinit var appBarConfiguration: AppBarConfiguration
	@Inject lateinit var userPreferences: PreferenceStorage
	private var currentFolder: FolderEntity? = null
	private val deviceCredentialLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
		if (result.resultCode == Activity.RESULT_OK) {
			currentFolder?.let { viewModel.currentDirectory.value = it }
		}
		else {
			binding.root.showSnackbar(R.string.error_auth)
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		installSplashScreen()
		super.onCreate(savedInstanceState)
		setNightMode()
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)
		setSupportActionBar(binding.toolbar)
		setupNavigation()
		createGeneralDirectory()
	}

	override fun onStart() {
		super.onStart()
		viewModel.directories.observe(this) { directories -> updateNavDrawer(directories) }
		binding.fab.setOnClickListener { onFabClickCallback.invoke() }
	}

	override fun onSupportNavigateUp(): Boolean {
		val navController = findNavController(R.id.nav_host_fragment)
		return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
	}

	private fun setupNavigation() {
		appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_notes), binding.drawerLayout)
		val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
		val navController = navHostFragment.navController
		navController.graph = navController.navInflater.inflate(R.navigation.nav_graph).apply {
			setStartDestination(if (userPreferences.isOnboardingCompleted) R.id.nav_notes else R.id.nav_welcome)
		}
		setupActionBarWithNavController(navController, appBarConfiguration)
		binding.navView.setupWithNavController(navController)
		navController.addOnDestinationChangedListener { _, destination, arguments ->
			changeToolbarScrollFlags(destination)
			updateUI(arguments)
		}
	}

	private fun updateNavDrawer(folders: List<FolderEntity>) {
		val item = binding.navView.menu.getItem(0)
		if (item.subMenu.isNotEmpty()) item.subMenu.clear()

		for (directory in folders) {
			item.subMenu.add(R.id.group_folders, Menu.NONE, Menu.NONE, directory.name)
				.setIcon(if (directory.id == 1) R.drawable.ic_folder_general else R.drawable.ic_folder)
				.setOnMenuItemClickListener {
					filterNotesByDirectory(directory); true
				}
		}
		item.subMenu.add(R.id.group_folders, R.id.nav_folders, 999, getString(R.string.edit_folders))
			.setIcon(R.drawable.ic_edit_folder_name)
	}

	private fun createGeneralDirectory() {
		if (userPreferences.isOnboardingCompleted) {
			val defaultDirectory = FolderEntity(name = userPreferences.defaultDirectoryName)
			viewModel.upsertDirectory(defaultDirectory)
		}
	}

	private fun filterNotesByDirectory(folder: FolderEntity) {
		binding.drawerLayout.closeDrawer(GravityCompat.START, true)
		if (folder != currentFolder) {
			currentFolder = folder

			if (folder.isProtected) {
				requestConfirmeDeviceCredential()
			}
			else {
				viewModel.currentDirectory.value = folder
			}
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

	private fun updateUI(arguments: Bundle?) {
		if (arguments != null) {
			binding.toolbar.isVisible = arguments.getBoolean("ShowToolbar")
			if (arguments.getBoolean("ShowFab")) binding.fab.show() else binding.fab.hide()
		}
	}

	private fun changeToolbarScrollFlags(destination: NavDestination) {
		if (destination.id == R.id.nav_editor) {
			toggleToolbarScrollFlags(isEnabled = false)
			toggleFabBehavior(isEnabled = false)
		}
		else {
			toggleToolbarScrollFlags(isEnabled = true)
			toggleFabBehavior(isEnabled = true)
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

	fun setOnFabClickListener(callback: () -> Unit) {
		onFabClickCallback = callback
	}

	private var onFabClickCallback: () -> Unit = {}
}
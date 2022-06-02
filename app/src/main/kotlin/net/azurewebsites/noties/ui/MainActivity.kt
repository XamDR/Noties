package net.azurewebsites.noties.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.AppBarLayout.LayoutParams
import com.google.android.material.navigation.NavigationBarView
import dagger.hilt.android.AndroidEntryPoint
import net.azurewebsites.noties.R
import net.azurewebsites.noties.core.FolderEntity
import net.azurewebsites.noties.databinding.ActivityMainBinding
import net.azurewebsites.noties.ui.folders.FoldersFragment
import net.azurewebsites.noties.ui.helpers.findNavController
import net.azurewebsites.noties.ui.helpers.setNightMode
import net.azurewebsites.noties.ui.helpers.tryNavigate
import net.azurewebsites.noties.ui.notes.FabScrollingBehavior
import net.azurewebsites.noties.ui.settings.PreferenceStorage
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener,
	NavigationBarView.OnItemReselectedListener {

	private val binding by lazy(LazyThreadSafetyMode.NONE) { ActivityMainBinding.inflate(layoutInflater) }
	private val navController by lazy(LazyThreadSafetyMode.NONE) { findNavController(R.id.nav_host_fragment) }
	private val appBarConfiguration by lazy(LazyThreadSafetyMode.NONE) {
		AppBarConfiguration(setOf(R.id.nav_folders, R.id.nav_notes, R.id.nav_trash))
	}
	@Inject lateinit var userPreferences: PreferenceStorage

	override fun onCreate(savedInstanceState: Bundle?) {
		installSplashScreen()
		super.onCreate(savedInstanceState)
		setNightMode()
		binding.activity = this
		setContentView(binding.root)
		setSupportActionBar(binding.toolbar)
		setupNavigation()
	}

	override fun onStart() {
		super.onStart()
		navController.addOnDestinationChangedListener(this)
		binding.navView.setOnItemReselectedListener(this)
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
		when (destination.id) {
			R.id.nav_notes, R.id.nav_trash -> toggleToolbarScrollFlags(isEnabled = true)
			else -> toggleToolbarScrollFlags(isEnabled = false)
		}
		if (destination.id == R.id.nav_notes) {
			toggleFabBehavior(isEnabled = true)
		}
		else {
			toggleFabBehavior(isEnabled = false)
		}
		binding.navView.isVisible = appBarConfiguration.topLevelDestinations.contains(destination.id)
	}

	override fun onNavigationItemReselected(item: MenuItem) {
		if (item.itemId == R.id.nav_notes) {
			val args = bundleOf(FoldersFragment.FOLDER to FolderEntity())
			navController.tryNavigate(R.id.action_notes_to_self, args)
		}
	}

	fun invokeCallback() = fabClickedListener()

	private fun setupNavigation() {
		navController.graph = navController.navInflater.inflate(R.navigation.nav_graph).apply {
			setStartDestination(if (userPreferences.isOnboardingCompleted) R.id.nav_folders else R.id.nav_welcome)
		}
		binding.toolbar.setupWithNavController(navController, appBarConfiguration)
		binding.navView.setupWithNavController(navController)
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
		fabClickedListener = callback
	}

	private var fabClickedListener: () -> Unit = {}
}
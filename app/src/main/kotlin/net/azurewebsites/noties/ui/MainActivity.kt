package net.azurewebsites.noties.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.core.view.isNotEmpty
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.AppBarLayout.LayoutParams
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import net.azurewebsites.noties.R
import net.azurewebsites.noties.core.Notebook
import net.azurewebsites.noties.core.NotebookEntity
import net.azurewebsites.noties.databinding.ActivityMainBinding
import net.azurewebsites.noties.ui.helpers.findNavController
import net.azurewebsites.noties.ui.helpers.setNightMode
import net.azurewebsites.noties.ui.helpers.tryNavigate
import net.azurewebsites.noties.ui.notebooks.NotebooksFragment
import net.azurewebsites.noties.ui.notebooks.NotebooksViewModel
import net.azurewebsites.noties.ui.notes.FabScrollingBehavior
import net.azurewebsites.noties.ui.settings.PreferenceStorage
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener,
	NavigationView.OnNavigationItemSelectedListener {

	private val binding by lazy(LazyThreadSafetyMode.NONE) { ActivityMainBinding.inflate(layoutInflater) }
	private val navController by lazy(LazyThreadSafetyMode.NONE) { findNavController(R.id.nav_host_fragment) }
	private val viewModel by viewModels<NotebooksViewModel>()
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
		binding.navView.setNavigationItemSelectedListener(this)
		viewModel.notebooks.observe(this) { updateNavDrawer(it) }
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
	}

	override fun onNavigationItemSelected(item: MenuItem): Boolean {
		binding.drawerLayout.closeDrawer(GravityCompat.START, true)
		return when (item.itemId) {
			R.id.nav_all_notes -> {
				navigateToNotes(NotebookEntity()); true
			}
			else -> NavigationUI.onNavDestinationSelected(item, navController)
		}
	}

	private fun setupNavigation() {
		val appBarConfiguration = AppBarConfiguration(
			setOf(R.id.nav_notes, R.id.nav_notebooks),
			binding.drawerLayout
		)
		navController.graph = navController.navInflater.inflate(R.navigation.nav_graph).apply {
			setStartDestination(
				if (userPreferences.isOnboardingCompleted) R.id.nav_notes else R.id.nav_welcome
			)
		}
		binding.toolbar.setupWithNavController(navController, appBarConfiguration)
		binding.navView.setupWithNavController(navController)
	}

	private fun updateNavDrawer(notebooks: List<Notebook>) {
		val item = binding.navView.menu.getItem(1)
		if (item.subMenu.isNotEmpty()) item.subMenu.clear()

		for (notebook in notebooks) {
			item.subMenu.add(R.id.group_notebooks, 1, 1, notebook.entity.name)
				.setIcon(R.drawable.ic_notebook)
				.setOnMenuItemClickListener { filterNotesByNotebook(notebook.entity); true }
		}
		item.subMenu.add(R.id.group_notebooks, R.id.nav_notebooks, 1000, getString(R.string.view_notebooks))
			.setIcon(R.drawable.ic_edit_notebooks)
	}

	private fun filterNotesByNotebook(notebook: NotebookEntity) {
		binding.drawerLayout.closeDrawer(GravityCompat.START, true)
		navigateToNotes(notebook)
	}

	private fun navigateToNotes(notebook: NotebookEntity) {
		val args = bundleOf(NotebooksFragment.NOTEBOOK to notebook)
		val currentDestinationId = navController.currentDestination?.id
		when (currentDestinationId) {
			R.id.nav_notes -> navController.tryNavigate(R.id.action_notes_to_self, args)
			R.id.nav_notebooks -> navController.tryNavigate(R.id.action_notebooks_to_notes, args)
			else -> throw Exception("There is no route from $currentDestinationId to ${R.id.nav_notes}")
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

	fun invokeCallback() = onFabClickCallback()

	fun setOnFabClickListener(callback: () -> Unit) {
		onFabClickCallback = callback
	}

	private var onFabClickCallback: () -> Unit = {}
}
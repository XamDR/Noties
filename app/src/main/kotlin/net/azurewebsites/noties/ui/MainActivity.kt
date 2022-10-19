package net.azurewebsites.noties.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.core.view.isNotEmpty
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.AppBarLayout.LayoutParams
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import net.azurewebsites.noties.R
import net.azurewebsites.noties.core.Note
import net.azurewebsites.noties.core.NoteEntity
import net.azurewebsites.noties.core.Notebook
import net.azurewebsites.noties.core.NotebookEntity
import net.azurewebsites.noties.databinding.ActivityMainBinding
import net.azurewebsites.noties.ui.editor.EditorViewModel
import net.azurewebsites.noties.ui.helpers.*
import net.azurewebsites.noties.ui.notebooks.*
import net.azurewebsites.noties.ui.notes.FabScrollingBehavior
import net.azurewebsites.noties.ui.settings.PreferenceStorage
import java.time.ZonedDateTime
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener,
	NavigationView.OnNavigationItemSelectedListener, NotebookToolbarItemListener {

	private val binding by lazy(LazyThreadSafetyMode.NONE) { ActivityMainBinding.inflate(layoutInflater) }
	private val navController by lazy(LazyThreadSafetyMode.NONE) { findNavController(R.id.nav_host_fragment) }
	private val viewModel by viewModels<NotebooksViewModel>()
	@Inject lateinit var preferenceStorage: PreferenceStorage

	override fun onCreate(savedInstanceState: Bundle?) {
		installSplashScreen()
		super.onCreate(savedInstanceState)
		setNightMode()
		binding.activity = this
		setContentView(binding.root)
		setSupportActionBar(binding.toolbar)
		setupNavigation()
		handleIntent()
	}

	private fun handleIntent() {
		if (intent.action == Intent.ACTION_SEND) {
			if (intent.type == "text/plain") {
				val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return
				val entity = NoteEntity(
					text = sharedText,
					modificationDate = ZonedDateTime.now(),
					urls = extractUrls(sharedText),
					notebookId = 1
				)
				val args = bundleOf(EditorViewModel.NOTE to Note(entity = entity))
				navController.tryNavigate(R.id.action_notes_to_editor, args)
			}
		}
		else {
			val uri = intent.data ?: return
			val entity = readUriContent(this, uri)
			if (entity != null) {
				val args = bundleOf(EditorViewModel.NOTE to Note(entity = entity))
				navController.tryNavigate(R.id.action_notes_to_editor, args)
			}
			else {
				showToast(R.string.error_open_file)
			}
		}
	}

	override fun onStart() {
		super.onStart()
		navController.addOnDestinationChangedListener(this)
		binding.navView.setNavigationItemSelectedListener(this)
		viewModel.notebooks.observe(this) { updateNavDrawer(it) }
		setDefaultCheckedItem()
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
			R.id.create_notebook -> {
				showCreateNotebookDialog(); true
			}
			else -> NavigationUI.onNavDestinationSelected(item, navController)
		}
	}

	override fun showCreateNotebookDialog() {
		binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN)
		val notebookDialog = NotebookDialogFragment.newInstance(NotebookUiState())
		notebookDialog.show(supportFragmentManager, NotebooksFragment.TAG)
	}

	suspend fun getNotebooks() = viewModel.getNotebooks()

	fun unlockDrawerLayout() {
		binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
	}

	private fun setupNavigation() {
		val appBarConfiguration = AppBarConfiguration(
			setOf(R.id.nav_notes, R.id.nav_notebooks),
			binding.drawerLayout
		)
		navController.graph = navController.navInflater.inflate(R.navigation.nav_graph).apply {
			setStartDestination(
				if (preferenceStorage.isOnboardingCompleted) R.id.nav_notes else R.id.nav_welcome
			)
		}
		binding.toolbar.setupWithNavController(navController, appBarConfiguration)
		binding.navView.setupWithNavController(navController)
	}

	private fun updateNavDrawer(notebooks: List<Notebook>) {
		val item = binding.navView.menu.getItem(1)
		if (item.subMenu.isNotEmpty()) item.subMenu.clear()

		for (notebook in notebooks) {
			item.subMenu.add(R.id.group_notebooks, 1, 1, notebook.entity.name).apply {
				setIcon(R.drawable.ic_notebook)
				isCheckable = true
				isChecked = this.title == binding.toolbar.title
				setOnMenuItemClickListener { filterNotesByNotebook(notebook.entity, item); true }
			}
		}
		item.subMenu.add(R.id.group_notebooks, R.id.create_notebook, 1000, getString(R.string.create_notebook))
			.setIcon(R.drawable.ic_add_item)
		binding.navView.menu.findItem(R.id.create_notebook).setActionView(R.layout.menu_edit_notebooks)
		navigateToNotebooks()
	}

	private fun filterNotesByNotebook(notebook: NotebookEntity, item: MenuItem) {
		binding.navView.setCheckedItem(item)
		binding.drawerLayout.closeDrawer(GravityCompat.START, true)
		navigateToNotes(notebook)
	}

	private fun navigateToNotebooks() {
		val view = binding.navView.menu.findItem(R.id.create_notebook).actionView
		view.findViewById<ImageView>(R.id.edit_notebooks).setOnClickListener {
			binding.drawerLayout.closeDrawer(GravityCompat.START, true)
			navController.navigate(R.id.nav_notebooks)
		}
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

	private fun setDefaultCheckedItem() {
		val allNotesItem = binding.navView.menu.getItem(0)
		if (allNotesItem.title == binding.toolbar.title) {
			binding.navView.setCheckedItem(allNotesItem)
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

	companion object {
		const val CHANNEL_ID = "NOTIES_CHANNEL"
	}
}
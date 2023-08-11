package io.github.xamdr.noties.ui

import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import io.github.xamdr.noties.R
import io.github.xamdr.noties.databinding.ActivityMainBinding
import io.github.xamdr.noties.domain.model.Tag
import io.github.xamdr.noties.ui.helpers.Constants
import io.github.xamdr.noties.ui.helpers.findNavController
import io.github.xamdr.noties.ui.helpers.launch
import io.github.xamdr.noties.ui.helpers.media.MediaHelper
import io.github.xamdr.noties.ui.helpers.onClick
import io.github.xamdr.noties.ui.helpers.setNightMode
import io.github.xamdr.noties.ui.helpers.showDialog
import io.github.xamdr.noties.ui.settings.PreferenceStorage
import io.github.xamdr.noties.ui.tags.TagDialogFragment
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener, NavigationView.OnNavigationItemSelectedListener {

	private val binding by lazy(LazyThreadSafetyMode.NONE) { ActivityMainBinding.inflate(layoutInflater) }
	private val navController by lazy(LazyThreadSafetyMode.NONE) { findNavController(R.id.nav_host_fragment) }
	private val viewModel by viewModels<MainViewModel>()
	private val photoPickerLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
		setWallpaper(uri)
	}
	@Inject lateinit var preferenceStorage: PreferenceStorage

	override fun onCreate(savedInstanceState: Bundle?) {
		installSplashScreen()
		super.onCreate(savedInstanceState)
		setNightMode()
		setContentView(binding.root)
		setSupportActionBar(binding.toolbar)
		setupNavigation()
	}

	override fun onStart() {
		super.onStart()
		navController.addOnDestinationChangedListener(this)
		binding.navView.setNavigationItemSelectedListener(this)
		binding.navView.getHeaderView(0).findViewById<ImageButton>(R.id.set_wallpaper).onClick {
			photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
		}
		binding.navView.getHeaderView(0).findViewById<ImageView>(R.id.wallpaper)
			.setImageURI(Uri.parse(preferenceStorage.wallpaper))
		viewModel.getTags().observe(this) { tags -> updateNavigationView(tags) }
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
	}

	override fun onNavigationItemSelected(item: MenuItem): Boolean {
		binding.drawerLayout.closeDrawer(GravityCompat.START, true)
		return when (item.itemId) {
			R.id.nav_settings -> {
				navController.navigate(R.id.action_notes_to_settings); true
			}
			R.id.nav_create_tag -> {
				showCreateTagDialog(); true
			}
			else -> NavigationUI.onNavDestinationSelected(item, navController)
		}
	}

	override fun onSupportNavigateUp() = navController.navigateUp() || super.onSupportNavigateUp()

	private fun setupNavigation() {
		val appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_all_notes))
		navController.graph = navController.navInflater.inflate(R.navigation.nav_graph).apply {
			setStartDestination(
				if (preferenceStorage.isOnboardingCompleted) R.id.nav_all_notes else R.id.nav_welcome
			)
		}
		setupActionBarWithNavController(navController, appBarConfiguration)
		binding.navView.setCheckedItem(R.id.nav_all_notes)
	}

	private fun setWallpaper(uri: Uri?) {
		launch {
			if (uri != null) {
				val newUri = MediaHelper.copyUri(this@MainActivity, uri)
				binding.navView.getHeaderView(0).findViewById<ImageView>(R.id.wallpaper).setImageURI(newUri)
				preferenceStorage.wallpaper = newUri.toString()
			}
		}
	}

	private fun showCreateTagDialog() {
		val tagDialog = TagDialogFragment.newInstance(Tag())
		showDialog(tagDialog, Constants.TAG_DIALOG)
	}

	private fun updateNavigationView(tags: List<Tag>) {
		val item = binding.navView.menu.getItem(3)
		item.subMenu?.let { subMenu ->
			subMenu.clear()
			for (tag in tags) {
				subMenu.add(R.id.group_tags, 1, 1, tag.name).apply {
					setIcon(R.drawable.ic_tag)
					isCheckable = true
					isChecked = this.title == binding.toolbar.title
					setOnMenuItemClickListener { filterNotesByTag(tag, this); true }
				}
			}
			subMenu.add(R.id.group_tags, R.id.nav_create_tag, 1000, R.string.create_tag)
				.setIcon(R.drawable.ic_create_tag)
		}
	}

	private fun filterNotesByTag(tag: Tag, menuItem: MenuItem) {
		binding.navView.setCheckedItem(menuItem)
		binding.drawerLayout.closeDrawer(GravityCompat.START, true)

	}
}
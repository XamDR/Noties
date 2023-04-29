package io.github.xamdr.noties.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
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
	}

	override fun onSupportNavigateUp() = navController.navigateUp() || super.onSupportNavigateUp()

	private fun setupNavigation() {
		val appBarConfiguration = AppBarConfiguration(emptySet())
		navController.graph = navController.navInflater.inflate(R.navigation.nav_graph).apply {
			setStartDestination(
				if (preferenceStorage.isOnboardingCompleted) R.id.nav_notes else R.id.nav_welcome
			)
		}
		setupActionBarWithNavController(navController, appBarConfiguration)
	}

	companion object {
		const val CHANNEL_ID = "NOTIES_CHANNEL"
	}
}
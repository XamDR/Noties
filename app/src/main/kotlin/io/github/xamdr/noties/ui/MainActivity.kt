package io.github.xamdr.noties.ui

import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import io.github.xamdr.noties.R
import io.github.xamdr.noties.databinding.ActivityMainBinding
import io.github.xamdr.noties.ui.helpers.findNavController
import io.github.xamdr.noties.ui.helpers.setNightMode
import io.github.xamdr.noties.ui.settings.PreferenceStorage
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

	@Inject lateinit var preferenceStorage: PreferenceStorage
	private val binding by lazy(LazyThreadSafetyMode.NONE) { ActivityMainBinding.inflate(layoutInflater) }
	private val navController by lazy(LazyThreadSafetyMode.NONE) { findNavController(R.id.nav_host_fragment) }

	override fun onCreate(savedInstanceState: Bundle?) {
		installSplashScreen()
		super.onCreate(savedInstanceState)
		setNightMode()
		setContentView(binding.root)
		setupNavigation()
	}

	private fun setupNavigation() {
		navController.graph = navController.navInflater.inflate(R.navigation.nav_graph).apply {
			setStartDestination(
				if (preferenceStorage.isOnboardingCompleted) R.id.nav_all_notes else R.id.nav_welcome
			)
		}
	}
}
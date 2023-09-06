package io.github.xamdr.noties.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import io.github.xamdr.noties.R
import io.github.xamdr.noties.ui.helpers.inflateTransition
import io.github.xamdr.noties.ui.theme.NotiesTheme

class SettingsFragment : Fragment() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enterTransition = inflateTransition(R.transition.slide_from_bottom)
	}

	override fun onCreateView(inflater: LayoutInflater,
	                          container: ViewGroup?,
	                          savedInstanceState: Bundle?): View {
		return ComposeView(requireContext()).apply {
			setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
			setContent { SettingsFragmentContent() }
		}
	}

	@Composable
	private fun SettingsFragmentContent() {
		NotiesTheme {
			SettingsScreen { findNavController().popBackStack() }
		}
	}
}
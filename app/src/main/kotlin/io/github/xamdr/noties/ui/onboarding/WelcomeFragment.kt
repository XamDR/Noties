package io.github.xamdr.noties.ui.onboarding

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import io.github.xamdr.noties.R
import io.github.xamdr.noties.ui.helpers.Constants
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.helpers.tryNavigate
import io.github.xamdr.noties.ui.settings.PreferenceStorage
import io.github.xamdr.noties.ui.theme.NotiesTheme
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class WelcomeFragment : Fragment() {

	@Inject lateinit var preferenceStorage: PreferenceStorage

	override fun onAttach(context: Context) {
		super.onAttach(context)
		createDirectories(context)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		return ComposeView(requireContext()).apply {
			setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
			setContent { WelcomeFragmentContent() }
		}
	}

	@Composable
	fun WelcomeFragmentContent() {
		NotiesTheme {
			val configuration = LocalConfiguration.current
			val padding = if (configuration.screenHeightDp >= 800) 32.dp else 16.dp
			val iconSize = if (configuration.screenHeightDp >= 800) 192.dp else 144.dp

			Column(
				modifier = Modifier
					.fillMaxSize()
					.padding(16.dp),
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.Center
			) {
				Text(
					text = stringResource(id = R.string.app_name),
					style = MaterialTheme.typography.titleLarge,
					fontWeight = FontWeight.Bold,
					modifier = Modifier.padding(padding),
				)
				Icon(
					imageVector = Icons.Outlined.Article,
					contentDescription = null,
					modifier = Modifier
						.size(iconSize)
						.padding(padding)
				)
				Text(
					text = stringResource(id = R.string.onboarding_message),
					style = MaterialTheme.typography.bodyLarge,
					textAlign = TextAlign.Justify,
					modifier = Modifier
						.fillMaxWidth()
						.padding(padding)
				)
				Button(
					onClick = { navigateToMainScreen() },
					modifier = Modifier.padding(padding)
				) {
					Text(
						stringResource(id = R.string.continue_button)
					)
				}
			}
		}
	}

	@DevicePreviews
	@Composable
	private fun WelcomePreview() = WelcomeFragmentContent()

	private fun navigateToMainScreen() {
		findNavController().tryNavigate(R.id.action_welcome_to_notes)
		preferenceStorage.isOnboardingCompleted = true
	}

	private fun createDirectories(context: Context) {
		val imagesDir = File(context.filesDir, Constants.DIRECTORY_IMAGES)
		val audiosDir = File(context.filesDir, Constants.DIRECTORY_VIDEOS)
		val videosDir = File(context.filesDir, Constants.DIRECTORY_AUDIOS)
		if (!imagesDir.exists()) imagesDir.mkdir()
		if (!audiosDir.exists()) audiosDir.mkdir()
		if (!videosDir.exists()) videosDir.mkdir()
	}
}
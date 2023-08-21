package io.github.xamdr.noties.ui.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialElevationScale
import dagger.hilt.android.AndroidEntryPoint
import io.github.xamdr.noties.R
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.ui.helpers.Constants
import io.github.xamdr.noties.ui.helpers.inflateTransition
import io.github.xamdr.noties.ui.helpers.tryNavigate
import io.github.xamdr.noties.ui.settings.PreferenceStorage
import io.github.xamdr.noties.ui.theme.NotiesTheme
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotesFragment : Fragment() {

	@Inject lateinit var preferenceStorage: PreferenceStorage
	private val viewModel by viewModels<NotesViewModel>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enterTransition = inflateTransition(R.transition.slide_from_bottom)
	}

	override fun onCreateView(inflater: LayoutInflater,
							  container: ViewGroup?,
							  savedInstanceState: Bundle?): View {
		return ComposeView(requireContext()).apply { 
			setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
			setContent { NotesFragmentContent() }
		}
	}

	@Composable
	private fun NotesFragmentContent() {
		NotiesTheme {
			val scope = rememberCoroutineScope()
			val drawerState = DrawerState(DrawerValue.Closed)
			NavigationDrawer(
				drawerState = drawerState,
				preferenceStorage = preferenceStorage,
			) {
				NotesScreen(
					query = "",
					onQueryChange = {},
					onSearch = {},
					active = false,
					onActiveChange = {},
					onLeadingIconClick = { scope.launch { drawerState.open() } },
					onTrailingIconClick = {},
					onFabClick = { navigateToEditor() },
					viewModel = viewModel
				) {

				}
			}
		}
	}

	private fun navigateToEditor(note: Note = Note()) {
		exitTransition = MaterialElevationScale(false).apply {
			duration = resources.getInteger(R.integer.motion_duration_large).toLong()
		}
		reenterTransition = MaterialElevationScale(true).apply {
			duration = resources.getInteger(R.integer.motion_duration_large).toLong()
		}
		val args = bundleOf(Constants.BUNDLE_NOTE_ID to note.id)
		findNavController().tryNavigate(R.id.action_notes_to_editor, args)
	}
}
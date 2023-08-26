package io.github.xamdr.noties.ui.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import io.github.xamdr.noties.domain.model.Tag
import io.github.xamdr.noties.ui.helpers.Constants
import io.github.xamdr.noties.ui.helpers.inflateTransition
import io.github.xamdr.noties.ui.helpers.tryNavigate
import io.github.xamdr.noties.ui.settings.PreferenceStorage
import io.github.xamdr.noties.ui.tags.TagDialog
import io.github.xamdr.noties.ui.tags.TagDialogViewModel
import io.github.xamdr.noties.ui.theme.NotiesTheme
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class NotesFragment : Fragment() {

	@Inject lateinit var preferenceStorage: PreferenceStorage
	private val viewModel by viewModels<NotesViewModel>()
	private val tagDialogViewModel by viewModels<TagDialogViewModel>()

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
			var openDialog by rememberSaveable { mutableStateOf(false) }

			NavigationDrawer(
				drawerState = drawerState,
				viewModel = viewModel,
				preferenceStorage = preferenceStorage,
				onCreateTag = { openDialog = true }
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
				if (openDialog) {
					TagDialog(
						tag = Tag(),
						viewModel = tagDialogViewModel,
						onCancel = {
							tagDialogViewModel.clearNameState()
							openDialog = false
						},
						onSave = { tagName ->
							scope.launch {
								createTag(tagName)
								openDialog = false
							}
						}
					)
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

	private suspend fun createTag(tagName: String) {
		val newTag = Tag(name = tagName)
		tagDialogViewModel.createTag(newTag)
		Timber.d("New tag created: $newTag")
	}
}
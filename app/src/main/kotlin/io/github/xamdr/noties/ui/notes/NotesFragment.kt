package io.github.xamdr.noties.ui.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
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
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialElevationScale
import dagger.hilt.android.AndroidEntryPoint
import io.github.xamdr.noties.R
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.domain.model.Tag
import io.github.xamdr.noties.ui.editor.NoteAction
import io.github.xamdr.noties.ui.helpers.Constants
import io.github.xamdr.noties.ui.helpers.getNavigationResult
import io.github.xamdr.noties.ui.helpers.inflateTransition
import io.github.xamdr.noties.ui.helpers.tryNavigate
import io.github.xamdr.noties.ui.settings.PreferenceStorage
import io.github.xamdr.noties.ui.tags.TagDialog
import io.github.xamdr.noties.ui.theme.NotiesTheme
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotesFragment : Fragment() {

	@Inject lateinit var preferenceStorage: PreferenceStorage

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
	fun NotesFragmentContent() {
		NotiesTheme {
			val scope = rememberCoroutineScope()
			val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
			var openTagDialog by rememberSaveable { mutableStateOf(value = false) }
			val noteAction = getNavigationResult<NoteAction>(Constants.BUNDLE_ACTION) ?: NoteAction.NoAction
			var screen by rememberSaveable { mutableStateOf(value = Screen()) }
			var openDeleteTagDialog by rememberSaveable { mutableStateOf(value = false) }
			var isNewTag by rememberSaveable { mutableStateOf(value = true) }

			fun onSave(tag: Tag) {
				openTagDialog = false
				if (tag.id != 0) {
					screen = screen.copy(title = tag.name)
				}
			}

			BackHandler(enabled = screen.type != ScreenType.Main) {
				screen = Screen()
			}

			NavigationDrawer(
				drawerState = drawerState,
				screenType = screen.type,
				preferenceStorage = preferenceStorage,
				onItemClick = { item ->
					scope.launch {
						when (item.id) {
							R.id.create_tag -> {
								openTagDialog = true
								isNewTag = true
							}
							else -> {
								drawerState.close()
								if (item.id == R.id.settings) {
									findNavController().tryNavigate(R.id.action_notes_to_settings)
								}
								else {
									screen = onItemClick(item)
								}
							}
						}
					}
				},
				content = {
					NotesScreen(
						screen = screen,
						query = "",
						onQueryChange = {},
						onSearch = {},
						active = false,
						onActiveChange = {},
						onLeadingIconClick = { scope.launch { drawerState.open() } },
						onTrailingIconClick = {},
						onNavigationIconClick = { scope.launch { drawerState.open() } },
						onFabClick = { navigateToEditor(tagName = screen.title) },
						onItemClick = ::navigateToEditor,
						onRenameTag = {
							openTagDialog = true
							isNewTag = false
						},
						onDeleteTag = { openDeleteTagDialog = true },
						noteAction = noteAction,
						searchContent = {

						}
					)
					if (openTagDialog) {
						TagDialog(
							tag = if (isNewTag) Tag() else Tag(id = screen.id, name = screen.title),
							onCancel = { openTagDialog = false },
							onSave = ::onSave
						)
					}
					if (openDeleteTagDialog) {
						DeleteTagDialog(
							tag = Tag(id = screen.id, name = screen.title),
							onDismiss = { openDeleteTagDialog = false },
							onDeleteTag = {
								openDeleteTagDialog = false
								screen = Screen()
							}
						)
					}
				}
			)
		}
	}

	private fun navigateToEditor(note: Note = Note(), tagName: String = String.Empty) {
		exitTransition = MaterialElevationScale(false).apply {
			duration = resources.getInteger(R.integer.motion_duration_large).toLong()
		}
		reenterTransition = MaterialElevationScale(true).apply {
			duration = resources.getInteger(R.integer.motion_duration_large).toLong()
		}
		val args = bundleOf(
			Constants.BUNDLE_NOTE_ID to note.id,
			Constants.BUNDLE_TAG to tagName
		)
		findNavController().tryNavigate(R.id.action_notes_to_editor, args)
	}

	private fun onItemClick(item: DrawerItem): Screen {
		return when (item) {
			is DrawerItem.DefaultItem -> {
				when (item.id) {
					R.id.all_notes -> Screen()
					R.id.reminders -> Screen(type = ScreenType.Reminder, title = getString(item.label))
					R.id.protected_notes -> Screen(type = ScreenType.Protected, title = getString(item.label))
					R.id.archived_notes -> Screen(type = ScreenType.Archived, title = getString(item.label))
					R.id.recycle_bin -> Screen(type = ScreenType.Trash, title = getString(item.label))
					else -> throw IllegalArgumentException("Invalid ${item.id}.")
				}
			}
			is DrawerItem.TagItem -> Screen(id = item.id, type = ScreenType.Tag, title = item.label)
			else -> throw IllegalArgumentException("$item shouldn't be clickable.")
		}
	}
}
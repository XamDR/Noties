package io.github.xamdr.noties.ui.editor

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import io.github.xamdr.noties.R
import io.github.xamdr.noties.ui.helpers.Constants
import io.github.xamdr.noties.ui.helpers.setNavigationResult
import io.github.xamdr.noties.ui.theme.NotiesTheme
import timber.log.Timber

@AndroidEntryPoint
class EditorFragment : Fragment() {

	private val noteId by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getLong(Constants.BUNDLE_NOTE_ID, 0L)
	}
	private val pickeMediaLauncher = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
		addMediaItems(uris)
	}

	override fun onCreateView(inflater: LayoutInflater,
							  container: ViewGroup?,
							  savedInstanceState: Bundle?): View {
		return ComposeView(requireContext()).apply {
			setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
			setContent { EditorFragmentContent() }
		}
	}

	@OptIn(ExperimentalMaterial3Api::class)
	@Composable
	private fun EditorFragmentContent() {
		NotiesTheme {
			var openMenu by rememberSaveable { mutableStateOf(false) }

			EditorScreen(
				onNavigationIconClick = {},
				noteId = noteId,
				onNoteAction = ::onNoteAction,
				onAddAttachmentIconClick = { openMenu = true },
				onPickColorIconClick = {}
			)
			if (openMenu) {
				EditorMenuBottomSheet(
					sheetState = SheetState(skipPartiallyExpanded = true)
				) { item ->
					openMenu = false
					when (item.id) {
						R.id.attach_media -> pickeMediaLauncher.launch(arrayOf("image/*", "video/*"))
					}
				}
			}
		}
	}

	private fun onNoteAction(action: NoteAction) {
		setNavigationResult(Constants.BUNDLE_ACTION, action)
		findNavController().popBackStack()
	}

	private fun addMediaItems(uris: List<Uri>) {
		if (uris.isEmpty()) return
		Timber.d("Uris: ${uris.size}")
	}
}
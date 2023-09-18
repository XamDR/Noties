package io.github.xamdr.noties.ui.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import io.github.xamdr.noties.ui.helpers.Constants
import io.github.xamdr.noties.ui.helpers.onBackPressed
import io.github.xamdr.noties.ui.helpers.setNavigationResult
import io.github.xamdr.noties.ui.theme.NotiesTheme

@AndroidEntryPoint
class EditorFragment : Fragment() {

	private val noteId by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getLong(Constants.BUNDLE_NOTE_ID, 0L)
	}

	override fun onCreateView(inflater: LayoutInflater,
							  container: ViewGroup?,
							  savedInstanceState: Bundle?): View {
		return ComposeView(requireContext()).apply {
			setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
			setContent { EditorFragmentContent() }
		}
	}

	@Composable
	private fun EditorFragmentContent() {
		NotiesTheme {
			EditorScreen(
				onNavigationIconClick = ::onBackPressed,
				noteId = noteId,
				onNoteAction = ::onNoteAction
			)
		}
	}

	private fun onNoteAction(action: NoteAction) {
		setNavigationResult(Constants.BUNDLE_ACTION, action)
		findNavController().popBackStack()
	}
}
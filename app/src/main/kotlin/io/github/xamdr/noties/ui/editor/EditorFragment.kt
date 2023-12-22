package io.github.xamdr.noties.ui.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint
import io.github.xamdr.noties.R
import io.github.xamdr.noties.ui.helpers.Constants
import io.github.xamdr.noties.ui.helpers.getNavigationResult
import io.github.xamdr.noties.ui.helpers.onBackPressed
import io.github.xamdr.noties.ui.helpers.setNavigationResult
import io.github.xamdr.noties.ui.helpers.tryNavigate
import io.github.xamdr.noties.ui.theme.NotiesTheme

@AndroidEntryPoint
class EditorFragment : Fragment() {

	private val noteId by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getLong(Constants.BUNDLE_NOTE_ID, 0L)
	}
	private val tagName by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getString(Constants.BUNDLE_TAG, String.Empty)
	}
	private val text by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getString(Constants.BUNDLE_NOTE_TEXT)
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
			val selectedTags = getNavigationResult<List<String>>(Constants.BUNDLE_SELECTED_TAGS)
			val allTags = if (tagName.isEmpty()) selectedTags else listOf(tagName) + selectedTags.orEmpty()
			val defaultColor = MaterialTheme.colorScheme.surface
			EditorScreen(
				noteId = noteId,
				text = text,
				selectedTags = allTags,
				onNavigationIconClick = ::onBackPressed,
				onNavigatoToTags = ::navigateToTags,
				onNoteAction = ::onNoteAction,
				onEditorColorChanged = { color ->
					requireActivity().window.statusBarColor = color?.toArgb() ?: defaultColor.toArgb()
				}
			)
		}
	}

	private fun onNoteAction(action: NoteAction, noteId: Long) {
		setNavigationResult(Constants.BUNDLE_NOTE_ID, noteId)
		setNavigationResult(Constants.BUNDLE_ACTION, action)
		findNavController().popBackStack()
	}

	private fun navigateToTags(tags: List<String>) {
		exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
			duration = 300
		}
		reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
			duration = 300
		}
		val args = bundleOf(Constants.BUNDLE_TAGS to ArrayList(tags))
		findNavController().tryNavigate(R.id.action_editor_to_tags, args)
	}
}
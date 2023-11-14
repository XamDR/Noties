package io.github.xamdr.noties.ui.tags

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint
import io.github.xamdr.noties.R
import io.github.xamdr.noties.ui.helpers.Constants
import io.github.xamdr.noties.ui.helpers.setNavigationResult
import io.github.xamdr.noties.ui.theme.NotiesTheme

@AndroidEntryPoint
class TagsFragment : Fragment() {

	private val tagsFromNote by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getStringArrayList(Constants.BUNDLE_TAGS) ?: emptyList()
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
			duration = resources.getInteger(R.integer.motion_duration_large).toLong()
		}
		returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
			duration = resources.getInteger(R.integer.motion_duration_large).toLong()
		}
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		return ComposeView(requireContext()).apply {
			setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
			setContent { TagsFragmentContent() }
		}
	}

	@Composable
	private fun TagsFragmentContent() {
		NotiesTheme {
			TagsScreen(
				tagsFromNote = tagsFromNote,
				onNavigationIconClick = { selectedTags ->
					setNavigationResult(Constants.BUNDLE_SELECTED_TAGS, ArrayList(selectedTags))
					findNavController().popBackStack()
				}
			)
		}
	}
}
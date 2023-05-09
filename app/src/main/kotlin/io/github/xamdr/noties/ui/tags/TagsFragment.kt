package io.github.xamdr.noties.ui.tags

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint
import io.github.xamdr.noties.R
import io.github.xamdr.noties.databinding.FragmentTagsBinding
import io.github.xamdr.noties.domain.model.Tag
import io.github.xamdr.noties.ui.helpers.Constants
import io.github.xamdr.noties.ui.helpers.addMenuProvider
import io.github.xamdr.noties.ui.helpers.launch
import io.github.xamdr.noties.ui.helpers.showDialog
import timber.log.Timber

@AndroidEntryPoint
class TagsFragment : Fragment(), TagToolbarItemListener, TagPopupMenuItemListener {

	private var _binding: FragmentTagsBinding? = null
	private val binding get() = _binding!!
	private val viewModel by viewModels<TagsViewModel>()
	private val tagAdapter = TagAdapter(this)
	private val menuProvider = TagsMenuProvider(this)

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
		_binding = FragmentTagsBinding.inflate(inflater, container, false)
		addMenuProvider(menuProvider, viewLifecycleOwner)
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setupRecyclerView()
		submitTags()
	}

	override fun showCreateTagDialog() {
		val tagDialog = TagDialogFragment.newInstance(Tag())
		showDialog(tagDialog, Constants.TAG_DIALOG)
	}

	override fun showCreateTagDialog(tag: Tag) {
		val tagDialog = TagDialogFragment.newInstance(tag)
		showDialog(tagDialog, Constants.TAG_DIALOG)
	}

	override fun deleteTag(tag: Tag) {
		launch {
			viewModel.deleteTags(listOf(tag))
			Timber.d("Tag deleted: $tag")
		}
	}

	private fun setupRecyclerView() {
		binding.recyclerView.setEmptyView(binding.emptyView)
		binding.recyclerView.adapter = tagAdapter
		postponeEnterTransition()
	}

	private fun submitTags() {
		viewModel.getTags().observe(viewLifecycleOwner) { tags ->
			tagAdapter.submitList(tags)
			binding.root.doOnPreDraw { startPostponedEnterTransition() }
		}
	}
}
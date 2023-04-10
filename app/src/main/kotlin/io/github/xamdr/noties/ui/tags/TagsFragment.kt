package io.github.xamdr.noties.ui.tags

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import io.github.xamdr.noties.databinding.FragmentTagsBinding
import io.github.xamdr.noties.domain.model.Tag
import io.github.xamdr.noties.ui.helpers.addMenuProvider
import io.github.xamdr.noties.ui.helpers.showDialog
import timber.log.Timber

@AndroidEntryPoint
class TagsFragment : Fragment(), TagToolbarItemListener {

	private var _binding: FragmentTagsBinding? = null
	private val binding get() = _binding!!
	private val viewModel by viewModels<TagsViewModel>()
	private val menuProvider = TagsMenuProvider(this)

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
		Timber.d("ViewModel", viewModel)
	}

	override fun showCreateTagDialog() {
		val tagDialog = TagDialogFragment.newInstance(Tag())
		showDialog(tagDialog, "TAG_DIALOG")
	}
}
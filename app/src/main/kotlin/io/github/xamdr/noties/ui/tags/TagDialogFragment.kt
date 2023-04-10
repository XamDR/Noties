package io.github.xamdr.noties.ui.tags

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.xamdr.noties.R
import io.github.xamdr.noties.databinding.DialogFragmentTagBinding
import io.github.xamdr.noties.domain.model.Tag
import io.github.xamdr.noties.ui.helpers.getSerializableCompat

class TagDialogFragment : DialogFragment() {

	private var _binding: DialogFragmentTagBinding? = null
	private val binding get() = _binding!!
	private val viewModel by viewModels<TagsViewModel>({ requireParentFragment() })
	private val tag by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getSerializableCompat(BUNDLE_TAG, Tag::class.java)
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		_binding = DialogFragmentTagBinding.inflate(layoutInflater)
		return MaterialAlertDialogBuilder(requireContext())
			.setTitle(R.string.new_tag)
			.setView(binding.root)
			.setNegativeButton(R.string.cancel_button) { _, _ -> dismiss() }
			.setPositiveButton(R.string.save_button, null)
			.create().apply {
				setOnShowListener {
					getButton(AlertDialog.BUTTON_POSITIVE).apply {
						setOnClickListener { createOrUpdateTag(this@TagDialogFragment.tag) }
					}
				}
			}
	}

	private fun createOrUpdateTag(tag: Tag) {
		viewModel.createTag(tag)
	}

	companion object {
		const val BUNDLE_TAG = "BUNDLE_TAG"

		fun newInstance(tag: Tag) = TagDialogFragment().apply {
			arguments = bundleOf(BUNDLE_TAG to tag)
		}
	}
}
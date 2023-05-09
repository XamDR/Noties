package io.github.xamdr.noties.ui.tags

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.xamdr.noties.R
import io.github.xamdr.noties.databinding.DialogFragmentTagBinding
import io.github.xamdr.noties.domain.model.Tag
import io.github.xamdr.noties.ui.helpers.*
import timber.log.Timber

class TagDialogFragment : DialogFragment() {

	private var _binding: DialogFragmentTagBinding? = null
	private val binding get() = _binding!!
	private val viewModel by viewModels<TagsViewModel>({ requireParentFragment() })
	private val tag by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getParcelableCompat(Constants.BUNDLE_TAG, Tag::class.java)
	}
	private val textWatcher = TagNameTextWatcher()

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		_binding = DialogFragmentTagBinding.inflate(layoutInflater)
		binding.tagName.setText(tag.name)
		return MaterialAlertDialogBuilder(requireContext())
			.setTitle(if (tag.id == 0) R.string.new_tag else R.string.edit_tag_name)
			.setView(binding.root)
			.setNegativeButton(R.string.cancel_button) { _, _ -> cancel() }
			.setPositiveButton(R.string.save_button, null)
			.create().apply {
				setOnShowListener {
					getButton(AlertDialog.BUTTON_POSITIVE).apply {
						setOnClickListener { createOrUpdateTag(this@TagDialogFragment.tag) }
					}
				}
			}
	}

	override fun onStart() {
		super.onStart()
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			requireDialog().window?.let {
				binding.tagName.requestFocus()
				WindowCompat.getInsetsController(it, binding.root).show(WindowInsetsCompat.Type.ime())
			}
		}
		else {
			binding.root.post { binding.tagName.showSoftKeyboard() }
		}
	}

	override fun onResume() {
		super.onResume()
		binding.tagName.addTextChangedListener(textWatcher)
		viewModel.nameState.observe(this) { tagNameState ->
			when (tagNameState) {
				TagNameState.EmptyOrUpdatingName -> {
					binding.tagName.selectAll()
					getPositiveButton().isEnabled = false
				}
				TagNameState.EditingName -> {
					if (binding.inputLayout.error != null) binding.inputLayout.error = null
					if (!getPositiveButton().isEnabled) getPositiveButton().isEnabled = true
				}
				TagNameState.ErrorDuplicateName -> {
					getPositiveButton().isEnabled = false
					binding.inputLayout.error = getString(R.string.error_message_tag_duplicate)
				}
			}
		}
	}

	override fun onPause() {
		super.onPause()
		binding.tagName.removeTextChangedListener(textWatcher)
	}

	private fun createOrUpdateTag(tag: Tag) {
		launch {
			if (tag.id == 0) {
				val newTag = Tag(name = binding.tagName.textAsString())
				viewModel.createTag(newTag)
				Timber.d("New tag created: $newTag")
			}
			else {
				val updatedTag = tag.copy(name = binding.tagName.textAsString())
				viewModel.updateTag(updatedTag, tag)
				Timber.d("Tag updated: $updatedTag")
			}
			dismiss()
		}
	}

	private fun cancel() {
		viewModel.clearNameState()
		dismiss()
	}

	companion object {
		fun newInstance(tag: Tag) = TagDialogFragment().apply {
			arguments = bundleOf(Constants.BUNDLE_TAG to tag)
		}
	}

	private inner class TagNameTextWatcher : TextWatcher {

		override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

		override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

		override fun afterTextChanged(s: Editable?) = viewModel.onTagNameChanged(s.toString())
	}
}
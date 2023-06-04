package io.github.xamdr.noties.ui.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.xamdr.noties.databinding.DialogFragmentEditorMenuBinding
import io.github.xamdr.noties.ui.helpers.onClick

class EditorMenuFragment : BottomSheetDialogFragment() {

	private var _binding: DialogFragmentEditorMenuBinding? = null
	private val binding get() = _binding!!
	private var listener: EditorMenuListener? = null

	override fun onCreateView(inflater: LayoutInflater,
							  container: ViewGroup?,
							  savedInstanceState: Bundle?): View {
		_binding = DialogFragmentEditorMenuBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.attachMedia.onClick { onAttachMediaFiles() }
		binding.takePicture.onClick { onTakePicture() }
		binding.takeVideo.onClick { onTakeVideo() }
	}

	fun setEditorMenuListener(listener: EditorMenuListener) {
		this.listener = listener
	}

	private fun onAttachMediaFiles() {
		listener?.onAttachMediaFiles()
		dismiss()
	}

	private fun onTakePicture() {
		listener?.onTakePicture()
		dismiss()
	}

	private fun onTakeVideo() {
		listener?.onTakeVideo()
		dismiss()
	}
}
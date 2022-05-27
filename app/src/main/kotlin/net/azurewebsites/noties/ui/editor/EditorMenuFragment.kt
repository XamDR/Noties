package net.azurewebsites.noties.ui.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import net.azurewebsites.noties.databinding.DialogFragmentEditorMenuBinding

class EditorMenuFragment : BottomSheetDialogFragment() {

	private var _binding: DialogFragmentEditorMenuBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(inflater: LayoutInflater,
							  container: ViewGroup?,
							  savedInstanceState: Bundle?): View {
		_binding = DialogFragmentEditorMenuBinding.inflate(inflater, container, false).apply {
			fragment = this@EditorMenuFragment
		}
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	fun invokeCallback() {
		onActivityResultCallback()
		dismiss()
	}

	fun setOnActivityResultListener(callback: () -> Unit) {
		onActivityResultCallback = callback
	}

	private var onActivityResultCallback: () -> Unit = {}
}
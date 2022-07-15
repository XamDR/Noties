package net.azurewebsites.noties.ui.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import net.azurewebsites.noties.databinding.DialogFragmentEditorMenuBinding

class EditorMenuFragment : BottomSheetDialogFragment() {

	private var _binding: DialogFragmentEditorMenuBinding? = null
	private val binding get() = _binding!!
	private val viewModel by viewModels<EditorViewModel>({ requireParentFragment() })

	override fun onCreateView(inflater: LayoutInflater,
							  container: ViewGroup?,
							  savedInstanceState: Bundle?): View {
		_binding = DialogFragmentEditorMenuBinding.inflate(inflater, container, false).apply {
			fragment = this@EditorMenuFragment
			vm = viewModel
		}
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	fun invokeActivityResultCallback() {
		onActivityResultCallback()
		dismiss()
	}

	fun invokeTakePictureCallback() {
		onTakePictureCallback()
		dismiss()
	}

	fun invokeMakeTodoListCallback() {
		onMakeTodoListCallback()
		dismiss()
	}

	fun setOnActivityResultListener(callback: () -> Unit) {
		onActivityResultCallback = callback
	}

	fun setOnTakePictureListener(callback: () -> Unit) {
		onTakePictureCallback = callback
	}

	fun setOnMakeTodoListListener(callback: () -> Unit) {
		onMakeTodoListCallback = callback
	}

	private var onActivityResultCallback: () -> Unit = {}

	private var onTakePictureCallback: () -> Unit = {}

	private var onMakeTodoListCallback: () -> Unit = {}
}
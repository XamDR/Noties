package net.azurewebsites.noties.ui.urls

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import net.azurewebsites.noties.databinding.DialogFragmentUrlsBinding

class UrlsDialogFragment : BottomSheetDialogFragment(), OnCloseDialogListener {

	private var _binding: DialogFragmentUrlsBinding? = null
	private val binding get() = _binding!!
	private val urls by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getStringArray(KEY)?.toList() ?: emptyList()
	}

	override fun onCreateView(inflater: LayoutInflater,
							  container: ViewGroup?,
							  savedInstanceState: Bundle?): View {
		_binding = DialogFragmentUrlsBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.list.adapter = UrlAdapter(urls, this)
	}

	companion object {
		private const val KEY = "urls"

		fun newInstance(urls: Array<String>) = UrlsDialogFragment().apply {
			arguments = bundleOf(KEY to urls)
		}
	}
}
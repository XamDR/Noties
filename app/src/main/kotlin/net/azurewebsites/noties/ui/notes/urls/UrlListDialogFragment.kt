package net.azurewebsites.noties.ui.notes.urls

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import net.azurewebsites.noties.databinding.DialogFragmentUrlsBinding
import net.azurewebsites.noties.ui.helpers.printDebug

class UrlListDialogFragment : BottomSheetDialogFragment() {

	private var _binding: DialogFragmentUrlsBinding? = null
	private val binding get() = _binding!!
	private lateinit var urls: List<String>

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		urls = requireArguments().getStringArray("urls")?.toList() ?: emptyList()
		printDebug("URLS", urls)
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
		binding.list.adapter = UrlAdapter(urls).apply {
			setOnUrlOpenedListener { this@UrlListDialogFragment.dismiss() }
		}
	}

	companion object {
		fun newInstance(urls: Array<String>) = UrlListDialogFragment().apply {
			arguments = bundleOf("urls" to urls)
		}
	}
}
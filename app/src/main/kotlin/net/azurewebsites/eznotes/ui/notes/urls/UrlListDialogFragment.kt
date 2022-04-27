package net.azurewebsites.eznotes.ui.notes.urls

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import net.azurewebsites.eznotes.databinding.FragmentUrlBottomSheetDialogBinding
import net.azurewebsites.eznotes.ui.helpers.printDebug

class UrlListDialogFragment : BottomSheetDialogFragment() {

	private var _binding: FragmentUrlBottomSheetDialogBinding? = null
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
		_binding = FragmentUrlBottomSheetDialogBinding.inflate(inflater, container, false)
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
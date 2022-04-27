package net.azurewebsites.eznotes.ui.onboarding

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionInflater
import net.azurewebsites.eznotes.R
import net.azurewebsites.eznotes.databinding.FragmentWelcomeBinding
import net.azurewebsites.eznotes.ui.settings.UserPreferences
import java.io.File

class WelcomeFragment : Fragment() {

	private var _binding: FragmentWelcomeBinding? = null
	private val binding get() = _binding!!
	private lateinit var userPreferences: UserPreferences

	override fun onAttach(context: Context) {
		super.onAttach(context)
		userPreferences = UserPreferences(context)
		createDirectories(context)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		exitTransition = TransitionInflater.from(requireContext()).inflateTransition(R.transition.slide_out_top)
	}

	override fun onCreateView(inflater: LayoutInflater,
							  container: ViewGroup?,
							  savedInstanceState: Bundle?): View {
		_binding = FragmentWelcomeBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		navigateToMainScreen()
	}

	private fun createDirectories(context: Context) {
		val imagesDir = File(context.filesDir, "images")
		val audiosDir = File(context.filesDir, "audios")
		val videosDir = File(context.filesDir, "videos")
		if (!imagesDir.exists()) imagesDir.mkdir()
		if (!audiosDir.exists()) audiosDir.mkdir()
		if (!videosDir.exists()) videosDir.mkdir()
	}

	private fun navigateToMainScreen() {
		binding.nextButton.setOnClickListener {
			findNavController().navigate(R.id.action_welcome_to_notes)
			userPreferences.isFirstRun = false
		}
	}
}
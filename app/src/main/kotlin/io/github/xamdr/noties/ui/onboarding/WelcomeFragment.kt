package io.github.xamdr.noties.ui.onboarding

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import io.github.xamdr.noties.R
import io.github.xamdr.noties.databinding.FragmentWelcomeBinding
import io.github.xamdr.noties.ui.helpers.Constants
import io.github.xamdr.noties.ui.helpers.tryNavigate
import io.github.xamdr.noties.ui.settings.PreferenceStorage
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class WelcomeFragment : Fragment() {

	private var _binding: FragmentWelcomeBinding? = null
	private val binding get() = _binding!!
	@Inject lateinit var preferenceStorage: PreferenceStorage

	override fun onAttach(context: Context) {
		super.onAttach(context)
		createDirectories(context)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
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
		binding.nextButton.setOnClickListener { navigateToMainScreen() }
	}

	private fun navigateToMainScreen() {
		findNavController().tryNavigate(R.id.action_welcome_to_notes)
		preferenceStorage.isOnboardingCompleted = true
	}

	private fun createDirectories(context: Context) {
		val imagesDir = File(context.filesDir, Constants.DIRECTORY_IMAGES)
		val audiosDir = File(context.filesDir, Constants.DIRECTORY_VIDEOS)
		val videosDir = File(context.filesDir, Constants.DIRECTORY_AUDIOS)
		if (!imagesDir.exists()) imagesDir.mkdir()
		if (!audiosDir.exists()) audiosDir.mkdir()
		if (!videosDir.exists()) videosDir.mkdir()
	}
}
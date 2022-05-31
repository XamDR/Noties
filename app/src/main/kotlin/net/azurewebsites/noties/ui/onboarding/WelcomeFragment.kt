package net.azurewebsites.noties.ui.onboarding

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import net.azurewebsites.noties.R
import net.azurewebsites.noties.databinding.FragmentWelcomeBinding
import net.azurewebsites.noties.ui.helpers.tryNavigate
import net.azurewebsites.noties.ui.settings.PreferenceStorage
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
		_binding = FragmentWelcomeBinding.inflate(inflater, container, false).apply {
			fragment = this@WelcomeFragment
		}
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	fun navigateToMainScreen() {
		findNavController().tryNavigate(R.id.action_welcome_to_folders)
		preferenceStorage.isOnboardingCompleted = true
	}

	private fun createDirectories(context: Context) {
		val imagesDir = File(context.filesDir, "images")
		val audiosDir = File(context.filesDir, "audios")
		val videosDir = File(context.filesDir, "videos")
		if (!imagesDir.exists()) imagesDir.mkdir()
		if (!audiosDir.exists()) audiosDir.mkdir()
		if (!videosDir.exists()) videosDir.mkdir()
	}
}
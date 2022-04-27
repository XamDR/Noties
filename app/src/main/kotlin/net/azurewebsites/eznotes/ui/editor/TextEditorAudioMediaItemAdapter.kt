package net.azurewebsites.eznotes.ui.editor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import net.azurewebsites.eznotes.core.MediaItemEntity
import net.azurewebsites.eznotes.databinding.FragmentTextEditorAudiosBinding
import net.azurewebsites.eznotes.ui.audio.AudioAdapter

class TextEditorAudioMediaItemAdapter(
	private val audioAdapter: AudioAdapter) : RecyclerView.Adapter<TextEditorAudioMediaItemAdapter.TextEditorAudiosViewHolder>() {

	inner class TextEditorAudiosViewHolder(
		binding: FragmentTextEditorAudiosBinding) : RecyclerView.ViewHolder(binding.root) {

		init {
			binding.audios.adapter = audioAdapter
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextEditorAudiosViewHolder {
		val binding = FragmentTextEditorAudiosBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		return TextEditorAudiosViewHolder(binding)
	}

	override fun onBindViewHolder(holder: TextEditorAudiosViewHolder, position: Int) { }

	override fun getItemCount() = 1

	fun submitList(list: List<MediaItemEntity>) = audioAdapter.submitList(list)
}
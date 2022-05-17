package net.azurewebsites.noties.ui.editor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import net.azurewebsites.noties.R
import net.azurewebsites.noties.databinding.FragmentEditorContentBinding

class EditorContentAdapter(private val viewModel: EditorViewModel) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

	inner class EditorViewHolder(private val binding: FragmentEditorContentBinding) : RecyclerView.ViewHolder(binding.root) {

		fun bind(viewModel: EditorViewModel) {
			binding.vm = viewModel
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when(viewType) {
		EDITOR -> {
			val binding = FragmentEditorContentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
			EditorViewHolder(binding)
		}
		else -> throw Exception("Unknown view type.")
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		if (holder is EditorViewHolder) {
			holder.bind(viewModel)
		}
	}

	override fun getItemCount() = 1

	override fun getItemViewType(position: Int) = EDITOR

	companion object {
		private const val EDITOR = R.layout.fragment_editor_content
	}
}
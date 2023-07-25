package io.github.xamdr.noties.ui.editor.tags

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.github.xamdr.noties.databinding.ItemEditorFooterBinding

class EditorFooterAdapter(private val tagAdapter: ChipTagAdapter) : RecyclerView.Adapter<EditorFooterAdapter.NoteFooterViewHolder>() {

	class NoteFooterViewHolder(private val binding: ItemEditorFooterBinding) : RecyclerView.ViewHolder(binding.root) {

		fun setAdapter(tagAdapter: ChipTagAdapter) {
			binding.recyclerViewFooter.adapter = tagAdapter
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteFooterViewHolder {
		val binding = ItemEditorFooterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		return NoteFooterViewHolder(binding).apply { setAdapter(tagAdapter) }
	}

	override fun getItemCount(): Int = 1

	override fun onBindViewHolder(holder: NoteFooterViewHolder, position: Int) {}
}
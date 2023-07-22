package io.github.xamdr.noties.ui.editor.tags

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.github.xamdr.noties.databinding.ItemNoteFooterBinding

class NoteFooterAdapter(private val tagAdapter: ChipTagAdapter) : RecyclerView.Adapter<NoteFooterAdapter.NoteFooterViewHolder>() {

	class NoteFooterViewHolder(private val binding: ItemNoteFooterBinding) : RecyclerView.ViewHolder(binding.root) {

		fun setAdapter(tagAdapter: ChipTagAdapter) {
			binding.recyclerViewFooter.adapter = tagAdapter
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteFooterViewHolder {
		val binding = ItemNoteFooterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		return NoteFooterViewHolder(binding).apply { setAdapter(tagAdapter) }
	}

	override fun getItemCount(): Int = 1

	override fun onBindViewHolder(holder: NoteFooterViewHolder, position: Int) {}
}
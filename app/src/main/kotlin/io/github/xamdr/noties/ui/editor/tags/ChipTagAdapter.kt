package io.github.xamdr.noties.ui.editor.tags

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.xamdr.noties.databinding.ItemChipTagBinding
import io.github.xamdr.noties.domain.model.Tag

class ChipTagAdapter : ListAdapter<Tag, ChipTagAdapter.ChipTagViewHolder>(TagCallback) {

	inner class ChipTagViewHolder(private val binding: ItemChipTagBinding) : RecyclerView.ViewHolder(binding.root) {

		fun bind(tag: Tag) {
			binding.chip.text = tag.name
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChipTagViewHolder {
		val binding = ItemChipTagBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		return ChipTagViewHolder(binding)
	}

	override fun onBindViewHolder(holder: ChipTagViewHolder, position: Int) {
		val tag = getItem(position)
		holder.bind(tag)
	}

	object TagCallback : DiffUtil.ItemCallback<Tag>() {

		override fun areItemsTheSame(oldItem: Tag, newItem: Tag) = oldItem.id == newItem.id

		override fun areContentsTheSame(oldItem: Tag, newItem: Tag) = oldItem == newItem
	}
}
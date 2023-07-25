package io.github.xamdr.noties.ui.editor.tags

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.xamdr.noties.R
import io.github.xamdr.noties.databinding.ItemChipTagBinding
import io.github.xamdr.noties.domain.model.Tag
import io.github.xamdr.noties.ui.helpers.DateTimeHelper
import io.github.xamdr.noties.ui.helpers.onClick

class ChipTagAdapter(private val onChipClick: () -> Unit) : ListAdapter<Tag, ChipTagAdapter.ChipTagViewHolder>(TagCallback) {

	inner class ChipTagViewHolder(private val binding: ItemChipTagBinding) : RecyclerView.ViewHolder(binding.root) {

		init {
			binding.chip.onClick { onChipClick() }
		}

		fun bind(tag: Tag) {
			binding.chip.apply {
				text = tag.name
				chipIcon = if (DateTimeHelper.isValidDate(tag.name)) {
					ContextCompat.getDrawable(this.context, R.drawable.ic_alarm)
				} else null
			}
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

	fun removeTagAt(position: Int) {
		val list = currentList.toMutableList()
		list.removeAt(position)
		submitList(list)
	}

	object TagCallback : DiffUtil.ItemCallback<Tag>() {

		override fun areItemsTheSame(oldItem: Tag, newItem: Tag) = oldItem.id == newItem.id

		override fun areContentsTheSame(oldItem: Tag, newItem: Tag) = oldItem == newItem
	}
}
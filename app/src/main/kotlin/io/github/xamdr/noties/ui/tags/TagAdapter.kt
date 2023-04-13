package io.github.xamdr.noties.ui.tags

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.xamdr.noties.R
import io.github.xamdr.noties.databinding.ItemTagBinding
import io.github.xamdr.noties.domain.model.Tag
import io.github.xamdr.noties.ui.helpers.setOnSingleClickListener

class TagAdapter(private val listener: TagPopupMenuItemListener) : ListAdapter<Tag, TagAdapter.TagViewHolder>(TagCallback) {

	inner class TagViewHolder(private val binding: ItemTagBinding) : RecyclerView.ViewHolder(binding.root) {

		init {
			binding.moreOptions.setOnSingleClickListener {
				if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
					showPopupMenu(it, bindingAdapterPosition)
				}
			}
		}

		fun bind(tag: Tag) {
			binding.tagName.text = tag.name
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
		val binding = ItemTagBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		return TagViewHolder(binding)
	}

	override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
		val tag = getItem(position)
		holder.bind(tag)
	}

	object TagCallback : DiffUtil.ItemCallback<Tag>() {

		override fun areItemsTheSame(oldItem: Tag, newItem: Tag) = oldItem.id == newItem.id

		override fun areContentsTheSame(oldItem: Tag, newItem: Tag) = oldItem == newItem
	}

	private fun showPopupMenu(view: View, position: Int) {
		val tag = getItem(position)
		PopupMenu(view.context, view).apply {
			inflate(R.menu.menu_tag_item)
			setOnMenuItemClickListener { menuItem ->
				when (menuItem.itemId) {
					R.id.edit_tag_name -> {
						listener.showCreateTagDialog(tag); true
					}
					R.id.delete_tag -> {
						listener.deleteTag(tag); true
					}
					else -> false
				}
			}
		}.show()
	}
}
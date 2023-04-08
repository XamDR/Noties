package io.github.xamdr.noties.ui.notebooks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.wrap
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.google.android.material.divider.MaterialDividerItemDecoration
import io.github.xamdr.noties.R
import io.github.xamdr.noties.core.Notebook
import io.github.xamdr.noties.core.NotebookEntity
import io.github.xamdr.noties.databinding.NotebookItemBinding
import io.github.xamdr.noties.ui.helpers.setOnSingleClickListener

class NotebookAdapter(private val listener: NotebookItemPopupMenuListener) :
	ListAdapter<Notebook, NotebookAdapter.NotebookViewHolder>(NotebookCallback()) {

	inner class NotebookViewHolder(private val binding: NotebookItemBinding) : RecyclerView.ViewHolder(binding.root) {

		init {
			binding.moreOptions.setOnSingleClickListener {
				if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
					showPopupMenu(it, bindingAdapterPosition)
				}
			}
		}

		fun bind(notebook: NotebookEntity) {
			binding.apply {
//				this.notebook = notebook
//				executePendingBindings()
			}
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotebookViewHolder {
		val binding = NotebookItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		return NotebookViewHolder(binding)
	}

	override fun onBindViewHolder(holder: NotebookViewHolder, position: Int) {
		val folder = getItem(position)
		holder.bind(folder.entity)
	}

	override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
		super.onAttachedToRecyclerView(recyclerView)
		recyclerView.addItemDecoration(MaterialDividerItemDecoration(
			recyclerView.context,
			MaterialDividerItemDecoration.VERTICAL
		).apply { dividerColor = MaterialColors.getColor(recyclerView, R.attr.colorPrimary) })
	}

	private fun showPopupMenu(view: View, position: Int) {
		val notebook = getItem(position)
		PopupMenu(view.context, view).apply {
			inflate(R.menu.menu_notebook_item)
			menu.findItem(R.id.delete_notebook).isVisible = notebook.entity.id != 1
			setOnMenuItemClickListener { menuItem ->
				when (menuItem.itemId) {
					R.id.edit_notebook_name -> {
						listener.showEditNotebookNameDialog(notebook.entity); true
					}
					R.id.delete_notebook -> {
						listener.deleteNotebook(notebook); true
					}
					else -> false
				}
			}
			wrap().setForceShowIcon(true) // Force the Popup to show icons
			show()
		}
	}

	class NotebookCallback : DiffUtil.ItemCallback<Notebook>() {

		override fun areItemsTheSame(oldItem: Notebook, newItem: Notebook) = oldItem.entity.id == newItem.entity.id

		override fun areContentsTheSame(oldItem: Notebook, newItem: Notebook) = oldItem == newItem
	}
}
package net.azurewebsites.noties.ui.notebooks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.google.android.material.divider.MaterialDividerItemDecoration
import net.azurewebsites.noties.R
import net.azurewebsites.noties.core.Notebook
import net.azurewebsites.noties.core.NotebookEntity
import net.azurewebsites.noties.databinding.NotebookItemBinding
import net.azurewebsites.noties.ui.helpers.setOnSingleClickListener

class NotebookAdapter(private val listener: EditNotebookNameListener) :
	ListAdapter<Notebook, NotebookAdapter.NotebookViewHolder>(NotebookCallback()) {

	inner class NotebookViewHolder(private val binding: NotebookItemBinding) : RecyclerView.ViewHolder(binding.root) {

		init {
			binding.moreOptions.setOnSingleClickListener {
				if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
					showEditNotebookNameDialog(bindingAdapterPosition)
				}
			}
		}

		fun bind(notebook: NotebookEntity) {
			binding.apply {
				this.notebook = notebook
				executePendingBindings()
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

	private fun showEditNotebookNameDialog(position: Int) {
		val notebook = getItem(position)
		listener.showEditNotebookNameDialog(notebook.entity)
	}

	class NotebookCallback : DiffUtil.ItemCallback<Notebook>() {

		override fun areItemsTheSame(oldItem: Notebook, newItem: Notebook) = oldItem.entity.id == newItem.entity.id

		override fun areContentsTheSame(oldItem: Notebook, newItem: Notebook) = oldItem == newItem
	}
}
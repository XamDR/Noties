package net.azurewebsites.noties.ui.notebooks

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.divider.MaterialDividerItemDecoration
import net.azurewebsites.noties.R
import net.azurewebsites.noties.core.Notebook
import net.azurewebsites.noties.core.NotebookEntity
import net.azurewebsites.noties.databinding.NotebookItemBinding
import net.azurewebsites.noties.ui.helpers.setOnSingleClickListener

class NotebookAdapter(private val listener: NotebookItemContextMenuListener) :
	ListAdapter<Notebook, NotebookAdapter.NotebookViewHolder>(NotebookCallback()) {

	inner class NotebookViewHolder(private val binding: NotebookItemBinding) : RecyclerView.ViewHolder(binding.root) {

		init {
			binding.moreOptions.setOnSingleClickListener {
				if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
					showContextMenu(bindingAdapterPosition, it)
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

	private fun showContextMenu(position: Int, view: View) {
		val context = view.context
		val selectedFolder = getItem(position)
		val items = getContextMenuItems(selectedFolder, context)
		MaterialAlertDialogBuilder(context, R.style.MyThemeOverlay_MaterialAlertDialog)
			.setTitle(selectedFolder.entity.name)
			.setAdapter(FolderItemContextMenuAdapter(context, R.layout.notebook_context_menu_item, items)) { _, which ->
				when (which) {
					0 -> listener.changeNotebookName(selectedFolder.entity)
					1 -> listener.lockNotebook(selectedFolder.entity)
					2 -> listener.deleteNotebook(selectedFolder)
				}
			}
			.setNegativeButton(R.string.cancel_button, null)
			.show()
			.window?.setWindowAnimations(R.style.ScaleAnimationDialog)
	}

	private fun getContextMenuItems(selectedNotebook: Notebook, context: Context): List<FolderContextMenuItem> {
		val items = if (selectedNotebook.entity.id == 1) {
			listOf(
				FolderContextMenuItem(R.drawable.ic_edit_notebooks, context.getString(R.string.update_notebook_name)),
				FolderContextMenuItem(
					if (selectedNotebook.entity.isProtected) R.drawable.ic_unlock_notebook else R.drawable.ic_lock_notebook,
					if (selectedNotebook.entity.isProtected) context.getString(R.string.unlock_notebook)
					else context.getString(R.string.lock_notebook)
				),
			)
		}
		else {
			listOf(
				FolderContextMenuItem(R.drawable.ic_edit_notebooks, context.getString(R.string.update_notebook_name)),
				FolderContextMenuItem(
					if (selectedNotebook.entity.isProtected) R.drawable.ic_unlock_notebook else R.drawable.ic_lock_notebook,
					if (selectedNotebook.entity.isProtected) context.getString(R.string.unlock_notebook)
					else context.getString(R.string.lock_notebook)
				),
				FolderContextMenuItem(R.drawable.ic_delete, context.getString(R.string.delete_notebook))
			)
		}
		return items
	}

	class NotebookCallback : DiffUtil.ItemCallback<Notebook>() {

		override fun areItemsTheSame(oldItem: Notebook, newItem: Notebook) = oldItem.entity.id == newItem.entity.id

		override fun areContentsTheSame(oldItem: Notebook, newItem: Notebook) = oldItem == newItem
	}
}
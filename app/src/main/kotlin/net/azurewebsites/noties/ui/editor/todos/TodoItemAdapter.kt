package net.azurewebsites.noties.ui.editor.todos

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import net.azurewebsites.noties.R
import net.azurewebsites.noties.core.DataItem
import net.azurewebsites.noties.databinding.TodoItemBinding
import net.azurewebsites.noties.databinding.TodoItemFooterBinding
import net.azurewebsites.noties.ui.editor.EditorFragment
import net.azurewebsites.noties.ui.helpers.SpanSizeLookupOwner
import net.azurewebsites.noties.ui.helpers.strikethrough

class TodoItemAdapter(
	val todoList: MutableList<DataItem>,
	private val itemTouchHelper: ItemTouchHelper) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), SpanSizeLookupOwner {

	inner class TodoItemViewHolder(private val binding: TodoItemBinding) : RecyclerView.ViewHolder(binding.root) {

		init {
			startDragging(binding.dragItem, this, itemTouchHelper)
			binding.removeItem.setOnClickListener { removeItem(bindingAdapterPosition) }
		}

		fun bind(todoItem: DataItem.TodoItem) {
			binding.apply {
				this.item = todoItem
				this.viewHolder = this@TodoItemViewHolder
				executePendingBindings()
			}
		}

		fun setItemDoneStatus(view: View) {
			val isDone = (view as CompoundButton).isChecked
			binding.todoItem.apply {
				strikethrough(isDone)
				isEnabled = !isDone
			}
		}
	}

	inner class FooterViewHolder(binding: TodoItemFooterBinding) : RecyclerView.ViewHolder(binding.root) {
		init {
			binding.addItem.setOnClickListener { addItem() }
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
		TODO_ITEM -> {
			val binding = TodoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
			TodoItemViewHolder(binding)
		}
		FOOTER -> {
			val binding = TodoItemFooterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
			FooterViewHolder(binding)
		}
		else -> throw ClassCastException("Unknown viewType $viewType")
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		when (holder) {
			is TodoItemViewHolder -> {
				val todoItem = todoList[position]
				holder.bind(todoItem as DataItem.TodoItem)
			}
		}
	}

	override fun getItemCount() = todoList.size

	override fun getItemViewType(position: Int) = when (todoList[position]) {
		is DataItem.TodoItem -> TODO_ITEM
		is DataItem.Footer -> FOOTER
	}

	override fun getSpanSizeLookup() = object : GridLayoutManager.SpanSizeLookup() {
		override fun getSpanSize(position: Int) = EditorFragment.SPAN_COUNT
	}

	fun moveItem(from: Int, to: Int) {
		val fromItem = todoList[from]
		todoList.removeAt(from)
		todoList.add(to, fromItem)
		notifyItemMoved(from, to)
	}

	private fun addItem() {
		todoList.add(todoList.size - 1, DataItem.TodoItem())
		notifyItemInserted(todoList.size - 1)
	}

	private fun removeItem(position: Int) {
		if (position != RecyclerView.NO_POSITION) {
			val todoItem = todoList[position]
			todoList.remove(todoItem)
			notifyItemRemoved(position)
		}
	}

	private fun startDragging(view: View, holder: TodoItemViewHolder, itemTouchHelper: ItemTouchHelper) {
		view.setOnTouchListener { _, event ->
			if (event.actionMasked == MotionEvent.ACTION_DOWN) {
				itemTouchHelper.startDrag(holder)
			}
			view.performClick()
			false
		}
	}

	private companion object {
		private const val TODO_ITEM = R.layout.todo_item
		private const val FOOTER = R.layout.todo_item_footer
	}
}
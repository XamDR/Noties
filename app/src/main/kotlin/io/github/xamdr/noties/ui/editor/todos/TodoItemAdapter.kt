package io.github.xamdr.noties.ui.editor.todos

import android.text.TextUtils
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.CompoundButton
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import io.github.xamdr.noties.R
import io.github.xamdr.noties.domain.model.Todo
import io.github.xamdr.noties.databinding.TodoItemBinding
import io.github.xamdr.noties.databinding.TodoItemFooterBinding
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.ui.editor.EditorFragment
import io.github.xamdr.noties.ui.helpers.Constants
import io.github.xamdr.noties.ui.helpers.SpanSizeLookupOwner
import io.github.xamdr.noties.ui.helpers.showSoftKeyboard
import io.github.xamdr.noties.ui.helpers.strikethrough

class TodoItemAdapter(
	private val todoList: MutableList<Todo>,
	private val itemTouchHelper: ItemTouchHelper) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), SpanSizeLookupOwner {

	inner class TodoItemViewHolder(private val binding: TodoItemBinding) : RecyclerView.ViewHolder(binding.root) {

		init {
//			binding.viewHolder = this
			startDragging(binding.dragItem, this, itemTouchHelper)
			binding.removeItem.setOnClickListener { removeItem(bindingAdapterPosition) }
			setupEditText()
		}

		fun bind(todoItem: Todo.TodoItem) {
			binding.apply {
//				this.item = todoItem
//				executePendingBindings()
			}
		}

		fun setItemDoneStatus(view: View) {
			val isDone = (view as CompoundButton).isChecked
			binding.todoItem.apply {
				strikethrough(isDone)
			}
		}

		private fun setupEditText() {
			binding.todoItem.apply {
				setOnFocusChangeListener { _, hasFocus -> binding.removeItem.isVisible = hasFocus }
				setOnEditorActionListener(TodoItemActionListener())
				post { if (TextUtils.isEmpty(text)) showSoftKeyboard() }
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
				holder.bind(todoItem as Todo.TodoItem)
			}
		}
	}

	override fun getItemCount() = todoList.size

	override fun getItemViewType(position: Int) = when (todoList[position]) {
		is Todo.TodoItem -> TODO_ITEM
		is Todo.Footer -> FOOTER
	}

	override fun getSpanSizeLookup() = object : GridLayoutManager.SpanSizeLookup() {
		override fun getSpanSize(position: Int) = Constants.SPAN_COUNT
	}

	fun moveItem(from: Int, to: Int) {
		val fromItem = todoList[from]
		todoList.removeAt(from)
		todoList.add(to, fromItem)
		notifyItemMoved(from, to)
	}

	fun convertItemsToString(): String {
		val todoItems = todoList.filterIsInstance<Todo.TodoItem>()
		return todoItems.joinToString(Note.NEWLINE) {
			if (it.done) {
				if (it.content.startsWith(Note.PREFIX_DONE)) it.content
				else "${Note.PREFIX_DONE}${it.content}"
			}
			else {
				if (it.content.startsWith(Note.PREFIX_NOT_DONE)) it.content
				else "${Note.PREFIX_NOT_DONE}${it.content}"
			}
		}
	}

	fun joinToString(): String {
		val todoItems = todoList.filterIsInstance<Todo.TodoItem>()
		return todoItems.joinToString(Note.NEWLINE) { it.content }
	}

	private fun addItem() {
		todoList.add(todoList.size - 1, Todo.TodoItem())
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

	private inner class TodoItemActionListener : TextView.OnEditorActionListener {
		override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
			if (actionId == EditorInfo.IME_ACTION_DONE) {
				addItem()
				return true
			}
			return false
		}
	}

	private companion object {
		private const val TODO_ITEM = R.layout.todo_item
		private const val FOOTER = R.layout.todo_item_footer
	}
}
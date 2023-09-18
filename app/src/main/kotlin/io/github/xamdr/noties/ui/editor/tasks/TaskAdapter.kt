package io.github.xamdr.noties.ui.editor.tasks

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.CompoundButton
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryOwner
import io.github.xamdr.noties.R
import io.github.xamdr.noties.databinding.ItemTaskBinding
import io.github.xamdr.noties.databinding.ItemTaskFooterBinding
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.domain.model.Task
import io.github.xamdr.noties.ui.helpers.*

class TaskAdapter(
	registryOwner: SavedStateRegistryOwner,
	private val tasks: MutableList<Task>,
	private val itemTouchHelper: ItemTouchHelper) : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
	SavedStateRegistry.SavedStateProvider {

	init {
		registryOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
			if (event == Lifecycle.Event.ON_CREATE) {
				val registry = registryOwner.savedStateRegistry
				registry.registerSavedStateProvider(PROVIDER, this)
				val restoredState = registry.consumeRestoredStateForKey(PROVIDER)
				if (restoredState != null && restoredState.containsKey(BUNDLE_TASKS)) {
					val restoredTasks = restoredState.getParcelableArrayListCompat(BUNDLE_TASKS, Task::class.java)
					submitTasks(restoredTasks)
				}
			}
		})
	}

	inner class TaskViewHolder(private val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root), TextWatcher {

		init {
			startDragging(binding.dragItem, this, itemTouchHelper)
			binding.checkItem.onClick { setItemStatus(it) }
			binding.removeItem.onClick { removeItem(bindingAdapterPosition) }
			setupEditText()
		}

		fun bind(item: Task.Item) {
			binding.apply {
				taskItem.setText(item.content)
				checkItem.isChecked = item.done
				taskItem.strikethrough(item.done)
				root.tag = item
			}
		}

		fun bindIsDone(item: Task.Item) {
			binding.apply {
				checkItem.isChecked = item.done
				taskItem.strikethrough(item.done)
			}
		}

		fun bindTextWatcher() {
			binding.taskItem.addTextChangedListener(this)
		}

		fun unbindTextWatcher() {
			binding.taskItem.removeTextChangedListener(this)
		}

		override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

		override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

		override fun afterTextChanged(s: Editable) {
			val currentTask = binding.root.tag as? Task.Item ?: return
			currentTask.content = s.toString()
		}

		private fun setItemStatus(view: View) {
			val isDone = (view as CompoundButton).isChecked
			binding.apply {
				taskItem.strikethrough(isDone)
				val currenTask = root.tag as? Task.Item ?: return
				currenTask.done = isDone
			}
		}

		private fun setupEditText() {
			binding.taskItem.apply {
				setOnFocusChangeListener { _, hasFocus -> binding.removeItem.isVisible = hasFocus }
				setOnEditorActionListener(TodoItemActionListener())
				post { if (TextUtils.isEmpty(text)) showSoftKeyboard() }
			}
		}
	}

	inner class FooterViewHolder(binding: ItemTaskFooterBinding) : RecyclerView.ViewHolder(binding.root) {
		init {
			binding.addItem.onClick { addItem() }
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
		TASK -> {
			val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
			TaskViewHolder(binding)
		}
		FOOTER -> {
			val binding = ItemTaskFooterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
			FooterViewHolder(binding)
		}
		else -> throw ClassCastException("Unknown viewType $viewType")
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		when (holder) {
			is TaskViewHolder -> {
				val task = tasks[position]
				holder.bind(task as Task.Item)
			}
		}
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
		if (payloads.isNotEmpty() && payloads[0] == PAYLOAD_DONE) {
			when (holder) {
				is TaskViewHolder -> {
					val task = tasks[position]
					holder.bindIsDone(task as Task.Item)
				}
			}
		}
		else {
			super.onBindViewHolder(holder, position, payloads)
		}
	}

	override fun getItemCount() = tasks.size

	override fun getItemViewType(position: Int) = when (tasks[position]) {
		is Task.Item -> TASK
		is Task.Footer -> FOOTER
	}

	override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
		super.onViewAttachedToWindow(holder)
		if (holder is TaskViewHolder) {
			holder.bindTextWatcher()
		}
	}

	override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
		super.onViewDetachedFromWindow(holder)
		if (holder is TaskViewHolder) {
			holder.unbindTextWatcher()
		}
	}

	override fun saveState(): Bundle {
		return bundleOf(BUNDLE_TASKS to ArrayList(tasks))
	}

	@SuppressLint("NotifyDataSetChanged")
	fun submitTasks(tasks: MutableList<Task>) {
		this.tasks.apply {
			clear()
			addAll(tasks)
		}
		notifyDataSetChanged()
	}

	fun markAllTasksAsDone(value: Boolean) {
		val items = tasks.filterIsInstance<Task.Item>()
		for (item in items) {
			item.done = value
			notifyItemChanged(items.indexOf(item), PAYLOAD_DONE)
		}
	}

	fun moveItem(from: Int, to: Int) {
		val fromItem = tasks[from]
		tasks.removeAt(from)
		tasks.add(to, fromItem)
		notifyItemMoved(from, to)
	}

	fun convertItemsToString(): String {
		val items = tasks.filterIsInstance<Task.Item>()
		return items.joinToString(Note.NEWLINE) {
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
		val items = tasks.filterIsInstance<Task.Item>()
		return items.joinToString(Note.NEWLINE) { it.content }
	}

	private fun addItem() {
		tasks.add(tasks.size - 1, Task.Item())
		notifyItemInserted(tasks.size - 1)
	}

	private fun removeItem(position: Int) {
		if (position != RecyclerView.NO_POSITION) {
			val todoItem = tasks[position]
			tasks.remove(todoItem)
			notifyItemRemoved(position)
		}
	}

	private fun startDragging(view: View, holder: TaskViewHolder, itemTouchHelper: ItemTouchHelper) {
		view.setOnTouchListener { _, event ->
			if (event.actionMasked == MotionEvent.ACTION_DOWN) {
				itemTouchHelper.startDrag(holder)
			}
			view.performClick()
			false
		}
	}

	private inner class TodoItemActionListener : TextView.OnEditorActionListener {
		override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
			if (actionId == EditorInfo.IME_ACTION_DONE) {
				addItem()
				return true
			}
			return false
		}
	}

	private companion object {
		private const val TASK = R.layout.item_task
		private const val FOOTER = R.layout.item_task_footer
		private const val PAYLOAD_DONE = "PAYLOAD_DONE"
		private const val BUNDLE_TASKS = "BUNDLE_TASKS"
		private const val PROVIDER = "TASK_ADAPTER"
	}
}
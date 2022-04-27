package net.azurewebsites.noties.ui.editor

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import net.azurewebsites.noties.R
import net.azurewebsites.noties.databinding.FragmentEditorContentBinding
import net.azurewebsites.noties.domain.NoteEntity
import net.azurewebsites.noties.domain.Task
import net.azurewebsites.noties.databinding.TaskItemBinding
import net.azurewebsites.noties.ui.helpers.printDebug
import net.azurewebsites.noties.ui.media.MediaContentReceiverListener

class EditorContentAdapter(private val note: NoteEntity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

	inner class EditorViewHolder(private val binding: FragmentEditorContentBinding) : RecyclerView.ViewHolder(binding.root) {

		private val listener = MediaContentReceiverListener { uri, _ ->
			printDebug("LISTENER", uri)
			contentReceivedCallback.invoke(uri)
		}

		init {
			if (note.id == 0L) {
				binding.editor.requestFocus()
				binding.editor.setOnEmptyListener { emptyContentCallback.invoke() }
			}
			ViewCompat.setOnReceiveContentListener(binding.editor, MediaContentReceiverListener.MIME_TYPES, listener)
		}

		fun bind(note: NoteEntity) {
			binding.note = note
		}
	}

	inner class TaskViewHolder(private val binding: TaskItemBinding) : RecyclerView.ViewHolder(binding.root) {

		fun bind(task: Task) {
			printDebug("BIND", "${binding.root}$task")
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when(viewType) {
		EDITOR -> {
			val binding = FragmentEditorContentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
			EditorViewHolder(binding)
		}
		TASK_LIST -> {
			val binding = TaskItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
			TaskViewHolder(binding)
		}
		else -> throw Exception("Unknown view type.")
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		if (holder is TaskViewHolder && note.toTaskList().isNotEmpty()) {
			val task = note.toTaskList()[position]
			holder.bind(task)
		}
		else if (holder is EditorViewHolder) {
			holder.bind(note)
		}
	}

	override fun getItemCount() = if (note.isTaskList && note.toTaskList().isNotEmpty()) note.toTaskList().size else 1

	override fun getItemViewType(position: Int) = if (note.isTaskList && note.toTaskList().isNotEmpty()) TASK_LIST else EDITOR

	fun setOnContentReceivedListener(callback: (uri: Uri) -> Unit) {
		contentReceivedCallback = callback
	}

	fun setOnEmptyContentListener(callback: () -> Unit) {
		emptyContentCallback = callback
	}

	private var contentReceivedCallback: (uri: Uri) -> Unit = {}

	private var emptyContentCallback: () -> Unit = {}

	companion object {
		private const val EDITOR = R.layout.fragment_editor_content
		private const val TASK_LIST = 1
	}
}
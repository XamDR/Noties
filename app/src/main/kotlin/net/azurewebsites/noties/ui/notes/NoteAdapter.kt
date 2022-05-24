package net.azurewebsites.noties.ui.notes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.azurewebsites.noties.R
import net.azurewebsites.noties.core.Note
import net.azurewebsites.noties.databinding.NoteItemBinding
import net.azurewebsites.noties.ui.editor.EditorFragment
import net.azurewebsites.noties.ui.helpers.setOnClickListener
import net.azurewebsites.noties.ui.helpers.tryNavigate

class NoteAdapter(private val listener: SwipeToDeleteListener) : ListAdapter<Note, RecyclerView.ViewHolder>(NoteAdapterCallback()) {

	inner class NoteViewHolder(private val binding: NoteItemBinding) : RecyclerView.ViewHolder(binding.root) {

		init {
			binding.url.setOnClickListener { }
		}

		fun bind(note: Note, isSelected: Boolean = false) {
			binding.apply {
				this.note = note
				ViewCompat.setTransitionName(root, note.entity.id.toString())
				executePendingBindings()
			}
			itemView.isActivated = isSelected
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
		NOTE_LINEAR -> {
			val binding = NoteItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
			NoteViewHolder(binding).apply {
				setOnClickListener { position, _ -> editNote(this, position) }
			}
		}
		else -> throw Exception("Unknown view type.")
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		when (holder) {
			is NoteViewHolder -> {
				val note = getItem(position)
				holder.bind(note)
			}
		}
	}

	override fun getItemViewType(position: Int) = NOTE_LINEAR

	override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
		super.onAttachedToRecyclerView(recyclerView)
		recyclerView.addItemDecoration(SpaceItemDecoration(spacing = 16))
	}

	fun moveNoteToTrash(position: Int) {
		val note = getItem(position)
		listener.moveNoteToTrash(note.entity)
	}

	private fun editNote(holder: RecyclerView.ViewHolder, position: Int) {
		val note = getItem(position)
		if (!note.entity.isTrashed) {
			val args = bundleOf(EditorFragment.NOTE to note)
			val extras = FragmentNavigatorExtras(holder.itemView to note.entity.id.toString())
			holder.itemView.findNavController().tryNavigate(
				resId = R.id.action_notes_to_editor,
				args = args,
				navOptions = null,
				navigatorExtras = extras
			)
		}
	}

	private class NoteAdapterCallback : DiffUtil.ItemCallback<Note>() {

		override fun areItemsTheSame(oldNote: Note, newNote: Note) = oldNote.entity.id == newNote.entity.id

		override fun areContentsTheSame(oldNote: Note, newNote: Note) = oldNote == newNote
	}

	private companion object {
		private const val NOTE_LINEAR = R.layout.note_item
	}
}
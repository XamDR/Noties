package io.github.xamdr.noties.ui.notes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import io.github.xamdr.noties.R
import io.github.xamdr.noties.databinding.ItemNoteBinding
import io.github.xamdr.noties.databinding.ItemProtectedNoteBinding
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.ui.helpers.blur
import io.github.xamdr.noties.ui.helpers.setOnClickListener

class NoteAdapter(
	private val onNoteClicked: (view: View?, note: Note) -> Unit,
	private val onNoteSwiped: (note: Note) -> Unit) : ListAdapter<Note, NoteAdapter.BaseViewHolder>(NoteAdapterCallback()) {

	var tracker: SelectionTracker<Note>? = null

	open inner class BaseViewHolder(private val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

		fun bind(note: Note, isSelected: Boolean = false) {
			when (binding) {
				is ItemNoteBinding -> bindNote(binding, note)
				is ItemProtectedNoteBinding -> {

				}
			}
			itemView.isActivated = isSelected
		}

		fun getItemDetails() = object : ItemDetailsLookup.ItemDetails<Note>() {
			override fun getPosition(): Int = bindingAdapterPosition
			override fun getSelectionKey(): Note = getItem(position)
		}

		private fun bindNote(binding: ItemNoteBinding, note: Note) {
			binding.title.isVisible = note.title.isNotEmpty()
			binding.title.text = note.title
			binding.content.isVisible = note.text.isNotEmpty()
			binding.content.text = note.text
			binding.url.isVisible = note.urls.isNotEmpty()
			binding.url.text = note.urls.size.toString()
			binding.image.isVisible = note.getPreviewImage() != null
			binding.image.setImageURI(note.getPreviewImage())
			ViewCompat.setTransitionName(binding.root, note.id.toString())
		}
	}

	inner class NoteViewHolder(binding: ItemNoteBinding) : BaseViewHolder(binding)

	inner class ProtectedNoteViewHolder(binding: ItemProtectedNoteBinding) : BaseViewHolder(binding) {
		init {
			binding.placeholderContent.blur()
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
		NOTE_LINEAR_LAYOUT -> {
			val binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
			NoteViewHolder(binding).apply {
				setOnClickListener(this@NoteAdapter::onItemClick)
			}
		}
		NOTE_PROTECTED -> {
			val binding = ItemProtectedNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
			ProtectedNoteViewHolder(binding)
		}
		else -> throw ClassCastException("Unknown view type: $viewType.")
	}

	override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
		val note = getItem(position)
		if (tracker == null) {
			holder.bind(note)
		}
		else {
			tracker?.let { holder.bind(note, it.isSelected(note)) }
		}
	}

	override fun getItemViewType(position: Int): Int {
		val note = getItem(position)
		return if (note.isProtected) NOTE_PROTECTED else NOTE_LINEAR_LAYOUT
	}

	override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
		super.onAttachedToRecyclerView(recyclerView)
		recyclerView.addItemDecoration(SpaceItemDecoration(spacing = 16))
	}

	private fun onItemClick(view: View?, position: Int) {
		if (position != RecyclerView.NO_POSITION) {
			onNoteClicked.invoke(view, getItem(position))
		}
	}

	fun moveNoteToTrash(position: Int) {
		val note = getItem(position)
		onNoteSwiped.invoke(note)
	}

	private fun getSelectedNotes(): List<Note> {
		val iterator = tracker?.selection?.iterator()
		val notes = mutableListOf<Note>()
		iterator?.let {
			while (iterator.hasNext()) {
				notes.add(iterator.next())
			}
		}
		return notes
	}

	fun deleteNotes() = onDeleteNotesCallback(getSelectedNotes())

	fun selectAllNotes() {
		for (note in currentList) {
			if (tracker?.isSelected(note) == false) {
				tracker?.select(note)
			}
		}
	}

	fun setOnDeleteNotesListener(callback: (notes: List<Note>) -> Unit) {
		onDeleteNotesCallback = callback
	}

	private var onDeleteNotesCallback: (notes: List<Note>) -> Unit = {}

	private class NoteAdapterCallback : DiffUtil.ItemCallback<Note>() {

		override fun areItemsTheSame(oldNote: Note, newNote: Note) = oldNote.id == newNote.id

		override fun areContentsTheSame(oldNote: Note, newNote: Note) = oldNote == newNote
	}

	private companion object {
		private const val NOTE_LINEAR_LAYOUT = R.layout.item_note
		private const val NOTE_PROTECTED = R.layout.item_protected_note
	}
}
package io.github.xamdr.noties.ui.notes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import io.github.xamdr.noties.R
import io.github.xamdr.noties.databinding.NoteItemBinding
import io.github.xamdr.noties.databinding.ProtectedNoteItemBinding
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.ui.editor.EditorViewModel
import io.github.xamdr.noties.ui.helpers.blur
import io.github.xamdr.noties.ui.helpers.setOnClickListener
import io.github.xamdr.noties.ui.helpers.tryNavigate

class NoteAdapter : ListAdapter<Note, NoteAdapter.BaseViewHolder>(NoteAdapterCallback()) {

	var tracker: SelectionTracker<Note>? = null

	open inner class BaseViewHolder(private val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

		fun bind(note: Note, isSelected: Boolean = false) {
			when (binding) {
				is NoteItemBinding -> {
					binding.title.text = note.title
					binding.content.text = note.text
					binding.url.text = note.urls.size.toString()
					binding.image.setImageURI(note.getPreviewImage())
					ViewCompat.setTransitionName(binding.root, note.id.toString())
				}
				is ProtectedNoteItemBinding -> {

				}
			}
			itemView.isActivated = isSelected
		}

		fun getItemDetails() = object : ItemDetailsLookup.ItemDetails<Note>() {
			override fun getPosition(): Int = bindingAdapterPosition
			override fun getSelectionKey(): Note = getItem(position)
		}
	}

	inner class NoteViewHolder(binding: NoteItemBinding) : BaseViewHolder(binding) {
		init {
			binding.url.setOnClickListener { showUrlsDialog(bindingAdapterPosition) }
		}
	}

	inner class ProtectedNoteViewHolder(binding: ProtectedNoteItemBinding) : BaseViewHolder(binding) {
		init {
			binding.placeholderContent.blur()
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
		NOTE_LINEAR_LAYOUT -> {
			val binding = NoteItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
			NoteViewHolder(binding).apply {
				setOnClickListener { position -> editNote(this, position) }
			}
		}
		NOTE_PROTECTED -> {
			val binding = ProtectedNoteItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
			ProtectedNoteViewHolder(binding).apply {
				setOnClickListener { position -> editNote(this, position) }
			}
		}
		else -> throw Exception("Unknown view type.")
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

	fun moveNoteToTrash(position: Int) {
//		val note = getItem(position)
//		listener.moveNoteToTrash(note.entity)
	}

	fun getSelectedNotes(): List<Note> {
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

	fun toggleLockedValueForNotes() = onLockNotesCallback(getSelectedNotes())

	fun selectAllNotes() {
		for (note in currentList) {
			if (tracker?.isSelected(note) == false) {
				tracker?.select(note)
			}
		}
	}

	fun restoreNotes() = onRestoreNotesCallback(getSelectedNotes())

	fun togglePinnedValueForNotes() = onPinNotesCallback(getSelectedNotes())

	fun moveNotes() = onMoveNotesCallback(getSelectedNotes())

	private fun editNote(holder: RecyclerView.ViewHolder, position: Int) {
		val note = getItem(position)
		if (!note.isTrashed) {
			val args = bundleOf(EditorViewModel.NOTE to note)
			val extras = FragmentNavigatorExtras(holder.itemView to note.id.toString())
			holder.itemView.findNavController().tryNavigate(
				resId = R.id.action_notes_to_editor,
				args = args,
				navOptions = null,
				navigatorExtras = extras
			)
		}
	}

	private fun showUrlsDialog(position: Int) {
		if (position != RecyclerView.NO_POSITION) {
			val note = getItem(position)
			onShowUrlsCallback(note.urls)
		}
	}

	fun setOnDeleteNotesListener(callback: (notes: List<Note>) -> Unit) {
		onDeleteNotesCallback = callback
	}

	fun setOnRestoreNotesListener(callback: (notes: List<Note>) -> Unit) {
		onRestoreNotesCallback = callback
	}

	private var onShowUrlsCallback: (urls: List<String>) -> Unit = {}

	private var onDeleteNotesCallback: (notes: List<Note>) -> Unit = {}

	private var onLockNotesCallback: (notes: List<Note>) -> Unit = {}

	private var onRestoreNotesCallback: (notes: List<Note>) -> Unit = {}

	private var onPinNotesCallback: (notes: List<Note>) -> Unit = {}

	private var onMoveNotesCallback: (notes: List<Note>) -> Unit = {}

	private class NoteAdapterCallback : DiffUtil.ItemCallback<Note>() {

		override fun areItemsTheSame(oldNote: Note, newNote: Note) = oldNote.id == newNote.id

		override fun areContentsTheSame(oldNote: Note, newNote: Note) = oldNote == newNote
	}

	private companion object {
		private const val NOTE_LINEAR_LAYOUT = R.layout.note_item
		private const val NOTE_PROTECTED = R.layout.protected_note_item
	}
}
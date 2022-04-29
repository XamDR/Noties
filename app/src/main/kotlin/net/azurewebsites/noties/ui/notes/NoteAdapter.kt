package net.azurewebsites.noties.ui.notes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.azurewebsites.noties.R
import net.azurewebsites.noties.domain.Note
import net.azurewebsites.noties.databinding.NoteItemBinding
import net.azurewebsites.noties.ui.editor.EditorFragment
import net.azurewebsites.noties.ui.helpers.addItemTouchHelper
import net.azurewebsites.noties.ui.helpers.printError
import net.azurewebsites.noties.ui.helpers.tryNavigate
import net.azurewebsites.noties.ui.helpers.setOnClickListener

class NoteAdapter : ListAdapter<Note, NoteAdapter.NoteViewHolder>(NoteAdapterCallback()) {

	inner class NoteViewHolder(private val binding: NoteItemBinding): RecyclerView.ViewHolder(binding.root) {

		init {
			binding.url.setOnClickListener {
				showNoteHyperlinks(getItem(bindingAdapterPosition))
			}
		}

		fun bind(note: Note, isSelected: Boolean = false) {
			binding.apply {
				this.note = note.entity
				ViewCompat.setTransitionName(root, note.entity.id.toString())
				executePendingBindings()
			}
			itemView.isActivated = isSelected
		}

		private fun showNoteHyperlinks(note: Note) = onChipClickedCallback.invoke(note.entity.urls)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
		val binding = NoteItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		return NoteViewHolder(binding).apply {
			setOnClickListener { position, _ -> editNote(this, position) }
		}
	}

	override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
		val note = getItem(position)
		holder.bind(note)
	}

	override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
		super.onAttachedToRecyclerView(recyclerView)
		recyclerView.addItemDecoration(SpaceItemDecoration(spacing = 16))
		recyclerView.addItemTouchHelper(ItemTouchHelper(SwipeToDeleteCallback(this)))
	}

	fun deleteNote(position: Int) {
		val note = getItem(position)
		onNotesDeletedCallback.invoke(listOf(note), true)
	}

	private fun editNote(holder: RecyclerView.ViewHolder, position: Int) {
		val note = getItem(position)
		val args = bundleOf(EditorFragment.NOTE to note)
		val extras = FragmentNavigatorExtras(holder.itemView to note.entity.id.toString())
		try {
			holder.itemView.findNavController().tryNavigate(
				resId = R.id.action_notes_to_editor,
				args = args,
				navOptions = null,
				navigatorExtras = extras
			)
		}
		catch (e: IllegalStateException) {
			printError("ERROR_NAVIGATION", e.message)
		}
	}

	fun setOnNotesDeletedListener(callback: (notes: List<Note>, hasUndo: Boolean) -> Unit) {
		onNotesDeletedCallback = callback
	}

	private var onNotesDeletedCallback: (notes: List<Note>, hasUndo: Boolean) -> Unit = { _, _ -> }

	fun setOnChipClickedListener(callback: (urls: List<String>) -> Unit) {
		onChipClickedCallback = callback
	}

	private var onChipClickedCallback: (urls: List<String>) -> Unit = {}

	private class NoteAdapterCallback : DiffUtil.ItemCallback<Note>() {

		override fun areItemsTheSame(oldNote: Note, newNote: Note) = oldNote.entity.id == newNote.entity.id

		override fun areContentsTheSame(oldNote: Note, newNote: Note) = oldNote == newNote
	}
}
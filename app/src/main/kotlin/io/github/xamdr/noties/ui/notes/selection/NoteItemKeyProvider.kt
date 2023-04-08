package io.github.xamdr.noties.ui.notes.selection

import androidx.recyclerview.selection.ItemKeyProvider
import io.github.xamdr.noties.core.Note
import io.github.xamdr.noties.ui.notes.NoteAdapter

class NoteItemKeyProvider(private val adapter: NoteAdapter) : ItemKeyProvider<Note>(SCOPE_CACHED) {

	override fun getKey(position: Int): Note = adapter.currentList[position]

	override fun getPosition(key: Note) = adapter.currentList.indexOfFirst { it == key }
}
package io.github.xamdr.noties.domain.usecase

import io.github.xamdr.noties.data.repository.MediaItemRepository
import io.github.xamdr.noties.data.repository.NoteRepository
import io.github.xamdr.noties.domain.model.MediaItem
import io.github.xamdr.noties.domain.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

class InsertNoteUseCase @Inject constructor(
	private val noteRepository: NoteRepository,
	private val mediaItemRepository: MediaItemRepository) {

	suspend operator fun invoke(note: Note) {
		val newNote = note.copy(modificationDate = Instant.now().toEpochMilli())
		val id = noteRepository.insertNote(newNote.asDatabaseEntity())
		val updatedItems = mutableListOf<MediaItem>()
		newNote.items.forEach { item -> updatedItems.add(item.copy(noteId = id)) }
		mediaItemRepository.insertItems(updatedItems.map { it.asDatabaseEntity() })
	}
}

class GetNotesUseCase @Inject constructor(private val noteRepository: NoteRepository) {

	operator fun invoke(tagName: String): Flow<List<Note>> {
		return noteRepository.getNotesByTag(tagName).map { result ->
			result.map { (note, items) ->
				Note.create(note.asDomainModel(), items.map { it.asDomainModel() })
			}
		}
	}
}

class GetNoteByIdUseCase @Inject constructor(private val noteRepository: NoteRepository) {

	suspend operator fun invoke(noteId: Long): Note {
		val result = noteRepository.getNoteById(noteId).entries.firstOrNull() ?: return Note()
		val note = result.key.asDomainModel()
		val items = result.value.map { it.asDomainModel() }
		return Note.create(note, items)
	}
}

class GetAllNotesUseCase @Inject constructor(private val noteRepository: NoteRepository) {

	operator fun invoke(): Flow<List<Note>> {
		return noteRepository.getAllNotes().map { result ->
			result.map { (note, images) ->
				Note.create(note.asDomainModel(), images.map { it.asDomainModel() })
			}
		}
	}
}

class UpdateNoteUseCase @Inject constructor(
	private val noteRepository: NoteRepository,
	private val mediaItemRepository: MediaItemRepository) {

	suspend operator fun invoke(note: Note) {
		val updatedNote = note.copy(modificationDate = Instant.now().toEpochMilli())
		noteRepository.updateNote(updatedNote.asDatabaseEntity())
		val newItems = mutableListOf<MediaItem>()
		note.items
			.filter { item -> item.id == 0 }
			.forEach { newItem -> newItems.add(newItem.copy(noteId = note.id)) }
		mediaItemRepository.insertItems(newItems.map { it.asDatabaseEntity() })
	}
}

class DeleteNotesUseCase @Inject constructor(
	private val noteRepository: NoteRepository,
	private val mediaItemRepository: MediaItemRepository) {

	suspend operator fun invoke(notes: List<Note>) {
		for (note in notes) {
			val items = note.items.map { it.asDatabaseEntity() }
			mediaItemRepository.deleteItems(items)
			noteRepository.deleteNote(note.asDatabaseEntity())
		}
	}
}

class GetTrashedNotesUseCase @Inject constructor(private val noteRepository: NoteRepository) {
	operator fun invoke(): Flow<List<Note>> {
		return noteRepository.getTrashedNotes().map { result ->
			result.map { (note, items) ->
				Note.create(note.asDomainModel(), items.map { it.asDomainModel() })
			}
		}
	}
}

class EmptyTrashUseCase @Inject constructor(
	private val noteRepository: NoteRepository,
	private val mediaItemRepository: MediaItemRepository) {

	suspend operator fun invoke(notes: List<Note>): Int {
		val entities = notes.map { it.asDatabaseEntity() }
		for (note in notes) {
			val items = note.items.map { it.asDatabaseEntity() }
			mediaItemRepository.deleteItems(items)
		}
		return noteRepository.deleteTrashedNotes(entities)
	}
}

class MoveNotesToTrashUseCase @Inject constructor(private val noteRepository: NoteRepository) {

	suspend operator fun invoke(notes: List<Note>): List<Note> {
		val trashedNotes = mutableListOf<Note>()
		for (note in notes) {
			val trashedNote = note.copy(trashed = true)
			noteRepository.updateNote(trashedNote.asDatabaseEntity())
			trashedNotes.add(trashedNote)
		}
		return trashedNotes
	}
}

class ArchiveNotesUseCase @Inject constructor(private val noteRepository: NoteRepository) {

	suspend operator fun invoke(notes: List<Note>): List<Note> {
		val archivedNotes = mutableListOf<Note>()
		for (note in notes) {
			val archivedNote = note.copy(archived = true)
			noteRepository.updateNote(archivedNote.asDatabaseEntity())
			archivedNotes.add(archivedNote)
		}
		return archivedNotes
	}
}

class RestoreNotesUseCase @Inject constructor(private val noteRepository: NoteRepository) {

	suspend operator fun invoke(notes: List<Note>, fromTrash: Boolean) {
		for (note in notes) {
			val restoredNote = if (fromTrash) note.copy(trashed = false) else note.copy(archived = false)
			noteRepository.updateNote(restoredNote.asDatabaseEntity())
		}
	}
}

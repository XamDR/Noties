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
		for (item in newNote.items) {
			val updatedItem = item.copy(noteId = id)
			updatedItems.add(updatedItem)
		}
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
		val newItems = note.items.filter { item -> item.id == 0 }.map { it.asDatabaseEntity() }
		mediaItemRepository.insertItems(newItems)
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
			val trashedNote = note.copy(isTrashed =  true)
			noteRepository.updateNote(trashedNote.asDatabaseEntity())
			trashedNotes.add(trashedNote)
		}
		return trashedNotes
	}
}

class RestoreNotesUseCase @Inject constructor(private val noteRepository: NoteRepository) {

	suspend operator fun invoke(notes: List<Note>) {
		for (note in notes) {
			val restoredNote = note.copy(isTrashed = false)
			noteRepository.updateNote(restoredNote.asDatabaseEntity())
		}
	}
}

package io.github.xamdr.noties.domain.usecase

import io.github.xamdr.noties.data.repository.NoteRepository
import io.github.xamdr.noties.domain.model.Image
import io.github.xamdr.noties.domain.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject

class InsertNoteUseCase @Inject constructor(
	private val noteRepository: NoteRepository,
	private val insertImagesUseCase: InsertImagesUseCase) {

	suspend operator fun invoke(note: Note) {
		val id = noteRepository.insertNote(note.asDatabaseEntity())
		val updateImages = mutableListOf<Image>()
		for (image in note.images) {
			val updatedImage = image.copy(noteId = id)
			updateImages.add(updatedImage)
		}
		insertImagesUseCase(updateImages)
	}
}

class GetNotesUseCase @Inject constructor(private val noteRepository: NoteRepository) {

	operator fun invoke(tagName: String): Flow<List<Note>> {
		return noteRepository.getNotesByTag(tagName).map { result ->
			result.map { (note, images) ->
				Note.create(note.asDomainModel(), images.map { it.asDomainModel() })
			}
		}
	}
}

class GetNoteByIdUseCase @Inject constructor(private val noteRepository: NoteRepository) {

	suspend operator fun invoke(noteId: Long): Note {
		val result = noteRepository.getNoteById(noteId).entries.first()
		val note = result.key.asDomainModel()
		val images = result.value.map { it.asDomainModel() }
		return Note.create(note, images)
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
	private val insertImagesUseCase: InsertImagesUseCase) {

	suspend operator fun invoke(note: Note) {
		val updatedNote = note.copy(modificationDate = LocalDateTime.now())
		noteRepository.updateNote(updatedNote.asDatabaseEntity())
		insertImagesUseCase(note.images.filter { image -> image.id == 0 })
	}
}

class DeleteNotesUseCase @Inject constructor(
	private val noteRepository: NoteRepository,
	private val deleteImagesUseCase: DeleteImagesUseCase) {

	suspend operator fun invoke(notes: List<Note>) {
		for (note in notes) {
			deleteImagesUseCase(note.images)
			noteRepository.deleteNote(note.asDatabaseEntity())
		}
	}
}

class GetTrashedNotesUseCase @Inject constructor(private val noteRepository: NoteRepository) {
	operator fun invoke(): Flow<List<Note>> {
		return noteRepository.getTrashedNotes().map { result ->
			result.map { (note, images) ->
				Note.create(note.asDomainModel(), images.map { it.asDomainModel() })
			}
		}
	}
}

class EmptyTrashUseCase @Inject constructor(
	private val noteRepository: NoteRepository,
	private val deleteImagesUseCase: DeleteImagesUseCase) {

	suspend operator fun invoke(notes: List<Note>): Int {
		val entities = notes.map { it.asDatabaseEntity() }
		for (note in notes) {
			deleteImagesUseCase(note.images)
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

package net.azurewebsites.noties.domain

import net.azurewebsites.noties.core.ImageEntity
import net.azurewebsites.noties.core.Note
import net.azurewebsites.noties.core.NoteEntity
import net.azurewebsites.noties.data.NotebookDao
import net.azurewebsites.noties.data.ImageDao
import net.azurewebsites.noties.data.NoteDao
import javax.inject.Inject

class InsertNoteWithImagesUseCase @Inject constructor(
	private val notebookDao: NotebookDao,
	private val noteDao: NoteDao,
	private val imageDao: ImageDao) {

	suspend operator fun invoke(note: NoteEntity, images: List<ImageEntity>) {
		notebookDao.incrementNotebookNoteCount(note.notebookId)
		val id = noteDao.insertNote(note)
		for (image in images) {
			image.noteId = id
		}
		imageDao.insertImages(images)
	}
}

class GetNotesUseCase @Inject constructor(private val noteDao: NoteDao) {
	operator fun invoke(folderId: Int) = noteDao.getNotes(folderId)
}

class GetAllNotesUseCase @Inject constructor(private val noteDao: NoteDao) {
	operator fun invoke() = noteDao.getAllNotes()
}

class UpdateNoteUseCase @Inject constructor(
	private val noteDao: NoteDao,
	private val imageDao: ImageDao) {

	suspend operator fun invoke(note: NoteEntity, images: List<ImageEntity>) {
		noteDao.updateNote(note)
		imageDao.insertImages(images.filter { it.id == 0 })
	}
}

class DeleteNotesUseCase @Inject constructor(
	private val noteDao: NoteDao,
	private val imageDao: ImageDao) {

	suspend operator fun invoke(notes: List<Note>) {
		for (note in notes) {
			imageDao.deleteImages(note.images)
		}
		noteDao.deleteNotes(notes.map { it.entity })
	}
}

class MoveNoteToTrashUseCase @Inject constructor(
	private val noteDao: NoteDao,
	private val notebookDao: NotebookDao) {

	suspend operator fun invoke(note: NoteEntity) {
		notebookDao.decrementNotebookNoteCount(note.notebookId)
		val trashedNote = note.copy(notebookId = -1, isTrashed = true)
		noteDao.updateNote(trashedNote)
		notebookDao.incrementNotebookNoteCount(notebookId = -1)
	}
}

class RestoreNoteUseCase @Inject constructor(
	private val noteDao: NoteDao,
	private val notebookDao: NotebookDao) {

	suspend operator fun invoke(note: NoteEntity, notebookId: Int) {
		notebookDao.decrementNotebookNoteCount(notebookId = -1)
		val restoredNote = note.copy(notebookId = notebookId, isTrashed = false)
		noteDao.updateNote(restoredNote)
		notebookDao.incrementNotebookNoteCount(notebookId)
	}
}

class GetTrashedNotesUseCase @Inject constructor(private val noteDao: NoteDao) {
	operator fun invoke() = noteDao.getTrashedNotes()
}

class EmptyTrashUseCase @Inject constructor(
	private val noteDao: NoteDao,
	private val notebookDao: NotebookDao) {

	suspend operator fun invoke() {
		val numDeletedRows = noteDao.emptyTrash()
		repeat(numDeletedRows) {
			notebookDao.decrementNotebookNoteCount(notebookId = -1)
		}
	}
}

class LockNotesUseCase @Inject constructor(private val noteDao: NoteDao) {

	suspend operator fun invoke(notes: List<NoteEntity>) {
		for (note in notes) {
			if (!note.isProtected) {
				val updatedNote = note.copy(isProtected = true)
				noteDao.updateNote(updatedNote)
			}
		}
	}
}

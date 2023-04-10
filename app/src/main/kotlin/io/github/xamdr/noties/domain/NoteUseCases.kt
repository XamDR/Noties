package io.github.xamdr.noties.domain

import io.github.xamdr.noties.core.ImageEntity
import io.github.xamdr.noties.core.Note
import io.github.xamdr.noties.core.NoteEntity
import io.github.xamdr.noties.data.ImageDao
import io.github.xamdr.noties.data.NoteDao
import io.github.xamdr.noties.data.dao.TagDao
import javax.inject.Inject

class InsertNoteUseCase @Inject constructor(
	private val tagDao: TagDao,
	private val noteDao: NoteDao,
	private val imageDao: ImageDao
) {

	suspend operator fun invoke(note: NoteEntity, images: List<ImageEntity>) {
//		tagDao.incrementNotebookNoteCount(note.notebookId)
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
	private val imageDao: ImageDao,
	private val tagDao: TagDao) {

	suspend operator fun invoke(notes: List<Note>) {
		for (note in notes) {
			imageDao.deleteImages(note.images)
			noteDao.deleteNote(note.entity)
			if (!note.entity.isTrashed) {
//				tagDao.decrementNotebookNoteCount(note.entity.notebookId)
			}
		}
	}
}

class MoveNoteToTrashUseCase @Inject constructor(
	private val noteDao: NoteDao,
	private val tagDao: TagDao) {

	suspend operator fun invoke(note: NoteEntity) {
		val trashedNote = note.copy(isTrashed = true)
		noteDao.updateNote(trashedNote)
//		tagDao.decrementNotebookNoteCount(trashedNote.notebookId)
	}
}

class RestoreNoteUseCase @Inject constructor(
	private val noteDao: NoteDao,
	private val tagDao: TagDao) {

	suspend operator fun invoke(note: NoteEntity) {
//		val idExists = tagDao.getIfNotebookIdExists(note.notebookId)
//		val restoredNote = if (idExists) note.copy(isTrashed = false)
//		else note.copy(isTrashed = false, notebookId = 1)
//		noteDao.updateNote(restoredNote)
//		tagDao.incrementNotebookNoteCount(restoredNote.notebookId)
	}
}

class GetTrashedNotesUseCase @Inject constructor(private val noteDao: NoteDao) {
	operator fun invoke() = noteDao.getTrashedNotes()
}

class GetTrashedNotesSyncUseCase @Inject constructor(private val noteDao: NoteDao) {
	suspend operator fun invoke() = noteDao.getTrashedNotesSync()
}

class EmptyTrashUseCase @Inject constructor(
	private val noteDao: NoteDao,
	private val imageDao: ImageDao) {

	suspend operator fun invoke(): Int {
		imageDao.deleteImagesForTrashedNotes()
		return noteDao.deleteTrashedNotes()
	}
}

class LockNotesUseCase @Inject constructor(private val noteDao: NoteDao) {
	suspend operator fun invoke(notes: List<NoteEntity>) {
		for (note in notes) {
			if (!note.isProtected) {
				val protectedNote = note.copy(isProtected = true)
				noteDao.updateNote(protectedNote)
			}
		}
	}
}

class UnlockNotesUseCase @Inject constructor(private val noteDao: NoteDao) {
	suspend operator fun invoke(notes: List<NoteEntity>) {
		for (note in notes) {
			val unprotectedNote = note.copy(isProtected = false)
			noteDao.updateNote(unprotectedNote)
		}
	}
}

class PinNotesUseCase @Inject constructor(private val noteDao: NoteDao) {
	suspend operator fun invoke(notes: List<NoteEntity>) {
		for (note in notes) {
			if (!note.isPinned) {
				val pinnedNote = note.copy(isPinned = true)
				noteDao.updateNote(pinnedNote)
			}
		}
	}
}

class UnpinNotesUseCase @Inject constructor(private val noteDao: NoteDao) {
	suspend operator fun invoke(notes: List<NoteEntity>) {
		for (note in notes) {
			val unpinnedNote = note.copy(isPinned = false)
			noteDao.updateNote(unpinnedNote)
		}
	}
}

class MoveNotesUseCase @Inject constructor(
	private val noteDao: NoteDao,
	private val tagDao: TagDao) {

	suspend operator fun invoke(notes: List<NoteEntity>, notebookId: Int) {
		for (note in notes) {
			if (note.notebookId != notebookId) {
//				tagDao.decrementNotebookNoteCount(note.notebookId)
				val updatedNote = note.copy(notebookId = notebookId)
				noteDao.updateNote(updatedNote)
//				tagDao.incrementNotebookNoteCount(notebookId)
			}
		}
	}
}
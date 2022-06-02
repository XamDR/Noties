package net.azurewebsites.noties.domain

import net.azurewebsites.noties.core.ImageEntity
import net.azurewebsites.noties.core.NoteEntity
import net.azurewebsites.noties.data.FolderDao
import net.azurewebsites.noties.data.ImageDao
import net.azurewebsites.noties.data.NoteDao
import javax.inject.Inject

class InsertNoteWithImagesUseCase @Inject constructor(
	private val folderDao: FolderDao,
	private val noteDao: NoteDao, private val imageDao: ImageDao) {

	suspend operator fun invoke(note: NoteEntity, images: List<ImageEntity>) {
		folderDao.incrementNoteCount(note.folderId)
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

class DeleteNotesUseCase @Inject constructor(private val noteDao: NoteDao) {
	suspend operator fun invoke(notes: List<NoteEntity>) = noteDao.deleteNotes(notes)
}

class MoveNoteToTrashUseCase @Inject constructor(
	private val noteDao: NoteDao,
	private val folderDao: FolderDao) {

	suspend operator fun invoke(note: NoteEntity) {
		folderDao.decrementNoteCount(note.folderId)
		val trashedNote = note.copy(folderId = -1, isTrashed = true)
		noteDao.updateNote(trashedNote)
		folderDao.incrementNoteCount(folderId = -1)
	}
}

class RestoreNoteUseCase @Inject constructor(
	private val noteDao: NoteDao,
	private val folderDao: FolderDao) {

	suspend operator fun invoke(note: NoteEntity, folderId: Int) {
		folderDao.decrementNoteCount(folderId = -1)
		val restoredNote = note.copy(folderId = folderId, isTrashed = false)
		noteDao.updateNote(restoredNote)
		folderDao.incrementNoteCount(folderId)
	}
}

class GetTrashedNotesUseCase @Inject constructor(private val noteDao: NoteDao) {
	operator fun invoke() = noteDao.getTrashedNotes()
}

class EmptyTrashUseCase @Inject constructor(
	private val noteDao: NoteDao,
	private val folderDao: FolderDao) {

	suspend operator fun invoke() {
		val numDeletedRows = noteDao.emptyTrash()
		repeat(numDeletedRows) {
			folderDao.decrementNoteCount(folderId = -1)
		}
	}
}


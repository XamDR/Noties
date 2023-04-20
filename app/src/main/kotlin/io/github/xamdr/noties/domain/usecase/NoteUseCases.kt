package io.github.xamdr.noties.domain.usecase

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import io.github.xamdr.noties.data.repository.NoteRepository
import io.github.xamdr.noties.domain.model.Image
import io.github.xamdr.noties.domain.model.Note
import javax.inject.Inject

class InsertNoteUseCase @Inject constructor(
	private val noteRepository: NoteRepository,
	private val insertImagesUseCase: InsertImagesUseCase
) {

	suspend operator fun invoke(note: Note, images: List<Image>) {
		val id = noteRepository.insertNote(note.asDatabaseEntity())
		val updateImages = mutableListOf<Image>()
		for (image in images) {
			val updatedImage = image.copy(noteId = id)
			updateImages.add(updatedImage)
		}
		insertImagesUseCase(images)
	}
}

class GetNotesUseCase @Inject constructor(private val noteRepository: NoteRepository) {
	operator fun invoke(tagName: String): LiveData<List<Note>> {
		return noteRepository.getNotesByTag(tagName).asLiveData().map { list ->
			list.map { it.asDomainModel() }
		}
	}
}

class GetAllNotesUseCase @Inject constructor(private val noteRepository: NoteRepository) {
	operator fun invoke(): LiveData<List<Note>> {
		return noteRepository.getAllNotes().asLiveData().map { list ->
			list.map { it.asDomainModel() }
		}
	}
}

//
//class UpdateNoteUseCase @Inject constructor(
//	private val noteDao: NoteDao,
//	private val imageDao: ImageDao
//) {
//
//	suspend operator fun invoke(note: DatabaseNoteEntity, images: List<DatabaseImageEntity>) {
//		noteDao.updateNote(note)
//		imageDao.insertImages(images.filter { it.id == 0 })
//	}
//}
//
//class DeleteNotesUseCase @Inject constructor(
//	private val noteDao: NoteDao,
//	private val imageDao: ImageDao,
//	private val tagDao: TagDao) {
//
//	suspend operator fun invoke(notes: List<Note>) {
//		for (note in notes) {
//			imageDao.deleteImages(note.images)
//			noteDao.deleteNote(note.entity)
//			if (!note.entity.isTrashed) {
////				tagDao.decrementNotebookNoteCount(note.entity.notebookId)
//			}
//		}
//	}
//}
//
//class MoveNoteToTrashUseCase @Inject constructor(
//	private val noteDao: NoteDao,
//	private val tagDao: TagDao) {
//
//	suspend operator fun invoke(note: DatabaseNoteEntity) {
//		val trashedNote = note.copy(isTrashed = true)
//		noteDao.updateNote(trashedNote)
////		tagDao.decrementNotebookNoteCount(trashedNote.notebookId)
//	}
//}
//
//class RestoreNoteUseCase @Inject constructor(
//	private val noteDao: NoteDao,
//	private val tagDao: TagDao) {
//
//	suspend operator fun invoke(note: DatabaseNoteEntity) {
////		val idExists = tagDao.getIfNotebookIdExists(note.notebookId)
////		val restoredNote = if (idExists) note.copy(isTrashed = false)
////		else note.copy(isTrashed = false, notebookId = 1)
////		noteDao.updateNote(restoredNote)
////		tagDao.incrementNotebookNoteCount(restoredNote.notebookId)
//	}
//}
//
//class GetTrashedNotesUseCase @Inject constructor(private val noteDao: NoteDao) {
//	operator fun invoke() = noteDao.getTrashedNotes()
//}
//
//class GetTrashedNotesSyncUseCase @Inject constructor(private val noteDao: NoteDao) {
//	suspend operator fun invoke() = noteDao.getTrashedNotesSync()
//}
//
//class EmptyTrashUseCase @Inject constructor(
//	private val noteDao: NoteDao,
//	private val imageDao: ImageDao
//) {
//
//	suspend operator fun invoke(): Int {
//		imageDao.deleteImagesForTrashedNotes()
//		return noteDao.deleteTrashedNotes()
//	}
//}
//
//class LockNotesUseCase @Inject constructor(private val noteDao: NoteDao) {
//	suspend operator fun invoke(notes: List<DatabaseNoteEntity>) {
//		for (note in notes) {
//			if (!note.isProtected) {
//				val protectedNote = note.copy(isProtected = true)
//				noteDao.updateNote(protectedNote)
//			}
//		}
//	}
//}
//
//class UnlockNotesUseCase @Inject constructor(private val noteDao: NoteDao) {
//	suspend operator fun invoke(notes: List<DatabaseNoteEntity>) {
//		for (note in notes) {
//			val unprotectedNote = note.copy(isProtected = false)
//			noteDao.updateNote(unprotectedNote)
//		}
//	}
//}
//
//class PinNotesUseCase @Inject constructor(private val noteDao: NoteDao) {
//	suspend operator fun invoke(notes: List<DatabaseNoteEntity>) {
//		for (note in notes) {
//			if (!note.isPinned) {
//				val pinnedNote = note.copy(isPinned = true)
//				noteDao.updateNote(pinnedNote)
//			}
//		}
//	}
//}
//
//class UnpinNotesUseCase @Inject constructor(private val noteDao: NoteDao) {
//	suspend operator fun invoke(notes: List<DatabaseNoteEntity>) {
//		for (note in notes) {
//			val unpinnedNote = note.copy(isPinned = false)
//			noteDao.updateNote(unpinnedNote)
//		}
//	}
//}
//
//class MoveNotesUseCase @Inject constructor(
//	private val noteDao: NoteDao,
//	private val tagDao: TagDao) {
//
//	suspend operator fun invoke(notes: List<DatabaseNoteEntity>, notebookId: Int) {
//		for (note in notes) {
//			if (note.notebookId != notebookId) {
////				tagDao.decrementNotebookNoteCount(note.notebookId)
//				val updatedNote = note.copy(notebookId = notebookId)
//				noteDao.updateNote(updatedNote)
////				tagDao.incrementNotebookNoteCount(notebookId)
//			}
//		}
//	}
//}
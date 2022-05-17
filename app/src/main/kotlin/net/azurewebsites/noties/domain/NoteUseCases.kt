package net.azurewebsites.noties.domain

import net.azurewebsites.noties.core.NoteEntity
import net.azurewebsites.noties.data.FolderDao
import net.azurewebsites.noties.data.NoteDao
import javax.inject.Inject

class InsertNoteUseCase @Inject constructor(
	private val folderDao: FolderDao,
	private val noteDao: NoteDao) {

	suspend operator fun invoke(note: NoteEntity): Long {
		folderDao.incrementNoteCount(note.folderId)
		return noteDao.insertNote(note)
	}
}

class UpdateNoteUseCase constructor(private val noteDao: NoteDao) {
	suspend operator fun invoke(note: NoteEntity) = noteDao.updateNote(note)
}

class DeleteNotesUseCase constructor(private val noteDao: NoteDao) {
	suspend operator fun invoke(notes: List<NoteEntity>) = noteDao.deleteNotes(notes)
}


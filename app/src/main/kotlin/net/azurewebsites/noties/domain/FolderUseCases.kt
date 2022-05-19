package net.azurewebsites.noties.domain

import net.azurewebsites.noties.core.Folder
import net.azurewebsites.noties.core.FolderEntity
import net.azurewebsites.noties.data.FolderDao
import net.azurewebsites.noties.data.NoteDao
import javax.inject.Inject

class InsertFolderUseCase @Inject constructor(private val dao: FolderDao) {
	suspend operator fun invoke(folder: FolderEntity) = dao.insertFolder(folder)
}

class GetFoldersUseCase @Inject constructor(private val dao: FolderDao) {
	operator fun invoke() = dao.getFolders()
}

class UpdateFolderUseCase @Inject constructor(private val dao: FolderDao) {
	suspend operator fun invoke(folder: FolderEntity) = dao.updateFolder(folder)
}

class DeleteFolderAndMoveNotesToTrashUseCase @Inject constructor(
	private val folderDao: FolderDao,
	private val noteDao: NoteDao) {

	suspend operator fun invoke(folder: Folder) {
		for (note in folder.notes) {
			noteDao.moveNotesToTrash(note.folderId)
			folderDao.incrementNoteCount(folderId = -1)
		}
		folderDao.deleteFolders(listOf(folder.entity))
	}
}
package net.azurewebsites.noties.ui.folders

import net.azurewebsites.noties.domain.FolderEntity

interface FolderItemContextMenuListener {
	fun updateFolderName(folder: FolderEntity)
	fun deleteFolder(folder: FolderEntity)
	fun lockFolder(folder: FolderEntity)
}
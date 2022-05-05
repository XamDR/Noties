package net.azurewebsites.noties.ui.folders

import net.azurewebsites.noties.core.Folder
import net.azurewebsites.noties.core.FolderEntity

interface FolderItemContextMenuListener {
	fun updateFolderName(folder: FolderEntity, position: Int)
	fun deleteFolder(folder: Folder)
	fun lockFolder(folder: FolderEntity)
}
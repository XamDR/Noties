package net.azurewebsites.noties.ui.folders

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import net.azurewebsites.noties.data.FolderDao
import net.azurewebsites.noties.core.Folder
import net.azurewebsites.noties.core.FolderEntity
import net.azurewebsites.noties.ui.helpers.printError
import javax.inject.Inject

@HiltViewModel
class FoldersViewModel @Inject constructor(private val folderDao: FolderDao) : ViewModel() {

	val folders = folderDao.getFolders().asLiveData()

	val currentFolder = MutableLiveData(FolderEntity())

	fun upsertFolder(folder: FolderEntity) {
		viewModelScope.launch {
			try {
				if (folder.id == 0) {
					folderDao.insertFolder(folder)
				}
				else {
					folderDao.updateFolder(folder)
				}
				onResultCallback.invoke(true)
			}
			catch (e: SQLiteConstraintException) {
				printError("SQLITE_EX", e.message)
				onResultCallback.invoke(false)
			}
		}
	}

	fun deleteFolderAndNotes(folder: Folder) {
		viewModelScope.launch {
			folderDao.deleteFolderAndNotes(folder)
		}
	}

	fun setResultListener(callback: (succeed: Boolean) -> Unit) {
		onResultCallback = callback
	}

	private var onResultCallback: (succeed: Boolean) -> Unit = {}
}
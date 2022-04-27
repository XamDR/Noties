package net.azurewebsites.noties.ui.folders

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import net.azurewebsites.noties.domain.FolderEntity
import net.azurewebsites.noties.data.AppRepository
import net.azurewebsites.noties.ui.helpers.printError

class FoldersViewModel : ViewModel() {

	val directories = AppRepository.Instance.fetchDirectories().asLiveData()

	val currentDirectory = MutableLiveData(FolderEntity())

	fun upsertDirectory(folder: FolderEntity) {
		viewModelScope.launch {
			try {
				if (folder.id == 0) {
					AppRepository.Instance.insertDirectory(folder)
				}
				else {
					AppRepository.Instance.updateDirectory(folder)
				}
				onResultCallback.invoke(true)
			}
			catch (e: SQLiteConstraintException) {
				printError("SQLITE_EX", e.message)
				onResultCallback.invoke(false)
			}
		}
	}

	fun deleteDirectories(folders: List<FolderEntity>) {
		viewModelScope.launch {
			AppRepository.Instance.deleteDirectories(folders)
		}
	}

	fun setResultListener(callback: (succeed: Boolean) -> Unit) {
		onResultCallback = callback
	}

	private var onResultCallback: (succeed: Boolean) -> Unit = {}
}
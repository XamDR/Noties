package net.azurewebsites.eznotes.ui.folders

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import net.azurewebsites.eznotes.core.DirectoryEntity
import net.azurewebsites.eznotes.data.AppRepository
import net.azurewebsites.eznotes.ui.helpers.printError

class FolderListViewModel : ViewModel() {

	val directories = AppRepository.Instance.fetchDirectories().asLiveData()

	val currentDirectory = MutableLiveData(DirectoryEntity())

	fun upsertDirectory(directory: DirectoryEntity) {
		viewModelScope.launch {
			try {
				if (directory.id == 0) {
					AppRepository.Instance.insertDirectory(directory)
				}
				else {
					AppRepository.Instance.updateDirectory(directory)
				}
				onResultCallback.invoke(true)
			}
			catch (e: SQLiteConstraintException) {
				printError("SQLITE_EX", e.message)
				onResultCallback.invoke(false)
			}
		}
	}

	fun deleteDirectories(directories: List<DirectoryEntity>) {
		viewModelScope.launch {
			AppRepository.Instance.deleteDirectories(directories)
		}
	}

	fun setResultListener(callback: (succeed: Boolean) -> Unit) {
		onResultCallback = callback
	}

	private var onResultCallback: (succeed: Boolean) -> Unit = {}
}
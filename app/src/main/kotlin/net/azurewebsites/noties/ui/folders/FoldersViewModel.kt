package net.azurewebsites.noties.ui.folders

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import net.azurewebsites.noties.core.Folder
import net.azurewebsites.noties.core.FolderEntity
import net.azurewebsites.noties.domain.DeleteFolderAndMoveNotesToTrashUseCase
import net.azurewebsites.noties.domain.GetFoldersUseCase
import net.azurewebsites.noties.domain.InsertFolderUseCase
import net.azurewebsites.noties.domain.UpdateFolderUseCase
import net.azurewebsites.noties.ui.helpers.printError
import javax.inject.Inject

@HiltViewModel
class FoldersViewModel @Inject constructor(
	getFoldersUseCase: GetFoldersUseCase,
	private val insertFolderUseCase: InsertFolderUseCase,
	private val updateFolderUseCase: UpdateFolderUseCase,
	private val deleteFolderAndMoveNotesToTrashUseCase: DeleteFolderAndMoveNotesToTrashUseCase) : ViewModel() {

	val folders = getFoldersUseCase().asLiveData()

	val currentFolder = MutableLiveData(FolderEntity())

	fun upsertFolder(folder: FolderEntity) {
		viewModelScope.launch {
			try {
				if (folder.id == 0) {
					insertFolderUseCase(folder)
				}
				else {
					updateFolderUseCase(folder)
				}
				onResultCallback(true)
			}
			catch (e: SQLiteConstraintException) {
				printError(TAG, e.message)
				onResultCallback(false)
			}
		}
	}

	fun deleteFolderAndNotes(folder: Folder) {
		viewModelScope.launch {
			deleteFolderAndMoveNotesToTrashUseCase(folder)
		}
	}

	fun setResultListener(callback: (succeed: Boolean) -> Unit) {
		onResultCallback = callback
	}

	private var onResultCallback: (succeed: Boolean) -> Unit = {}

	private companion object {
		private const val TAG = "SQLITE"
	}
}
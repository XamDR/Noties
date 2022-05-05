package net.azurewebsites.noties.ui.folders

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.*
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
	private val deleteFolderAndMoveNotesToTrashUseCase: DeleteFolderAndMoveNotesToTrashUseCase,
	private val savedState: SavedStateHandle) : ViewModel() {

	val folders = getFoldersUseCase().asLiveData()

	var position = savedState.get<Int>(POSITION) ?: 0
		set(value) {
			field = value
			savedState.set(POSITION, value)
		}

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
		private const val POSITION = "position"
		private const val TAG = "SQLITE"
	}
}
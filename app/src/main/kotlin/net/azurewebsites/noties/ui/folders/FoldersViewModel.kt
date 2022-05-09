package net.azurewebsites.noties.ui.folders

import android.text.Editable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.azurewebsites.noties.core.Folder
import net.azurewebsites.noties.core.FolderEntity
import net.azurewebsites.noties.domain.DeleteFolderAndMoveNotesToTrashUseCase
import net.azurewebsites.noties.domain.GetFoldersUseCase
import net.azurewebsites.noties.domain.InsertFolderUseCase
import net.azurewebsites.noties.domain.UpdateFolderUseCase
import javax.inject.Inject

@HiltViewModel
class FoldersViewModel @Inject constructor(
	getFoldersUseCase: GetFoldersUseCase,
	private val insertFolderUseCase: InsertFolderUseCase,
	private val updateFolderUseCase: UpdateFolderUseCase,
	private val deleteFolderAndMoveNotesToTrashUseCase: DeleteFolderAndMoveNotesToTrashUseCase) : ViewModel() {

	init {
		viewModelScope.launch {
			folders.collect { names += it.map { folder -> folder.entity.name } }
		}
	}

	val folders = getFoldersUseCase()
	private val names = mutableListOf<String>()

	private val _folderName = MutableStateFlow(String.Empty)
	val folderName: StateFlow<String> = _folderName

	private val _result: MutableStateFlow<Result> = MutableStateFlow(Result.EmptyName)
	val result: StateFlow<Result> = _result

	fun updateFolderName(s: Editable) {
		_folderName.update { s.toString() }
		if (_folderName.value.isNotEmpty()) {
			if (names.contains(_folderName.value)) {
				_result.update { Result.ErrorDuplicateName }
			}
			else {
				_result.update { Result.EditingName }
			}
		}
		else {
			_result.update { Result.EmptyName }
		}
	}

	fun insertFolder(folder: FolderEntity) {
		viewModelScope.launch {
			insertFolderUseCase(folder)
			_result.update { Result.Success }
		}
	}

	fun updateFolder(folder: FolderEntity) {
		viewModelScope.launch {
			updateFolderUseCase(folder)
			_result.update { Result.Success }
		}
	}

	fun deleteFolderAndNotes(folder: Folder) {
		viewModelScope.launch { deleteFolderAndMoveNotesToTrashUseCase(folder) }
	}

	fun reset() {
		_folderName.update { String.Empty }
		_result.update { Result.EmptyName }
	}
}

sealed class Result {
	object EmptyName : Result()
	object EditingName : Result()
	object Success : Result()
	object ErrorDuplicateName : Result()
}
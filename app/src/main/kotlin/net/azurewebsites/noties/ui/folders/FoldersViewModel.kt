package net.azurewebsites.noties.ui.folders

import android.text.Editable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
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
			getFoldersUseCase().collect { names += it.map { folder -> folder.entity.name } }
		}
	}

	val folders = getFoldersUseCase().asLiveData()
	private val names = mutableListOf<String>()

	private val currentFolder = MutableStateFlow(FolderEntity())
	val selectedFolder = currentFolder.asLiveData()

	private val uiState = MutableStateFlow(FolderUiState())
	val folderUiState: StateFlow<FolderUiState> = uiState

	private val nameState: MutableStateFlow<InputNameState> = MutableStateFlow(InputNameState.EmptyName)
	val inputNameState = nameState.asLiveData()

	fun updateCurrentFolder(folder: FolderEntity) = currentFolder.update { folder }

	fun updateFolderState(folderUiState: FolderUiState) = uiState.update { folderUiState }

	fun updateFolderName(s: Editable) {
		uiState.update { uiState.value.copy(name = s.toString()) }

		if (uiState.value.name.isNotEmpty()) {
			if (names.contains(uiState.value.name)) {
				when (uiState.value.operation) {
					Operation.Insert -> nameState.update { InputNameState.ErrorDuplicateName }
					Operation.Update -> {
						nameState.update { InputNameState.UpdatingName }
						uiState.update { uiState.value.copy(operation = Operation.Insert) }
					}
				}
			}
			else {
				nameState.update { InputNameState.EditingName }
			}
		}
		else {
			nameState.update { InputNameState.EmptyName }
		}
	}

	fun insertFolder(folder: FolderEntity) {
		viewModelScope.launch { insertFolderUseCase(folder) }
	}

	fun updateFolder(folder: FolderEntity) {
		viewModelScope.launch { updateFolderUseCase(folder) }
	}

	fun deleteFolderAndNotes(folder: Folder) {
		viewModelScope.launch { deleteFolderAndMoveNotesToTrashUseCase(folder) }
	}

	fun reset() {
		uiState.update { FolderUiState() }
		nameState.update { InputNameState.EmptyName }
	}
}
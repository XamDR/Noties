package net.azurewebsites.noties.ui.notebooks

import android.text.Editable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.azurewebsites.noties.core.Notebook
import net.azurewebsites.noties.core.NotebookEntity
import net.azurewebsites.noties.domain.DeleteNotebookAndMoveNotesToTrashUseCase
import net.azurewebsites.noties.domain.GetNotebooksUseCase
import net.azurewebsites.noties.domain.InsertNotebookUseCase
import net.azurewebsites.noties.domain.UpdateNotebookUseCase
import javax.inject.Inject

@HiltViewModel
class NotebooksViewModel @Inject constructor(
	getNotebooksUseCase: GetNotebooksUseCase,
	private val insertNotebookUseCase: InsertNotebookUseCase,
	private val updateNotebookUseCase: UpdateNotebookUseCase,
	private val deleteNotebookAndMoveNotesToTrashUseCase: DeleteNotebookAndMoveNotesToTrashUseCase,
	private val savedState: SavedStateHandle) : ViewModel() {

	init {
		viewModelScope.launch {
			getNotebooksUseCase().collect { names += it.map { folder -> folder.entity.name } }
		}
	}

	private var _shouldNavigate = savedState.get<Boolean>(KEY) ?: false
		set(value) {
			field = value
			savedState.set(KEY, value)
		}
	val shouldNavigate get() = _shouldNavigate

	val notebooks = getNotebooksUseCase().asLiveData()
	private val names = mutableListOf<String>()

	private val uiState = MutableStateFlow(NotebookUiState())
	val notebookUiState = uiState.asStateFlow()

	private val nameState: MutableStateFlow<InputNameState> = MutableStateFlow(InputNameState.EmptyName)
	val inputNameState = nameState.asLiveData()

	fun updateNotebookState(notebookUiState: NotebookUiState) = uiState.update { notebookUiState }

	fun updateNotebookName(s: Editable) {
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

	fun insertNotebook(notebook: NotebookEntity) {
		viewModelScope.launch { insertNotebookUseCase(notebook) }
	}

	fun updateNotebook(notebook: NotebookEntity) {
		viewModelScope.launch { updateNotebookUseCase(notebook) }
	}

	fun deleteNotebookAndNotes(notebook: Notebook) {
		viewModelScope.launch {
			deleteNotebookAndMoveNotesToTrashUseCase(notebook)
			_shouldNavigate = true
		}
	}

	fun reset() {
		uiState.update { NotebookUiState() }
		nameState.update { InputNameState.EmptyName }
	}

	private companion object {
		private const val KEY = "shouldNavigate"
	}
}
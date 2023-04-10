package io.github.xamdr.noties.ui.notebooks

import android.text.Editable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.xamdr.noties.data.entity.NotebookEntityCrossRefLocal
import io.github.xamdr.noties.data.entity.NotebookEntityLocal
import io.github.xamdr.noties.domain.*
import io.github.xamdr.noties.domain.interactor.NotebookInteractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotebooksViewModel @Inject constructor(
	getNotebooksUseCase: GetNotebooksUseCase,
	private val createNotebookUseCase: CreateNotebookUseCase,
	private val updateNotebookUseCase: UpdateNotebookUseCase,
	private val deleteNotebookAndMoveNotesToTrashUseCase: DeleteNotebookAndMoveNotesToTrashUseCase,
	private val getNotebooksUseCaseAsync: GetNotebooksUseCaseAsync,
	private val savedState: SavedStateHandle) : ViewModel() {

	init {
		viewModelScope.launch {
			getNotebooksUseCase()
				.flowOn(Dispatchers.Main)
				.conflate()
				.collect { names += it.map { notebook -> notebook.entity.name } }
		}
	}

	private var _shouldNavigate = savedState.get<Boolean>(KEY) ?: false
		set(value) {
			field = value
			savedState[KEY] = value
		}
	val shouldNavigate get() = _shouldNavigate

	val notebooks = getNotebooksUseCase().asLiveData()
	private val names = mutableSetOf<String>()

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

	suspend fun getNotebooks(): List<NotebookEntityLocal> {
		return getNotebooksUseCaseAsync()
	}

	fun createNotebook(notebook: io.github.xamdr.noties.domain.model.Notebook) {
		viewModelScope.launch { createNotebookUseCase(notebook.toEntityLocal()) }
//		viewModelScope.launch { interactor.createNotebook(notebook) }
	}

	fun updateNotebook(notebook: NotebookEntityLocal) {
		viewModelScope.launch { updateNotebookUseCase(notebook) }
	}

	fun deleteNotebookAndNotes(notebook: NotebookEntityCrossRefLocal, action: () -> Unit) {
		viewModelScope.launch {
			deleteNotebookAndMoveNotesToTrashUseCase(notebook)
			names.remove(notebook.entity.name)
			_shouldNavigate = true
			action()
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
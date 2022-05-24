package net.azurewebsites.noties.ui.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import net.azurewebsites.noties.domain.EmptyTrashUseCase
import net.azurewebsites.noties.domain.GetTrashedNotesUseCase
import javax.inject.Inject

@HiltViewModel
class RecycleBinViewModel @Inject constructor(
	private val getTrashedNotesUseCase: GetTrashedNotesUseCase,
	private val emptyTrashUseCase: EmptyTrashUseCase) : ViewModel() {

	fun getTrashedNotes() = getTrashedNotesUseCase().asLiveData()

	fun emptyRecycleBin() {
		viewModelScope.launch { emptyTrashUseCase() }
	}
}
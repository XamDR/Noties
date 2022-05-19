package net.azurewebsites.noties.ui.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import net.azurewebsites.noties.domain.GetTrashedNotesUseCase
import javax.inject.Inject

@HiltViewModel
class RecycleBinViewModel @Inject constructor(
	private val getTrashedNotesUseCase: GetTrashedNotesUseCase) : ViewModel() {

	fun getTrashedNotes() = getTrashedNotesUseCase().asLiveData()
}
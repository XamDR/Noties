package net.azurewebsites.noties.ui.notebooks

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class InputNameState {
	object EmptyName : InputNameState()
	object EditingName : InputNameState()
	object UpdatingName: InputNameState()
	object ErrorDuplicateName : InputNameState()
}

@Parcelize
sealed class Operation : Parcelable {
	object Insert: Operation()
	object Update: Operation()
}

@Parcelize
data class NotebookUiState(
	val id: Int = 0,
	val name: String = String.Empty,
	val operation: Operation = Operation.Insert) : Parcelable

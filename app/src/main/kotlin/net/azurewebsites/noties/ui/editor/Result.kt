package net.azurewebsites.noties.ui.editor

sealed class Result {
	object NoteSaved : Result()
	object NoteUpdated : Result()
	object EmptyNote: Result()
}

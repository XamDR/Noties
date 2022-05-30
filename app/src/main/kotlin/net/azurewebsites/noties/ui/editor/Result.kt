package net.azurewebsites.noties.ui.editor

sealed class Result {
	object SuccesfulInsert : Result()
	object SuccesfulUpdate : Result()
	object Nothing : Result()
}

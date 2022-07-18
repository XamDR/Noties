package net.azurewebsites.noties.core

sealed class DataItem {
	data class TodoItem(var content: String = String.Empty, var done: Boolean = false) : DataItem()
	object Footer : DataItem()
}
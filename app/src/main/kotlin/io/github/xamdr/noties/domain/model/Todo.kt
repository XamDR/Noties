package io.github.xamdr.noties.domain.model

sealed class Todo {
	data class TodoItem(var content: String = String.Empty, var done: Boolean = false) : Todo()
	object Footer : Todo()
}
package io.github.xamdr.noties.domain.model

sealed class Task {
	data class Item(var content: String = String.Empty, var done: Boolean = false) : Task()
	object Footer : Task()
}
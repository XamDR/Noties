package net.azurewebsites.noties.ui.notes

sealed class SortMode(val value: Int) {
	object Content : SortMode(value = 0)
	object LastEdit : SortMode(value = 1)
	object Title : SortMode(value = 2)
}

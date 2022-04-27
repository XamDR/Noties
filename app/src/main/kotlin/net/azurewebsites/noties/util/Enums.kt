package net.azurewebsites.noties.util

enum class LayoutType(val spanCount: Int) {
	Linear(spanCount = 1),
	Grid(spanCount = 2);

	companion object {
		fun from(value: Int): LayoutType? = values().firstOrNull { it.spanCount == value }
	}
}

enum class SortMode(val value: Int) {
	Content(value = 0),
	LastEdit(value = 1),
	Title(value = 2);

	companion object {
		fun from(value: Int): SortMode? = values().firstOrNull{ it.value == value }
	}
}

enum class EditorStyle(val style: String) {
	Blank(style = "blank"),
	Striped(style = "striped");

	companion object {
		fun from(value: String): EditorStyle? = values().firstOrNull{ it.style == value }
	}
}

package io.github.xamdr.noties.ui.tags

sealed interface TagNameState {
	data object EmptyOrUpdatingName : TagNameState
	data object EditingName : TagNameState
	data object ErrorDuplicateName: TagNameState
}

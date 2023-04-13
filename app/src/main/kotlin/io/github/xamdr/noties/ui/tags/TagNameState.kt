package io.github.xamdr.noties.ui.tags

sealed interface TagNameState {
	object EmptyOrUpdatingName : TagNameState
	object EditingName : TagNameState
	object ErrorDuplicateName: TagNameState
}

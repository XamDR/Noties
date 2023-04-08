package io.github.xamdr.noties.ui.image

sealed class AltTextState {
	object EmptyDescription : AltTextState()
	object EditingDescription: AltTextState()
}

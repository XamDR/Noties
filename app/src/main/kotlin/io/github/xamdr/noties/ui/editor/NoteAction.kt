package io.github.xamdr.noties.ui.editor

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class NoteAction : Parcelable {
	data object InsertNote : NoteAction()
	data object UpdateNote : NoteAction()
	data object DeleteEmptyNote : NoteAction()
	data object NoAction: NoteAction()
}
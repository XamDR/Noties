package io.github.xamdr.noties.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class Task : Parcelable {
	data class Item(var content: String = String.Empty, var done: Boolean = false) : Task()
	data object Footer : Task()
}
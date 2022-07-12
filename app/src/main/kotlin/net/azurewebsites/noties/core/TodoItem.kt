package net.azurewebsites.noties.core

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class DataItem : Parcelable {
	data class TodoItem(var content: String = String.Empty, var done: Boolean = false) : DataItem()
	object Footer : DataItem()
}
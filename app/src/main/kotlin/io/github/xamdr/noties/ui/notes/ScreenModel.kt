package io.github.xamdr.noties.ui.notes

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class ScreenType : Parcelable {
	data object Main : ScreenType()
	data object Reminder : ScreenType()
	data object Protected : ScreenType()
	data object Tag : ScreenType()
	data object Archived : ScreenType()
	data object Trash : ScreenType()
}

@Parcelize
data class Screen(
	val id: Int = 0,
	val type: ScreenType = ScreenType.Main,
	val title: String = String.Empty
) : Parcelable
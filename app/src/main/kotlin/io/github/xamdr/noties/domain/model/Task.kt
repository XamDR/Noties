package io.github.xamdr.noties.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
sealed class Task : Parcelable {
	data class Item(
		val id: String = UUID.randomUUID().toString(),
		val content: String = String.Empty,
		val done: Boolean = false
	) : Task()

	data object Footer : Task()
}
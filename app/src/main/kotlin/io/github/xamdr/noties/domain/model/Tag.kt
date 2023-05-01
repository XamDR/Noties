package io.github.xamdr.noties.domain.model

import android.os.Parcelable
import io.github.xamdr.noties.data.entity.tag.DatabaseTagEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class Tag(val id: Int = 0, val name: String = String.Empty) : Parcelable {

	fun asDatabaseEntity(): DatabaseTagEntity {
		return DatabaseTagEntity(
			id = this.id,
			name = this.name
		)
	}
}

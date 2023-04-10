package io.github.xamdr.noties.domain.model

import io.github.xamdr.noties.data.entity.tag.DatabaseTagEntity
import java.io.Serializable

data class Tag(val id: Int = 0, val name: String = String.Empty) : Serializable {

	fun asDatabaseEntity(): DatabaseTagEntity {
		return DatabaseTagEntity(
			id = this.id,
			name = this.name
		)
	}
}

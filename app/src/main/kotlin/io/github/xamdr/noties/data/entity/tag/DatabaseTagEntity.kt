package io.github.xamdr.noties.data.entity.tag

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.xamdr.noties.domain.model.Tag

@Entity(tableName = "Tags", indices = [Index(value = ["name"], unique = true)])
data class DatabaseTagEntity(
	@PrimaryKey(autoGenerate = true) val id: Int = 0,
	val name: String = String.Empty,
	val count: Int = 0
) {

	fun asDomainModel(): Tag {
		return Tag(
			id = this.id,
			name = this.name,
			count = this.count
		)
	}
}

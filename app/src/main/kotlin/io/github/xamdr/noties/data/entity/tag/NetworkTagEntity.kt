package io.github.xamdr.noties.data.entity.tag

data class NetworkTagEntity(
	val id: Int = 0,
	val name: String = String.Empty
) {

	fun asDatabaseEntity(): DatabaseTagEntity {
		return DatabaseTagEntity(
			id = this.id,
			name = this.name
		)
	}
}

package io.github.xamdr.noties.data.entity.tag

data class NetworkTagEntity(
	val id: Int = 0,
	val name: String = String.Empty,
	val count: Int = 0
) {

	fun asDatabaseEntity(): DatabaseTagEntity {
		return DatabaseTagEntity(
			id = this.id,
			name = this.name,
			count = this.count
		)
	}
}

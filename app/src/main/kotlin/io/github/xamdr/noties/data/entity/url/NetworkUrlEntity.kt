package io.github.xamdr.noties.data.entity.url

data class NetworkUrlEntity(
	val id: Int = 0,
	val source: String = String.Empty,
	val host: String? = null,
	val title: String? = null,
	val image: String? = null,
	val noteId: Long = 0
) {

	fun asDatabaseEntity(): DatabaseUrlEntity {
		return DatabaseUrlEntity(
			id = id,
			source = source,
			title = title,
			host = host,
			image = image,
			noteId = noteId
		)
	}
}

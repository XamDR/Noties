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
			id = this.id,
			source = this.source,
			title = this.title,
			host = this.host,
			image = this.image,
			noteId = this.noteId
		)
	}
}

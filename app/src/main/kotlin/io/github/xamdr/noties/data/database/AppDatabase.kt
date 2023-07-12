package io.github.xamdr.noties.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.xamdr.noties.data.dao.MediaDao
import io.github.xamdr.noties.data.dao.NoteDao
import io.github.xamdr.noties.data.dao.TagDao
import io.github.xamdr.noties.data.entity.media.DatabaseMediaItemEntity
import io.github.xamdr.noties.data.entity.note.DatabaseNoteEntity
import io.github.xamdr.noties.data.entity.tag.DatabaseTagEntity

@Database(entities = [
	DatabaseTagEntity::class,
	DatabaseNoteEntity::class,
	DatabaseMediaItemEntity::class], version = 1)
@TypeConverters(
	LocalDateTimeToStringConverter::class,
	StringArrayToStringConverter::class,
	UriToStringConverter::class,
	MediaTypeToStringConverter::class
)
abstract class AppDatabase : RoomDatabase() {

	abstract fun mediaDao(): MediaDao

	abstract fun noteDao(): NoteDao

	abstract fun tagDao(): TagDao
}

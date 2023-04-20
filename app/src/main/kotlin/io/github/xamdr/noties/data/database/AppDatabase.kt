package io.github.xamdr.noties.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.xamdr.noties.data.dao.ImageDao
import io.github.xamdr.noties.data.dao.NoteDao
import io.github.xamdr.noties.data.entity.image.DatabaseImageEntity
import io.github.xamdr.noties.data.entity.note.DatabaseNoteEntity
import io.github.xamdr.noties.data.dao.TagDao
import io.github.xamdr.noties.data.entity.tag.DatabaseTagEntity

@Database(entities = [DatabaseTagEntity::class, DatabaseImageEntity::class, DatabaseNoteEntity::class], version = 1)
@TypeConverters(
	ZonedDateTimeToStringConverter::class,
	StringArrayToStringConverter::class,
	UriToStringConverter::class
)
abstract class AppDatabase : RoomDatabase() {

	abstract fun imageDao(): ImageDao

	abstract fun noteDao(): NoteDao

	abstract fun tagDao(): TagDao
}

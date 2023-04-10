package io.github.xamdr.noties.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.xamdr.noties.core.ImageEntity
import io.github.xamdr.noties.core.NoteEntity
import io.github.xamdr.noties.data.dao.TagDao
import io.github.xamdr.noties.data.entity.tag.DatabaseTagEntity

@Database(entities = [DatabaseTagEntity::class, ImageEntity::class, NoteEntity::class], version = 1)
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

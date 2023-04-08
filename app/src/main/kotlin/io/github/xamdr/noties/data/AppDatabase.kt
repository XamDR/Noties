package io.github.xamdr.noties.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.xamdr.noties.core.ImageEntity
import io.github.xamdr.noties.core.NoteEntity
import io.github.xamdr.noties.core.NotebookEntity

@Database(entities = [NotebookEntity::class, ImageEntity::class, NoteEntity::class], version = 1)
@TypeConverters(
	ZonedDateTimeToStringConverter::class,
	StringArrayToStringConverter::class,
	UriToStringConverter::class
)
abstract class AppDatabase : RoomDatabase() {
	abstract fun notebookDao(): NotebookDao
	abstract fun imageDao(): ImageDao
	abstract fun noteDao(): NoteDao
}

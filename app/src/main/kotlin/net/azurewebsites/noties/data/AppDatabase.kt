package net.azurewebsites.noties.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import net.azurewebsites.noties.core.NotebookEntity
import net.azurewebsites.noties.core.ImageEntity
import net.azurewebsites.noties.core.NoteEntity

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

package net.azurewebsites.noties.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import net.azurewebsites.noties.domain.FolderEntity
import net.azurewebsites.noties.domain.ImageEntity
import net.azurewebsites.noties.domain.NoteEntity

@Database(entities = [FolderEntity::class, ImageEntity::class, NoteEntity::class], version = 1)
@TypeConverters(
	ZonedDateTimeToStringConverter::class,
	StringArrayToStringConverter::class,
	UriToStringConverter::class
)
abstract class AppDatabase : RoomDatabase() {
	abstract fun directoryDao(): FolderDao
	abstract fun mediaItemDao(): ImageDao
	abstract fun noteDao(): NoteDao
}

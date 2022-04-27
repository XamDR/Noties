package net.azurewebsites.eznotes.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import net.azurewebsites.eznotes.core.DirectoryEntity
import net.azurewebsites.eznotes.core.MediaItemEntity
import net.azurewebsites.eznotes.core.NoteEntity

@Database(entities = [DirectoryEntity::class, MediaItemEntity::class, NoteEntity::class], version = 1)
@TypeConverters(
	ZonedDateTimeToStringConverter::class,
	StringArrayToStringConverter::class,
	UriToStringConverter::class
)
abstract class AppDatabase : RoomDatabase() {
	abstract fun directoryDao(): DirectoryDao
	abstract fun mediaItemDao(): MediaItemDao
	abstract fun noteDao(): NoteDao
}

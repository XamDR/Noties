package net.azurewebsites.eznotes.data

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import net.azurewebsites.eznotes.core.DirectoryEntity
import net.azurewebsites.eznotes.core.MediaItemEntity
import net.azurewebsites.eznotes.core.Note

class AppRepository private constructor(context: Context) {

//	fun fetchNotesByContent(contentToSearch: String)
//			= noteDao.getNotesByContent(contentToSearch).flowOn(Dispatchers.Main).conflate()

	suspend fun insertDirectory(directory: DirectoryEntity) = directoryDao.insertDirectory(directory)

	fun fetchDirectories() = directoryDao.getDirectories().flowOn(Dispatchers.Main).conflate()

	suspend fun updateDirectory(directory: DirectoryEntity) = directoryDao.updateDirectory(directory)

	suspend fun deleteDirectories(directories: List<DirectoryEntity>)
		= noteDao.deleteDirectoriesAndNotes(directories)

	suspend fun updateMediaItem(mediaItem: MediaItemEntity) = mediaItemDao.updateMediaItem(mediaItem)

	suspend fun deleteMediaItem(mediaItem: MediaItemEntity) = mediaItemDao.deleteMediaItem(mediaItem)

	suspend fun insertNote(directoryId: Int, note: Note, mediaItems: List<MediaItemEntity>)
		= mediaItemDao.insertNoteWithMediaItems(directoryId, note, mediaItems)

	fun fetchNotes(directoryId: Int): Flow<List<Note>>
		= mediaItemDao.getNoteWithMediaItems(directoryId).flowOn(Dispatchers.Main).conflate()

	suspend fun updateNote(note: Note) = mediaItemDao.updateNote(note)

	suspend fun deleteNotes(directoryId: Int, notes: List<Note>)
		= mediaItemDao.deleteNotes(directoryId, notes)

	private val database = Room
		.databaseBuilder(context.applicationContext, AppDatabase::class.java, DATABASE_NAME)
		.build()

	private val directoryDao = database.directoryDao()
	private val mediaItemDao = database.mediaItemDao()
	private val noteDao = database.noteDao()

	companion object {
		private const val DATABASE_NAME = "App.db"

		val Instance: AppRepository
			get() = instance ?: throw IllegalStateException("Repository service must be initialized")
		private var instance: AppRepository? = null

		fun initialize(context: Context) {
			if (instance == null) {
				instance = AppRepository(context)
			}
		}
	}
}

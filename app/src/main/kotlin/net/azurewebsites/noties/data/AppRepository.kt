package net.azurewebsites.noties.data

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import net.azurewebsites.noties.domain.FolderEntity
import net.azurewebsites.noties.domain.ImageEntity
import net.azurewebsites.noties.domain.Note

class AppRepository private constructor(context: Context) {

//	fun fetchNotesByContent(contentToSearch: String)
//			= noteDao.getNotesByContent(contentToSearch).flowOn(Dispatchers.Main).conflate()

	suspend fun insertDirectory(folder: FolderEntity) = directoryDao.insertDirectory(folder)

	fun fetchDirectories() = directoryDao.getDirectories().flowOn(Dispatchers.Main).conflate()

	suspend fun updateDirectory(folder: FolderEntity) = directoryDao.updateDirectory(folder)

	suspend fun deleteDirectories(folders: List<FolderEntity>)
		= noteDao.deleteDirectoriesAndNotes(folders)

	suspend fun updateMediaItem(image: ImageEntity) = mediaItemDao.updateMediaItem(image)

	suspend fun deleteMediaItem(image: ImageEntity) = mediaItemDao.deleteMediaItem(image)

	suspend fun insertNote(directoryId: Int, note: Note, images: List<ImageEntity>)
		= mediaItemDao.insertNoteWithMediaItems(directoryId, note, images)

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

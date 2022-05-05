package net.azurewebsites.noties

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.asLiveData
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
//import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
//import net.azurewebsites.noties.core.FolderEntity
import net.azurewebsites.noties.data.AppDatabase
import net.azurewebsites.noties.data.FolderDao
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DirectoryDaoTest {
	private lateinit var directoryDao: FolderDao
	private lateinit var db: AppDatabase

	@get:Rule
	val rule: TestRule = InstantTaskExecutorRule()

	@Before
	fun createDb() {
		val context = ApplicationProvider.getApplicationContext<Context>()
		// Using an in-memory database because the information stored here disappears when the
		// process is killed.
		db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
			// Allowing main thread queries, just for testing.
			.allowMainThreadQueries()
			.build()
		directoryDao = db.folderDao()
	}

	@After
	@Throws(IOException::class)
	fun closeDb() {
		db.close()
	}

	@Test
	fun insert_directory() = runBlocking {
//		val directory = FolderEntity(name = "Test")
//		directoryDao.insertFolder(directory)
//		val allDirectories = directoryDao.getFolders().asLiveData().getValueForTesting() ?: return@runBlocking
//		assertTrue(allDirectories.contains(directory))
	}

	@Test
	fun get_directories() = runBlocking {
		val allDirectories = directoryDao.getFolders().asLiveData().getValueForTesting()
		assertNotNull(allDirectories)
	}

	@Test
	fun updateDirectory() = runBlocking {
//		val directory = FolderEntity(name = "Test")
//		directoryDao.insertFolder(directory)
//		directory.name = "Test updated"
//		directoryDao.updateFolder(directory)
//		val allDirectories = directoryDao.getFolders().first()
//		assertEquals(allDirectories[0].name, directory.name)
	}

	@Test
	fun delete_directory() = runBlocking {
//		val directory = FolderEntity(name = "Test")
//		directoryDao.insertFolder(directory)
//		directoryDao.deleteFolders(listOf(directory))
//		val allDirectories = directoryDao.getFolders().asLiveData().getValueForTesting() ?: return@runBlocking
//		assertFalse(allDirectories.contains(directory))
	}
}
package net.azurewebsites.noties

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.asLiveData
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import net.azurewebsites.noties.domain.FolderEntity
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
		directoryDao = db.directoryDao()
	}

	@After
	@Throws(IOException::class)
	fun closeDb() {
		db.close()
	}

	@Test
	fun insert_directory() = runBlocking {
		val directory = FolderEntity(name = "Test")
		directoryDao.insertDirectory(directory)
		val allDirectories = directoryDao.getDirectories().asLiveData().getValueForTesting() ?: return@runBlocking
		assertTrue(allDirectories.contains(directory))
	}

	@Test
	fun get_directories() = runBlocking {
		val allDirectories = directoryDao.getDirectories().asLiveData().getValueForTesting()
		assertNotNull(allDirectories)
	}

	@Test
	fun updateDirectory() = runBlocking {
		val directory = FolderEntity(name = "Test")
		directoryDao.insertDirectory(directory)
		directory.name = "Test updated"
		directoryDao.updateDirectory(directory)
		val allDirectories = directoryDao.getDirectories().first()
		assertEquals(allDirectories[0].name, directory.name)
	}

	@Test
	fun delete_directory() = runBlocking {
		val directory = FolderEntity(name = "Test")
		directoryDao.insertDirectory(directory)
		directoryDao.deleteDirectories(listOf(directory))
		val allDirectories = directoryDao.getDirectories().asLiveData().getValueForTesting() ?: return@runBlocking
		assertFalse(allDirectories.contains(directory))
	}
}
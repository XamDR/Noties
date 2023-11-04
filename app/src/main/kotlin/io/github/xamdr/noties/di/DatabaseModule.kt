package io.github.xamdr.noties.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.xamdr.noties.data.dao.MediaDao
import io.github.xamdr.noties.data.dao.NoteDao
import io.github.xamdr.noties.data.dao.TagDao
import io.github.xamdr.noties.data.database.AppDatabase
import io.github.xamdr.noties.data.repository.MediaItemRepository
import io.github.xamdr.noties.data.repository.NoteRepository
import io.github.xamdr.noties.data.repository.TagRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

	@Singleton
	@Provides
	fun providesAppDatabase(@ApplicationContext context: Context) =
		Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
			.build()

	@Provides
	fun providesTagDao(db: AppDatabase) = db.tagDao()

	@Provides
	fun providesNoteDao(db: AppDatabase) = db.noteDao()

	@Provides
	fun providesMediaDao(db: AppDatabase) = db.mediaDao()

	@Provides
	fun providesTagRepository(tagDao: TagDao) = TagRepository(tagDao)

	@Provides
	fun providesNoteRepository(noteDao: NoteDao) = NoteRepository(noteDao)

	@Provides
	fun providesMediaRepository(mediaDao: MediaDao) = MediaItemRepository(mediaDao)

	private const val DATABASE_NAME = "App.db"
}
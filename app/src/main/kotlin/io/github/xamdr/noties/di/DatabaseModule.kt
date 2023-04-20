package io.github.xamdr.noties.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.xamdr.noties.data.dao.ImageDao
import io.github.xamdr.noties.data.dao.NoteDao
import io.github.xamdr.noties.data.database.AppDatabase
import io.github.xamdr.noties.data.dao.TagDao
import io.github.xamdr.noties.data.repository.ImageRepository
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
	fun providesImageDao(db: AppDatabase) = db.imageDao()

	@Provides
	fun providesTagRepository(tagDao: TagDao) = TagRepository(tagDao)

	@Provides
	fun providesImageRepository(imageDao: ImageDao) = ImageRepository(imageDao)

	@Provides
	fun providesNoteRepository(noteDao: NoteDao) = NoteRepository(noteDao)

	private const val DATABASE_NAME = "App.db"
}
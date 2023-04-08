package io.github.xamdr.noties.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.xamdr.noties.data.AppDatabase
import io.github.xamdr.noties.data.AppDatabaseCallback
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

	@Singleton
	@Provides
	fun provideAppDatabase(@ApplicationContext context: Context) =
		Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
			.addCallback(AppDatabaseCallback(context))
			.build()

	@Provides
	fun provideNotebookDao(db: AppDatabase) = db.notebookDao()

	@Provides
	fun providesNoteDao(db: AppDatabase) = db.noteDao()

	@Provides
	fun provideImageDao(db: AppDatabase) = db.imageDao()

	private const val DATABASE_NAME = "App.db"
}
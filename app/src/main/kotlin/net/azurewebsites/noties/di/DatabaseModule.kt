package net.azurewebsites.noties.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.azurewebsites.noties.data.AppDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

	@Singleton
	@Provides
	fun provideAppDatabase(@ApplicationContext context: Context) =
		Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME).build()

	@Provides
	fun provideFolderDao(db: AppDatabase) = db.folderDao()

	@Provides
	fun providesNoteDao(db: AppDatabase) = db.noteDao()

	@Provides
	fun provideImageDao(db: AppDatabase) = db.imageDao()

	private const val DATABASE_NAME = "App.db"
}
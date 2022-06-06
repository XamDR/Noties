package net.azurewebsites.noties.data

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

class AppDatabaseCallback : RoomDatabase.Callback() {

	override fun onCreate(db: SupportSQLiteDatabase) {
		db.execSQL("INSERT INTO Notebooks VALUES (-1, 'Trash', 0);")
	}
}
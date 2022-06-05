package net.azurewebsites.noties.data

import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import net.azurewebsites.noties.R

class AppDatabaseCallback(private val context: Context) : RoomDatabase.Callback() {

	override fun onCreate(db: SupportSQLiteDatabase) {
		val defaultNotebookName = context.getString(R.string.default_notebook)
		db.execSQL("INSERT INTO Notebooks VALUES " +
				"(1, '${defaultNotebookName}', 0, 0), " +
				"(-1, 'Trash', 0, 0);")
	}
}
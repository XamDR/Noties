package net.azurewebsites.noties.data

import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import net.azurewebsites.noties.R

class AppDatabaseCallback(private val context: Context) : RoomDatabase.Callback() {

	override fun onCreate(db: SupportSQLiteDatabase) {
		val personalNotebook = context.getString(R.string.personal_notebook)
		val workNotebook = context.getString(R.string.work_notebook)
		db.execSQL("INSERT INTO Notebooks VALUES " +
				"(1, '${personalNotebook}', 0), " +
				"(2, '${workNotebook}', 0);")
	}
}
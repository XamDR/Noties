package net.azurewebsites.noties.data

import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import net.azurewebsites.noties.R

class AppDatabaseCallback(private val context: Context) : RoomDatabase.Callback() {

	override fun onCreate(db: SupportSQLiteDatabase) {
		val generalNotebook = context.getString(R.string.general_notebook)
		db.execSQL("INSERT INTO Notebooks VALUES (1, '${generalNotebook}', 0);")
	}
}
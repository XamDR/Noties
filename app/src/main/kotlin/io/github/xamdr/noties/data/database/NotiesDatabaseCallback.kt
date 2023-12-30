package io.github.xamdr.noties.data.database

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

class NotiesDatabaseCallback : RoomDatabase.Callback() {

	override fun onCreate(db: SupportSQLiteDatabase) {
		super.onCreate(db)
		db.execSQL("""
			CREATE TRIGGER IF NOT EXISTS update_media_items_trashed AFTER UPDATE OF trashed ON Notes
			BEGIN
				UPDATE MediaItems SET trashed = NEW.trashed WHERE note_id = OLD.id;
			END;
		""".trimIndent())
	}
}
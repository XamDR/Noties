package net.azurewebsites.noties

import net.azurewebsites.noties.core.ImageEntity
import net.azurewebsites.noties.core.Note
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class NoteEqualityTest {

	@Test
	fun note_equality_if_clone_used() {
		val note1 = Note()
		val note2 = note1.clone()
		assertEquals(note1, note2)
	}

	@Test
	fun note_update_date_equality_truncated_to_millis() {
		val note1 = Note()
		val note2 = note1.clone()
		val comparator = Comparator.comparing<ZonedDateTime, ZonedDateTime> { zdt ->
			zdt.truncatedTo(ChronoUnit.MILLIS)
		}
		val result = comparator.compare(note1.entity.dateModification, note2.entity.dateModification)
		assertEquals(0, result)
	}

	@Test
	fun note_text_different_if_clone_used() {
		val note1 = Note()
		val note2 = note1.clone()
		note1.entity.text = "Hello world"
		assertNotEquals(note1.entity.text, note2.entity.text)
	}

	@Test
	fun note_text_equals_if_copy_used() {
		val note1 = Note()
		val note2 = note1.copy()
		note1.entity.text = "Hello world"
		assertEquals(note1.entity.text, note2.entity.text)
	}

	@Test
	fun note_mediaItems_different_if_clone_used() {
		val note1 = Note()
		val note2 = note1.clone()
		note1.images += ImageEntity()
		assertNotEquals(note1.images, note2.images)
	}

	@Test
	fun note_mediaItems_different_if_copy_used() {
		val note1 = Note()
		val note2 = note1.copy()
		note1.images += ImageEntity()
		assertNotEquals(note1.images, note2.images)
	}
}
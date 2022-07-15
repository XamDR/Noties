package net.azurewebsites.noties.ui.helpers

import androidx.core.text.buildSpannedString
import androidx.core.text.strikeThrough
import net.azurewebsites.noties.core.Note

object SpannableConverter {

	@JvmStatic
	fun convertToSpannable(input: String): CharSequence {
		val list = input.split(Note.LINE_BREAK).map {
			if (it.startsWith(Note.PREFIX_NOT_DONE)) {
				buildSpannedString { append(it.removePrefix(Note.PREFIX_NOT_DONE)) }
			}
			else {
				buildSpannedString {
					strikeThrough { append(it.removePrefix(Note.PREFIX_DONE)) }
				}
			}
		}
		return list.toBulletedList()
	}
}
package net.azurewebsites.noties.ui.helpers

import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BulletSpan
import android.text.style.StrikethroughSpan
import net.azurewebsites.noties.core.Note

object SpannableConverter {

	@JvmStatic
	fun convertToSpannable(input: String): CharSequence {
		val list = input.split(Note.LINE_BREAK).map { str ->
			if (str.startsWith(Note.PREFIX_NOT_DONE)) {
				SpannableStringBuilder(str.removePrefix(Note.PREFIX_NOT_DONE))
			}
			else {
				SpannableStringBuilder().append(
					str.removePrefix(Note.PREFIX_DONE),
					StrikethroughSpan(),
					Spanned.SPAN_INCLUSIVE_EXCLUSIVE
				)
			}
		}
		return list.toBulletedList()
	}

	private fun List<SpannableStringBuilder>.toBulletedList(): CharSequence {
		val builder = SpannableStringBuilder()
		for (i in 0 until this.size) {
			val line = this[i].append(if (i < this.size - 1) "\n" else String.Empty)
			val spannable = SpannableString(line)
			spannable.setSpan(BulletSpan(16), 0, spannable.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
			builder.append(spannable)
		}
		return builder
	}
}
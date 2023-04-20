package io.github.xamdr.noties.ui.editor.todos

import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BulletSpan
import android.text.style.StrikethroughSpan
import androidx.core.text.set
import io.github.xamdr.noties.domain.model.Note

object SpannableConverter {

	@JvmStatic
	fun convertToSpannable(input: String): CharSequence {
		val list = input.split(NEWLINE).map { str ->
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
			val line = this[i].append(if (i < this.size - 1) NEWLINE else String.Empty)
			val spannable = SpannableString(line)
			spannable[0, spannable.length] = BulletSpan(16) // SPAN_INCLUSIVE_EXCLUSIVE
			builder.append(spannable)
		}
		return builder
	}

	private const val NEWLINE = "\n"
}
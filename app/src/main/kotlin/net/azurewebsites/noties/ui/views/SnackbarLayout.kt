package net.azurewebsites.noties.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import net.azurewebsites.noties.R
import com.google.android.material.snackbar.ContentViewCallback as Callback

class SnackbarLayout @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyle: Int = 0) : ConstraintLayout(context, attrs, defStyle), Callback {

	lateinit var messageView: TextView
	lateinit var actionView: Button
	lateinit var extraActionView: Button

	private var maxInlineActionWidth = 0

	override fun onFinishInflate() {
		super.onFinishInflate()
		messageView = findViewById(R.id.snackbar_text)
		actionView = findViewById(R.id.snackbar_action)
		extraActionView = findViewById(R.id.snackbar_extra_action)
	}

	@SuppressLint("PrivateResource")
	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec)
		val multiLineVPadding =
			resources.getDimensionPixelSize(R.dimen.design_snackbar_padding_vertical_2lines)
		val singleLineVPadding =
			resources.getDimensionPixelSize(R.dimen.design_snackbar_padding_vertical)
		val messageLayout = messageView.layout
		val isMultiLine = messageLayout != null && messageLayout.lineCount > 1

		var remeasure = false

		if (isMultiLine
			&& maxInlineActionWidth > 0
			&& actionView.measuredWidth > maxInlineActionWidth) {
			if (updateViewsWithinLayout(
					multiLineVPadding,
					multiLineVPadding - singleLineVPadding)
			) {
				remeasure = true
			}
		}
		else {
			val messagePadding = if (isMultiLine) multiLineVPadding else singleLineVPadding
			if (updateViewsWithinLayout(messagePadding, messagePadding)) {
				remeasure = true
			}
		}
		if (remeasure) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec)
		}
	}

	private fun updateViewsWithinLayout(messagePadTop: Int, messagePadBottom: Int): Boolean {
		var changed = false

		if (messageView.paddingTop != messagePadTop ||
			messageView.paddingBottom != messagePadBottom) {
			updateTopBottomPadding(messageView, messagePadTop, messagePadBottom)
			changed = true
		}
		return changed
	}

	override fun animateContentIn(delay: Int, duration: Int) {
		messageView.alpha = 0f
		messageView
			.animate()
			.alpha(1f)
			.setDuration(duration.toLong())
			.setStartDelay(delay.toLong())
			.start()

		if (actionView.visibility == VISIBLE) {
			actionView.alpha = 0f
			actionView
				.animate()
				.alpha(1f)
				.setDuration(duration.toLong())
				.setStartDelay(delay.toLong())
				.start()
		}
	}

	override fun animateContentOut(delay: Int, duration: Int) {
		messageView.alpha = 1f
		messageView
			.animate()
			.alpha(0f)
			.setDuration(duration.toLong())
			.setStartDelay(delay.toLong())
			.start()

		if (actionView.visibility == VISIBLE) {
			actionView.alpha = 1f
			actionView
				.animate()
				.alpha(0f)
				.setDuration(duration.toLong())
				.setStartDelay(delay.toLong())
				.start()
		}
	}

	private companion object {
		fun updateTopBottomPadding(view: View, topPadding: Int, bottomPadding: Int) {
			if (ViewCompat.isPaddingRelative(view)) {
				ViewCompat.setPaddingRelative(
					view,
					ViewCompat.getPaddingStart(view),
					topPadding,
					ViewCompat.getPaddingEnd(view),
					bottomPadding
				)
			}
			else {
				view.setPadding(view.paddingLeft, topPadding, view.paddingRight, bottomPadding)
			}
		}
	}
}
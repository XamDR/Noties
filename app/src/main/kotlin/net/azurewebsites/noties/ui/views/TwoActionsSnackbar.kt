package net.azurewebsites.noties.ui.views

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import com.google.android.material.snackbar.BaseTransientBottomBar
import net.azurewebsites.noties.R
import com.google.android.material.snackbar.ContentViewCallback as Callback

class TwoActionsSnackbar(
	parent: ViewGroup,
	content: View,
	callback: Callback) : BaseTransientBottomBar<TwoActionsSnackbar>(parent, content, callback) {

	fun setText(message: CharSequence): TwoActionsSnackbar {
		getContentLayout().messageView.text = message
		return this
	}

	fun setText(@StringRes message: Int): TwoActionsSnackbar {
		getContentLayout().messageView.setText(message)
		return this
	}

	fun setAction(@StringRes resId: Int, listener: View.OnClickListener): TwoActionsSnackbar {
		val button = getContentLayout().actionView
		button.apply {
			isVisible = true
			setText(resId)
			setOnClickListener { view ->
				listener.onClick(view)
				dispatchDismiss(BaseCallback.DISMISS_EVENT_ACTION)
			}
		}
		return this
	}

	fun setExtraAction(@StringRes resId: Int, listener: View.OnClickListener): TwoActionsSnackbar {
		val button = getContentLayout().extraActionView
		button.apply {
			isVisible = true
			setText(resId)
			setOnClickListener { view ->
				listener.onClick(view)
				dispatchDismiss(BaseCallback.DISMISS_EVENT_ACTION)
			}
		}
		return this
	}

	private fun getContentLayout(): SnackbarLayout {
		return view.getChildAt(0) as SnackbarLayout
	}

	companion object {
		fun make(view: View,
		         text: CharSequence,
		         duration: Int): TwoActionsSnackbar {
			val snackbar = makeInternal(view).apply {
				setText(text)
				setDuration(duration)
			}
			return snackbar
		}

		private fun makeInternal(view: View): TwoActionsSnackbar {
			val parent = findSuitableParent(view) ?: throw IllegalArgumentException(
				"No suitable parent found from the given view. Please provide a valid view."
			)
			val content = LayoutInflater.from(view.context).inflate(
				R.layout.two_actions_snackbar_layout,
				parent,
				false
			) as SnackbarLayout

			return TwoActionsSnackbar(parent, content, content)
		}

		private fun findSuitableParent(view: View?): ViewGroup? {
			var tempView = view
			var fallback: ViewGroup? = null
			do {
				if (tempView is CoordinatorLayout) {
					return tempView
				}
				else if (tempView is FrameLayout) {
					if (tempView.id == android.R.id.content) {
						return tempView
					}
					else {
						fallback = tempView
					}
				}
				if (tempView != null) {
					val parent = tempView.parent
					tempView = if (parent is View) parent else null
				}
			} while (tempView != null)

			return fallback
		}
	}
}
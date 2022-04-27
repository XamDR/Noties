package net.azurewebsites.eznotes.ui.helpers

import android.content.Context
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import net.azurewebsites.eznotes.R

// Based on this code:
// https://github.com/signalapp/Signal-Android/blob/master/app/src/main/java/org/thoughtcrime/securesms/permissions/RationaleDialog.java
// See also: https://github.com/signalapp/Signal-Android/blob/master/app/src/main/res/layout/permissions_rationale_dialog.xml
object PermissionRationaleDialog {

	fun createFor(
		@NonNull context: Context,
		@NonNull message: String,
		@DrawableRes drawableRes: Int): MaterialAlertDialogBuilder {

		val view = LayoutInflater.from(context).inflate(R.layout.permission_rationale_dialog, null)
		val header = view.findViewById<LinearLayout>(R.id.header_container)
		val textView = view.findViewById<TextView>(R.id.message_permission_rationale)
		val drawable = ContextCompat.getDrawable(context, drawableRes)?.also {
			DrawableCompat.setTint(it, ContextCompat.getColor(context, R.color.white))
		}
		val imageView = ShapeableImageView(context).apply {
			minimumWidth = context.resources.getDimensionPixelSize(R.dimen.imageViewSize)
			minimumHeight = context.resources.getDimensionPixelSize(R.dimen.imageViewSize)
			setImageDrawable(drawable)
			layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
		}
		header.addView(imageView)
		textView.text = message
		return MaterialAlertDialogBuilder(context, R.style.MyThemeOverlay_MaterialAlertDialog).setView(view)
	}
}
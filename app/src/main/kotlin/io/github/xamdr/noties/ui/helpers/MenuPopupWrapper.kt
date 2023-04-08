@file:Suppress("PackageDirectoryMismatch")

package androidx.appcompat.widget

import android.annotation.SuppressLint

class MenuPopupWrapper(private val popupMenu: PopupMenu) {

	@SuppressLint("RestrictedApi")
	fun setForceShowIcon(show: Boolean) {
		popupMenu.mPopup.setForceShowIcon(show)
	}
}

fun PopupMenu.wrap() = MenuPopupWrapper(this)
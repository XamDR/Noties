@file:Suppress("PackageDirectoryMismatch")

package kotlin

val String.Companion.Empty
	inline get() = ""

fun Int.sqrt() = kotlin.math.sqrt(this.toDouble()).toInt()

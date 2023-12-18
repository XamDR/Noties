@file:Suppress("PackageDirectoryMismatch")

package kotlin

val String.Companion.Empty
	inline get() = ""

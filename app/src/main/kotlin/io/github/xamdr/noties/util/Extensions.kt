@file:Suppress("PackageDirectoryMismatch")

package kotlin

val String.Companion.Empty
	inline get() = ""

fun <T> Collection<T>.isSingleton(): Boolean = size == 1

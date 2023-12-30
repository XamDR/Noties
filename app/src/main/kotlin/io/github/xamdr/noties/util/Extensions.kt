@file:Suppress("PackageDirectoryMismatch")

package kotlin

val String.Companion.Empty
	inline get() = ""

fun <T> Collection<T>.isSingleton(): Boolean = size == 1

fun String.remove(substring: String): String {
	val index = indexOf(substring)
	return if (index == -1) this else this.removeRange(index, index + substring.length)
}

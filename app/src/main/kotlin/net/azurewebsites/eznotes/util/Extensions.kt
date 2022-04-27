package net.azurewebsites.eznotes.util

val String.Companion.Empty: String
	get() = ""

fun <T> List<T>.isSingleton() = this.size == 1

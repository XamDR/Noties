package net.azurewebsites.noties.ui.settings

import android.content.SharedPreferences
import androidx.annotation.WorkerThread
import androidx.core.content.edit
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class BooleanPreference(
	private val preferences: Lazy<SharedPreferences>,
	private val name: String,
	private val defaultValue: Boolean) : ReadWriteProperty<Any, Boolean> {

	@WorkerThread
	override fun getValue(thisRef: Any, property: KProperty<*>) =
		preferences.value.getBoolean(name, defaultValue)

	override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
		preferences.value.edit { putBoolean(name, value) }
	}
}

class StringPreference(
	private val preferences: Lazy<SharedPreferences>,
	private val name: String,
	private val defaultValue: String) : ReadWriteProperty<Any, String> {

	@WorkerThread
	override fun getValue(thisRef: Any, property: KProperty<*>) =
		preferences.value.getString(name, defaultValue) ?: defaultValue

	override fun setValue(thisRef: Any, property: KProperty<*>, value: String) {
		preferences.value.edit { putString(name, value) }
	}
}
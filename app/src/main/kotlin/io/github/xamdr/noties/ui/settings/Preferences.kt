package io.github.xamdr.noties.ui.settings

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class BooleanPreference(
	private val preferences: Lazy<SharedPreferences>,
	private val key: String,
	private val defaultValue: Boolean) : ReadWriteProperty<Any, Boolean> {

	override fun getValue(thisRef: Any, property: KProperty<*>) =
		preferences.value.getBoolean(key, defaultValue)

	override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
		preferences.value.edit { putBoolean(key, value) }
	}
}

class IntegerPreference(
	private val preferences: Lazy<SharedPreferences>,
	private val key: String,
	private val defaultValue: Int) : ReadWriteProperty<Any, Int> {

	override fun getValue(thisRef: Any, property: KProperty<*>) =
		preferences.value.getInt(key, defaultValue)

	override fun setValue(thisRef: Any, property: KProperty<*>, value: Int) {
		preferences.value.edit { putInt(key, value) }
	}
}

class StringPreference(
	private val preferences: Lazy<SharedPreferences>,
	private val key: String,
	private val defaultValue: String) : ReadWriteProperty<Any, String> {

	override fun getValue(thisRef: Any, property: KProperty<*>) =
		preferences.value.getString(key, defaultValue) ?: defaultValue

	override fun setValue(thisRef: Any, property: KProperty<*>, value: String) {
		preferences.value.edit { putString(key, value) }
	}
}
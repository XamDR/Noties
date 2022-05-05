package net.azurewebsites.noties.core

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Task(val content: String = String.Empty, var done: Boolean = false) : Parcelable
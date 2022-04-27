package net.azurewebsites.eznotes.core

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import net.azurewebsites.eznotes.util.Empty

@Parcelize
data class Task(val content: String = String.Empty, var done: Boolean = false) : Parcelable
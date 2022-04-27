package net.azurewebsites.noties.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import net.azurewebsites.noties.util.Empty

@Parcelize
data class Task(val content: String = String.Empty, var done: Boolean = false) : Parcelable
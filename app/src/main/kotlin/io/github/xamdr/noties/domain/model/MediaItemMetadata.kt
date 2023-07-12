package io.github.xamdr.noties.domain.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaItemMetadata(val thumbnail: Uri? = null, val duration: Long = 0) : Parcelable
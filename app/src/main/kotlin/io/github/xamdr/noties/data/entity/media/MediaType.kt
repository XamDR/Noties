package io.github.xamdr.noties.data.entity.media

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class MediaType : Parcelable {
	data object Image : MediaType()
	data object Video : MediaType()
	data object Audio : MediaType()
}

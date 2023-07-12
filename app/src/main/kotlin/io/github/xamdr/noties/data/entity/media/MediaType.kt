package io.github.xamdr.noties.data.entity.media

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class MediaType : Parcelable {
	object Image : MediaType()
	object Video : MediaType()
	object Audio : MediaType()
}

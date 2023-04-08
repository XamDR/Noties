package io.github.xamdr.noties.ui.editor

import android.net.Uri

interface OpenFileListener {
	fun readFileContent(uri: Uri?)
}
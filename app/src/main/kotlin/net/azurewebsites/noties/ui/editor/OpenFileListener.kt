package net.azurewebsites.noties.ui.editor

import android.net.Uri

interface OpenFileListener {
	fun readFileContent(uri: Uri?)
}
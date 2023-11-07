package io.github.xamdr.noties.ui.notes

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import io.github.xamdr.noties.R
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.helpers.annotatedStringResource
import io.github.xamdr.noties.ui.theme.NotiesTheme

@Composable
fun DeleteNotesDialog(
	onDeleteNotes: () -> Unit,
	onDismiss: () -> Unit
) {
	AlertDialog(
		onDismissRequest = onDismiss,
		confirmButton = {
			TextButton(onClick = { onDeleteNotes() }) {
				Text(text = stringResource(id = R.string.ok_button))
			}
		},
		dismissButton = {
			TextButton(onClick = onDismiss) {
				Text(text = stringResource(id = R.string.cancel_button))
			}
		},
		title = { Text(text = stringResource(id = R.string.delete_notes_title)) },
		text = {
			Text(
				text = annotatedStringResource(
					id = R.string.delete_notes_question,
					spanStyles = { annotation ->
						when (annotation.value) {
							"bold" -> SpanStyle(fontWeight = FontWeight.Bold)
							else -> null
						}
					}
				)
			)
		}
	)
}

@DevicePreviews
@Composable
private fun DeleteNotesDialogPreview() {
	NotiesTheme {
		DeleteNotesDialog(onDeleteNotes = {}, onDismiss = {})
	}
}
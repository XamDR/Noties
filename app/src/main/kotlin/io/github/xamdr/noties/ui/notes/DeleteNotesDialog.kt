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
	isBatchDelete: Boolean,
	onDeleteNotes: () -> Unit,
	onDismiss: () -> Unit
) {
	AlertDialog(
		onDismissRequest = onDismiss,
		confirmButton = {
			TextButton(onClick = onDeleteNotes) {
				Text(text = stringResource(id = if (isBatchDelete) R.string.ok_button else R.string.empty_trash))
			}
		},
		dismissButton = {
			TextButton(onClick = onDismiss) {
				Text(text = stringResource(id = R.string.cancel_button))
			}
		},
		title = {
			Text(text = stringResource(id = if (isBatchDelete) R.string.delete_notes_title else R.string.empty_trash_question))
		},
		text = {
			if (isBatchDelete) {
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
			else {
				Text(text = stringResource(id = R.string.empty_trash_message_warning))
			}
		}
	)
}

@DevicePreviews
@Composable
private fun DeleteNotesDialogBatchPreview() {
	NotiesTheme {
		DeleteNotesDialog(isBatchDelete = true, onDeleteNotes = {}, onDismiss = {})
	}
}

@DevicePreviews
@Composable
private fun DeleteNotesDialogFromTrashPreview() {
	NotiesTheme {
		DeleteNotesDialog(isBatchDelete = false, onDeleteNotes = {}, onDismiss = {})
	}
}
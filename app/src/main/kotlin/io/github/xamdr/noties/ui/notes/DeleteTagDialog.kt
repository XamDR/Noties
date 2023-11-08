package io.github.xamdr.noties.ui.notes

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.xamdr.noties.R
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.theme.NotiesTheme

@Composable
fun DeleteTagDialog(
	onDeleteTag: () -> Unit,
	onDismiss: () -> Unit
) {
	AlertDialog(
		onDismissRequest = onDismiss,
		confirmButton = {
			TextButton(onClick = onDeleteTag) {
				Text(text = stringResource(id = R.string.delete_button))
			}
		},
		dismissButton = {
			TextButton(onClick = onDismiss) {
				Text(text = stringResource(id = R.string.cancel_button))
			}
		},
		title = {
			Text(text = stringResource(id = R.string.delete_tag_question))
		},
		text = {
			Text(text = stringResource(id = R.string.delete_tag_message))
		}
	)
}

@DevicePreviews
@Composable
private fun DeleteTagDialogPreview() {
	NotiesTheme {
		DeleteTagDialog(onDeleteTag = {}, onDismiss = {})
	}
}
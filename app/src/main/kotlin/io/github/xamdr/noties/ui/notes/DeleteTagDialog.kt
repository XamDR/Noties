package io.github.xamdr.noties.ui.notes

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.xamdr.noties.R
import io.github.xamdr.noties.domain.model.Tag
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.tags.TagsViewModel
import io.github.xamdr.noties.ui.theme.NotiesTheme
import kotlinx.coroutines.launch

@Composable
fun DeleteTagDialog(
	tag: Tag,
	onDismiss: () -> Unit,
	onDeleteTag: () -> Unit,
	viewModel: TagsViewModel = hiltViewModel()
) {
	val scope = rememberCoroutineScope()

	fun deleteTag() {
		scope.launch { viewModel.deleteTag(tag) }
		onDeleteTag()
	}

	AlertDialog(
		onDismissRequest = onDismiss,
		confirmButton = {
			TextButton(onClick = ::deleteTag) {
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

@Composable
private fun DeleteTagDialog() {
	AlertDialog(
		onDismissRequest = {},
		confirmButton = {
			TextButton(onClick = {}) {
				Text(text = stringResource(id = R.string.delete_button))
			}
		},
		dismissButton = {
			TextButton(onClick = {}) {
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
private fun DeleteTagDialogPreview() = NotiesTheme { DeleteTagDialog() }
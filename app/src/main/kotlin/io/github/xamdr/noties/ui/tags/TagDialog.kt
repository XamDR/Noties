package io.github.xamdr.noties.ui.tags

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.xamdr.noties.R
import io.github.xamdr.noties.domain.model.Tag
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.helpers.showSoftKeyboardOnFocus
import io.github.xamdr.noties.ui.theme.NotiesTheme
import kotlinx.coroutines.launch

@Composable
fun TagDialog(
	tag: Tag,
	onCancel: () -> Unit,
	onSave: (String) -> Unit,
	viewModel: TagsViewModel = hiltViewModel(),
) {
	val scope = rememberCoroutineScope()
	val tagNameState by viewModel.nameState.collectAsStateWithLifecycle(initialValue = TagNameState.EmptyOrUpdatingName)
	var isConfirmButtonEnabled by rememberSaveable { mutableStateOf(false) }
	var showError by rememberSaveable { mutableStateOf(false) }
	var tagName by rememberSaveable { mutableStateOf(String.Empty) }
	val focusRequester = remember { FocusRequester() }

	when (tagNameState) {
		TagNameState.EditingName -> {
			showError = false
			isConfirmButtonEnabled = true
		}
		TagNameState.EmptyOrUpdatingName -> {
			isConfirmButtonEnabled = false
		}
		TagNameState.ErrorDuplicateName -> {
			showError = true
			isConfirmButtonEnabled = false
		}
	}

	suspend fun createTag(tagName: String) {
		val newTag = Tag(name = tagName)
		viewModel.createTag(newTag)
		onSave(tagName)
	}

	fun onDismiss() {
		viewModel.clearNameState()
		onCancel()
	}

	AlertDialog(
		onDismissRequest = ::onDismiss,
		confirmButton = {
			TextButton(
				onClick = { scope.launch { createTag(tagName) } },
				enabled = isConfirmButtonEnabled
			) {
				Text(text = stringResource(id = R.string.save_button))
			}
		},
		dismissButton = {
			TextButton(onClick = ::onDismiss) {
				Text(text = stringResource(id = R.string.cancel_button))
			}
		},
		title = { Text(text = stringResource(id = R.string.new_tag)) },
		text = {
			OutlinedTextField(
				value = tag.name.ifEmpty { tagName },
				onValueChange = { s ->
					tagName = s
					viewModel.onTagNameChanged(s)
				},
				modifier = Modifier.showSoftKeyboardOnFocus(focusRequester),
				label = { Text(text = stringResource(id = R.string.tag_name)) },
				leadingIcon = {
					Icon(
						imageVector = Icons.Outlined.Label,
						contentDescription = null
					)
				},
				isError = showError,
				keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
				supportingText = {
					if (showError) {
						Text(text = stringResource(id = R.string.error_message_tag_duplicate))
					}
				}
			)
		}
	)
}

@Composable
private fun TagDialog() {
	AlertDialog(
		onDismissRequest = { },
		confirmButton = {
			TextButton(onClick = {}) {
				Text(text = stringResource(id = R.string.save_button))
			}
		},
		dismissButton = {
			TextButton(onClick = {}) {
				Text(text = stringResource(id = R.string.cancel_button))
			}
		},
		title = { Text(text = stringResource(id = R.string.new_tag)) },
		text = {
			OutlinedTextField(
				value = "",
				onValueChange = {},
				label = { Text(text = stringResource(id = R.string.tag_name)) },
				leadingIcon = {
					Icon(
						imageVector = Icons.Outlined.Label,
						contentDescription = null
					)
				},
				supportingText = { Text(text = stringResource(id = R.string.error_message_tag_duplicate)) }
			)
		}
	)
}

@DevicePreviews
@Composable
private fun TagDialogPreview() = NotiesTheme { TagDialog() }
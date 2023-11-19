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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.xamdr.noties.R
import io.github.xamdr.noties.domain.model.Tag
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.helpers.onFocusSelectAll
import io.github.xamdr.noties.ui.helpers.onFocusShowSoftKeyboard
import io.github.xamdr.noties.ui.theme.NotiesTheme
import kotlinx.coroutines.launch

@Composable
fun TagDialog(
	tag: Tag,
	onCancel: () -> Unit,
	onSave: (Tag) -> Unit,
	viewModel: TagsViewModel = hiltViewModel(),
) {
	val scope = rememberCoroutineScope()
	val tagNameState by viewModel.nameState.collectAsStateWithLifecycle(initialValue = TagNameState.EmptyOrUpdatingName)
	var isConfirmButtonEnabled by rememberSaveable { mutableStateOf(value = false) }
	var showError by rememberSaveable { mutableStateOf(false) }
	val focusRequester = remember { FocusRequester() }
	val tagState = rememberSaveable(stateSaver = TextFieldValue.Saver) {
		mutableStateOf(value = TextFieldValue(text = tag.name))
	}

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

	suspend fun createOrUpdateTag(tagName: String) {
		if (tag.id == 0) {
			val newTag = Tag(name = tagName)
			viewModel.createTag(newTag)
			onSave(newTag)
		}
		else {
			val updatedTag = tag.copy(name = tagName)
			viewModel.updateTag(updatedTag, tag)
			onSave(updatedTag)
		}
	}

	fun onDismiss() {
		viewModel.clearNameState()
		onCancel()
	}

	AlertDialog(
		onDismissRequest = ::onDismiss,
		confirmButton = {
			TextButton(
				onClick = { scope.launch { createOrUpdateTag(tagState.value.text) } },
				enabled = isConfirmButtonEnabled
			) {
				Text(text = stringResource(id = if (tag.id == 0) R.string.save_button else R.string.rename_button))
			}
		},
		dismissButton = {
			TextButton(onClick = ::onDismiss) {
				Text(text = stringResource(id = R.string.cancel_button))
			}
		},
		title = { Text(text = stringResource(id = if (tag.id == 0) R.string.new_tag else R.string.update_tag)) },
		text = {
			OutlinedTextField(
				value = tagState.value,
				onValueChange = { s ->
					if (s.text != tagState.value.text) {
						viewModel.onTagNameChanged(s.text)
					}
					tagState.value = s
				},
				modifier = Modifier
					.onFocusShowSoftKeyboard(focusRequester)
					.onFocusSelectAll(tagState),
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
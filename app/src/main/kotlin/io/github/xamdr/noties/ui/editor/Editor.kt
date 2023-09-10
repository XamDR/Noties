package io.github.xamdr.noties.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.xamdr.noties.R
import io.github.xamdr.noties.ui.components.TextBox

@Composable
fun Editor(modifier: Modifier) {
	var text by remember { mutableStateOf(String.Empty) }

	Box(modifier = modifier) {
		LazyColumn(
			contentPadding = PaddingValues(8.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp),
		) {
			items(count = 1) {
				TextBox(
					placeholder = stringResource(id = R.string.placeholder),
					value = text,
					onValueChange = { text = it },
					modifier = Modifier.fillMaxWidth()
				)
			}
		}
	}
}
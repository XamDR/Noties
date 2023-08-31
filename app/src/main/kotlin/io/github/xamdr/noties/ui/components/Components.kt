package io.github.xamdr.noties.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun EmptyView(icon: ImageVector, @StringRes message: Int) {
	val iconSize = if (LocalConfiguration.current.screenHeightDp >= 600) 128.dp else 64.dp
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		Icon(
			imageVector = icon,
			contentDescription = null,
			modifier = androidx.compose.ui.Modifier.size(iconSize)
		)
		Text(text = stringResource(id = message))
	}
}
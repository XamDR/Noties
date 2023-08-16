package io.github.xamdr.noties.ui.helpers

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "QVGA - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, device = "id:3.2in QVGA (ADP2)")
@Preview(name = "WQVGA - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, device = "id:3.4in WQVGA")
@Preview(name = "Pixel 2 - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, device = "id:pixel_2")
@Preview(name = "Default - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "QVGA - Light", uiMode = Configuration.UI_MODE_NIGHT_NO, device = "id:3.2in QVGA (ADP2)")
@Preview(name = "WQVGA - Light", uiMode = Configuration.UI_MODE_NIGHT_NO, device = "id:3.4in WQVGA")
@Preview(name = "Pixel 2 - Light", uiMode = Configuration.UI_MODE_NIGHT_NO, device = "id:pixel_2")
@Preview(name = "Default - Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
annotation class DevicePreviews

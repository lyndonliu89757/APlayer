package remix.myplayer.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.core.graphics.toColorInt
import remix.myplayer.R
import remix.myplayer.util.ColorUtil


val LocalTheme = compositionLocalOf<AppTheme> {
    return@compositionLocalOf AppTheme(
        primary = Color(0xff1da57a),
        secondary = Color(0xaa1da57a)
    )
}

data class AppTheme(
    var primary: Color,
    var secondary: Color,
    var coloredNaviBar: Boolean = false
) {

    val background: Color = Color(0xFFF2F3F5)
    val container: Color = Color(0xFFFFFEFF)

    val textPrimary
        @Composable
        @ReadOnlyComposable
        get() = colorResource(R.color.light_text_color_primary)

    val textSecondary
        @Composable
        @ReadOnlyComposable
        get() = colorResource(R.color.light_text_color_secondary)

    val ripple
        @Composable
        @ReadOnlyComposable
        get() = colorResource(R.color.light_ripple_color)

    val select
        @Composable
        @ReadOnlyComposable
        get() = colorResource(R.color.light_select_color)

    val mainBackground
        @Composable
        @ReadOnlyComposable
        get() = colorResource(R.color.light_background_color_main)

    val dialogBackground
        @Composable
        @ReadOnlyComposable
        get() = colorResource(R.color.light_background_color_dialog)

    val albumPlaceHolder: Int
        @Composable
        @ReadOnlyComposable
        get() = R.drawable.album_empty_bg_day

    val artistPlaceHolder: Int
        @Composable
        @ReadOnlyComposable
        get() = R.drawable.artist_empty_bg_day

    val isPrimaryLight: Boolean
        get() = ColorUtil.isColorLight(primary.toArgb())

    val isPrimaryCloseToWhite: Boolean
        get() = ColorUtil.isColorCloseToWhite(primary.toArgb())

}

@Composable
@ReadOnlyComposable
fun AppTheme.highLightText(): Color {
    var primaryColor = primary
    if (isPrimaryCloseToWhite) {
        primaryColor = textPrimary
    }
    return primaryColor
}

@Composable
@ReadOnlyComposable
fun AppTheme.icon() = Color.Black

@Composable
@ReadOnlyComposable
fun AppTheme.popupButton() = Color("#6C6A6C".toColorInt())
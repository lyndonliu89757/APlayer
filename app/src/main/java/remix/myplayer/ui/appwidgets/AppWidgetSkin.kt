package remix.myplayer.ui.appwidgets

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import remix.myplayer.R
import remix.myplayer.data.prefs.SettingPrefs
import remix.myplayer.service.MusicService
import remix.myplayer.service.playback.MusicStateSource
import remix.myplayer.util.ColorUtil

enum class AppWidgetSkin(
  @param:ColorInt var titleColor: Int, @param:ColorInt var artistColor: Int,
  @param:ColorInt var progressColor: Int, @param:ColorInt var btnColor: Int,
  @param:DrawableRes var background: Int, @param:DrawableRes val timerRes: Int,
  @param:DrawableRes val nextRes: Int, @param:DrawableRes val prevRes: Int,
  @param:DrawableRes val loveRes: Int, @param:DrawableRes val modeRepeatRes: Int,
  @param:DrawableRes val modeNormalRes: Int, @param:DrawableRes val modeShuffleRes: Int,
  @param:DrawableRes val playRes: Int, @param:DrawableRes val pauseRes: Int
) {

  WHITE_1F(
    ColorUtil.getColor(R.color.appwidget_title_color_white_1f),
    ColorUtil.getColor(R.color.appwidget_artist_color_white_1f),
    ColorUtil.getColor(R.color.appwidget_progress_color_white_1f),
    ColorUtil.getColor(R.color.appwidget_btn_color_white_1f),
    R.drawable.bg_corner_app_widget_white_1f,
    R.drawable.widget_btn_timer,
    R.drawable.ic_next,
    R.drawable.ic_previous,
    R.drawable.widget_btn_like_nor,
    R.drawable.ic_play_mode_loop_one,
    R.drawable.ic_play_mode_loop,
    R.drawable.ic_play_mode_random,
    R.drawable.ic_play,
    R.drawable.ic_pause
  );

  val lovedRes: Int
    get() = R.drawable.widget_btn_like_prs

  fun getModeRes(service: MusicService): Int {
    return when (MusicStateSource.currentPlaybackUiState.playMode) {
      SettingPrefs.MODE_SHUFFLE -> modeShuffleRes
      SettingPrefs.MODE_REPEAT -> modeRepeatRes
      else -> modeNormalRes
    }
  }
}

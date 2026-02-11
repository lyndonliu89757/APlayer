package remix.myplayer.ui.screen.setting

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import remix.myplayer.R
import remix.myplayer.helper.EQHelper
import remix.myplayer.ui.nav.LocalNavController
import remix.myplayer.ui.screen.setting.logic.common.BlackListLogic
import remix.myplayer.ui.screen.setting.logic.common.BreakPointLogic
import remix.myplayer.ui.screen.setting.logic.common.ForceSortLogic
import remix.myplayer.ui.screen.setting.logic.common.LanguageLogic
import remix.myplayer.ui.screen.setting.logic.common.ManualScanLogic
import remix.myplayer.ui.screen.setting.logic.common.RestoreDeleteLogic
import remix.myplayer.ui.screen.setting.logic.common.ScanSizeLogic
import remix.myplayer.ui.screen.setting.logic.common.ShowDisplayNameLogic
import remix.myplayer.ui.screen.setting.logic.cover.AutoDownloadLogic
import remix.myplayer.ui.screen.setting.logic.cover.DownloadSourceLogic
import remix.myplayer.ui.screen.setting.logic.cover.IgnoreMediaStoreLogic
import remix.myplayer.ui.screen.setting.logic.lyric.DesktopLyricLogic
import remix.myplayer.ui.screen.setting.logic.lyric.LyricPriorityLogic
import remix.myplayer.ui.screen.setting.logic.lyric.StatusBarLyricLogic
import remix.myplayer.ui.screen.setting.logic.other.ClearCacheLogic
import remix.myplayer.ui.screen.setting.logic.play.AutoPlayLogic
import remix.myplayer.ui.screen.setting.logic.play.IgnoreAudioFocusLogic
import remix.myplayer.ui.screen.setting.logic.play.PlayFadeLogic
import remix.myplayer.ui.screen.setting.logic.playingscreen.KeepScreenOnLogic
import remix.myplayer.ui.screen.setting.logic.playingscreen.PlayingScreenBackgroundLogic
import remix.myplayer.viewmodel.mainViewModel

@Composable
fun SettingScreen() {
  val preferenceSections = listOf<@Composable () -> Unit>(
    { CommonPreferences() },
    { PlayPreferences() },
    { PlayingScreenPreferences() },
    { CoverPreferences() },
    { LyricPreferences() },
    { OtherPreferences() }
  )

  LazyColumn(
    modifier = Modifier.padding(horizontal = 10.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
    contentPadding = PaddingValues(bottom = 10.dp),
  ) {
    items(
      items = preferenceSections,
      key = { it.hashCode() }
    ) { preferenceSection ->
      Card(colors = CardDefaults.cardColors().copy(containerColor = Color(0x331da57a))) {
        preferenceSection()
      }
    }
  }
}

@Composable
private fun CommonPreferences() {
  SettingTitle(R.string.common)

  ScanSizeLogic()

  BlackListLogic()

  ManualScanLogic()

  RestoreDeleteLogic()

  LanguageLogic()

  ShowDisplayNameLogic()

  ForceSortLogic()
}

@Composable
private fun PlayPreferences() {
  SettingTitle(R.string.play)

  IgnoreAudioFocusLogic()

  BreakPointLogic()

  PlayFadeLogic()

  AutoPlayLogic()
}

@Composable
private fun PlayingScreenPreferences() {
  SettingTitle(R.string.playing_screen)

  PlayingScreenBackgroundLogic()

  KeepScreenOnLogic()
}

@Composable
private fun CoverPreferences() {
  SettingTitle(R.string.cover)

  IgnoreMediaStoreLogic()

  AutoDownloadLogic()

  DownloadSourceLogic()
}

@Composable
private fun LyricPreferences() {
  SettingTitle(R.string.lrc)

  DesktopLyricLogic()

  StatusBarLyricLogic()

  LyricPriorityLogic()
}

@Composable
private fun OtherPreferences() {
  SettingTitle(R.string.other)

  val activity = LocalActivity.current
  val nav = LocalNavController.current

  ArrowPreference(R.string.eq_setting) {
    EQHelper.startEqualizer(activity ?: return@ArrowPreference, nav)
  }

  ClearCacheLogic()
}

@Composable
private fun SettingTitle(res: Int) {
  Text(
    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    text = stringResource(res),
    fontSize = 16.sp,
    fontWeight = FontWeight.Bold
  )
}
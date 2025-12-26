package remix.myplayer.ui.widget.library

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import remix.myplayer.R
import remix.myplayer.data.model.audio.Song
import remix.myplayer.data.prefs.SettingPrefs
import remix.myplayer.ui.clickableWithoutRipple
import remix.myplayer.misc.helper.MusicServiceRemote.setPlayQueue
import remix.myplayer.service.Command
import remix.myplayer.ui.nav.MessageNotifier
import remix.myplayer.ui.screen.playing.PlayModeMap
import remix.myplayer.ui.theme.LocalTheme
import remix.myplayer.util.ColorUtil
import remix.myplayer.util.MusicUtil
import remix.myplayer.util.Util
import remix.myplayer.viewmodel.playbackViewModel

@Composable
fun SongListHeader(songSize: Int, scrollToTop: () -> Unit = {}, scrollToCurrent: () -> Unit = {}) {
  if (songSize == 0) {
    return
  }
  val playbackState by playbackViewModel.playbackUiState.collectAsStateWithLifecycle()

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(LocalTheme.current.background)
      .padding(horizontal = 20.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .weight(1f)
        .clickableWithoutRipple(interactionSource) {
          Util.sendCMDLocalBroadcast(Command.CHANGE_MODEL)
        }
    ) {
      val playMode = playbackState.playMode
      Icon(
        modifier = Modifier.size(24.dp),
        painter = painterResource(PlayModeMap[playMode]!!.first),
        contentDescription = "ListHeaderIcon"
      )
      Text(
        text = songSize.toString(),
        color = LocalTheme.current.textPrimary,
        fontSize = 14.sp
      )
    }

    Row(
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      if (scrollToTop != {}) {
        Icon(
          modifier = Modifier
            .size(24.dp)
            .clickableWithoutRipple(interactionSource) {
              scrollToTop()
            },
          painter = painterResource(R.drawable.ic_top),
          contentDescription = "ListHeaderIcon"
        )
      }
      if (scrollToCurrent != {}) {
        Icon(
          modifier = Modifier
            .size(24.dp)
            .clickableWithoutRipple(interactionSource) {
              scrollToCurrent()
            },
          painter = painterResource(R.drawable.ic_location),
          contentDescription = "ListHeaderIcon"
        )
      }
    }
  }
}

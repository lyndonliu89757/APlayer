package remix.myplayer.ui.widget.app

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import remix.myplayer.R
import remix.myplayer.service.Command
import remix.myplayer.service.MusicService
import remix.myplayer.service.MusicService.Companion.EXTRA_COMMAND
import remix.myplayer.ui.clickableWithoutRipple
import remix.myplayer.ui.nav.LocalNavController
import remix.myplayer.ui.nav.RoutePlayingScreen
import remix.myplayer.ui.theme.LocalTheme
import remix.myplayer.ui.widget.common.TextPrimary
import remix.myplayer.ui.widget.common.TextSecondary
import remix.myplayer.ui.widget.library.GlideCover
import remix.myplayer.util.Util
import remix.myplayer.viewmodel.PlaybackViewModel
import remix.myplayer.viewmodel.playbackViewModel
import kotlin.math.absoluteValue

private const val triggerThreshold = 10

@Composable
fun BottomBar(vm: PlaybackViewModel = playbackViewModel) {
  val playbackState by vm.playbackUiState.collectAsStateWithLifecycle()
  val nav = LocalNavController.current
  val interactionSource = remember { MutableInteractionSource() }

  var hasTriggerAct by remember { mutableStateOf(false) }
  var hasTriggerOp by remember { mutableStateOf(false) }

  val isSongValid = playbackState.song.valid()
  val interactionModifiers = if (isSongValid) {
    Modifier
      // 点击跳转播放页
      .clickableWithoutRipple(interactionSource) {
        nav.navigate(RoutePlayingScreen)
      }
      // 垂直滑动跳转播放页
      .pointerInput(Unit) {
        detectVerticalDragGestures(
          onDragStart = { hasTriggerAct = false }
        ) { _, dragAmount ->
          if (dragAmount < -triggerThreshold && !hasTriggerAct) {
            hasTriggerAct = true
            nav.navigate(RoutePlayingScreen)
          }
        }
      }
      // 水平滑动切换歌曲
      .pointerInput(Unit) {
        detectHorizontalDragGestures(
          onDragStart = { hasTriggerOp = false }
        ) { _, dragAmount ->
          if (dragAmount.absoluteValue > triggerThreshold && !hasTriggerOp) {
            hasTriggerOp = true
            Util.sendLocalBroadcast(
              Intent(MusicService.ACTION_CMD)
                .putExtra(
                  EXTRA_COMMAND,
                  if (dragAmount < 0) Command.SKIP_TO_NEXT else Command.SKIP_TO_PREVIOUS
                )
            )
          }
        }
      }
  } else {
    // 歌曲无效时，不响应任何操作
    Modifier
  }

  Row(
    modifier = Modifier
      .height(56.dp)
      .fillMaxWidth()
      .background(LocalTheme.current.container)
      .padding(top = 6.dp, start = 16.dp, end = 16.dp)
      .semantics { contentDescription = "BottomBar" }
      .then(interactionModifiers),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    GlideCover(
      model = playbackState.song,
      modifier = Modifier.size(52.dp)
    )
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      TextPrimary(playbackState.song.showName, fontSize = 16.sp)
      if (playbackState.song.artist.isNotEmpty())
        TextSecondary(text = String.format("%s《%s》", playbackState.song.artist, playbackState.song.album), fontSize = 12.sp)
    }

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      val buttonColor =
        Color("#323334".toColorInt())
      Icon(
        modifier = Modifier
          .size(36.dp)
          .clickableWithoutRipple(interactionSource) {
            Util.sendLocalBroadcast(
              Intent(MusicService.ACTION_CMD)
                .putExtra(EXTRA_COMMAND, Command.PLAY_PAUSE)
            )
          },
        painter = painterResource(if (playbackState.isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
        contentDescription = "PlayPause",
        tint = buttonColor
      )
      Icon(
        modifier = Modifier
          .size(36.dp)
          .clickableWithoutRipple(interactionSource) {
            Util.sendLocalBroadcast(
              Intent(MusicService.ACTION_CMD)
                .putExtra(EXTRA_COMMAND, Command.SKIP_TO_NEXT)
            )
          },
        painter = painterResource(R.drawable.ic_next),
        contentDescription = "Next",
        tint = buttonColor
      )
    }
  }
}

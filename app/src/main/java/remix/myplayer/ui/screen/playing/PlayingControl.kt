package remix.myplayer.ui.screen.playing

import android.content.Intent
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.palette.graphics.Palette
import kotlinx.coroutines.launch
import remix.myplayer.R
import remix.myplayer.data.prefs.SettingPrefs.Companion.MODE_LOOP
import remix.myplayer.data.prefs.SettingPrefs.Companion.MODE_REPEAT
import remix.myplayer.data.prefs.SettingPrefs.Companion.MODE_SHUFFLE
import remix.myplayer.misc.CenterInBox
import remix.myplayer.misc.isPortraitOrientation
import remix.myplayer.service.Command
import remix.myplayer.service.MusicService
import remix.myplayer.service.MusicService.Companion.EXTRA_POSITION
import remix.myplayer.service.playback.PlaybackUiState
import remix.myplayer.ui.clickWithRipple
import remix.myplayer.ui.dialog.BottomSheetDialog
import remix.myplayer.ui.theme.LocalTheme
import remix.myplayer.ui.widget.common.TextPrimary
import remix.myplayer.ui.widget.common.TextSecondary
import remix.myplayer.ui.widget.playpause.PlayPauseView
import remix.myplayer.util.MusicUtil.makeCmdIntent
import remix.myplayer.util.Util
import remix.myplayer.util.Util.sendLocalBroadcast
import remix.myplayer.viewmodel.playbackViewModel

val PlayModeMap = mapOf(
  MODE_SHUFFLE to Pair(R.drawable.ic_play_mode_random, R.string.model_random),
  MODE_LOOP to Pair(R.drawable.ic_play_mode_loop, R.string.model_normal),
  MODE_REPEAT to Pair(R.drawable.ic_play_mode_loop_one, R.string.model_repeat)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlayingControl(
  playbackUiState: PlaybackUiState,
  swatch: Palette.Swatch
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(60.dp)
      .padding(0.dp),
    horizontalArrangement = Arrangement.SpaceEvenly,
    verticalAlignment = Alignment.CenterVertically
  ) {
    val swatchColor = Color(swatch.rgb)

    // 播放模式
    val playMode = playbackUiState.playMode
    ControlButton(onClick = {
      Util.sendCMDLocalBroadcast(Command.CHANGE_MODEL)
    }) {
      Image(
        modifier = Modifier.size(36.dp),
        painter = painterResource(PlayModeMap[playMode]!!.first),
        contentDescription = "PlayingMode",
        colorFilter = ColorFilter.tint(swatchColor.copy(0.6f))
      )
    }

    // 前一首
    ControlButton(onClick = {
      sendLocalBroadcast(
        Intent(MusicService.ACTION_CMD).putExtra(
          MusicService.EXTRA_CONTROL,
          Command.SKIP_TO_PREVIOUS
        )
      )
    }) {
      Image(
        modifier = Modifier.size(40.dp),
        painter = painterResource(R.drawable.ic_previous),
        contentDescription = "PlayingPrev",
        colorFilter = ColorFilter.tint(swatchColor)
      )
    }

    // 播放/暂停
    ControlButton(onClick = {
      sendLocalBroadcast(
        Intent(MusicService.ACTION_CMD).putExtra(
          MusicService.EXTRA_CONTROL,
          Command.PLAY_PAUSE
        )
      )
    }) {
      val size = with(LocalDensity.current) { 56.dp.roundToPx() }
      AndroidView(
        factory = {
          PlayPauseView(it).apply {
            setBackgroundColor(Color.Transparent.toArgb())
            layoutParams = ViewGroup.LayoutParams(size, size)
          }
        },
        update = {
          it.setBackgroundColor(swatch.rgb)
          it.updateState(playbackUiState.isPlaying, true)
        }
      )
    }

    // 下一首
    ControlButton(onClick = {
      sendLocalBroadcast(
        Intent(MusicService.ACTION_CMD).putExtra(
          MusicService.EXTRA_CONTROL,
          Command.SKIP_TO_NEXT
        )
      )
    }) {
      Image(
        modifier = Modifier.size(40.dp),
        painter = painterResource(R.drawable.ic_next),
        contentDescription = "PlayingNext",
        colorFilter = ColorFilter.tint(swatchColor)
      )
    }

    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    PlayQueueDialog(state, playbackUiState)

    // 播放列表
    val scope = rememberCoroutineScope()
    ControlButton(onClick = {
      scope.launch {
        state.show()
      }
    }) {
      Image(
        modifier = Modifier.size(36.dp),
        painter = painterResource(R.drawable.ic_playlist),
        contentDescription = "PlayingPlayQueue",
        colorFilter = ColorFilter.tint(swatchColor.copy(0.6f))
      )
    }
  }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun PlayQueueDialog(
  state: SheetState,
  musicState: PlaybackUiState
) {
  val scope = rememberCoroutineScope()
  val playbackVM = playbackViewModel
  val playbackState by playbackVM.playbackUiState.collectAsStateWithLifecycle()
  val songs by playbackVM.playQueueSongs.collectAsStateWithLifecycle()
  val theme = LocalTheme.current;

  BottomSheetDialog(state) {
    Column {
      CenterInBox(
        modifier = Modifier
          .height(48.dp)
          .fillMaxWidth()
      ) {
        TextPrimary(
          stringResource(R.string.play_queue, songs.size),
          fontSize = 18.sp,
          textAlign = TextAlign.Center
        )
      }
    }

    val lazyState = rememberLazyListState()
    LazyColumn(state = lazyState) {
      itemsIndexed(songs, key = { _, song -> song.id }) { pos, song ->
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .height(50.dp)
            .clickWithRipple(false) {
              sendLocalBroadcast(
                makeCmdIntent(Command.PLAY_AT)
                  .putExtra(EXTRA_POSITION, pos)
              )
              scope.launch { state.hide() }
            }
            .background(if (pos % 2 == 0) Color(0xfff5f5f5) else theme.container)) {

          TextPrimary((pos + 1).toString(), modifier = Modifier.padding(horizontal = 16.dp))

          Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
          ) {
            if (!song.valid()) {
              TextPrimary(stringResource(R.string.song_lose_effect))
            } else {
              TextPrimary(
                song.title,
                color = if (song == musicState.song) theme.secondary else theme.textPrimary
              )
              TextSecondary(song.artist)
            }
          }

          if (song.valid()) {
            CenterInBox(
              modifier = Modifier
                .clickWithRipple {
                  playbackVM.removeFromQueue(song.id)
                }
                .padding(horizontal = 16.dp)
            ) {
              Image(
                modifier = Modifier.size(24.dp),
                painter = painterResource(R.drawable.ic_close),
                contentDescription = "PlayQueueDelete",
                colorFilter = ColorFilter.tint(theme.textSecondary)
              )
            }
          }
        }
      }
    }

    LaunchedEffect(state.isVisible) {
      if (state.isVisible) {
        val index = songs.indexOfFirst { it.id == playbackState.song.id }
        if (index != -1) {
          lazyState.scrollToItem(index)
        }
      }
    }
  }
}

@Composable
private fun RowScope.ControlButton(
  onClick: () -> Unit,
  content: @Composable BoxScope.() -> Unit
) {
  Box(
    modifier = Modifier
      .weight(1f, LocalContext.current.isPortraitOrientation())
      .aspectRatio(1f)
      .clickWithRipple { onClick() },
    contentAlignment = Alignment.Center
  ) {
    content()
  }
}

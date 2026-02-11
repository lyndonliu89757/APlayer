package remix.myplayer.ui.screen.playing

import android.content.Context.AUDIO_SERVICE
import android.media.AudioManager
import android.media.AudioManager.STREAM_MUSIC
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.palette.graphics.Palette
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import remix.myplayer.R
import remix.myplayer.util.ext.CenterInBox
import remix.myplayer.service.playback.PlaybackUiState
import remix.myplayer.util.ext.clickWithRipple
import remix.myplayer.ui.widget.common.LineSlider
import remix.myplayer.ui.widget.common.defaultLineSliderProperties

@Composable
internal fun PlayingBottomBar(
  musicState: PlaybackUiState,
  swatch: Palette.Swatch
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  ) {
    val swatchColor = Color(swatch.rgb)

    VolumeSeekbar(swatchColor)

    // 展示下一首歌
    CenterInBox(
      modifier = Modifier.fillMaxWidth()
    ) {
      Text(
        text = stringResource(R.string.next_song, musicState.nextSong.title),
        color = Color(
          "#a8a8a8".toColorInt()
        ),
        fontSize = 14.sp,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun VolumeSeekbar(swatchColor: Color) {
  val context = LocalContext.current
  val audioManager = remember {
    context.getSystemService(AUDIO_SERVICE) as AudioManager
  }
  Row(
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    CenterInBox(
      Modifier
        .size(48.dp)
        .clickWithRipple {
          audioManager.adjustStreamVolume(
            STREAM_MUSIC,
            AudioManager.ADJUST_LOWER,
            AudioManager.FLAG_PLAY_SOUND
          )
        }) {
      Image(
        modifier = Modifier.size(26.dp),
        painter = painterResource(R.drawable.ic_mute),
        contentDescription = "PlayingBottomBarVolumeDown",
        colorFilter = ColorFilter.tint(swatchColor.copy(0.6f))
      )
    }

    VolumeSeekBar(audioManager, swatchColor)

    CenterInBox(
      Modifier
        .size(48.dp)
        .clickWithRipple {
          audioManager.adjustStreamVolume(
            STREAM_MUSIC,
            AudioManager.ADJUST_RAISE,
            AudioManager.FLAG_PLAY_SOUND
          )
        }) {
      Image(
        modifier = Modifier.size(26.dp),
        painter = painterResource(R.drawable.ic_voice),
        contentDescription = "PlayingBottomBarVolumeUp",
        colorFilter = ColorFilter.tint(swatchColor.copy(0.8f))
      )
    }
  }
}

@Composable
private fun RowScope.VolumeSeekBar(
  audioManager: AudioManager,
  swatchColor: Color,
) {
  var min by remember {
    mutableIntStateOf(0)
  }
  var max by remember {
    mutableIntStateOf(1)
  }
  var current by remember {
    mutableIntStateOf(0)
  }

  LineSlider(
    value = current.toFloat(),
    valueRange = min.toFloat()..max.toFloat(),
    onValueChange = {
      current = it.toInt()
    },
    onValueChangeFinished = {
      audioManager.setStreamVolume(
        STREAM_MUSIC,
        current,
        AudioManager.FLAG_PLAY_SOUND
      )
    },
    modifier = Modifier
      .height(48.dp)
      .weight(1f),
    properties = defaultLineSliderProperties.copy(
      trackHeight = 4.dp,
      trackBackgroundColor = playingTrackBackgroundColor,
      trackProgressColor = swatchColor,
      thumbColor = swatchColor,
      thumbWidth = 4.dp,
      thumbHeight = 4.dp,
      thumbShape = RectangleShape
    )
  )

  LaunchedEffect(Unit) {
    min = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      audioManager.getStreamMinVolume(STREAM_MUSIC)
    } else 0
    max = audioManager.getStreamMaxVolume(STREAM_MUSIC)
  }

  val scope = rememberCoroutineScope()
  DisposableEffect(Unit) {
    scope.launch {
      while (isActive) {
        current = audioManager.getStreamVolume(STREAM_MUSIC)
        delay(1000)
      }
    }

    onDispose {

    }
  }
}
package remix.myplayer.ui.screen.playing

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.palette.graphics.Palette
import remix.myplayer.R
import remix.myplayer.data.model.audio.Song
import remix.myplayer.util.ext.clickWithRipple
import remix.myplayer.ui.nav.LocalNavController

@Composable
@Stable
internal fun PlayingTopBar(song: Song, swatch: Palette.Swatch) {
  val titleColor = Color(swatch.titleTextColor)
  val bodyColor = Color(swatch.bodyTextColor)
  val nav = LocalNavController.current

  Row(modifier = Modifier.height(56.dp), verticalAlignment = Alignment.CenterVertically) {
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .padding(8.dp)
        .size(40.dp)
        .clickWithRipple {
          nav.popBackStack()
        }
    ) {
      Image(
        modifier = Modifier.size(24.dp),
        painter = painterResource(R.drawable.ic_back),
        colorFilter = ColorFilter.tint(titleColor),
        contentDescription = "PlayingBack"
      )
    }

    Column(
      modifier = Modifier
        .weight(1f),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        song.displayName.ifEmpty { stringResource(R.string.unknown_song) },
        color = titleColor,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1
      )
      Text(String.format("《%s》", song.album), color = bodyColor, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }

    var expanded by remember { mutableStateOf(false) }

    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .padding(8.dp)
        .size(40.dp)
        .clickWithRipple {
          expanded = !expanded
        }
    ) {
      Image(
        modifier = Modifier.size(30.dp),
        painter = painterResource(R.drawable.ic_more),
        colorFilter = ColorFilter.tint(titleColor),
        contentDescription = "PlayingMore"
      )

      PlayingDropDownMenu(expanded, song) {
        expanded = false
      }
    }
  }
}
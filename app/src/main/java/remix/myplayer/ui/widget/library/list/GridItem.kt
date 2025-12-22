package remix.myplayer.ui.widget.library.list

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import remix.myplayer.data.model.audio.APlayerModel
import remix.myplayer.ui.theme.LocalTheme
import remix.myplayer.ui.widget.common.TextPrimary
import remix.myplayer.ui.widget.common.TextSecondary
import remix.myplayer.ui.widget.library.GlideCover
import remix.myplayer.ui.widget.popup.LibraryItemPopupButton

@Composable
fun GridItem(
  model: APlayerModel,
  text1: String,
  text2: String? = null,
  selected: Boolean,
  onClick: () -> Unit,
  onLongClick: () -> Unit
) {
  val theme = LocalTheme.current

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(8.dp)
      .background(Color.Transparent)
  ) {
    ElevatedCard(
      modifier = Modifier
        .fillMaxWidth()
        .combinedClickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = ripple(color = theme.ripple),
          onClick = { onClick() },
          onLongClick = { onLongClick() }
        ),
      colors = CardDefaults.cardColors().copy(containerColor = if (selected) theme.select else theme.container)
    ) {
      GlideCover(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(1f),
        model = model,
        circle = false
      )

      Row(verticalAlignment = Alignment.CenterVertically) {
        Column(
          modifier = Modifier
            .padding(start = 10.dp, top = 6.dp, bottom = 6.dp)
            .weight(1f)
        ) {
          TextPrimary(text = text1, maxLine = 2)
          TextSecondary(text2 ?: "")
        }

        LibraryItemPopupButton(model = model)
      }
    }
  }
}
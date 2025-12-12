package remix.myplayer.ui.dialog

import androidx.compose.runtime.Composable

/**
 * some common and reusable dialog
 */
@Composable
fun DialogContainer() {
  LoadingDialog()

  TimerDialog()

  RemoveSongDialog()

  InAppUpdateDialog()

  SongDetailDialog()

  SongEditDialog()
}
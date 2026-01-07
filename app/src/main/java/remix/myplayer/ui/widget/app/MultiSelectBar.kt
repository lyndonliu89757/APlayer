package remix.myplayer.ui.widget.app

import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import remix.myplayer.R
import remix.myplayer.data.db.room.entity.PlayList
import remix.myplayer.data.model.audio.APlayerModel
import remix.myplayer.ui.theme.LocalTheme
import remix.myplayer.viewmodel.MultiSelectState
import remix.myplayer.viewmodel.libraryViewModel
import remix.myplayer.viewmodel.mainViewModel
import remix.myplayer.viewmodel.playbackViewModel
import remix.myplayer.viewmodel.settingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiSelectBar(
  state: MultiSelectState,
  scrollBehavior: TopAppBarScrollBehavior?,
  parent: APlayerModel? = null,
  onSelectAll: (() -> Unit)? = null
) {
  val libraryVM = libraryViewModel
  val mainVM = mainViewModel
  val settingVM = settingViewModel
  val playbackVM = playbackViewModel
  val theme = LocalTheme.current
  val tintColor = if (theme.isPrimaryLight) Color.Black else Color.White
  val scope = rememberCoroutineScope()

  TopAppBar(
    scrollBehavior = scrollBehavior,
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = theme.primary,
      scrolledContainerColor = theme.primary,
      navigationIconContentColor = Color.White,
      actionIconContentColor = Color.White,
    ),
    title = {
      Text(
        stringResource(R.string.song_list_select_title_format, state.selectedModels.size),
        fontSize = 20.sp,
        color = tintColor
      )
    },
    navigationIcon = {
      IconButton(onClick = {
        mainVM.closeMultiSelect()
      }) {
        Icon(
          painter = painterResource(R.drawable.ic_close),
          contentDescription = stringResource(R.string.close),
          tint = tintColor
        )
      }
    },
    actions = {
      TooltipButton(R.string.add_to_play_queue, R.drawable.ic_playlist_add_white_24dp, tintColor) {
        scope.launch {
          mainVM.closeMultiSelect()
          val songs =
            withContext(Dispatchers.IO) { libraryVM.loadSongsByModels(state.selectedModels) }
          if (songs.isNotEmpty()) {
            playbackVM.insertToQueue(songs)
          }
        }
      }

      var morePopupExpand by rememberSaveable {
        mutableStateOf(false)
      }
      TooltipButton(R.string.more, R.drawable.ic_more, tintColor, extraContent = {
        DropdownMenu(
          modifier = Modifier.wrapContentSize(),
          expanded = morePopupExpand,
          containerColor = LocalTheme.current.dialogBackground,
          onDismissRequest = { morePopupExpand = false }
        ) {
          DropdownMenuItem(
            text = {
              Text(
                stringResource(R.string.select_all),
                color = Color.Black,
                fontSize = 16.sp
              )
            },
            onClick = {
              scope.launch {
                val allModels = when (mainVM.multiSelectState.value.where) {
                  MultiSelectState.Where.Song -> libraryVM.songs.value
                  MultiSelectState.Where.Album -> libraryVM.albums.value
                  MultiSelectState.Where.Artist -> libraryVM.artists.value
                  MultiSelectState.Where.Folder -> libraryVM.folders.value
                  MultiSelectState.Where.Detail -> withContext(Dispatchers.IO) {
                    libraryVM.loadSongsByModels(
                      listOf(parent!!)
                    )
                  }

                  else -> throw Exception("unknown type")
                }
                mainVM.updateMultiSelectModelsAll(allModels)
              }

              morePopupExpand = false
            }
          )
        }
      }) {
        morePopupExpand = !morePopupExpand
      }
    })

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TooltipButton(
  strRes: Int,
  drRes: Int,
  tintColor: Color,
  extraContent: (@Composable () -> Unit)? = null,
  onClick: () -> Unit
) {
  TooltipBox(
    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
    tooltip = { Text(stringResource(strRes)) },
    state = rememberTooltipState()
  ) {
    extraContent?.invoke()

    IconButton(onClick = onClick) {
      Icon(
        painter = painterResource(drRes),
        contentDescription = stringResource(strRes),
        tint = tintColor
      )
    }
  }
}
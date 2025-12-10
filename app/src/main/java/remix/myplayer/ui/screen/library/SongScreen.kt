package remix.myplayer.ui.screen.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import remix.myplayer.R
import remix.myplayer.misc.helper.MusicServiceRemote.setPlayQueue
import remix.myplayer.service.Command
import remix.myplayer.service.MusicService
import remix.myplayer.ui.theme.LocalTheme
import remix.myplayer.ui.widget.library.SongListHeader
import remix.myplayer.ui.widget.library.list.ListSong
import remix.myplayer.util.MusicUtil
import remix.myplayer.viewmodel.MultiSelectState
import remix.myplayer.viewmodel.libraryViewModel
import remix.myplayer.viewmodel.mainViewModel
import remix.myplayer.viewmodel.playbackViewModel

@Composable
fun SongScreen(scrollToCurrentEvent: SharedFlow<Unit>? = null) {
  val libraryVM = libraryViewModel
  val mainVM = mainViewModel

  val playbackState by playbackViewModel.playbackUiState.collectAsStateWithLifecycle()
  val multiSelectState by mainVM.multiSelectState.collectAsStateWithLifecycle()
  val listState = rememberLazyListState()
  val songs by libraryVM.songs.collectAsStateWithLifecycle()
  val theme = LocalTheme.current
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  // 计算是否显示滚动到当前歌曲的按钮（当前歌曲不在可视区域内时显示）
  val showScrollToCurrent by remember {
    derivedStateOf {
      val visibleItems = listState.layoutInfo.visibleItemsInfo
      if (visibleItems.isEmpty() || songs.isEmpty()) return@derivedStateOf false

      val currentSongIndex = songs.indexOfFirst { it.id == playbackState.song.id }
      if (currentSongIndex == -1) return@derivedStateOf false

      // 检查当前歌曲是否在可见范围内
      !visibleItems.any { it.index == currentSongIndex }
    }
  }

  LaunchedEffect(scrollToCurrentEvent) {
    scrollToCurrentEvent?.collect {
      val index = libraryVM.songs.value.indexOfFirst { it.id == playbackState.song.id }
      if (index != -1) {
        listState.animateScrollToItem(index)
      }
    }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize()) {
      if (songs.isNotEmpty()) {
        SongListHeader(songs)
      }

      val selectedIds by remember {
        derivedStateOf {
          multiSelectState.selectedModels(MultiSelectState.Where.Song)
        }
      }

      // TODO LocationRecyclerView
      LazyColumn(state = listState, modifier = Modifier.weight(1f)) {
        itemsIndexed(songs, key = { _, song ->
          song.id
        }) { pos, song ->
          val selected = selectedIds.contains(song.getKey())
          val isPlayingSong = playbackState.song.id == song.id

          ListSong(
            modifier = Modifier.height(64.dp),
            song = song,
            modelParent = song,
            selected = selected,
            playing = isPlayingSong,
            onClickSong = {
              if (songs.isEmpty()) {
                return@ListSong
              }

              if (multiSelectState.where == MultiSelectState.Where.Song) {
                mainVM.updateMultiSelectModel(song)
                return@ListSong
              }

              setPlayQueue(
                songs, MusicUtil.makeCmdIntent(Command.PLAY_AT)
                  .putExtra(MusicService.EXTRA_POSITION, pos)
              )
            },
            onLongClickSong = {
              mainVM.showMultiSelect(context, MultiSelectState.Where.Song, song)
            })
        }
      }
    }

    // 滚动到当前歌曲的悬浮按钮
    AnimatedVisibility(
      visible = showScrollToCurrent && songs.isNotEmpty(),
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(16.dp)
    ) {
      FloatingActionButton(
        modifier = Modifier
          .size(40.dp)
          .offset(x = (-40).dp, y = (-30).dp),
        onClick = {
          coroutineScope.launch {
            val index = songs.indexOfFirst { it.id == playbackState.song.id }
            if (index != -1) {
              listState.animateScrollToItem(index)
            }
          }
        },
        containerColor = theme.background,
        contentColor = theme.textSecondary
      ) {
        Icon(
          painter = painterResource(R.drawable.ic_my_location_24dp),
          contentDescription = "定位到当前播放歌曲",
        )
      }
    }
  }
}
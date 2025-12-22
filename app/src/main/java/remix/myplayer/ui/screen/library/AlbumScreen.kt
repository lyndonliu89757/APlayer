package remix.myplayer.ui.screen.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import remix.myplayer.misc.spanCount
import remix.myplayer.ui.nav.DetailScreenRoute
import remix.myplayer.ui.nav.LocalNavController
import remix.myplayer.ui.theme.LocalTheme
import remix.myplayer.ui.verticalScrollbar
import remix.myplayer.ui.widget.library.list.GridItem
import remix.myplayer.viewmodel.MultiSelectState
import remix.myplayer.viewmodel.libraryViewModel
import remix.myplayer.viewmodel.mainViewModel

@Composable
fun AlbumScreen() {
  val albums by libraryViewModel.albums.collectAsStateWithLifecycle()
  val nav = LocalNavController.current

  val mainVM = mainViewModel
  val multiSelectState by mainVM.multiSelectState.collectAsStateWithLifecycle()
  val context = LocalContext.current

  Column(
    modifier = Modifier
      .background(LocalTheme.current.background)
      .padding(horizontal = 10.dp)
  ) {
    val selectedIds by remember {
      derivedStateOf {
        multiSelectState.selectedModels(MultiSelectState.Where.Album)
      }
    }

    val gridState = rememberLazyGridState()
    LazyVerticalGrid(
      modifier = Modifier
        .weight(1f)
        .verticalScrollbar(gridState),
      state = gridState,
      columns = GridCells.Fixed(spanCount()),
      content = {
        items(albums, key = {
          it.albumID
        }) { album ->
          GridItem(
            album, album.album, album.artist,
            selected = selectedIds.contains(album.getKey()),
            onClick = {
              if (multiSelectState.where == MultiSelectState.Where.Album) {
                mainVM.updateMultiSelectModel(album)
                return@GridItem
              }

              nav.navigate(DetailScreenRoute(album = album))
            }, onLongClick = {
              mainVM.showMultiSelect(context, MultiSelectState.Where.Album, album)
            })
        }
      })

  }
}

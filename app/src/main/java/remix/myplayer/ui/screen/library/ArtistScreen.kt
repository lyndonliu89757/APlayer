package remix.myplayer.ui.screen.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import remix.myplayer.R
import remix.myplayer.ui.nav.DetailScreenRoute
import remix.myplayer.ui.nav.LocalNavController
import remix.myplayer.ui.theme.LocalTheme
import remix.myplayer.util.ext.verticalScrollbar
import remix.myplayer.ui.widget.library.list.ListItem
import remix.myplayer.viewmodel.MultiSelectState
import remix.myplayer.viewmodel.libraryViewModel
import remix.myplayer.viewmodel.mainViewModel

@Composable
fun ArtistScreen() {
  val artists by libraryViewModel.artists.collectAsStateWithLifecycle()
  val nav = LocalNavController.current

  val mainVM = mainViewModel
  val multiSelectState by mainVM.multiSelectState.collectAsStateWithLifecycle()
  val context = LocalContext.current

  Column(
    modifier = Modifier.background(LocalTheme.current.background)
  ) {
    val selectedIds by remember {
      derivedStateOf {
        multiSelectState.selectedModels(MultiSelectState.Where.Artist)
      }
    }

    val listState = rememberLazyListState()
    LazyColumn(
      state = listState,
      modifier = Modifier
        .weight(1f)
        .verticalScrollbar(listState)
    ) {
      itemsIndexed(artists, key = { _, artist ->
        artist.artistID
      }) { _, artist ->
        ListItem(
          modifier = Modifier.height(64.dp),
          model = artist,
          text1 = artist.artist,
          selected = selectedIds.contains(artist.getKey()),
          text2 = pluralStringResource(R.plurals.song_num, artist.count, artist.count),
          onClick = {
            if (multiSelectState.where == MultiSelectState.Where.Artist) {
              mainVM.updateMultiSelectModel(artist)
              return@ListItem
            }

            nav.navigate(DetailScreenRoute(artist = artist))
          },
          onLongClick = {
            mainVM.showMultiSelect(context, MultiSelectState.Where.Artist, artist)
          })
      }
    }

  }
}
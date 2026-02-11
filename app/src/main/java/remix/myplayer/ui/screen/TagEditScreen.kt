@file:OptIn(ExperimentalMaterial3Api::class)

package remix.myplayer.ui.screen

import android.net.Uri
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import remix.myplayer.R
import remix.myplayer.misc.cache.DiskCache
import remix.myplayer.ui.activity.base.BaseActivity
import remix.myplayer.ui.dialog.DialogState
import remix.myplayer.ui.dialog.ItemsCallback
import remix.myplayer.ui.dialog.NormalDialog
import remix.myplayer.ui.dialog.rememberDialogState
import remix.myplayer.ui.nav.LocalNavController
import remix.myplayer.ui.nav.MessageNotifier
import remix.myplayer.ui.nav.RouteTagEditCrop
import remix.myplayer.ui.theme.LocalTheme
import remix.myplayer.ui.widget.common.CommonAppBar
import remix.myplayer.ui.widget.common.EditField
import remix.myplayer.ui.widget.common.TextPrimary
import remix.myplayer.util.ImageUtil
import remix.myplayer.util.ext.clickableWithoutRipple
import remix.myplayer.viewmodel.tagEditViewModel
import timber.log.Timber
import java.io.File

@Composable
fun TagEditScreen(backStackEntry: NavBackStackEntry) {
  val tagEditVM = tagEditViewModel
  val state by tagEditVM.tagEditState.collectAsStateWithLifecycle()
  val navController = LocalNavController.current
  val song = state.song

  if (song == null || !song.valid() || !song.isLocal()) {
    LaunchedEffect(Unit) {
      navController.popBackStack()
    }
    return
  }

  val activity = LocalActivity.current as? BaseActivity ?: return
  val editAlbumArtState = rememberDialogState()
  val scope = rememberCoroutineScope()

  val cropDestinationUri = remember(song) {
    val cacheDir = DiskCache.getDiskCacheDir(activity, "song_cover")
    if (!cacheDir.exists() && !cacheDir.mkdirs()) {
      Uri.EMPTY
    } else {
      Uri.fromFile(File(cacheDir, "song-${song.id}.jpg"))
    }
  }

  val formState = state.tagFormState
  val albumArtState = state.albumArtState

  val cropResult by backStackEntry.savedStateHandle.getStateFlow<Long?>(
    "song_crop_result",
    null
  ).collectAsStateWithLifecycle()

  LaunchedEffect(cropResult) {
    Timber.v("cropResult: $cropResult")
    if (cropResult != null) {
      scope.launch {
        val bitmap = withContext(Dispatchers.IO) {
          ImageUtil.loadScaledBitmap(activity, cropDestinationUri, 1024)
        }
        if (bitmap != null) {
          tagEditVM.onTagEditAlbumArtPicked(bitmap)
        } else {
          MessageNotifier.show(R.string.save_error)
        }
      }
      backStackEntry.savedStateHandle.remove<Long>("song_crop_result")
    }
  }

  Scaffold(
    topBar = {
      CommonAppBar(
        title = stringResource(R.string.song_edit),
        actions = {
          TextPrimary(
            stringResource(R.string.split), modifier = Modifier
              .clickable(
                onClick = {
                  val arr = song.displayName.split(" - ")
                  if (arr.size == 2) {
                    tagEditVM.updateTagEditArtist(arr[0])
                    tagEditVM.updateTagEditTitle(arr[1])
                  }
                }
              )
              .padding(4.dp),
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold)

          IconButton(onClick = {
            tagEditVM.saveTagEdit(activity)
            navController.popBackStack()
          }) {
            Icon(
              painterResource(R.drawable.ic_save),
              contentDescription = "CustomSortSave",
              tint = Color.White,
              modifier = Modifier.size(28.dp)
            )
          }
        }
      )
    },
    containerColor = LocalTheme.current.background
  ) { contentPadding ->
    ProvideTextStyle(TextStyle(color = LocalTheme.current.textPrimary, fontSize = 18.sp)) {
      Column(
        modifier = Modifier
          .padding(contentPadding)
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
      ) {
        val modifier = Modifier
          .fillMaxWidth(0.7f)
          .aspectRatio(1f)
          .clip(RoundedCornerShape(12.dp))
          .clickable {
            editAlbumArtState.show()
          }

        if (albumArtState.albumArt != null) {
          Image(
            bitmap = albumArtState.albumArt,
            contentDescription = "AlbumBitmap",
            contentScale = ContentScale.Crop,
            modifier = modifier
          )
        } else {
          Box(
            modifier = modifier.background(Color.LightGray)
          )
        }

        val fieldModifier = Modifier.fillMaxWidth()

        EditField(
          formState.title,
          R.string.song_name_input_hint,
          isError = formState.title.isEmpty(),
          modifier = fieldModifier
        ) {
          tagEditVM.updateTagEditTitle(it)
        }
        EditField(formState.album, R.string.album_input_hint, modifier = fieldModifier) {
          tagEditVM.updateTagEditAlbum(it)
        }
        EditField(formState.artist, R.string.artist_input_hint, modifier = fieldModifier) {
          tagEditVM.updateTagEditArtist(it)
        }
        EditField(formState.genre, R.string.genre_input_hint, modifier = fieldModifier) {
          tagEditVM.updateTagEditGenre(it)
        }
        Row(
          modifier = fieldModifier,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          EditField(formState.year, R.string.year_input_hint, modifier = Modifier.weight(1f)) {
            tagEditVM.updateTagEditYear(it)
          }
          EditField(
            formState.track,
            R.string.track_number_input_hint,
            modifier = Modifier.weight(1f)
          ) {
            tagEditVM.updateTagEditTrack(it)
          }
        }
        ProvideTextStyle(TextStyle(fontSize = 16.sp)) {
          EditField(
            formState.lyrics,
            R.string.lyrics_input_hint,
            isLast = true,
            maxLine = 10,
            modifier = fieldModifier,
            onDone = {
              tagEditVM.saveTagEdit(activity)
            }) {
            tagEditVM.updateTagEditLyrics(it)
          }
        }
      }
    }
  }

  EditAlbumArtDialog(editAlbumArtState) { pos, _ ->
    when (pos) {
      0 -> {
        if (cropDestinationUri == Uri.EMPTY) {
          MessageNotifier.show(R.string.save_error)
        } else {
          navController.navigate("$RouteTagEditCrop/${Uri.encode(cropDestinationUri.toString())}")
        }
      }

      1 -> {
        tagEditVM.onTagEditAlbumArtCleared()
      }
    }
  }
}

@Composable
private fun EditAlbumArtDialog(state: DialogState, onItemsCallback: ItemsCallback) {
  NormalDialog(
    dialogState = state,
    items = listOf(stringResource(R.string.select_image), stringResource(R.string.clear_cover)),
    itemsCallback = onItemsCallback,
    negative = null,
    positive = null
  )
}

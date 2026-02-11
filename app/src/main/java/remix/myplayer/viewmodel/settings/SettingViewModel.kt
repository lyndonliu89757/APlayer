package remix.myplayer.viewmodel.settings

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import remix.myplayer.R
import remix.myplayer.data.db.room.entity.PlayList
import remix.myplayer.data.model.audio.APlayerModel
import remix.myplayer.data.model.audio.Song
import remix.myplayer.data.model.misc.LyricOrder
import remix.myplayer.data.prefs.LyricPrefs
import remix.myplayer.data.prefs.SettingPrefs
import remix.myplayer.lyric.LyricManager
import remix.myplayer.util.ext.updateIf
import remix.myplayer.repo.usecase.DeleteSongUseCase
import remix.myplayer.service.playback.MusicStateSource
import remix.myplayer.ui.activity.base.BaseActivity
import remix.myplayer.ui.dialog.DeleteSongState
import remix.myplayer.ui.dialog.DialogState
import remix.myplayer.ui.dialog.ImportPlayListState
import remix.myplayer.ui.dialog.ReNamePlayListState
import remix.myplayer.ui.dialog.SongDetailState
import remix.myplayer.ui.dialog.SongEditState
import remix.myplayer.ui.dialog.runWithLoading
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
  val settingPrefs: SettingPrefs,
  val lyricPrefs: LyricPrefs,
  val lyricManager: LyricManager
) : ViewModel() {

  @Inject
  lateinit var deleteSongUseCase: DeleteSongUseCase

  // 设置状态
  private val _settingsState = MutableStateFlow(loadState())
  val settingsState = _settingsState.asStateFlow()

  private fun loadState(): SettingsState = SettingsState(
    common = CommonSettings(
      scanSize = settingPrefs.scanSize,
      forceSort = settingPrefs.forceSort,
      manualScanFolder = settingPrefs.manualScanFolder,
      blacklist = settingPrefs.blacklist,
      deleteIds = settingPrefs.deleteIds,
      language = settingPrefs.language,
      showDisplayName = settingPrefs.showDisplayName
    ),
    play = PlaySettings(
      ignoreAudioFocus = settingPrefs.ignoreAudioFocus,
      playAtBreakPoint = settingPrefs.playAtBreakPoint,
      crossFade = settingPrefs.crossFade,
      autoPlay = settingPrefs.autoPlay,
      speed = settingPrefs.speed,
    ),
    library = LibrarySettings(
      songSortOrder = settingPrefs.songSortOrder,
      albumSortOrder = settingPrefs.albumSortOrder,
      artistSortOrder = settingPrefs.artistSortOrder,
      genreSortOrder = settingPrefs.genreSortOrder,
      playlistSortOrder = settingPrefs.playlistSortOrder,
      albumDetailSortOrder = settingPrefs.albumDetailSortOrder,
      artistDetailSortOrder = settingPrefs.artistDetailSortOrder,
      genreDetailSortOrder = settingPrefs.genreDetailSortOrder,
      playListDetailSortOrder = settingPrefs.playListDetailSortOrder,
      folderDetailSortOrder = settingPrefs.folderDetailSortOrder,
    ),
    playingScreen = PlayingScreenSettings(
      background = settingPrefs.playingScreenBackground,
      keepScreenOn = settingPrefs.keepScreenOn
    ),
    cover = CoverSettings(
      ignoreMediaStore = settingPrefs.ignoreMediaStore,
      autoDownloadCover = settingPrefs.autoDownloadCover,
      downloadSource = settingPrefs.downloadSource
    ),
    lyric = LyricSettings(
      desktopLyricEnabled = lyricPrefs.desktopLyricEnabled,
      statusBarLyricEnabled = lyricPrefs.statusBarLyricEnabled,
      fontScale = lyricPrefs.fontScale,
      generalLyricOrder = lyricPrefs.generalLyricOrderList
    ),
  )

  // -------- Common 分组 ----------
  fun setScanSize(kb: Int) {
    settingPrefs.scanSize = kb
    _settingsState.update { it.copy(common = it.common.copy(scanSize = kb)) }
  }

  fun setForceSort(enabled: Boolean) {
    settingPrefs.forceSort = enabled
    _settingsState.update { it.copy(common = it.common.copy(forceSort = enabled)) }
  }

  fun setManualScanFolder(path: String) {
    settingPrefs.manualScanFolder = path
    _settingsState.update { it.copy(common = it.common.copy(manualScanFolder = path)) }
  }

  fun setBlacklist(values: Set<String>) {
    settingPrefs.blacklist = values
    _settingsState.update { it.copy(common = it.common.copy(blacklist = values)) }
  }

  fun setDeleteIds(values: Set<String>) {
    settingPrefs.deleteIds = values
    _settingsState.update { it.copy(common = it.common.copy(deleteIds = values)) }
  }

  fun setShowDisplayName(enabled: Boolean) {
    settingPrefs.showDisplayName = enabled
    _settingsState.update { it.copy(common = it.common.copy(showDisplayName = enabled)) }
  }

  // -------- Play 分组 ----------
  fun setIgnoreAudioFocus(enabled: Boolean) {
    settingPrefs.ignoreAudioFocus = enabled
    _settingsState.update { it.copy(play = it.play.copy(ignoreAudioFocus = enabled)) }
  }

  fun setPlayAtBreakPoint(enabled: Boolean) {
    settingPrefs.playAtBreakPoint = enabled
    _settingsState.update { it.copy(play = it.play.copy(playAtBreakPoint = enabled)) }
  }

  fun setCrossFade(enabled: Boolean) {
    settingPrefs.crossFade = enabled
    _settingsState.update { it.copy(play = it.play.copy(crossFade = enabled)) }
  }

  fun setAutoPlay(mode: Int) {
    settingPrefs.autoPlay = mode
    _settingsState.update { it.copy(play = it.play.copy(autoPlay = mode)) }
  }

  fun setSpeed(speed: String) {
    settingPrefs.speed = speed
    _settingsState.update { it.copy(play = it.play.copy(speed = speed)) }
  }

  // 统一设置排序
  fun setSortOrder(category: SortCategory, order: String): Boolean {
    if (category.saveOrder(order, settingPrefs)) {
      _settingsState.update {
        val lib = it.library
        it.copy(
          library = when (category) {
            SortCategory.SONG -> lib.copy(songSortOrder = order)
            SortCategory.ALBUM -> lib.copy(albumSortOrder = order)
            SortCategory.ARTIST -> lib.copy(artistSortOrder = order)
            SortCategory.PLAYLIST -> lib.copy(playlistSortOrder = order)
            SortCategory.GENRE -> lib.copy(genreSortOrder = order)
            SortCategory.ALBUM_DETAIL -> lib.copy(albumDetailSortOrder = order)
            SortCategory.ARTIST_DETAIL -> lib.copy(artistDetailSortOrder = order)
            SortCategory.PLAYLIST_DETAIL -> lib.copy(playListDetailSortOrder = order)
            SortCategory.GENRE_DETAIL -> lib.copy(genreDetailSortOrder = order)
            SortCategory.FOLDER_DETAIL -> lib.copy(folderDetailSortOrder = order)
          }
        )
      }
      return true
    }

    return false
  }

  // -------- PlayingScreen 分组 ----------
  fun setPlayingScreenBackground(bg: Int) {
    settingPrefs.playingScreenBackground = bg
    _settingsState.update { it.copy(playingScreen = it.playingScreen.copy(background = bg)) }
  }

  fun setKeepScreenOn(enabled: Boolean) {
    settingPrefs.keepScreenOn = enabled
    _settingsState.update { it.copy(playingScreen = it.playingScreen.copy(keepScreenOn = enabled)) }
  }

  // -------- Cover 分组 ----------
  fun setIgnoreMediaStore(enabled: Boolean) {
    settingPrefs.ignoreMediaStore = enabled
    _settingsState.update { it.copy(cover = it.cover.copy(ignoreMediaStore = enabled)) }
  }

  fun setAutoDownloadCover(mode: Int) {
    settingPrefs.autoDownloadCover = mode
    _settingsState.update { it.copy(cover = it.cover.copy(autoDownloadCover = mode)) }
  }

  fun setDownloadSource(source: Int) {
    settingPrefs.downloadSource = source
    _settingsState.update { it.copy(cover = it.cover.copy(downloadSource = source)) }
  }

  // -------- Lyric 分组 ----------
  fun setDesktopLyricEnabled(enabled: Boolean) {
    lyricManager.isDesktopLyricEnabled = enabled
    _settingsState.update { it.copy(lyric = it.lyric.copy(desktopLyricEnabled = enabled)) }
  }

  fun setStatusBarLyricEnabled(enabled: Boolean) {
    lyricManager.isStatusBarLyricEnabled = enabled
    _settingsState.update { it.copy(lyric = it.lyric.copy(statusBarLyricEnabled = enabled)) }
  }

  fun setLyricFontScale(scale: Float) {
    lyricPrefs.fontScale = scale
    _settingsState.update { it.copy(lyric = it.lyric.copy(fontScale = scale)) }
  }

  fun setGeneralLyricOrder(orderList: List<LyricOrder>) {
    viewModelScope.launch(Dispatchers.IO) {
      // 清除歌词相关配置和缓存
      lyricPrefs.clearUserSave()
      lyricPrefs.generalLyricOrder = Json.encodeToString(orderList)
      lyricManager.clearAllCache(includePersistent = true)
      // 重新获取歌词
      lyricManager.updateLyrics(MusicStateSource.currentPlaybackUiState.song)
    }
    _settingsState.update { it.copy(lyric = it.lyric.copy(generalLyricOrder = orderList)) }
  }

  // -------- PlayList 分组 ----------

  private val _addSongToPlayListState =
    MutableStateFlow(ImportPlayListState(DialogState(false), DialogState(false)))
  val addSongToPlayListState = _addSongToPlayListState.asStateFlow()

  fun showAddSongToPlayListDialog(songIds: List<Long>, initialText: String = "") {
    _addSongToPlayListState.updateIf(
      condition = { !it.rootDialogState.isOpen },
      transform = {
        it.rootDialogState.show()
        it.copy(
          inputText = initialText,
          songIds = songIds
        )
      }
    )
  }

  fun updateImportPlayListState(text: String) {
    _addSongToPlayListState.update {
      it.copy(inputText = text)
    }
  }

  private val _deleteSongState =
    MutableStateFlow(DeleteSongState(deleteSource = settingPrefs.deleteSource))
  val deleteSongState = _deleteSongState.asStateFlow()

  fun showDeleteSongDialog(
    models: List<APlayerModel>,
    titleRes: Int = R.string.confirm_delete_from_library,
    parent: APlayerModel? = null
  ) {
    _deleteSongState.updateIf(
      condition = { !it.dialogState.isOpen },
      transform = {
        it.dialogState.show()
        it.copy(
          models = models,
          titleRes = titleRes,
          parent = parent
        )
      }
    )
  }

  fun updateDeleteSongState(deleteSource: Boolean) {
    _deleteSongState.update {
      it.copy(deleteSource = deleteSource)
    }
  }

  fun deleteSongs(
    activity: BaseActivity?,
    models: List<APlayerModel>,
    deleteSource: Boolean,
    parent: APlayerModel?
  ) =
    viewModelScope.runWithLoading {
      deleteSongUseCase(activity, models, deleteSource, parent)
    }

  private val _reNamePlayListState = MutableStateFlow(ReNamePlayListState(DialogState()))
  val reNamePlayListState = _reNamePlayListState.asStateFlow()

  fun showReNamePlayListDialog(playList: PlayList) {
    _reNamePlayListState.updateIf(
      condition = { !it.dialogState.isOpen },
      transform = {
        it.dialogState.show()
        it.copy(playList = playList)
      }
    )
  }

  private val _songDetailState = MutableStateFlow(SongDetailState(DialogState()))
  val songDetailState = _songDetailState.asStateFlow()

  fun showSongDetailDialog(song: Song) {
    _songDetailState.updateIf(
      condition = { !it.dialogState.isOpen },
      transform = {
        it.dialogState.show()
        it.copy(song = song)
      }
    )
  }

  private val _songEditState = MutableStateFlow(SongEditState(DialogState()))
  val songEditState = _songEditState.asStateFlow()

  fun showSongEditDialog(song: Song) {
    _songEditState.updateIf(
      condition = { !it.dialogState.isOpen },
      transform = {
        it.dialogState.show()
        it.copy(song = song)
      }
    )
  }

  fun clearCache(context: Context, andThen: () -> Unit = {}) {
    viewModelScope.launch(Dispatchers.IO) {
      context.cacheDir.deleteRecursively()
      context.externalCacheDir?.deleteRecursively()
      Glide.get(context).clearDiskCache()
      // 只清除非持久化歌词缓存，保留用户手动选择的歌词
      lyricManager.clearAllCache(includePersistent = false)
      andThen()
    }
  }
}

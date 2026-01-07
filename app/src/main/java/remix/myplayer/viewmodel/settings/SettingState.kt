package remix.myplayer.viewmodel.settings

import androidx.compose.runtime.Stable
import remix.myplayer.data.model.misc.LyricOrder

@Stable
data class CommonSettings(
  val scanSize: Int,
  val forceSort: Boolean,
  val manualScanFolder: String,
  val blacklist: Set<String>,
  val deleteIds: Set<String>,
  val language: Int,
  val showDisplayName: Boolean,
)

@Stable
data class PlaySettings(
  val ignoreAudioFocus: Boolean,
  val playAtBreakPoint: Boolean,
  val crossFade: Boolean,
  val autoPlay: Int,
  val speed: String,
)

@Stable
data class LibrarySettings(
  val songSortOrder: String,
  val albumSortOrder: String,
  val artistSortOrder: String,
  val albumDetailSortOrder: String,
  val artistDetailSortOrder: String,
  val folderDetailSortOrder: String,
)

@Stable
data class PlayingScreenSettings(
  val background: Int,
  val keepScreenOn: Boolean,
)

@Stable
data class CoverSettings(
  val ignoreMediaStore: Boolean,
  val autoDownloadCover: Int,
  val downloadSource: Int,
)

@Stable
data class LyricSettings(
  val desktopLyricEnabled: Boolean,
  val statusBarLyricEnabled: Boolean,
  val fontScale: Float,
  val generalLyricOrder: List<LyricOrder>,
)

@Stable
data class SettingsState(
  val common: CommonSettings,
  val play: PlaySettings,
  val library: LibrarySettings,
  val playingScreen: PlayingScreenSettings,
  val cover: CoverSettings,
  val lyric: LyricSettings,
)
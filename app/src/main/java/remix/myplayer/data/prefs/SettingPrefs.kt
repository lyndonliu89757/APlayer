package remix.myplayer.data.prefs

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import remix.myplayer.misc.helper.LanguageHelper.AUTO
import remix.myplayer.misc.helper.SortOrder
import remix.myplayer.util.Constants.MB
import javax.inject.Inject
import javax.inject.Singleton

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SettingPrefsEntryPoint {

  fun settingPrefs(): SettingPrefs
}

@Singleton
class SettingPrefs @Inject constructor(
  @ApplicationContext context: Context
) : AbstractPref(context, PrefKeys.Setting.NAME) {

  var firstLoad by PrefsDelegate(sp, PrefKeys.Setting.FIRST_LOAD, true)

  var scanSize by PrefsDelegate(sp, PrefKeys.Setting.SCAN_SIZE, MB)
  var forceSort by PrefsDelegate(sp, PrefKeys.Setting.FORCE_SORT, false)

  var songSortOrder by PrefsDelegate(sp, PrefKeys.Setting.SONG_SORT_ORDER, SortOrder.SONG_A_Z)
  var albumSortOrder by PrefsDelegate(sp, PrefKeys.Setting.ALBUM_SORT_ORDER, SortOrder.ALBUM_A_Z)
  var artistSortOrder by PrefsDelegate(sp, PrefKeys.Setting.ARTIST_SORT_ORDER, SortOrder.ARTIST_A_Z)

  var albumDetailSortOrder by PrefsDelegate(
    sp,
    PrefKeys.Setting.CHILD_ALBUM_SONG_SORT_ORDER,
    SortOrder.TRACK_NUMBER
  )
  var artistDetailSortOrder by PrefsDelegate(
    sp,
    PrefKeys.Setting.CHILD_ARTIST_SONG_SORT_ORDER,
    SortOrder.SONG_A_Z
  )
  var folderDetailSortOrder by PrefsDelegate(
    sp,
    PrefKeys.Setting.CHILD_FOLDER_SONG_SORT_ORDER,
    SortOrder.SONG_A_Z
  )

  var manualScanFolder by PrefsDelegate(sp, PrefKeys.Setting.MANUAL_SCAN_FOLDER, "")
  var deleteIds by PrefsDelegate(sp, PrefKeys.Setting.BLACKLIST_SONG, emptySet<String>())
  var blacklist by PrefsDelegate(sp, PrefKeys.Setting.BLACKLIST, emptySet<String>())
  var deleteSource by PrefsDelegate(sp, PrefKeys.Setting.DELETE_SOURCE, false)

  var language by PrefsDelegate(sp, PrefKeys.Setting.LANGUAGE, AUTO)
  var playAtBreakPoint by PrefsDelegate(sp, PrefKeys.Setting.PLAY_AT_BREAKPOINT, true)
  var showDisplayName by PrefsDelegate(sp, PrefKeys.Setting.SHOW_DISPLAYNAME, false)

  var ignoreAudioFocus by PrefsDelegate(sp, PrefKeys.Setting.AUDIO_FOCUS, false)
  var autoPlay by PrefsDelegate(sp, PrefKeys.Setting.AUTO_PLAY, NEVER)
  var crossFade by PrefsDelegate(sp, PrefKeys.Setting.CROSS_FADE, false)
  var speed by PrefsDelegate(sp, PrefKeys.Setting.SPEED, "1.0")
  val speedValue get() = speed.toFloat()
  var playModel by PrefsDelegate(sp, PrefKeys.Setting.PLAY_MODEL, MODE_LOOP)
  var lastSong by PrefsDelegate(sp, PrefKeys.Setting.LAST_SONG, "")
  var lastProgress by PrefsDelegate(sp, PrefKeys.Setting.LAST_PLAY_PROGRESS, 0)

  var playingScreenBackground by PrefsDelegate(
    sp,
    PrefKeys.Setting.PLAYER_BACKGROUND,
    BACKGROUND_ADAPTIVE_COLOR
  )
  var keepScreenOn by PrefsDelegate(sp, PrefKeys.Setting.SCREEN_ALWAYS_ON, true)

  var ignoreMediaStore by PrefsDelegate(sp, PrefKeys.Setting.IGNORE_MEDIA_STORE, false)
  var autoDownloadCover by PrefsDelegate(
    sp,
    PrefKeys.Setting.AUTO_DOWNLOAD_ALBUM_COVER,
    DOWNLOAD_COVER_ALWAYS
  )
  var downloadSource by PrefsDelegate(
    sp,
    PrefKeys.Setting.ALBUM_COVER_DOWNLOAD_SOURCE,
    DOWNLOAD_NETEASE
  )

  var exitAfterTimerFinish by PrefsDelegate(sp, PrefKeys.Setting.TIMER_EXIT_AFTER_FINISH, false)
  var timerStartAuto by PrefsDelegate(sp, PrefKeys.Setting.TIMER_DEFAULT, false)
  var timerDefaultDuration by PrefsDelegate(sp, PrefKeys.Setting.TIMER_DURATION, -1)

  var bassBoostStrength by PrefsDelegate(sp, PrefKeys.Setting.BASS_BOOST_STRENGTH, 0)
  var enableEq by PrefsDelegate(sp, PrefKeys.Setting.ENABLE_EQ, false)

  var checkMigration16600 by PrefsDelegate(sp, "check_migration_16600", false)
  var checkMigration20100 by PrefsDelegate(sp, "check_migration_20100", false)

  companion object {

    // 播放界面背景
    const val BACKGROUND_THEME = 0
    const val BACKGROUND_ADAPTIVE_COLOR = 1
    const val BACKGROUND_CUSTOM_IMAGE = 2

    // 封面下载
    const val DOWNLOAD_COVER_ALWAYS = 0
    const val DOWNLOAD_COVER_WIFI_ONLY = 1
    const val DOWNLOAD_COVER_NEVER = 2

    // 播放模式
    const val MODE_LOOP: Int = 1
    const val MODE_SHUFFLE: Int = 2
    const val MODE_REPEAT: Int = 3

    // 自动播放
    const val HEADSET_PLUG = 0
    const val OPEN_SOFTWARE = 1
    const val NEVER = 2

    // 封面下载源
    const val DOWNLOAD_LASTFM = 0
    const val DOWNLOAD_NETEASE = 1
  }
}

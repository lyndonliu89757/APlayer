package remix.myplayer.viewmodel.settings

import remix.myplayer.data.prefs.SettingPrefs


enum class SortCategory {
  SONG, ALBUM, ARTIST,
  ALBUM_DETAIL, ARTIST_DETAIL, FOLDER_DETAIL;

  fun getOrder(settingPrefs: SettingPrefs): String {
    return when (this) {
      SONG -> settingPrefs.songSortOrder
      ALBUM -> settingPrefs.albumSortOrder
      ARTIST -> settingPrefs.artistSortOrder
      ALBUM_DETAIL -> settingPrefs.albumDetailSortOrder
      ARTIST_DETAIL -> settingPrefs.artistDetailSortOrder
      FOLDER_DETAIL -> settingPrefs.folderDetailSortOrder
    }
  }

  fun saveOrder(newOrder: String, settingPrefs: SettingPrefs): Boolean {
    val old = getOrder(settingPrefs)
    if (old != newOrder) {
      when (this) {
        SONG -> settingPrefs.songSortOrder = newOrder
        ALBUM -> settingPrefs.albumSortOrder = newOrder
        ARTIST -> settingPrefs.artistSortOrder = newOrder
        ALBUM_DETAIL -> settingPrefs.albumDetailSortOrder = newOrder
        ARTIST_DETAIL -> settingPrefs.artistDetailSortOrder = newOrder
        FOLDER_DETAIL -> settingPrefs.folderDetailSortOrder = newOrder
      }
      return true
    }
    return false
  }
}
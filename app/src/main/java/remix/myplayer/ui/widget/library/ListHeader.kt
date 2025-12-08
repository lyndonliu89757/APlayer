package remix.myplayer.ui.widget.library

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import remix.myplayer.R
import remix.myplayer.data.bean.mp3.Song
import remix.myplayer.data.prefs.SettingPrefs
import remix.myplayer.misc.clickableWithoutRipple
import remix.myplayer.misc.helper.MusicServiceRemote.setPlayQueue
import remix.myplayer.service.Command
import remix.myplayer.ui.nav.MessageNotifier
import remix.myplayer.ui.screen.playing.PlayModeMap
import remix.myplayer.ui.theme.LocalTheme
import remix.myplayer.util.ColorUtil
import remix.myplayer.util.MusicUtil
import remix.myplayer.util.Util
import remix.myplayer.viewmodel.playbackViewModel

@Composable
fun SongListHeader(songs: List<Song>) {
    if (songs.isEmpty()) {
        return
    }
    val playbackState by playbackViewModel.playbackUiState.collectAsStateWithLifecycle()

    Row(
        modifier = Modifier
            .height(40.dp)
            .fillMaxWidth()
            .background(LocalTheme.current.background)
            .clickableWithoutRipple(remember { MutableInteractionSource() }) {
                Util.sendCMDLocalBroadcast(Command.CHANGE_MODEL)

                // 重新打乱顺序
//        if (songs.isEmpty()) {
//          MessageNotifier.show(R.string.no_song)
//          return@clickableWithoutRipple
//        }
//        setPlayQueue(songs, MusicUtil.makeCmdIntent(Command.SKIP_TO_NEXT, true))
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        val playMode = playbackState.playMode
        Icon(
            modifier = Modifier.padding(start = 16.dp, end = 10.dp),
            painter = painterResource(PlayModeMap[playMode]!!.first),
            tint = LocalTheme.current.secondary,
            contentDescription = "ListHeaderIcon"
        )
        Text(
            text = songs.size.toString(),
            color = LocalTheme.current.textPrimary,
            fontSize = 14.sp
        )
    }
}

@Composable
fun ModeHeader(grid: Boolean, onClick: (mode: Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .background(LocalTheme.current.background),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.clickableWithoutRipple(interactionSource = remember { MutableInteractionSource() }) {
                onClick(SettingPrefs.GRID_MODE)
            },
            painter = painterResource(R.drawable.ic_apps_white_24dp),
            contentDescription = "ModeGrid",
            tint = Color(if (grid) LocalTheme.current.secondary.toArgb() else ColorUtil.getColor(R.color.default_model_button_color))
        )
        Icon(
            modifier = Modifier
                .padding(horizontal = 18.dp)
                .clickableWithoutRipple(interactionSource = remember { MutableInteractionSource() }) {
                    onClick(SettingPrefs.LIST_MODE)
                },
            painter = painterResource(R.drawable.ic_format_list_bulleted_white_24dp),
            contentDescription = "ModeList",
            tint = Color(if (!grid) LocalTheme.current.secondary.toArgb() else ColorUtil.getColor(R.color.default_model_button_color))
        )
    }
}
package remix.myplayer.ui.widget.app

import android.content.ComponentName
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import kotlinx.coroutines.launch
import remix.myplayer.R
import remix.myplayer.misc.receiver.ExitReceiver
import remix.myplayer.ui.nav.LocalNavController
import remix.myplayer.ui.nav.RouteHistory
import remix.myplayer.ui.nav.RouteLastAdded
import remix.myplayer.ui.nav.RouteSetting
import remix.myplayer.ui.theme.LocalTheme
import remix.myplayer.ui.theme.highLightText
import remix.myplayer.ui.widget.common.TextPrimary
import remix.myplayer.util.Constants
import remix.myplayer.viewmodel.PlaybackViewModel
import remix.myplayer.viewmodel.playbackViewModel

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun Drawer(
    drawerState: DrawerState,
    pagerState: PagerState,
    vm: PlaybackViewModel = playbackViewModel
) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val theme = LocalTheme.current

    ModalDrawerSheet(
        modifier = Modifier
            .width(160.dp)
            .fillMaxHeight(),
        drawerShape = RectangleShape,
        drawerContainerColor = theme.background,
        windowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Start)
    ) {
        Spacer(
            modifier = Modifier.height(
                with(LocalDensity.current) { WindowInsets.systemBars.getTop(this).toDp() }
            ))

        var selectDrawer by remember { mutableIntStateOf(0) }
        val scope = rememberCoroutineScope()

        LazyColumn(
            modifier = Modifier
                .background(theme.background)
                .fillMaxHeight()
        ) {
            itemsIndexed(
                listOf(
                    "歌曲" to R.drawable.ic_library_music_24dp,
                    "专辑" to R.drawable.ic_library_music_24dp,
                    "歌手" to R.drawable.ic_library_music_24dp,
                    "流派" to R.drawable.ic_library_music_24dp,
                    "列表" to R.drawable.ic_library_music_24dp,
                    "文件夹" to R.drawable.ic_library_music_24dp,
                    "远程" to R.drawable.ic_library_music_24dp,

                    "播放历史" to R.drawable.ic_history_24dp,
                    "最近添加" to R.drawable.ic_recent_24dp,
                    "设置" to R.drawable.ic_settings_24dp,
                    "退出" to R.drawable.ic_exit_to_app_24dp,
                )
            ) { index, (label, icon) ->
                NavigationDrawerItem(
                    shape = RectangleShape,
                    label = {
                        TextPrimary(
                            modifier = Modifier.padding(start = 8.dp),
                            text = label,
                            fontSize = 16.sp
                        )
                    },
                    selected = selectDrawer == index,
                    onClick = {
                        selectDrawer = index

                        when (label) {
                            "歌曲", "专辑", "歌手", "流派", "列表", "文件夹", "远程" -> {
                                scope.launch {
                                    drawerState.close()
                                    pagerState.animateScrollToPage(index)
                                }
                            }

                            "播放历史" -> navController.navigate(RouteHistory)
                            "最近添加" -> navController.navigate(RouteLastAdded)
                            "设置" -> navController.navigate(RouteSetting)
                            "退出" -> {
                                context.sendBroadcast(
                                    Intent(Constants.ACTION_EXIT)
                                        .setComponent(
                                            ComponentName(
                                                context,
                                                ExitReceiver::class.java
                                            )
                                        )
                                )
                            }
                        }
                    },
                    icon = {
                        Icon(
                            painter = painterResource(icon),
                            contentDescription = label,
                            tint = theme.primary
                        )
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = theme.select,
                        unselectedContainerColor = theme.background
                    )
                )
            }

            item {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
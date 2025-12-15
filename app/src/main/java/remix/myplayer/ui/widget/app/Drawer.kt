package remix.myplayer.ui.widget.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import kotlinx.coroutines.launch
import remix.myplayer.BuildConfig
import remix.myplayer.R
import remix.myplayer.ui.nav.LocalNavController
import remix.myplayer.ui.nav.RouteSetting
import remix.myplayer.ui.theme.LocalTheme
import remix.myplayer.ui.widget.common.TextPrimary

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun Drawer(
  drawerState: DrawerState,
  pagerState: PagerState,
) {
  val navController = LocalNavController.current
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
    val navigationItems = listOf(
      "歌曲" to R.drawable.ic_music,
      "专辑" to R.drawable.ic_album,
      "歌手" to R.drawable.ic_singer,
      "文件夹" to R.drawable.ic_folder,
      "远程" to R.drawable.ic_webdav,
      "设置" to R.drawable.ic_setting,
    )

    Column(
      modifier = Modifier
        .background(theme.background)
        .fillMaxHeight()
    ) {
      navigationItems.forEachIndexed { index, (label, icon) ->
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
              "歌曲", "专辑", "歌手", "文件夹", "远程" -> {
                scope.launch {
                  drawerState.close()
                  pagerState.animateScrollToPage(index)
                }
              }

              "设置" -> navController.navigate(RouteSetting)
            }
          },
          icon = {
            Icon(
              modifier = Modifier.size(34.dp),
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

      Spacer(modifier = Modifier.weight(1f))

      TextPrimary(
        "v${BuildConfig.VERSION_NAME}",
        color = theme.textSecondary,
        textAlign = TextAlign.Center,
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 8.dp),
      )

      Spacer(
        modifier = Modifier.height(
          with(LocalDensity.current) { WindowInsets.systemBars.getBottom(this).toDp() }
        ))
    }
  }
}
package remix.myplayer.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import remix.myplayer.ui.activity.base.BaseMusicActivity
import remix.myplayer.ui.nav.AppNav
import remix.myplayer.ui.nav.LocalNavController
import remix.myplayer.ui.nav.playingScreenDeepLink
import remix.myplayer.ui.theme.APlayerTheme
import remix.myplayer.ui.theme.LocalTheme
import remix.myplayer.util.MusicUtil
import remix.myplayer.util.ThemeUtil
import remix.myplayer.viewmodel.LibraryViewModel
import remix.myplayer.viewmodel.MainViewModel
import remix.myplayer.viewmodel.PlaybackViewModel
import remix.myplayer.viewmodel.ProvideViewModels
import timber.log.Timber

@AndroidEntryPoint
class ComposeActivity : BaseMusicActivity() {

  private val libraryViewModel: LibraryViewModel by viewModels()
  private val playbackViewModel: PlaybackViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    addMusicServiceEventListener(libraryViewModel)
    addMusicServiceEventListener(playbackViewModel)

    enableEdgeToEdge(
      navigationBarStyle = SystemBarStyle.auto(
        android.graphics.Color.WHITE,
        android.graphics.Color.WHITE
      )
    )
    setContent {
      AppCompositionLocalProvider() {
        val theme = LocalTheme.current
        val color = if (theme.coloredNaviBar) {
          theme.primary
        } else {
          Color.White
        }
        // TODO
        window.navigationBarColor = color.toArgb()
        ThemeUtil.setLightNavigationBarAuto(this, theme.isPrimaryLight)

        APlayerTheme {
          AppNav()
        }
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
  }

  override fun onResume() {
    super.onResume()
    handleIntent()
  }

  private fun handleIntent() {
    intent?.data?.let {
      when (it.scheme) {
        playingScreenDeepLink.scheme -> {
          Timber.v("deepLink")
        }
        else -> {
          lifecycleScope.launch(Dispatchers.IO) {
            MusicUtil.playFromUri(this@ComposeActivity, it)
            intent = Intent()
          }
        }
      }
    }
  }
}

@Composable
fun AppCompositionLocalProvider(
  content: @Composable (() -> Unit)
) {
  CompositionLocalProvider(
    LocalNavController provides rememberNavController()
  ) {
    ProvideViewModels {
      content()
    }
  }
}
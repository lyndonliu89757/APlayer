package remix.myplayer.ui.screen

import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DismissibleDrawerSheet
import androidx.compose.material3.DismissibleNavigationDrawer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import kotlinx.coroutines.launch
import remix.myplayer.BuildConfig
import remix.myplayer.data.model.misc.Library
import remix.myplayer.ui.screen.library.AlbumScreen
import remix.myplayer.ui.screen.library.ArtistScreen
import remix.myplayer.ui.screen.library.FolderScreen
import remix.myplayer.ui.screen.library.SongScreen
import remix.myplayer.ui.screen.setting.SettingScreen
import remix.myplayer.ui.theme.LocalTheme
import remix.myplayer.ui.widget.app.BottomBar
import remix.myplayer.ui.widget.app.FAButton
import remix.myplayer.ui.widget.app.MultiSelectBar
import remix.myplayer.ui.widget.common.TextPrimary
import remix.myplayer.ui.widget.common.defaultAppBarActions
import remix.myplayer.ui.widget.popup.ScreenPopupButton
import remix.myplayer.viewmodel.mainViewModel
import remix.myplayer.viewmodel.webDavViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun HomeScreen() {
  val mainVM = mainViewModel
  val libraries = Library.allLibraries
  val theme = LocalTheme.current

  val multiSelectState by mainVM.multiSelectState.collectAsStateWithLifecycle()
  val drawerState = rememberDrawerState(DrawerValue.Closed)
  val pagerState = rememberPagerState { libraries.size }
  val scope = rememberCoroutineScope()

  BackPressHandler(enabled = drawerState.isOpen || multiSelectState.isShowing()) {
    if (drawerState.isOpen) {
      scope.launch {
        drawerState.close()
      }
    } else if (multiSelectState.isShowing()) {
      mainVM.closeMultiSelect()
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(bottom = with(LocalDensity.current) { WindowInsets.systemBars.getBottom(this).toDp() })
  ) {
    DismissibleNavigationDrawer(
      modifier = Modifier.weight(1f),
      drawerState = drawerState,
      drawerContent = {
        DismissibleDrawerSheet(
          modifier = Modifier.width(160.dp),
          drawerContainerColor = theme.background,
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 10.dp)
              .background(theme.background),
          ) {

            Card(colors = CardDefaults.cardColors().copy(containerColor = theme.container)) {
              TextPrimary(
                "v${BuildConfig.VERSION_NAME}",
                color = theme.textSecondary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 16.dp),
              )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(colors = CardDefaults.cardColors().copy(containerColor = theme.container)) {
              Column {
                libraries.forEachIndexed { index, lab ->
                  NavigationDrawerItem(
                    icon = {
                      Icon(
                        modifier = Modifier.size(28.dp),
                        painter = painterResource(lab.icon),
                        contentDescription = stringResource(lab.stringRes),
                        tint = Color.Unspecified
                      )
                    },
                    label = {
                      TextPrimary(
                        text = stringResource(lab.stringRes),
                        fontSize = 16.sp
                      )
                    },
                    selected = false,
                    onClick = {
                      scope.launch {
                        pagerState.animateScrollToPage(index)
                        drawerState.close()
                      }
                    },
                  )
                }
              }
            }
          }

        }
      }) {

      val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

      val showMultiSelect by remember {
        derivedStateOf {
          multiSelectState.isShowInLibrary()
        }
      }

      Scaffold(
        Modifier
          .fillMaxSize()
          .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = theme.background,
        topBar = {
          AnimatedContent(
            targetState = showMultiSelect,
            transitionSpec = {
              if (targetState) {
                slideInVertically() togetherWith slideOutVertically { height -> height / 2 }
              } else {
                slideInVertically { height -> height } togetherWith slideOutVertically()
              }
            }
          ) { isMultiSelect ->
            if (!isMultiSelect) {
              val library = libraries[pagerState.currentPage]
              TopAppBar(
                modifier = Modifier.padding(6.dp),
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                  containerColor = theme.background,
                  scrolledContainerColor = theme.background,
                  titleContentColor = Color.Black,
                  navigationIconContentColor = Color.Black,
                  actionIconContentColor = Color.Black,
                ),
                title = {
                  Text(stringResource(library.stringRes), fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                },
                navigationIcon = {
                  IconButton(onClick = { scope.launch { drawerState.open() } }) {
                    Icon(Icons.Filled.Menu, contentDescription = "Menu")
                  }
                },
                actions = {
                  if (listOf(Library.TAG_SONG, Library.TAG_ALBUM).contains(library.tag)) {
                    ScreenPopupButton(library)
                  }

                  if (listOf(Library.TAG_SONG, Library.TAG_ALBUM, Library.TAG_ARTIST, Library.TAG_FOLDER).contains(library.tag)) {
                    defaultAppBarActions.map { it ->
                      IconButton(onClick = {
                        it.action()
                      }) {
                        Icon(
                          modifier = Modifier.size(30.dp),
                          painter = painterResource(it.icon),
                          contentDescription = it.contentDescription
                        )
                      }
                    }
                  }
                })
            } else {
              MultiSelectBar(
                state = multiSelectState,
                scrollBehavior = scrollBehavior,
              )
            }
          }
        },
        floatingActionButton = {
          val selectLibrary by remember(libraries) {
            derivedStateOf {
              libraries.getOrElse(pagerState.currentPage) { libraries.first() }
            }
          }

          val webDavVM = webDavViewModel
          FAButton(selectLibrary.tag == Library.TAG_REMOTE) {
            if (mainVM.multiSelectState.value.isShowing()) {
              return@FAButton
            }

            if (selectLibrary.tag == Library.TAG_REMOTE) {
              webDavVM.showAddWebDavDialog()
            }
          }

        })
      { contentPadding ->

        Column(modifier = Modifier.padding(top = contentPadding.calculateTopPadding())) {
          HorizontalPager(
            modifier = Modifier.fillMaxSize(),
            state = pagerState,
            beyondViewportPageCount = 1,
            userScrollEnabled = false,
          ) { page ->
            val library = libraries.getOrNull(page) ?: return@HorizontalPager
            when (library.tag) {
              Library.TAG_SONG -> SongScreen()
              Library.TAG_ALBUM -> AlbumScreen()
              Library.TAG_ARTIST -> ArtistScreen()
              Library.TAG_FOLDER -> FolderScreen()
              Library.TAG_REMOTE -> RemoteScreen()
              Library.TAG_SETTING -> SettingScreen()
              else -> Text("无效页面")
            }
          }
        }
      }
    } // DismissibleNavigationDrawer

    Spacer(
      modifier = Modifier
        .fillMaxWidth()
        .height(1.dp)
        .background(theme.background)
    )
    BottomBar()
  }
}

@Composable
fun BackPressHandler(
  enabled: Boolean = true,
  onBackPressed: () -> Unit
) {
  val dispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
  val backCallback = remember {
    object : OnBackPressedCallback(enabled) {
      override fun handleOnBackPressed() {
        onBackPressed()
      }
    }
  }

  LaunchedEffect(enabled) {
    backCallback.isEnabled = enabled
  }

  DisposableEffect(dispatcher) {
    dispatcher?.addCallback(backCallback)
    onDispose {
      backCallback.remove()
    }
  }
}

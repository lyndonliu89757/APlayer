package remix.myplayer

import android.content.Context
import android.content.res.Configuration
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.hjq.permissions.XXPermissions
import dagger.hilt.android.HiltAndroidApp
import remix.myplayer.helper.AppMigration
import remix.myplayer.helper.LanguageHelper.onConfigurationChanged
import remix.myplayer.helper.LanguageHelper.saveSystemCurrentLanguage
import remix.myplayer.helper.LanguageHelper.setApplicationLanguage
import remix.myplayer.helper.LanguageHelper.setLocal
import remix.myplayer.misc.manager.APlayerActivityManager
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Remix on 16-3-16.
 */
@HiltAndroidApp
class App : MultiDexApplication() {

  @Inject
  lateinit var appMigration: AppMigration

  override fun attachBaseContext(base: Context) {
    saveSystemCurrentLanguage()
    super.attachBaseContext(setLocal(base))
    MultiDex.install(this)
  }

  override fun onCreate() {
    super.onCreate()
    context = this

    appMigration.check()
    setUp()

    registerActivityLifecycleCallbacks(APlayerActivityManager())
  }

  private fun setUp() {
    XXPermissions.setCheckMode(false)
    setApplicationLanguage(this)
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    onConfigurationChanged(applicationContext)
  }

  override fun onLowMemory() {
    super.onLowMemory()
    Timber.v("onLowMemory")
  }

  override fun onTrimMemory(level: Int) {
    super.onTrimMemory(level)
    Timber.v("onTrimMemory, %s", level)
  }

  companion object {

    @JvmStatic
    lateinit var context: App
      private set
  }
}
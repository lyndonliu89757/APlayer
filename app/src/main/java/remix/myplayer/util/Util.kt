package remix.myplayer.util

import android.app.Activity
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.RecoverableSecurityException
import android.app.Service
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaFormat
import android.media.MediaScannerConnection
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Vibrator
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.text.HtmlCompat
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import remix.myplayer.App.Companion.context
import remix.myplayer.R
import remix.myplayer.data.model.audio.Song
import remix.myplayer.misc.floatpermission.rom.RomUtils
import remix.myplayer.misc.manager.APlayerActivityManager
import remix.myplayer.ui.activity.base.BaseActivity
import remix.myplayer.ui.activity.base.PendingWriteRequest
import remix.myplayer.ui.nav.MessageNotifier
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.EnumMap

/**
 * 通用工具类 - 优化版本
 * 使用Kotlin特性简化代码，提高可读性和可维护性
 */
object Util {

  // 常量定义
  private const val DOUBLE_CLICK_INTERVAL = 500L
  private const val MD5_ALGORITHM = "MD5"

  // 使用枚举替代魔法数字，提高类型安全性 [3](@ref)
  enum class InfoType { SONG, ARTIST, ALBUM, DISPLAY_NAME }

  private var mLastClickTime: Long = 0

  // region 扩展函数 - 将常用功能改为扩展函数形式 [2,4](@ref)

  /**
   * Context的扩展函数：注册本地广播接收器
   */
  fun Context.registerLocalReceiver(receiver: BroadcastReceiver, filter: IntentFilter) {
    LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)
  }

  /**
   * Context的扩展函数：注销本地广播接收器
   */
  fun Context.unregisterLocalReceiver(receiver: BroadcastReceiver) {
    LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
  }

  /**
   * Context的扩展函数：发送本地广播
   */
  fun Context.sendLocalBroadcast(intent: Intent) {
    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
  }

  /**
   * Context的扩展函数：发送CMD本地广播
   */
  fun Context.sendCMDLocalBroadcast(cmd: Int) {
    LocalBroadcastManager.getInstance(this).sendBroadcast(MusicUtil.makeCmdIntent(cmd))
  }

  /**
   * BroadcastReceiver的扩展函数：安全注销
   */
  fun BroadcastReceiver.unregisterSafely(context: Context?) {
    try {
      context?.unregisterReceiver(this)
    } catch (e: Exception) {
      // 忽略注销异常
    }
  }

  /**
   * Context的扩展函数：振动功能
   */
  fun Context.vibrate(milliseconds: Long) {
    try {
      val vibrator = getSystemService(Service.VIBRATOR_SERVICE) as? Vibrator
      vibrator?.vibrate(milliseconds)
    } catch (ignore: Exception) {
      // 忽略振动异常
    }
  }

  /**
   * File的扩展函数：安全删除
   */
  fun File.deleteSafely(): Boolean {
    val tmpPath = "${parent ?: return false}${File.separator}${System.currentTimeMillis()}"
    val tmpFile = File(tmpPath)
    return renameTo(tmpFile) && tmpFile.delete()
  }

  /**
   * Closeable的扩展函数：安全关闭
   */
  fun Closeable?.closeSafely() {
    if (this == null) return
    if (this is Cursor && isClosed) return

    try {
      close()
    } catch (e: Exception) {
      Timber.e(e, "Closeable close failed")
    }
  }

  /**
   * String的扩展函数：打开URL
   */
  fun String?.openUrl() {
    if (isNullOrEmpty()) return

    val uri = Uri.parse(this)
    Intent(Intent.ACTION_VIEW, uri).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(this)
    }
  }

  /**
   * View的扩展函数：隐藏键盘
   */
  fun View.hideKeyboard(): Boolean = try {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    imm?.hideSoftInputFromWindow(windowToken, 0) ?: false
  } catch (e: Exception) {
    Timber.e(e, "Hide keyboard failed")
    false
  }

  // endregion

  // region 顶级函数优化 [2](@ref)

  /**
   * 发送本地广播 - 简化版本
   */
  fun sendLocalBroadcast(intent: Intent) {
    context.sendLocalBroadcast(intent)
  }

  /**
   * 发送CMD本地广播 - 简化版本
   */
  fun sendCMDLocalBroadcast(cmd: Int) {
    context.sendCMDLocalBroadcast(cmd)
  }

  /**
   * 判断应用是否在前台，使用更简洁的写法 [7](@ref)
   */
  val isAppOnForeground: Boolean
    get() = runCatching {
      val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
      val packageName = context.packageName

      activityManager?.runningAppProcesses?.firstOrNull { process ->
        process.processName == packageName &&
            process.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND
      } != null
    }.getOrElse {
      Timber.w("isAppOnForeground failed: ${it.message}")
      APlayerActivityManager.isAppForeground
    }

  /**
   * 计算目录大小，使用函数式编程简化 [7](@ref)
   */
  fun getFolderSize(file: File?): Long = file?.let { targetFile ->
    when {
      targetFile.isFile -> targetFile.length()
      targetFile.isDirectory -> targetFile.listFiles()?.sumOf { getFolderSize(it) } ?: 0L
      else -> 0L
    }
  } ?: 0L

  /**
   * 递归删除目录，使用扩展函数和函数式编程 [1](@ref)
   */
  fun deleteFilesByDirectory(directory: File?) {
    directory?.takeIf { it.exists() }?.let { targetDir ->
      when {
        targetDir.isFile -> targetDir.deleteSafely()
        targetDir.isDirectory -> {
          targetDir.listFiles()?.forEach { deleteFilesByDirectory(it) }
          targetDir.deleteSafely()
        }
      }
    }
  }

  /**
   * 获取音频文件类型，使用when表达式简化 [7](@ref)
   */
  fun getType(mimeType: String): String = when {
    mimeType == MediaFormat.MIMETYPE_AUDIO_MPEG -> "mp3"
    mimeType == MediaFormat.MIMETYPE_AUDIO_FLAC -> "flac"
    mimeType == MediaFormat.MIMETYPE_AUDIO_AAC -> "aac"
    mimeType.contains("ape") -> "ape"
    mimeType.startsWith("audio/") -> mimeType.substringAfter("audio/")
    else -> mimeType
  }

  /**
   * 格式化时间显示，使用字符串模板简化 [7](@ref)
   */
  fun getTime(duration: Long): String {
    val minute = duration / 1000 / 60
    val second = (duration / 1000) % 60

    val minuteStr = if (minute < 10) "0$minute" else "$minute"
    val secondStr = if (second < 10) "0$second" else "$second"

    return "$minuteStr:$secondStr"
  }

  /**
   * 检查Intent是否可用
   */
  fun isIntentAvailable(context: Context, intent: Intent): Boolean {
    val packageManager = context.packageManager
    return packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isNotEmpty()
  }

  /**
   * 安全启动Activity，使用扩展函数风格 [4](@ref)
   */
  fun Context.startActivitySafely(intent: Intent) = try {
    startActivity(intent)
  } catch (e: ActivityNotFoundException) {
    MessageNotifier.show(R.string.activity_not_found_tip)
  }

  fun Activity.startActivityForResultSafely(intent: Intent, requestCode: Int) = try {
    startActivityForResult(intent, requestCode)
  } catch (e: ActivityNotFoundException) {
    MessageNotifier.show(R.string.activity_not_found_tip)
  }

  /**
   * 网络连接状态检查
   */
  val isNetWorkConnected: Boolean
    get() {
      val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
      val netWorkInfo = connectivityManager?.activeNetworkInfo
      return netWorkInfo?.isAvailable == true && netWorkInfo.isConnected
    }

  /**
   * 处理歌曲信息，使用枚举参数提高类型安全性 [3](@ref)
   */
  fun processInfo(origin: String?, type: InfoType): String = when {
    origin.isNullOrEmpty() -> when (type) {
      InfoType.SONG, InfoType.DISPLAY_NAME -> context.getString(R.string.unknown_song)
      InfoType.ARTIST -> context.getString(R.string.unknown_artist)
      InfoType.ALBUM -> context.getString(R.string.unknown_album)
    }

    type == InfoType.DISPLAY_NAME -> {
      val lastDotIndex = origin.lastIndexOf(".")
      if (lastDotIndex > 0) origin.substring(0, lastDotIndex) else origin
    }

    else -> origin
  }

  /**
   * 防止快速连续点击，使用更精确的时间判断 [7](@ref)
   */
  val isFastDoubleClick: Boolean
    get() {
      val currentTime = System.currentTimeMillis()
      val timeInterval = currentTime - mLastClickTime
      val isFastClick = timeInterval in 1 until DOUBLE_CLICK_INTERVAL
      mLastClickTime = currentTime
      return isFastClick
    }

  /**
   * 生成MD5哈希值，使用更函数式的写法
   */
  fun hashKeyForDisk(key: String): String = try {
    MessageDigest.getInstance(MD5_ALGORITHM).digest(key.toByteArray()).toHexString()
  } catch (e: NoSuchAlgorithmException) {
    key.hashCode().toString()
  }

  private fun ByteArray.toHexString(): String = joinToString("") { byte ->
    Integer.toHexString(0xFF and byte.toInt()).padStart(2, '0')
  }

  /**
   * 判断WiFi是否连接
   */
  fun Context.isWifiConnected(): Boolean {
    val activeNetInfo = (getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)?.activeNetworkInfo
    return activeNetInfo?.type == ConnectivityManager.TYPE_WIFI
  }

  /**
   * 获取应用元数据
   */
  fun getAppMetaData(key: String?): String? = key?.takeIf { it.isNotEmpty() }?.let { metaKey ->
    runCatching {
      val appInfo = context.packageManager.getApplicationInfo(
        context.packageName,
        PackageManager.GET_META_DATA
      )
      appInfo.metaData?.getString(metaKey)
    }.getOrNull()
  }

  // endregion

  // region 共享和文件操作

  /**
   * 创建分享歌曲文件的Intent，使用apply作用域函数简化 [7](@ref)
   */
  fun createShareSongFileIntent(song: Song, context: Context): Intent = runCatching {
    val uri = FileProvider.getUriForFile(
      context,
      "${context.packageName}.fileprovider",
      File(song.data)
    )
    Intent(Intent.ACTION_SEND).apply {
      putExtra(Intent.EXTRA_STREAM, uri)
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      type = "audio/*"
    }
  }.getOrElse {
    Timber.e(it, "Create share song intent failed")
    Toast.makeText(context, R.string.cant_share_song, Toast.LENGTH_SHORT).show()
    Intent()
  }

  /**
   * 创建分享图片文件的Intent
   */
  fun createShareImageFileIntent(file: File, context: Context): Intent = runCatching {
    val uri = FileProvider.getUriForFile(
      context,
      "${context.packageName}.fileprovider",
      file
    )
    Intent(Intent.ACTION_SEND).apply {
      putExtra(Intent.EXTRA_STREAM, uri)
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      type = "image/*"
    }
  }.getOrElse {
    Timber.e(it, "Create share image intent failed")
    Toast.makeText(context, R.string.cant_share_song, Toast.LENGTH_SHORT).show()
    Intent()
  }

  /**
   * 安装APK文件
   */
  fun Context.installApk(path: String) {
    val apkFile = File(path)
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      val apkUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", apkFile)
      Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
        data = apkUri
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
      }
    } else {
      Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(apkFile.absolutePath.toUri(), "application/vnd.android.package-archive")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
    }
    startActivity(intent)
  }

  /**
   * 获取进程名
   */
  fun getProcessName(pid: Int): String? = runCatching {
    File("/proc/$pid/cmdline").bufferedReader().use { it.readLine()?.trim() }
  }.getOrNull()

  /**
   * 判断是否支持状态栏歌词
   */
  fun Context.isSupportStatusBarLyric(): Boolean =
    RomUtils.checkIsMeizuRom() ||
        Settings.System.getInt(contentResolver, "status_bar_show_lyric", 0) != 0 ||
        RomUtils.checkIsbaolong24Rom() ||
        RomUtils.checkIsexTHmUIRom()

  /**
   * HTML转纯文本
   */
  fun String.htmlToText(): String =
    HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()

  // endregion

  // region 协程相关操作 [5](@ref)

  /**
   * 保存图片到相册
   */
  suspend fun Context.saveToAlbum(resId: Int, fileName: String) = withContext(Dispatchers.IO) {
    runCatching {
      val bitmap = BitmapFactory.decodeResource(resources, resId)
      val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
      val file = File(picturesDir, fileName).apply {
        if (exists()) delete()
        createNewFile()
      }

      val values = ContentValues().apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        } else {
          put(MediaStore.MediaColumns.DATA, file.absolutePath)
        }
        put(MediaStore.Images.ImageColumns.TITLE, fileName)
        put(MediaStore.Images.ImageColumns.DISPLAY_NAME, fileName)
        put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/png")
        put(MediaStore.Images.ImageColumns.WIDTH, bitmap.width)
        put(MediaStore.Images.ImageColumns.HEIGHT, bitmap.height)
      }

      contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)?.let { uri ->
        contentResolver.openOutputStream(uri)?.use { outputStream ->
          ByteArrayOutputStream().use { bos ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, bos)
            outputStream.write(bos.toByteArray())
          }
        }
      }

      MessageNotifier.show(R.string.save_success)
    }.onFailure {
      Timber.e(it, "Save to album failed")
    }
  }

  /**
   * 请求保存音频标签
   */
  fun requestSaveAudioTag(
    activity: BaseActivity,
    song: Song,
    newTitle: String,
    newAlbum: String,
    newArtist: String,
    newGenre: String,
    newYear: String,
    newTrackNum: String
  ) {
    val fieldMap = EnumMap<FieldKey, String>(FieldKey::class.java).apply {
      put(FieldKey.TITLE, newTitle)
      put(FieldKey.ALBUM, newAlbum)
      put(FieldKey.ARTIST, newArtist)
      put(FieldKey.GENRE, newGenre)
      put(FieldKey.YEAR, newYear)
      put(FieldKey.TRACK, newTrackNum)
    }

    val request = PendingWriteRequest(song.data, fieldMap)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      activity.pendingWriteRequest = request
      activity.writeSongLauncher.launch(
        IntentSenderRequest.Builder(
          MediaStore.createWriteRequest(
            context.contentResolver,
            listOf(song.contentUri)
          ).intentSender
        ).build()
      )
    } else {
      activity.lifecycleScope.launch {
        try {
          saveAudioTag(activity, request)
        } catch (e: Exception) {
          handleSaveAudioTagException(activity, request, e)
        }
      }
    }
  }

  private fun handleSaveAudioTagException(activity: BaseActivity, request: PendingWriteRequest, e: Exception) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && e is RecoverableSecurityException) {
      activity.pendingWriteRequest = request
      activity.writeSongLauncher.launch(
        IntentSenderRequest.Builder(e.userAction.actionIntent.intentSender).build()
      )
    } else {
      Timber.e(e, "Save audio tag failed")
      MessageNotifier.show(R.string.save_error_arg)
    }
  }

  /**
   * 保存音频标签
   */
  suspend fun saveAudioTag(context: Context, request: PendingWriteRequest) = withContext(Dispatchers.IO) {
    runCatching {
      val audioFileObj = AudioFileIO.read(File(request.path))
      val tag = audioFileObj.tagOrCreateAndSetDefault

      request.fieldMap.forEach { (key, value) ->
        Timber.v("Setting field: $key to value: $value")
        tag.setField(key, value)
      }

      audioFileObj.commit()
      // 通知媒体库更新
      MediaScannerConnection.scanFile(context, arrayOf(request.path), null) { _, uri ->
        context.contentResolver.notifyChange(uri, null)
      }
    }.onFailure {
      Timber.e(it, "Save audio tag failed for path: ${request.path}")
      throw it
    }
  }

  // endregion

  // region 兼容性保留方法
  // 为了向后兼容，保留原有的静态方法

  @JvmStatic
  fun registerLocalReceiver(receiver: BroadcastReceiver?, filter: IntentFilter?) {
    receiver?.let { context.registerLocalReceiver(it, filter!!) }
  }

  @JvmStatic
  fun unregisterLocalReceiver(receiver: BroadcastReceiver?) {
    receiver?.let { context.unregisterLocalReceiver(it) }
  }

  // endregion
}
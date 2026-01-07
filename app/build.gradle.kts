import java.util.Properties

plugins {
  alias(libs.plugins.application)
  alias(libs.plugins.kotlin)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.ksp)
  alias(libs.plugins.room)
  alias(libs.plugins.hilt)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.baselineprofile)
}

val localProperties = Properties().apply {
  rootProject.file("local.properties").takeIf { it.exists() }?.readText()
}

kotlin.jvmToolchain(17)

android {
  namespace = "remix.myplayer"
  compileSdk = 35
  buildToolsVersion = "35.0.0"
  ndkVersion = "25.2.9519653"

  defaultConfig {
    applicationId = "remix.myplayer"
    minSdk = 21
    targetSdk = 33
    versionCode = 204
    versionName = "2.0.4"
    vectorDrawables.useSupportLibrary = true
    multiDexEnabled = true
    setProperty("archivesBaseName", "APlayer-v$versionName")

    buildConfigField("String", "LASTFM_API_KEY", "\"${localProperties.getProperty("LASTFM_API_KEY", "")}\"")

    ndk.abiFilters += "arm64-v8a"
  }

  signingConfigs {
    create("debugConfig") {
      storeFile = file("Debug.jks")
      storePassword = "123456"
      keyAlias = "Debug"
      keyPassword = "123456"
      enableV1Signing = true
      enableV2Signing = true
      enableV3Signing = true
    }

    create("releaseConfig") {
      localProperties.apply {
        getProperty("keystore.storeFile")?.let { storeFile = file(it) }
        storePassword = getProperty("keystore.storePassword", "")
        keyAlias = getProperty("keystore.keyAlias", "")
        keyPassword = getProperty("keystore.keyPassword", "")
      }
      enableV1Signing = true
      enableV2Signing = true
      enableV3Signing = true
    }
  }

  buildTypes {
    debug {
      isDebuggable = true
      isMinifyEnabled = false
      applicationIdSuffix = ".debug"
      versionNameSuffix = "-DEBUG"
    }

    release {
      signingConfig = signingConfigs.getByName("releaseConfig")
      isDebuggable = false
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }
  }

  sourceSets["main"].java.srcDir("src/third-party/jaudiotagger-android/src")
  externalNativeBuild.cmake.path = File("CMakeLists.txt")

  flavorDimensions += listOf("channel", "updater")
  productFlavors {
    create("nonGoogle") {
      dimension = "channel"
      isDefault = true
    }
    create("google") { dimension = "channel" }

    create("withUpdater") {
      dimension = "updater"
      buildConfigField("boolean", "ENABLE_UPDATER", "true")
    }
    create("withoutUpdater") {
      dimension = "updater"
      isDefault = true
      buildConfigField("boolean", "ENABLE_UPDATER", "false")
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlinOptions.jvmTarget = "17"

  lint {
    abortOnError = false
    checkReleaseBuilds = false
    disable += listOf("MissingTranslation", "InvalidPackage")
  }

  buildFeatures {
    buildConfig = true
    viewBinding = true
    compose = true
  }
  composeOptions.kotlinCompilerExtensionVersion = "1.5.13"

  dependenciesInfo.includeInApk = false
  room.schemaDirectory("$projectDir/schemas")
}

androidComponents.beforeVariants { variantBuilder ->
  if (variantBuilder.productFlavors.all {
      (it.first == "channel" && it.second == "google") ||
          (it.first == "updater" && it.second == "withUpdater")
    }) {
    variantBuilder.enable = false
  }
}

baselineProfile {
  saveInSrc = true
  warnings.disabledVariants = false
}

dependencies {
  implementation(libs.kotlinx.coroutines)
  implementation(libs.kotlinx.serialization)

  implementation(libs.appcompat)
  implementation(libs.media)
  implementation(libs.androidx.media3.exoplayer)
  implementation(libs.multidex)
  implementation(libs.palette.ktx)
  implementation(libs.material)

  implementation(libs.glide)
  ksp(libs.glide.ksp)
  implementation(libs.glide.compose)

  implementation(libs.retrofit)
  implementation(libs.retrofit.converter.gson)

  ksp(libs.room.compiler)
  implementation(libs.room.ktx)
  implementation(libs.room.runtime)

  implementation(libs.image.cropper)
  implementation(libs.logback.android)
  implementation(libs.xxpermissions)
  implementation(libs.sardine.android) {
    exclude(group = "xpp3", module = "xpp3")
  }
  implementation(libs.slf4j)
  implementation(libs.timber)
  implementation(libs.tinypinyin)

  debugImplementation(libs.leakcanary)

  "googleImplementation"(libs.billingclient)

  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.ui)
  implementation(libs.androidx.ui.tooling.preview)
  implementation(libs.androidx.material3)
  debugImplementation(libs.androidx.ui.tooling)
  debugImplementation(libs.androidx.ui.test.manifest)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.nav)
  implementation(libs.androidx.hilt.navi.compose)
  implementation(libs.reorderable)
  implementation(libs.androidx.work.runtime.ktx)

  implementation(libs.hilt.android)
  ksp(libs.hilt.android.compiler)

  implementation(libs.androidx.profileinstaller)
  "baselineProfile"(project(":baselineprofile"))
}
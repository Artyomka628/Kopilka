plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
  alias(libs.plugins.google.services)
}

val releaseStorePassword = providers.environmentVariable("STORE_PASSWORD").orNull.orEmpty()
val releaseKeyAlias = providers.environmentVariable("KEY_ALIAS").orNull.orEmpty()
val releaseKeyPassword = providers.environmentVariable("KEY_PASSWORD").orNull.orEmpty()

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.drygvalartyom.kopilka"
    minSdk = 24
    targetSdk = 36
    versionCode = 4
    versionName = "3.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/release.jks"
      storeFile = file(keystorePath)
      storePassword = releaseStorePassword
      keyAlias = releaseKeyAlias
      keyPassword = releaseKeyPassword
    }
    create("debugConfig") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/debug.keystore"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD") ?: "android"
      keyAlias = System.getenv("KEY_ALIAS") ?: "androiddebugkey"
      keyPassword = System.getenv("KEY_PASSWORD") ?: "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = true
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      isMinifyEnabled = false
      isShrinkResources = false
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

tasks.configureEach {
  if (name == "validateSigningRelease") {
    doFirst {
      check(releaseStorePassword.isNotBlank()) { "STORE_PASSWORD must be set for release signing." }
      check(releaseKeyAlias.isNotBlank()) { "KEY_ALIAS must be set for release signing." }
      check(releaseKeyPassword.isNotBlank()) { "KEY_PASSWORD must be set for release signing." }
    }
  }
}

val validateGoogleServicesJson by tasks.registering {
  val googleServicesFile = layout.projectDirectory.file("google-services.json")
  inputs.file(googleServicesFile).optional()

  doLast {
    val file = googleServicesFile.asFile
    check(file.isFile && file.length() > 0L) {
      "Missing app/google-services.json. Download it from Firebase Console and place it at app/google-services.json before building."
    }
    val content = file.readText()
    check("\"project_info\"" in content && "\"client\"" in content && "\"mobilesdk_app_id\"" in content) {
      "app/google-services.json is invalid or incomplete. Download the correct configuration file from Firebase Console."
    }
  }
}

tasks.configureEach {
  if (name.startsWith("pre") && name.endsWith("Build")) {
    dependsOn(validateGoogleServicesJson)
  }
}

secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation("androidx.security:security-crypto-ktx:1.1.0-alpha06")
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.converter.moshi)
  implementation(libs.firebase.auth)
  implementation(libs.firebase.firestore)
  implementation(libs.play.services.auth)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.moshi.kotlin.codegen)
}

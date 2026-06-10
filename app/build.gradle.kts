import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
}

val localProperties =
  Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
      file.inputStream().use(::load)
    }
  }

fun configuredValue(name: String, localName: String = name): String =
  providers.gradleProperty(name).orNull
    ?: providers.environmentVariable(name).orNull
    ?: localProperties.getProperty(localName)
    ?: ""

android {
    namespace = "com.example.aichatdemo"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.example.aichatdemo"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "DEEPSEEK_API_KEY", "\"${configuredValue("DEEPSEEK_API_KEY", "deepseek.api.key")}\"")
        buildConfigField("String", "DEEPSEEK_BASE_URL", "\"${configuredValue("DEEPSEEK_BASE_URL", "deepseek.base.url").ifBlank { "https://api.deepseek.com" }}\"")
        buildConfigField("String", "DEEPSEEK_MODEL", "\"${configuredValue("DEEPSEEK_MODEL", "deepseek.model").ifBlank { "deepseek-chat" }}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
      compose = true
      aidl = false
      buildConfig = true
      shaders = false
    }

    packaging {
      resources {
        excludes += "/META-INF/{AL2.0,LGPL2.1}"
      }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
  val composeBom = platform(libs.androidx.compose.bom)
  implementation(composeBom)
  androidTestImplementation(composeBom)

  // Core Android dependencies
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.recyclerview)
  implementation(libs.markwon.core)
  implementation(libs.okhttp)

  // Arch Components
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)

  // Compose
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  // Tooling
  debugImplementation(libs.androidx.compose.ui.tooling)
  // Instrumented tests
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.test.manifest)

  // Local tests: jUnit, coroutines, Android runner
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)

  // Instrumented tests: jUnit rules and runners
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.espresso.core)

  // Navigation
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.lifecycle.viewmodel.navigation3)
}

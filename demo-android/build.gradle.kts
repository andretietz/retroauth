plugins {
  id("com.android.application")
  kotlin("android")
  kotlin("kapt")
//  id("dagger.hilt.android.plugin")
}
android {
  compileSdkVersion(30)
  defaultConfig {
    applicationId = "com.andretietz.retroauth.demo"
    minSdkVersion(23)
    versionCode = 1
    versionName = "1.0.0"
  }
  buildTypes {
    getByName("release") {
      isMinifyEnabled = true
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  buildFeatures {
    viewBinding = true
//    compose = true
  }

  kotlinOptions {
    jvmTarget = "1.8"
  }

//  composeOptions {
//    kotlinCompilerVersion = Versions.kotlin
//    kotlinCompilerExtensionVersion = "1.0.0-rc02"
//  }
}

dependencies {
  api(project(":android-accountmanager"))
  implementation(project(":retroauth"))
  implementation(Dependencies.android.appcompat)
  implementation(Dependencies.coroutines)
  implementation(Dependencies.android.timber)
  implementation(Dependencies.android.fragment)
  implementation(Dependencies.android.constraintLayout)
  implementation(Dependencies.android.swipeRefresh)
  implementation(Dependencies.android.recyclerView)
  implementation(Dependencies.android.lifecycle.runtime)
  implementation(Dependencies.android.lifecycle.viewmodel)
  implementation(Dependencies.android.lifecycle.livedata)
  implementation(Dependencies.okhttp.okhttp)
  implementation(Dependencies.okhttp.loggingInterceptor)
  implementation(Dependencies.retrofit.moshiConverter)
  implementation(Dependencies.moshi.moshi) {
    exclude("org.jetbrains.kotlin", "kotlin-reflect")
  }
  kapt(Dependencies.moshi.moshiCodegen)

//  implementation(Dependencies.android.hilt)
//  kapt(Dependencies.android.hiltCodegen)

  // handling oauth login types. In this case: github
  implementation(Dependencies.scribe.scribe)
  implementation(Dependencies.scribe.okhttp)

//  implementation("androidx.compose.runtime:runtime:1.0.0-rc02")
//  implementation("androidx.compose.ui:ui:1.0.0-rc02")
//  implementation("androidx.compose.foundation:foundation-layout:1.0.0-rc02")
//  implementation("androidx.compose.material:material:1.0.0-rc02")
//  implementation("androidx.compose.material:material-icons-extended:1.0.0-rc02")
//  implementation("androidx.compose.foundation:foundation:1.0.0-rc02")
//  implementation("androidx.compose.animation:animation:1.0.0-rc02")
//  implementation("androidx.compose.ui:ui-tooling:1.0.0-rc02")
//  implementation("androidx.activity:activity-compose:1.3.0-rc02")


}

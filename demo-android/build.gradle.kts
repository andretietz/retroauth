plugins {
  id("com.android.application")
  kotlin("android")
  kotlin("kapt")
  id("dagger.hilt.android.plugin")
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
  }
}

dependencies {
  implementation(Dependencies.android.appcompat)
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
  implementation(Dependencies.moshi.moshi)
  kapt(Dependencies.moshi.moshiCodegen)

  implementation(Dependencies.android.hilt)
  kapt(Dependencies.android.hiltCodegen)

  // handling oauth login types. In this case: github
  implementation(Dependencies.scribe.scribe)
  implementation(Dependencies.scribe.okhttp)

  implementation(project(":android-accountmanager"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
  kotlinOptions.jvmTarget = "1.8"
}

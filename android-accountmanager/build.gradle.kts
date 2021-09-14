plugins {
  id("com.android.library")
  kotlin("android")
  id("com.vanniktech.maven.publish")
}

android {
  compileSdk = 31
  defaultConfig {
    minSdk = 21
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }
  buildTypes {
    getByName("release") {
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  testOptions.unitTests.isIncludeAndroidResources = true
  buildFeatures.buildConfig = false
}


dependencies {
  implementation(Dependencies.android.appcompat)
  implementation(Dependencies.android.startup)
  api(project(":retroauth"))

  testImplementation(Dependencies.test.junit)
  testImplementation(Dependencies.test.mockito)
  testImplementation(Dependencies.android.test.core)
  testImplementation(Dependencies.android.test.junit)
  testImplementation(Dependencies.android.test.robolectric)
  testImplementation(Dependencies.android.test.runner)
  testImplementation(Dependencies.android.test.rules)
  testImplementation(Dependencies.test.assertj)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
  kotlinOptions.jvmTarget = "1.8"
}

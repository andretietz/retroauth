object Versions {
  const val scribe = "8.1.0"
  const val kotlin = "1.5.21"
  const val coroutines = "1.5.1"
  const val junit = "4.13.2"
  const val retrofit = "2.9.0"
  const val okhttp = "4.9.1"
  const val moshi = "1.12.0"

  object android {
    const val recyclerView = "1.2.0"
    const val swipeRefresh = "1.1.0"
    const val fragment = "1.3.4"
    const val timber = "4.7.1"
    const val hilt = "2.38"
    const val appcompat = "1.3.0"
    const val viewmodel = "2.4.0-alpha01"
    const val constraintLayout = "2.0.4"
    const val startup = "1.0.0"

    object test {
      const val core = "1.3.0"
      const val junit = "1.1.2"
      const val robolectric = "4.5.1"
      const val runner = "1.3.0"
      const val rules = "1.3.0"
    }
  }

  object test {
    const val mockito = "3.7.7"
    const val mockitoKotlin = "2.2.0"
    const val junit = "4.13.2"
    const val assertj = "3.19.0"
  }

  object gradle {
    object plugin {
      const val android = "4.2.2"

      //      const val android = "7.0.0"
      const val compose = "0.4.0"
      const val dokka = "1.4.32"
      const val mavenPublish = "0.14.1"
      const val versions = "0.28.0"
    }
  }
}

object Dependencies {
  const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"

  object kotlin {
    const val kotlin = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}"
    const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
    const val coroutinesReactive = "org.jetbrains.kotlinx:kotlinx-coroutines-reactive:${Versions.coroutines}"
  }

  object scribe {
    const val scribe = "com.github.scribejava:scribejava-apis:${Versions.scribe}"
    const val okhttp = "com.github.scribejava:scribejava-httpclient-okhttp:${Versions.scribe}"
  }

  object okhttp {
    const val okhttp = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
    const val mockwebserver = "com.squareup.okhttp3:mockwebserver:${Versions.okhttp}"
    const val loggingInterceptor = "com.squareup.okhttp3:logging-interceptor:${Versions.okhttp}"
  }

  object retrofit {
    const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    const val moshiConverter = "com.squareup.retrofit2:converter-moshi:${Versions.retrofit}"
    const val gsonConverter = "com.squareup.retrofit2:converter-gson:${Versions.retrofit}"
  }

  object moshi {
    const val moshi = "com.squareup.moshi:moshi-kotlin:${Versions.moshi}"

    //    const val moshi = "com.squareup.moshi:moshi:${Versions.moshi}"
    const val moshiCodegen = "com.squareup.moshi:moshi-kotlin-codegen:${Versions.moshi}"
  }

  object test {
    const val junit = "junit:junit:${Versions.test.junit}"
    const val mockito = "org.mockito:mockito-core:${Versions.test.mockito}"
    const val mockitoKotlin =
      "com.nhaarman.mockitokotlin2:mockito-kotlin:${Versions.test.mockitoKotlin}"
    const val assertj = "org.assertj:assertj-core:${Versions.test.assertj}"
    const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutines}"
  }

  object android {
    const val appcompat = "androidx.appcompat:appcompat:${Versions.android.appcompat}"
    const val startup = "androidx.startup:startup-runtime:${Versions.android.startup}"
    const val timber = "com.jakewharton.timber:timber:${Versions.android.timber}"
    const val fragment = "androidx.fragment:fragment-ktx:${Versions.android.fragment}"
    const val constraintLayout =
      "androidx.constraintlayout:constraintlayout:${Versions.android.constraintLayout}"
    const val swipeRefresh =
      "androidx.swiperefreshlayout:swiperefreshlayout:${Versions.android.swipeRefresh}"
    const val recyclerView = "androidx.recyclerview:recyclerview:${Versions.android.recyclerView}"
    const val hilt = "com.google.dagger:hilt-android:${Versions.android.hilt}"
    const val hiltCodegen = "com.google.dagger:hilt-compiler:${Versions.android.hilt}"

    object lifecycle {
      const val runtime = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.android.viewmodel}"
      const val viewmodel =
        "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.android.viewmodel}"
      const val livedata = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.android.viewmodel}"
    }

    object test {
      const val core = "androidx.test:core:${Versions.android.test.core}"
      const val junit = "androidx.test.ext:junit:${Versions.android.test.junit}"
      const val robolectric = "org.robolectric:robolectric:${Versions.android.test.robolectric}"
      const val runner = "androidx.test:runner:${Versions.android.test.runner}"
      const val rules = "androidx.test:rules:${Versions.android.test.rules}"
    }
  }

  object gradle {
    object plugin {
      const val versions =
        "com.github.ben-manes:gradle-versions-plugin:${Versions.gradle.plugin.versions}"
      const val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
      const val compose =
        "org.jetbrains.compose:compose-gradle-plugin:${Versions.gradle.plugin.compose}"
      const val dokka = "org.jetbrains.dokka:dokka-gradle-plugin:${Versions.gradle.plugin.dokka}"
      const val android = "com.android.tools.build:gradle:${Versions.gradle.plugin.android}"
      const val hilt = "com.google.dagger:hilt-android-gradle-plugin:${Versions.android.hilt}"
      const val mavenPublish =
        "com.vanniktech:gradle-maven-publish-plugin:${Versions.gradle.plugin.mavenPublish}"
    }
  }
}

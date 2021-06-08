object Versions {
  const val kotlin = "1.5.10"
  const val junit = "4.13.2"
  const val retrofit = "2.9.0"
  const val okhttp = "4.9.1"
  const val moshi = "1.12.0"
  const val rxjava2 = "2.2.16"

  object Android {
    const val hilt = "2.36"
    const val compat = "1.3.0"
    const val viewmodel = "2.4.0-alpha01"
    const val constraintLayout = "2.0.4"
  }

  object Test {
    const val mockitoKotlin = "2.2.0"
    const val junit = "4.13.2"
  }
}

object Dependencies {
  const val kotlin = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}"

  const val rxjava2 = "io.reactivex.rxjava2:rxjava:${Versions.rxjava2}"
  const val okhttp = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"


  object retrofit {
    const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    const val moshiConverter = "com.squareup.retrofit2:converter-moshi:${Versions.retrofit}"
    const val gsonConverter = "com.squareup.retrofit2:converter-gson:${Versions.retrofit}"
    const val rxjava2Adapter = "com.squareup.retrofit2:adapter-rxjava2:${Versions.retrofit}"

  }

  object test {
    const val junit = "junit:junit:${Versions.Test.junit}"
    const val mockitoKotlin =
      "com.nhaarman.mockitokotlin2:mockito-kotlin:${Versions.Test.mockitoKotlin}"
    const val mockwebserver = "com.squareup.okhttp3:mockwebserver:${Versions.okhttp}"
  }
}

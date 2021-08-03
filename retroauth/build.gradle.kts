plugins {
  kotlin("jvm")
}

dependencies {
  api(Dependencies.kotlin.kotlin)
  api(Dependencies.kotlin.coroutines)
  api(Dependencies.retrofit.retrofit)
  api(Dependencies.okhttp.okhttp)
  api(Dependencies.coroutines)

  testImplementation(Dependencies.test.junit)
  testImplementation(Dependencies.test.coroutines)
  testImplementation(kotlin("reflect", version = Versions.kotlin))
  testImplementation(Dependencies.okhttp.mockwebserver)
  testImplementation(Dependencies.retrofit.moshiConverter)
  testImplementation(Dependencies.retrofit.gsonConverter)
  testImplementation(Dependencies.test.mockitoKotlin)
  testImplementation(Dependencies.test.assertj)
}

apply {
  from("$rootDir/gradle/publish.gradle")
}

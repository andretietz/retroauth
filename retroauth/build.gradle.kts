plugins {
  kotlin("jvm")
}

dependencies {
  api(Dependencies.kotlin)
  api(Dependencies.retrofit.retrofit)
  api(Dependencies.okhttp.okhttp)
  api(Dependencies.coroutines)

  testImplementation(Dependencies.test.junit)
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.1")
//  testImplementation(Dependencies.rxjava2)
  testImplementation(kotlin("reflect", version = Versions.kotlin))
  testImplementation(Dependencies.okhttp.mockwebserver)
  testImplementation(Dependencies.retrofit.moshiConverter)
//  testImplementation(Dependencies.retrofit.rxjava2Adapter)
  testImplementation(Dependencies.retrofit.gsonConverter)
  testImplementation(Dependencies.test.mockitoKotlin)
  testImplementation(Dependencies.test.assertj)
}

apply {
  from("$rootDir/gradle/publish.gradle")
}

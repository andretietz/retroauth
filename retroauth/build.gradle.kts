plugins {
  kotlin("jvm")
}

dependencies {
  api(Dependencies.kotlin)
  api(Dependencies.retrofit.retrofit)
  api(Dependencies.okhttp.okhttp)

  testImplementation(Dependencies.test.junit)
  testImplementation(Dependencies.rxjava2)
  testImplementation(kotlin("reflect", version = Versions.kotlin))
  testImplementation(Dependencies.okhttp.mockwebserver)
  testImplementation(Dependencies.retrofit.moshiConverter)
  testImplementation(Dependencies.retrofit.rxjava2Adapter)
  testImplementation(Dependencies.retrofit.gsonConverter)
  testImplementation(Dependencies.test.mockitoKotlin)
}

apply {
  from("$rootDir/gradle/publish.gradle")
}
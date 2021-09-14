plugins {
  kotlin("jvm")
  id("com.vanniktech.maven.publish")
}

dependencies {
  api(Dependencies.kotlin.kotlin)
  api(Dependencies.retrofit.retrofit)
  api(Dependencies.okhttp.okhttp)

  testImplementation(Dependencies.test.junit)
  testImplementation(Dependencies.test.coroutines)
  testImplementation(kotlin("reflect", version = Versions.kotlin))
  testImplementation(Dependencies.okhttp.mockwebserver)
  testImplementation(Dependencies.retrofit.moshiConverter)
  testImplementation(Dependencies.retrofit.gsonConverter)
  testImplementation(Dependencies.test.mockitoKotlin)
  testImplementation(Dependencies.test.assertj)
}

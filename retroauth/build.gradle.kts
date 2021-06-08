plugins {
  kotlin("jvm")
}

dependencies {
  api(Dependencies.kotlin)
  api(Dependencies.retrofit.retrofit)
  api(Dependencies.okhttp)

  testImplementation(Dependencies.test.junit)
  testImplementation(Dependencies.rxjava2)
  testImplementation(kotlin("reflect", version = Versions.kotlin))
  testImplementation(Dependencies.test.mockwebserver)
  testImplementation(Dependencies.retrofit.moshiConverter)
  testImplementation(Dependencies.retrofit.rxjava2Adapter)
  testImplementation(Dependencies.retrofit.gsonConverter)
  testImplementation(Dependencies.test.mockitoKotlin)
}
//tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
//  kotlinOptions.jvmTarget = "16"
////  kotlinOptions.freeCompilerArgs += "-Xuse-experimental=androidx.compose.foundation.ExperimentalFoundationApi"
////  kotlinOptions.freeCompilerArgs += "-Xuse-experimental=androidx.compose.ui.ExperimentalComposeUiApi"
////  kotlinOptions.freeCompilerArgs += "-Xuse-experimental=kotlin.io.path.ExperimentalPathApi"
//}
//
//
//java {
//    sourceCompatibility = JavaVersion.VERSION_1_8
//    targetCompatibility = JavaVersion.VERSION_1_8
//}
//
//apply from: rootProject.file('gradle/publish.gradle')

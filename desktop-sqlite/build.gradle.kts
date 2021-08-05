plugins {
  kotlin("jvm")
}

dependencies {
  api(Dependencies.kotlin.kotlin)
  implementation(project(":retroauth"))
  implementation("org.jetbrains.exposed:exposed-core:0.32.1")
  implementation("org.jetbrains.exposed:exposed-dao:0.32.1")
  implementation("org.jetbrains.exposed:exposed-jdbc:0.32.1")
  implementation("org.xerial:sqlite-jdbc:3.36.0.1")

  testImplementation(Dependencies.test.mockitoKotlin)
  testImplementation(Dependencies.test.assertj)
  testImplementation(Dependencies.test.junit)
  testImplementation(Dependencies.test.coroutines)
  testImplementation(kotlin("reflect", version = Versions.kotlin))
  // https://mvnrepository.com/artifact/com.h2database/h2
  testImplementation("com.h2database:h2:1.4.200")

}

apply {
  from("$rootDir/gradle/publish.gradle")
}

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
}

apply {
  from("$rootDir/gradle/publish.gradle")
}

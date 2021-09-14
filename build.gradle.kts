buildscript {
  repositories {
    google()
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    gradlePluginPortal()
    maven("https://plugins.gradle.org/m2/")
  }

  dependencies {
    classpath(Dependencies.gradle.plugin.compose)
    classpath(kotlin("gradle-plugin", version = Versions.kotlin))
    classpath(Dependencies.gradle.plugin.android)
    classpath(Dependencies.gradle.plugin.hilt)
    classpath(Dependencies.gradle.plugin.dokka)
    classpath(Dependencies.gradle.plugin.versions)
    classpath(Dependencies.gradle.plugin.mavenPublish)
  }
}

allprojects {
  repositories {
    google()
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  }
//  configurations.all {
//    resolutionStrategy.eachDependency { DependencyResolveDetails details ->
//      def requested = details.requested
//        if (requested.group == 'org.jetbrains.kotlin') {
//          details.useVersion "$kotlinVersion"
//        }
//    }
//  }
}


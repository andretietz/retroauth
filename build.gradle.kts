import Versions

buildscript {
  repositories {
    // TODO: remove after new build is published
    google()
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    gradlePluginPortal()
    maven("https://plugins.gradle.org/m2/")
  }

  dependencies {
    classpath("org.jetbrains.compose:compose-gradle-plugin:0.4.0")
    classpath(kotlin("gradle-plugin", version = Versions.kotlin))
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}")
    classpath("com.android.tools.build:gradle:4.2.1")
    classpath("com.google.dagger:hilt-android-gradle-plugin:${Versions.Android.hilt}")
    classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.4.32")
    classpath("com.github.ben-manes:gradle-versions-plugin:0.28.0")
    classpath("com.vanniktech:gradle-maven-publish-plugin:0.14.2")
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


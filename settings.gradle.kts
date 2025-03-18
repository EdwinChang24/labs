pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage") repositories {
        google()
        mavenCentral()
    }
}

plugins {
//    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "labs"
include("hello-world")
include("time-picker")
include("sudoku")

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.compose)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvm()
    sourceSets {
        commonMain {
            dependencies {
                implementation(compose.material3)
            }
        }
        jvmMain {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

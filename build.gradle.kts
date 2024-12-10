// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp") version "2.1.0-1.0.29" apply false
    id("org.jetbrains.kotlin.plugin.parcelize") version "2.1.0"
}

android {
    namespace = "com.spacemishka.app.shutterspeeder"
    compileSdk = 34
}
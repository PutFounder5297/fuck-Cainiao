plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "io.github.duzhaokun123.fuckcainiao"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.reova.fuckcainiao"
        minSdk = 24
        targetSdk = 36
        versionCode = 15
        versionName = "0.2.7"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    buildFeatures {
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    packaging.resources.excludes.add("**")
}

kotlin {
    // 本机只安装了 JDK 21；字节码目标仍为 Java 11，不影响产物。
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    compileOnly(libs.xposed.api)
    implementation(libs.ezXHelper)
    testImplementation(libs.junit)

}

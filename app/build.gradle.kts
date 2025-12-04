plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.catmusic"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.catmusic"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Android 官方库（通过版本目录）
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
//    第一步 安装依赖
    implementation("io.github.youth5201314:banner:2.2.3")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    // 第三方库（通过版本目录）
    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation(libs.picasso)

    // 测试依赖
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
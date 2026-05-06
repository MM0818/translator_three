plugins {
    id("com.android.application")  //应用程序模块插件
    id("org.jetbrains.kotlin.android")   //使用kotlin开发必须要用的插件
    id("kotlin-kapt")  //Room注解处理器需要的kapt插件
}

android {
    namespace = "com.example.translator_three"
    compileSdk = 34  //至少为34，不然有些依赖库不兼容  //项目的编译版本

    defaultConfig {
        applicationId = "com.example.translator_three"  //每个应用的唯一标识符
        minSdk = 24  //最小安卓兼容版本
        targetSdk = 34  //这个也改成34  //已经在该目标版本上测试过了，系统会给该app最新的功能和特性
        versionCode = 1  //版本号
        versionName = "1.0"  //版本名字

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"  //做测试
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {  //安装文件的配置，debug闭包可以不写
        release {  //release闭包，正式安装文件配置
            isMinifyEnabled = false  //是否混淆项目代码
            proguardFiles(   //指定混淆规则文件
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // 重构：启用 DataBinding & ViewBinding
    buildFeatures {
        compose = true
        dataBinding = true
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        dataBinding= true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    sourceSets {
        getByName("main") {
            /*jni {
                srcDirs("src\\main\\jni", "src\\main\\jni")
            }*/
            // 在这里配置main source set
            jniLibs.srcDirs("libs")
            jni {
                srcDirs("src\\main\\jni", "src\\main\\jniLibs")
            }
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")  //远程依赖
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.0")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.activity:activity:1.9.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    //讯飞语音识别库
    implementation(files("libs/Msc.jar"))

    //百度翻译：Retrofit和Gson转换器的依赖
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.google.code.gson:gson:2.8.9")

    // Room 核心依赖（版本号可根据 Android Studio 提示更新）
    implementation("androidx.room:room-runtime:2.6.1")
    //annotationProcessor("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-rxjava3:2.6.1")

    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // 2. 新增：协程依赖（适配 Kotlin 1.8.10 + JDK 17）
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // ViewModel + LiveData（Jetpack核心）
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")

    //和flow有关依赖
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
}
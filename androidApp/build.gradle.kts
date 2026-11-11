import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    // Lee androidApp/google-services.json (proyecto de la consola de Firebase).
    alias(libs.plugins.googleServices)
    // Sin este plugin Crashlytics no tiene build ID y la app se cierra al iniciar.
    alias(libs.plugins.firebaseCrashlytics)
}

// Guía 14: datos de firma fuera del repositorio. keystore.properties está en
// .gitignore y apunta a un .jks guardado fuera de la carpeta del proyecto.
// Si el archivo no existe (un clon nuevo, la integración continua), el
// proyecto igual compila en debug; solo el release queda sin firmar.
val archivoFirma = rootProject.file("keystore.properties")
val firma = Properties().apply {
    if (archivoFirma.exists()) {
        archivoFirma.inputStream().use { load(it) }
    }
}

// Guía 14: URL de PharmaSoft según dónde corre la app.
//   Emulador (por defecto): http://10.0.2.2:8080/api/v1/
//   Celular en la misma wifi: ./gradlew entregaAndroid -PurlServicio=http://10.40.29.85:8080/api/v1/
val urlServicio = (findProperty("urlServicio") as String? ?: "http://10.0.2.2:8080/api/v1/")
    .let { if (it.endsWith("/")) it else "$it/" }

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.activity.compose)
    // ContextCompat, ActivityCompat y NotificationCompat (permisos, Guía 13).
    implementation(libs.androidx.core.ktx)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    // Se mantiene el paquete registrado en Firebase (pharmamobil-1c729) en
    // lugar de pe.upeu.<proyecto>: cambiarlo invalida google-services.json.
    namespace = "pe.edu.upeu.pharmamobil"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "pe.edu.upeu.pharmamobil"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        // Guía 14: versión de entrega. Cada corrección posterior sube versionCode.
        versionCode = 1
        versionName = "1.0.0"
        buildConfigField("String", "URL_SERVICIO", "\"$urlServicio\"")
    }

    signingConfigs {
        if (archivoFirma.exists()) {
            create("entrega") {
                storeFile = file(firma.getProperty("storeFile"))
                storePassword = firma.getProperty("storePassword")
                keyAlias = firma.getProperty("keyAlias")
                keyPassword = firma.getProperty("keyPassword")
            }
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        release {
            // Guía 14: R8 reduce y ofusca; las reglas propias están en proguard-rules.pro.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (archivoFirma.exists()) {
                signingConfig = signingConfigs.getByName("entrega")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

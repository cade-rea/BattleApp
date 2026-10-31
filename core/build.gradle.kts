import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Game rules only: no Android dependencies, so the same module can run on a server.
plugins {
    alias(libs.plugins.kotlin.jvm)
    // fighters are saved as JSON: on the phone for now, on the server later
    alias(libs.plugins.kotlin.serialization)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    api(libs.kotlinx.serialization.json)

    testImplementation(libs.kotlin.test)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}

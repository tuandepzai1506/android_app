plugins {
    kotlin("jvm") version "2.2.0"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    // Firebase Admin SDK
    implementation("com.google.firebase:firebase-admin:9.10.0")

    // Gson
    implementation("com.google.code.gson:gson:2.13.2")

    // Ktor
    implementation("io.ktor:ktor-server-core-jvm:3.3.0")
    implementation("io.ktor:ktor-server-netty-jvm:3.3.0")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:3.3.0")

    testImplementation(kotlin("test"))
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass.set("com.example.backend.BackendServerKt")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
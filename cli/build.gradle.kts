plugins {
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("application")
}

group = "com.moea"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation("com.github.ajalt.clikt:clikt:5.0.2")
    implementation("io.ktor:ktor-client-core:3.0.2")
    implementation("io.ktor:ktor-client-cio:3.0.2")
    implementation("io.ktor:ktor-client-content-negotiation:3.0.2")
    implementation("io.ktor:ktor-serialization-gson:3.0.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.slf4j:slf4j-simple:2.0.16")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("com.moea.MainKt")
    applicationName = "moea-client"
}

tasks.named("installDist") {
    doLast({
        println("Installed binaries")
    })
}

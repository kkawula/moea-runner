plugins {
    id("java")
    id("application")
    id("org.springframework.boot") version "3.4.0"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "com.moea"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Test dependencies
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Spring Boot dependencies
    implementation("org.springframework.boot:spring-boot-starter-web")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // Annotation processor
    annotationProcessor("org.projectlombok:lombok")

    // Other dependencies
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.0")

    // MOEA Framework
    implementation("org.moeaframework:moeaframework:4.5")

    // RxJava
    implementation("io.reactivex.rxjava3:rxjava:3.1.9")

    // Math library
    implementation("org.apache.commons:commons-math3:3.6.1")
}

configurations {
    val compileOnly by getting {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

application {
    mainClass.set("com.moea.ServerApp")
    applicationName = "moea-server"
}

tasks.test {
    useJUnitPlatform()
}
plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
}

group = "ru.travelplanner"
version = "1.0.0"

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(platform(libs.spring.boot.bom))

    implementation(libs.spring.web)
    implementation(libs.spring.jdbc)
    implementation(libs.spring.validation)
    implementation(libs.jackson.kotlin)
    implementation(libs.kotlin.reflect)
    implementation(libs.liquibase)
    implementation(libs.springdoc)

    runtimeOnly(libs.postgresql)

    testImplementation(libs.spring.test)
    testImplementation(libs.testcontainers.postgresql)
    testRuntimeOnly(libs.junit.launcher)
}

tasks.test {
    useJUnitPlatform {
        excludeTags("external")
    }
}

tasks.register<Test>("externalTest") {
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath

    useJUnitPlatform {
        includeTags("external")
    }

    outputs.upToDateWhen { false }
}

tasks.bootJar {
    archiveFileName.set("travel-planner.jar")
}

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version "1.9.24"
}

group = "com.orcchg.trustwallet.task"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kt.coroutines.core)

    testImplementation(kotlin("test"))
    testImplementation(libs.kt.coroutines.test)
}

application {
    mainClassName = "com.orcchg.trustwallet.task.presentation.cli.CliMainKt"
}

tasks.run.configure {
    standardInput = System.`in`
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

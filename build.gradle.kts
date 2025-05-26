import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

val kotlinVersion = "1.9.24"

allprojects {
    group = "com.hibob"
}

plugins {
    `java-library`
    kotlin("jvm") version "1.9.24" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

allprojects {
    afterEvaluate {
        tasks.register<DependencyReportTask>("dependenciesRecursive") {
            group = "help"
            description = "Displays all dependencies declared in all projects recursively"
        }
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            showStandardStreams = true
            displayGranularity = 1
            showCauses = true
            showStackTraces = true
            exceptionFormat = TestExceptionFormat.FULL
            events(
                TestLogEvent.PASSED,
                TestLogEvent.SKIPPED,
                TestLogEvent.FAILED,
            )
        }
        reports.junitXml
        finalizedBy("ktlintCheck")
    }

    dependencies {
        implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    }
}

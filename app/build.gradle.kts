import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion = "1.9.24"

plugins {
    id("org.springframework.boot") version "3.2.4"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.24"
    kotlin("plugin.spring") version "1.9.24"
}

val javaMajorVersion = JavaVersion.VERSION_21.majorVersion

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(javaMajorVersion)
    }
}

println("Java toolchain compiler - ${javaToolchains.compilerFor(java.toolchain).get().executablePath}")

tasks {

    withType<KotlinCompile> {
        compilerOptions {
            jvmTarget = JvmTarget.fromTarget(javaMajorVersion)
        }
    }

    register<Copy>("stageDocker") {
        val clientModule = "lib"
        val buildDirectory = layout.buildDirectory
        val stageDockerDir = buildDirectory.dir("stageDocker").get()

        from(zipTree(buildDirectory.file("libs/${project.name}.jar"))) {
            exclude("**/$clientModule*.jar")
        }
        into(stageDockerDir)

        from(project(":$clientModule").layout.buildDirectory.dir("classes/kotlin/main")) {
            into("BOOT-INF/classes")
        }

        outputs.dir(stageDockerDir)
        dependsOn(":$clientModule:assemble", "assemble")
    }

    assemble {
        finalizedBy("stageDocker")
    }

    test {
        val defaultDbUrl = "jdbc:postgresql://localhost:5432/kotlin-template?user=bob&password=dev"
        val springProfiles = "development,test"

        val dbUrl = System.getenv("DB_URL").takeUnless { it.isNullOrBlank() } ?: defaultDbUrl

        systemProperty("DB_URL", dbUrl)
        systemProperty("spring.profiles.active", springProfiles)
    }
}

repositories {
    mavenCentral()
}

configurations.all {
    exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
    exclude(group = "commons-logging", module = "commons-logging")
}

dependencies {

    implementation(platform("com.hibob:bom:${project.property("com.hibob.bom")}"))

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("org.springframework.boot:spring-boot-starter-jersey")
    implementation("org.springframework.boot:spring-boot-starter-jetty")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("com.hibob:spring-boot-aws-starter")
    implementation("com.hibob:spring-boot-authentication-starter")
    implementation("com.hibob:spring-boot-database-starter")
    implementation("com.hibob:spring-boot-http-client-starter")
    implementation("com.hibob:spring-boot-jackson-starter")
    implementation("com.hibob:spring-boot-jersey-starter")
    implementation("com.hibob:spring-boot-s2s-starter")
    implementation("com.hibob:spring-boot-scheduler-starter")
    implementation("com.hibob:spring-boot-user-sessions-starter")
    implementation("com.hibob:spring-boot-redis-starter")
    implementation("com.hibob:spring-boot-redis-rate-limiter-starter")
    implementation("com.hibob:spring-boot-toggles-service-client-starter")

    implementation(project(":lib"))
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("javax.xml.bind:jaxb-api:2.3.1")
    implementation("org.jooq:jooq:3.16.9")
    implementation("org.bouncycastle:bcprov-jdk18on")
    implementation("org.apache.httpcomponents:httpclient")
    implementation("commons-io:commons-io")
    implementation("com.google.guava:guava")
    implementation("org.apache.commons:commons-lang3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.hibob:authentication")
    implementation("com.hibob:async-events")
    implementation("com.hibob:s2s")
    implementation("com.hibob:kms")
    implementation("com.hibob:encryption")
    implementation("com.hibob:metrics")

    // Logging
    implementation("com.hibob:kotlin-logging")
    implementation("ch.qos.logback:logback-classic")
    implementation("ch.qos.logback:logback-access")

    implementation("com.amazonaws:aws-java-sdk-kms")
    implementation("com.amazonaws:aws-java-sdk-core")
    implementation("com.amazonaws:aws-java-sdk-sts")
    implementation("com.hibob:user-sessions")
    implementation("com.hibob:toggles-service-client")
    implementation("com.hibob:kotlin-utils")

    runtimeOnly("org.postgresql:postgresql")

    testImplementation(testFixtures("com.hibob:spring-boot-test-utils"))
    testImplementation(testFixtures("com.hibob:rate-limiter-testkit"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.hamcrest:hamcrest-library")
    testImplementation("org.mockito.kotlin:mockito-kotlin")
    testImplementation("org.reflections:reflections")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

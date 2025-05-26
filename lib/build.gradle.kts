plugins {
    `maven-publish`
}

version = project.properties["publishVersion"] ?: "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(JavaVersion.VERSION_11.majorVersion))
    }
    withSourcesJar()
}
println("Java toolchain compiler - ${javaToolchains.compilerFor(java.toolchain).get().executablePath}")

publishing {
    repositories {
        maven {
            url = uri("https://hibob.jfrog.io/hibob/sbt-release")
            credentials {
                username = System.getenv("ARTIFACTORY_USER")
                password = System.getenv("ARTIFACTORY_PASSWORD")
            }
        }
    }
    publications {
        create<MavenPublication>(project.name) {
            from(components["java"])
            artifactId = "kotlin-template"

            versionMapping {
                allVariants {
                    fromResolutionResult()
                }
            }
        }
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.slf4j:slf4j-api:2.0.12")
    implementation(platform("com.fasterxml.jackson:jackson-bom:2.13.3"))
    implementation("com.fasterxml.jackson.core:jackson-core")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
}

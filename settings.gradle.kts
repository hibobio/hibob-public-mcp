rootProject.name = "kotlin-template"
include("app")
include("lib")

pluginManagement {
    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
    }

    repositories {
        gradlePluginPortal()
        maven {
            url = uri("https://hibob.jfrog.io/artifactory/gradle-plugins/")
            credentials {
                username = providers.gradleProperty("bob.artifactory.readonly.username").get()
                password = providers.gradleProperty("bob.artifactory.readonly.password").get()
            }
        }
        maven {
            url = uri("https://hibob.jfrog.io/artifactory/bob-maven/")
            credentials {
                username = providers.gradleProperty("bob.artifactory.readonly.username").get()
                password = providers.gradleProperty("bob.artifactory.readonly.password").get()
            }
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention")
}

gradle.allprojects {
    buildscript {
        repositories {
            mavenCentral()
            maven {
                url = uri("https://hibob.jfrog.io/artifactory/bob-maven/")
                credentials {
                    username = "${properties["bob.artifactory.readonly.username"]}"
                    password = "${properties["bob.artifactory.readonly.password"]}"
                }
            }
            mavenLocal()
        }
    }

    repositories {
        maven {
            url = uri("https://hibob.jfrog.io/artifactory/bob-maven/")
            credentials {
                username = "${properties["bob.artifactory.readonly.username"]}"
                password = "${properties["bob.artifactory.readonly.password"]}"
            }
        }
        mavenLocal()
    }
}

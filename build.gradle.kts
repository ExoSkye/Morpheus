import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform") version "1.7.20"
}

buildscript {
    dependencies {
        classpath("com.github.Strumenta.antlr-kotlin:antlr-kotlin-gradle-plugin:ebf3caf7ff")
    }
}

kotlin {
    linuxX64() {
        binaries {
            executable {
                entryPoint = "main"
            }
        }
    }
    mingwX64()  {
        binaries {
            executable {
                entryPoint = "main"
            }
        }
    }

    sourceSets {
        val commonAntlr by creating {
            dependencies {
                api(kotlin("stdlib-common"))
                api("com.github.Strumenta.antlr-kotlin:antlr-kotlin-runtime:ebf3caf7ff")
            }
            kotlin.srcDir("build/generated-src/commonAntlr/kotlin")
        }
        val commonMain by getting {
            dependsOn(commonAntlr)
        }

        val commonTest by getting {
            val commonTest by getting {
                dependencies {
                    implementation(kotlin("test"))
                }
            }
        }

        val desktopMain by creating {
            dependsOn(commonMain)
        }
        val linuxX64Main by getting {
            dependsOn(desktopMain)
        }
        val mingwX64Main by getting {
            dependsOn(desktopMain)
        }
    }
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
}

tasks.register<com.strumenta.antlrkotlin.gradleplugin.AntlrKotlinTask>("generateKotlinCommonGrammarSource") {
    antlrClasspath = configurations.detachedConfiguration(
        project.dependencies.create("com.github.Strumenta.antlr-kotlin:antlr-kotlin-target:ebf3caf7ff")
    )
    maxHeapSize = "64m"
    arguments = listOf("-no-visitor")
    source = project.objects
        .sourceDirectorySet("antlr", "antlr")
        .srcDir("src/commonAntlr/antlr").apply {
            include("*.g4")
        }
    outputDirectory = File("build/generated-src/commonAntlr/kotlin")
}


tasks.getByName("compileKotlinLinuxX64").dependsOn("generateKotlinCommonGrammarSource")
tasks.getByName("compileKotlinMingwX64").dependsOn("generateKotlinCommonGrammarSource")


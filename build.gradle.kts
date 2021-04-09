plugins {
    kotlin("jvm") version "1.3.72"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "com.anatawa12.crashReportPublisher"
version = "1.1"

repositories {
    mavenCentral()
    maven(url = "https://libraries.minecraft.net/")
}

dependencies {
    implementation(kotlin("stdlib"))
    shadow("net.minecraft:launchwrapper:1.12")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    jar {
        manifest {
            attributes("TweakClass" to "com.anatawa12.crashReportPublisher.CrashReportPublisherTweaker")
            attributes("TweakOrder" to "1000")
        }
        enabled = false
        dependsOn(shadowJar.get())
    }

    shadowJar {
        archiveClassifier.set("")
        val basePkg = "com.anatawa12.crashReportPublisher.libs"
        relocate("kotlin.", "$basePkg.kotlin.")
    }
}

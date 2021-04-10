plugins {
    kotlin("jvm") version "1.4.32"
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
    implementation("org.apache.httpcomponents:httpclient:4.5.13")
    implementation("org.apache.httpcomponents:httpmime:4.5.13")
    shadow("net.minecraft:launchwrapper:1.12")
    shadow("org.ow2.asm:asm:5.0")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.6"
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.6"
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
        relocate("org.apache.http.", "$basePkg.apache.http.")
        relocate("org.apache.commons.codec.", "$basePkg.apache.commons.codec.")
        relocate("org.apache.commons.logging.", "$basePkg.apache.commons.logging.")
        relocate("org.apache.commons.codec.", "$basePkg.apache.commons.codec.")
        relocate("org.intellij.lang.annotations.", "$basePkg.ij_annotations.")
        relocate("org.jetbrains.annotations.", "$basePkg.jb_annotations.")

        exclude("META-INF/**")
    }
}

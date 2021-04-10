plugins {
    id("com.github.johnrengelman.shadow") version "6.1.0"
    java
}

group = "com.anatawa12.crashReportPublisher"
version = "1.1"

repositories {
    mavenCentral()
    maven(url = "https://libraries.minecraft.net/")
}

dependencies {
    implementation("org.apache.httpcomponents:httpclient:4.5.13")
    implementation("org.apache.httpcomponents:httpmime:4.5.13")
    shadow("net.minecraft:launchwrapper:1.12")
    shadow("org.ow2.asm:asm:5.0")
}

java {
    targetCompatibility = JavaVersion.VERSION_1_6
    sourceCompatibility = JavaVersion.VERSION_1_6
}

tasks {
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
        relocate("org.apache.http.", "$basePkg.apache.http.")
        relocate("org.apache.commons.codec.", "$basePkg.apache.commons.codec.")
        relocate("org.apache.commons.logging.", "$basePkg.apache.commons.logging.")
        relocate("org.apache.commons.codec.", "$basePkg.apache.commons.codec.")

        exclude("META-INF/**")
    }
}

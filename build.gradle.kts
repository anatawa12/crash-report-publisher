buildscript {
    repositories {
        jcenter()
        google()
    }
    dependencies {
        classpath("com.guardsquare:proguard-gradle:7.0.1")
    }
}

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

val merges by configurations.creating
configurations.implementation.get().extendsFrom(merges)

dependencies {
    merges("org.apache.httpcomponents:httpclient:4.3.3")
    merges("org.apache.httpcomponents:httpmime:4.3.3")
    implementation("net.minecraft:launchwrapper:1.12")
    implementation("org.ow2.asm:asm:5.0")
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
    }

    shadowJar {
        destinationDirectory.set(buildDir.resolve("jars"))
        archiveBaseName.set("shadow")
        archiveVersion.set("")
        archiveClassifier.set("")

        configurations = mutableListOf(merges)

        relocate("org.apache.http.", "com.anatawa12.crashReportPublisher.http.")

        exclude("META-INF/**")
    }

    val proguard by creating(proguard.gradle.ProGuardTask::class) {
        dependsOn(shadowJar)
        injars(shadowJar.get().archiveFile)
        outjars(buildDir.resolve("jars/progurad.jar"))

        val javaHome = System.getProperty("java.home")

        libraryjars(files(
            file(javaHome).resolve("lib/rt.jar"),
            file(javaHome).resolve("lib/jce.jar")
        ))
        libraryjars(configurations.compileClasspath.get().files)

        keepattributes("*")
        keep("class com.anatawa12.crashReportPublisher.CrashReportPublisherTweaker { *; }")
        keepclassmembers("class com.anatawa12.crashReportPublisher.CrashReportPublisher {" +
                "  public static void onSaveToFile(...);" +
                "}")
        keepnames("class * { *; } ")
        keepnames("interface * { *; } ")

        dontwarn("org.apache.commons.logging.**")
    }

    val copyToLibs by creating(Zip::class) {
        dependsOn(proguard)
        from(zipTree(buildDir.resolve("jars/progurad.jar")))

        exclude("org/apache/commons/codec/language/bm/*")
        exclude("org/apache/commons/codec/language/bm")
        exclude("org/apache/commons/codec/language")
        exclude("org/apache/commons/codec")
        exclude("org/apache/commons")
        exclude("org/apache")
        exclude("org")

        val f = jar.get().archiveFile.get().asFile
        destinationDirectory.set(f.parentFile)
        archiveBaseName.set(f.name)
        archiveVersion.set("")
        archiveExtension.set("")
    }

    jar.get().dependsOn(copyToLibs)
}

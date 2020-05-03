plugins {
    kotlin("jvm") version "1.3.72"
}

group = "com.anatawa12.crashReportPublisher"
version = "1.0"

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.github.ajalt:clikt:2.6.0")
    implementation("net.dv8tion:JDA:4.1.1_141")

    runtimeOnly("org.slf4j:slf4j-simple:1.7.25")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    processResources {
        into("runtime-libs") {
            from(configurations.runtimeClasspath) {
                include("**/*.jar")
            }
        }
    }

    jar {
        manifest {
            attributes("Main-Class" to "RuntimeLibsLauncher")
        }
    }
}

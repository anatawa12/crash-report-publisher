plugins {
    kotlin("jvm") version "1.3.72"
}

group = "com.anatawa12.crashReportPublisher"
version = "1.1"

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.github.ajalt.clikt:clikt:3.1.0")
    implementation("net.dv8tion:JDA:4.2.0_247")

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

plugins {
    kotlin("jvm") version "2.4.10"
    id("com.gradleup.shadow") version "9.6.1"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.helpch.at/releases/")
}

val adventureApi = configurations.create("adventureApi") {
    isCanBeConsumed = false
    isCanBeResolved = false
}

configurations.compileOnly {
    extendsFrom(adventureApi)
}

configurations.testImplementation {
    extendsFrom(adventureApi)
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT") {
        isTransitive = false
    }
    compileOnly("me.clip:placeholderapi:2.11.6") {
        isTransitive = false
    }
    add(adventureApi.name, "net.kyori:adventure-api:4.14.0")
    add(adventureApi.name, "net.kyori:adventure-text-serializer-legacy:4.14.0")

    implementation("org.yaml:snakeyaml:2.5")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(25)
    compilerOptions {
        allWarningsAsErrors = true
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        mergeServiceFiles()
        relocate("org.yaml.snakeyaml", "moe.skd.displaynamefmt.lib.snakeyaml")
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
        manifest {
            attributes["Implementation-Version"] = project.version
        }
    }

    processResources {
        val props = mapOf("version" to version, "description" to project.description)
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    test {
        useJUnitPlatform()
    }
}

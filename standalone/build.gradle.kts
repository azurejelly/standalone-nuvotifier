import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.gradleup.shadow") version("8.3.0")
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":nuvotifier-common"))

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${Versions.JACKSON_YAML}")
    implementation("commons-cli:commons-cli:${Versions.COMMONS_CLI}")
    implementation("org.slf4j:slf4j-api:${Versions.SLF4J}")
    implementation("org.slf4j:slf4j-simple:${Versions.SLF4J}")
    implementation("redis.clients:jedis:${Versions.JEDIS}")

    testImplementation(platform("org.junit:junit-bom:${Versions.JUNIT}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks {
    test {
        useJUnitPlatform()
    }

    jar {
        from("src/main/resources") {
            include("config.yml")
        }

        manifest {
            attributes["Main-Class"] = "com.vexsoftware.nuvotifier.standalone.Main"
        }

        duplicatesStrategy = DuplicatesStrategy.WARN
    }

    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("nuvotifier-standalone")
        archiveClassifier.set("")
        archiveVersion.set("")
    }

    named("assemble").configure {
        dependsOn("shadowJar")
    }
}

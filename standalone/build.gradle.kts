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

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.2")
    implementation("commons-cli:commons-cli:1.9.0")
    implementation("com.google.guava:guava:33.3.0-jre")
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("org.slf4j:slf4j-simple:2.0.16")
    implementation("com.google.inject:guice:7.0.0") {
        exclude(group = "com.google.guava", module = "guava")
    }

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
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

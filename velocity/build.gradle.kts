import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    id("net.kyori.blossom")
}

apply(plugin = "com.github.johnrengelman.shadow")
applyPlatformAndCoreConfiguration()

blossom {
    replaceToken("@version@", project.ext["internalVersion"])
}

repositories {
    maven("https://nexus.velocitypowered.com/repository/maven-public/")
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:${Versions.VELOCITY}")
    annotationProcessor("com.velocitypowered:velocity-api:${Versions.VELOCITY}")

    api(project(":nuvotifier-api"))
    api(project(":nuvotifier-common"))
}

tasks {
    named<Copy>("processResources") {
        val internalVersion = project.ext["internalVersion"]
        inputs.property("internalVersion", internalVersion)

        filesMatching("bungee.yml") {
            expand("internalVersion" to internalVersion)
        }
    }

    named<Jar>("jar") {
        val projectVersion = project.version
        inputs.property("projectVersion", projectVersion)
        manifest {
            attributes("Implementation-Version" to projectVersion)
        }
    }

    named<ShadowJar>("shadowJar") {
        archiveClassifier.set("dist")

        val reloc = "com.vexsoftware.votifier.libs"
        relocate("redis", "$reloc.redis")
        relocate("org.json", "$reloc.json")
        relocate("org.apache", "$reloc.apache")
        relocate("org.slf4j", "$reloc.slf4j")
        relocate("io.netty", "$reloc.netty")
        relocate("com.google.gson", "$reloc.gson")

        exclude("GradleStart**")
        exclude(".cache");
        exclude("LICENSE*")
        exclude("META-INF/services/**")
        exclude("META-INF/maven/**")
        exclude("META-INF/versions/**")
        exclude("org/intellij/**")
        exclude("org/jetbrains/**")
        exclude("**/module-info.class")
        exclude("*.yml")
    }

    named("assemble").configure {
        dependsOn("shadowJar")
    }
}
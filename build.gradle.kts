plugins {
    id("java")
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
    }

    dependencies {
        compileOnly(rootProject.libs.findbugs)
        testImplementation(rootProject.libs.bundles.junit.jupiter)
        testImplementation(rootProject.libs.bundles.mockito)
    }

    configurations.all {
        resolutionStrategy {
            cacheChangingModulesFor(5, "MINUTES")
        }
    }

    tasks {
        configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(17))
            }

            disableAutoTargetJvm()
            withJavadocJar()
            withSourcesJar()
        }

        withType<JavaCompile> {
            val disabledLint = listOf("processing", "path", "fallthrough", "serial")

            options.release.set(11)
            options.compilerArgs.addAll(listOf("-Xlint:all") + disabledLint.map { "-Xlint:-$it" })
            options.isDeprecation = true
            options.encoding = "UTF-8"
            options.compilerArgs.add("-parameters")
        }

        withType<Test>().configureEach {
            useJUnitPlatform()
        }

        withType<Javadoc>().configureEach {
            options.encoding = "UTF-8"
            (options as StandardJavadocDocletOptions).apply {
                addStringOption("Xdoclint:none", "-quiet")
                tags(
                    "apiNote:a:API Note:",
                    "implSpec:a:Implementation Requirements:",
                    "implNote:a:Implementation Note:"
                )
            }
        }

        named<Copy>("processResources") {
            filesMatching("bungee.yml|plugin.yml") {
                expand("version" to rootProject.version)
            }
        }

        named<Jar>("jar") {
            manifest {
                attributes("Implementation-Version" to rootProject.version)
            }
        }
    }
}
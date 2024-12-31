plugins {
    `java-library`
}

applyPlatformAndCoreConfiguration()

dependencies {
    compileOnly("com.google.code.gson:gson:${Versions.GSON}")
    testImplementation("com.google.code.gson:gson:${Versions.GSON}")
}

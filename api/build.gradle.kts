plugins {
    `java-library`
}

dependencies {
    compileOnly(libs.gson)
    testImplementation(libs.gson)
}

plugins {
    `java-library`
}

dependencies {
    api(project(":nuvotifier-api"))
    compileOnly(libs.gson)
    implementation(libs.bundles.netty)
    implementation(libs.jedis)
    testImplementation(libs.json) // retain this for testing reasons
    testImplementation(libs.guava)
}
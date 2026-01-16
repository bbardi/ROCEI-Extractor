plugins {
    id("java")
    id("com.gradleup.shadow") version "9.3.0"
}

group = "ro.bbardi"
version = "1.0-SNAPSHOT"



repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("org.bouncycastle:bcprov-jdk18on:1.83")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "ro.bbardi.Main"
    }
    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA", "META-INF/INDEX.LIST")
    from(configurations.runtimeClasspath.get().map({ if (it.isDirectory) it else zipTree(it) }))
}
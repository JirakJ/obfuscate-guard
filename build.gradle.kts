plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.intellij.platform)
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

kotlin {
    jvmToolchain(providers.gradleProperty("javaVersion").get().toInt())
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        create(
            providers.gradleProperty("platformType"),
            providers.gradleProperty("platformVersion"),
        )
        bundledPlugin("com.intellij.java")
        instrumentationTools()
        pluginVerifier()
        zipSigner()
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }

    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)

    // Platform test framework for UI/editor-level tests and screenshot rendering
    testImplementation("junit:junit:4.13.2")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.11.4")
}

// Exclude IDE-provided libraries from plugin distribution
configurations {
    runtimeClasspath {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains", module = "annotations")
    }
}

intellijPlatform {
    pluginConfiguration {
        id = providers.gradleProperty("pluginGroup")
        name = providers.gradleProperty("pluginName")
        version = providers.gradleProperty("pluginVersion")

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
        }

        vendor {
            name = "Jakub Jirák"
            url = "https://htmeta.dev"
        }
    }

    pluginVerification {
        ides {
            ide("IC", "2023.2.8")
            ide("IC", "2024.3")
            ide("IC", "2025.1.3")
        }
    }

    signing {
        // ORG_GRADLE_PROJECT_signCertificateChain
        // ORG_GRADLE_PROJECT_signPrivateKey
        // ORG_GRADLE_PROJECT_signPassword
    }

    publishing {
        token.set(System.getenv("PLUGIN_TOKEN"))
    }
}

tasks {
    test {
        useJUnitPlatform()
        // Screenshot rendering opts in via: ./gradlew test -PscreenshotsDir=<absolute path>
        systemProperty("obfuscateguard.screenshots.dir", (project.findProperty("screenshotsDir") ?: "").toString())
    }

    wrapper {
        gradleVersion = "8.12"
    }
}

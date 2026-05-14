import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform")
    id("org.jetbrains.changelog")
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = "17"
    targetCompatibility = "17"
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            // Honour the since-build declared in plugin.xml (233 = 2023.3) instead of
            // auto-bumping to the SDK build number.
            sinceBuild = "233"
            untilBuild = provider { null }
        }
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")

    // IntelliJ Platform Gradle Plugin Dependencies Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html
    intellijPlatform {
        intellijIdeaCommunity("2024.3.5")
        bundledPlugin("org.jetbrains.kotlin")
        // Uncomment to hard-require Android Studio (or IntelliJ Ultimate + Android plugin):
        // bundledPlugin("org.jetbrains.android")
        testFramework(TestFrameworkType.Platform)
    }
}

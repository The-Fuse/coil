import coil.by
import coil.groupId
import coil.versionName
import com.android.build.gradle.BaseExtension
import kotlinx.validation.ApiValidationExtension
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import java.net.URL

buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        gradlePluginPortal()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.0-rc02")
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.13.0-SNAPSHOT")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.4.0")
        classpath("org.jetbrains.kotlinx:binary-compatibility-validator:0.2.3")
        classpath("org.jlleitschuh.gradle:ktlint-gradle:9.3.0")
        classpath(kotlin("gradle-plugin", version = "1.4.0"))
    }
}

apply(plugin = "binary-compatibility-validator")

extensions.configure<ApiValidationExtension> {
    ignoredProjects = mutableSetOf("coil-sample", "coil-test")
}

allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter()
    }

    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    group = project.groupId
    version = project.versionName

    extensions.configure<KtlintExtension>("ktlint") {
        version by "0.38.1"
        enableExperimentalRules by true
        disabledRules by setOf(
            "experimental:annotation",
            "import-ordering",
            "indent",
            "max-line-length",
            "parameter-list-wrapping"
        )
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            allWarningsAsErrors = true
            freeCompilerArgs = listOf("-progressive", "-Xopt-in=kotlin.RequiresOptIn")
            jvmTarget = "1.8"
        }
    }

    tasks.withType<Test> {
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            events = setOf(TestLogEvent.SKIPPED, TestLogEvent.PASSED, TestLogEvent.FAILED)
            showStandardStreams = true
        }
    }

    tasks.withType<DokkaTask> {
        dokkaSourceSets {
            configureEach {
                jdkVersion.set(8)
                reportUndocumented.set(false)
                skipDeprecated.set(true)
                skipEmptyPackages.set(true)
                outputDirectory.set(file("$rootDir/docs/api"))

                externalDocumentationLink {
                    url.set(URL("https://developer.android.com/reference/"))
                    packageListUrl.set(URL("https://developer.android.com/reference/androidx/package-list"))
                }
                externalDocumentationLink {
                    url.set(URL("https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-android/"))
                    packageListUrl.set(URL("https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-android/package-list"))
                }
                externalDocumentationLink {
                    url.set(URL("https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/"))
                    packageListUrl.set(URL("https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/package-list"))
                }
                externalDocumentationLink {
                    url.set(URL("https://square.github.io/okhttp/3.x/okhttp/"))
                    packageListUrl.set(URL("https://square.github.io/okhttp/3.x/okhttp/package-list"))
                }
                externalDocumentationLink {
                    url = URL("https://square.github.io/okio/2.x/okio/")
                    packageListUrl = URL("file://$rootDir/package-list-okio")
                }

                // Include the coil-base documentation link for extension artifacts.
                if (project.name != "coil-base") {
                    externalDocumentationLink {
                        url = URL("https://coil-kt.github.io/coil/api/coil-base/")
                        packageListUrl = URL("file://$rootDir/package-list-coil-base")
                    }
                }
            }
        }
    }
}

subprojects {
    afterEvaluate {
        extensions.configure<BaseExtension> {
            // Require that all Android projects target Java 8.
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_1_8
                targetCompatibility = JavaVersion.VERSION_1_8
            }
        }
    }
}

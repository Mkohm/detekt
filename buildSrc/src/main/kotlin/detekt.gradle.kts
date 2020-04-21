import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask

plugins {
    id("io.gitlab.arturbosch.detekt")
}

val analysisDir = file(projectDir)
val baselineFile = file("$rootDir/config/detekt/baseline.xml")
val configFile = file("$rootDir/config/detekt/detekt.yml")
val formatConfigFile = file("$rootDir/config/detekt/format.yml")
val statisticsConfigFile = file("$rootDir/config/detekt/statistics.yml")

val kotlinFiles = "**/*.kt"
val kotlinScriptFiles = "**/*.kts"
val resourceFiles = "**/resources/**"
val buildFiles = "**/build/**"

subprojects {

    apply {
        plugin("io.gitlab.arturbosch.detekt")
    }

    tasks.withType<Detekt> {
        jvmTarget = "1.8"
    }

    val userHome = System.getProperty("user.home")

    detekt {
        buildUponDefaultConfig = true
        baseline = baselineFile

        reports {
            xml.enabled = true
            html.enabled = true
            txt.enabled = true
        }

        idea {
            path = "$userHome/.idea"
            codeStyleScheme = "$userHome/.idea/idea-code-style.xml"
            inspectionsProfile = "$userHome/.idea/inspect.xml"
            report = "${project.projectDir}/reports"
            mask = "*.kt"
        }
    }
}

allprojects {

    dependencies {
        detekt(project(":detekt-cli"))
        detektPlugins(project(":detekt-formatting"))
        detektPlugins("io.github.mkohm:detekt-hint:0.1.4")
    }
}

val detektFormat by tasks.registering(Detekt::class) {
    description = "Formats whole project."
    parallel = true
    disableDefaultRuleSets = true
    buildUponDefaultConfig = true
    autoCorrect = true
    setSource(analysisDir)
    config.setFrom(listOf(statisticsConfigFile, formatConfigFile))
    include(kotlinFiles)
    include(kotlinScriptFiles)
    exclude(resourceFiles)
    exclude(buildFiles)
    baseline.set(baselineFile)
    reports {
        xml.enabled = false
        html.enabled = false
        txt.enabled = false
    }
}

val detektAll by tasks.registering(Detekt::class) {
    description = "Runs the whole project at once."
    parallel = true
    buildUponDefaultConfig = true
    setSource(analysisDir)
    config.setFrom(listOf(statisticsConfigFile, configFile))
    include(kotlinFiles)
    include(kotlinScriptFiles)
    exclude(resourceFiles)
    exclude(buildFiles)
    baseline.set(baselineFile)
    reports {
        xml.enabled = false
        html.enabled = false
        txt.enabled = false
    }
}

task<Detekt>("detektHint") {
    description = "Running detekt-hint and outputs report for Danger to consume."
    setSource(analysisDir)
    config.setFrom(file("$rootDir/config/detekt/detekt-hint.yml"))
    classpath.setFrom(detektClasspath)
    reports {
        xml {
            enabled = true
            destination = file("$buildDir/reports/detekt-hint-report.xml")
        }
        html.enabled = false
        txt.enabled = false
    }
    include(kotlinFiles)
    exclude(resourceFiles)
    exclude(buildFiles)
}

val detektProjectBaseline by tasks.registering(DetektCreateBaselineTask::class) {
    description = "Overrides current baseline."
    buildUponDefaultConfig.set(true)
    ignoreFailures.set(true)
    parallel.set(true)
    setSource(analysisDir)
    config.setFrom(listOf(statisticsConfigFile, configFile))
    include(kotlinFiles)
    include(kotlinScriptFiles)
    exclude(resourceFiles)
    exclude(buildFiles)
    baseline.set(baselineFile)
}

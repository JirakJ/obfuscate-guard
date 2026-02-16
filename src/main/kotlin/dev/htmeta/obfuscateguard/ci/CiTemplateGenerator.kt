package dev.htmeta.obfuscateguard.ci

import dev.htmeta.obfuscateguard.core.model.EngineSpecificConfig
import dev.htmeta.obfuscateguard.core.model.EngineType

/**
 * Generates CI/CD pipeline templates for running obfuscation in headless mode.
 */
object CiTemplateGenerator {

    fun generateGitHubActions(config: EngineSpecificConfig): String = """
        |name: Obfuscation Build
        |
        |on:
        |  push:
        |    branches: [ main ]
        |  pull_request:
        |    branches: [ main ]
        |
        |jobs:
        |  obfuscate:
        |    runs-on: ubuntu-latest
        |
        |    steps:
        |    - uses: actions/checkout@v4
        |
        |    - name: Set up JDK 17
        |      uses: actions/setup-java@v4
        |      with:
        |        java-version: '17'
        |        distribution: 'temurin'
        |
        |    - name: Setup Gradle
        |      uses: gradle/actions/setup-gradle@v4
        |
        |    - name: Run obfuscation
        |      run: ${getGradleCommand(config)}
        |
        |    - name: Upload obfuscated artifacts
        |      uses: actions/upload-artifact@v4
        |      with:
        |        name: obfuscated-output
        |        path: build/outputs/
        |
        |    - name: Upload mapping file
        |      uses: actions/upload-artifact@v4
        |      with:
        |        name: mapping
        |        path: build/outputs/mapping/
    """.trimMargin()

    fun generateGitLabCI(config: EngineSpecificConfig): String = """
        |stages:
        |  - build
        |  - obfuscate
        |
        |obfuscate:
        |  stage: obfuscate
        |  image: eclipse-temurin:17-jdk
        |  script:
        |    - ${getGradleCommand(config)}
        |  artifacts:
        |    paths:
        |      - build/outputs/
        |      - build/outputs/mapping/
        |    expire_in: 30 days
    """.trimMargin()

    fun generateJenkinsfile(config: EngineSpecificConfig): String = """
        |pipeline {
        |    agent any
        |
        |    tools {
        |        jdk 'JDK17'
        |    }
        |
        |    stages {
        |        stage('Obfuscate') {
        |            steps {
        |                sh '${getGradleCommand(config)}'
        |            }
        |            post {
        |                always {
        |                    archiveArtifacts artifacts: 'build/outputs/mapping/**', allowEmptyArchive: true
        |                }
        |            }
        |        }
        |    }
        |}
    """.trimMargin()

    private fun getGradleCommand(config: EngineSpecificConfig): String = when (config.engineType) {
        EngineType.PROGUARD -> "./gradlew assembleRelease -Pandroid.enableR8=true"
        EngineType.YGUARD -> "./gradlew yguardObfuscate"
        EngineType.ALLATORI -> "./gradlew allatoriObfuscate"
    }

    fun allTemplates(config: EngineSpecificConfig): Map<String, String> = mapOf(
        "GitHub Actions (.github/workflows/obfuscate.yml)" to generateGitHubActions(config),
        "GitLab CI (.gitlab-ci.yml)" to generateGitLabCI(config),
        "Jenkinsfile" to generateJenkinsfile(config),
    )
}

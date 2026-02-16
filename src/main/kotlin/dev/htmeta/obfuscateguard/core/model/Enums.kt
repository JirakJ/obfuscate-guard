package dev.htmeta.obfuscateguard.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class EngineType(val displayName: String) {
    PROGUARD("ProGuard/R8"),
    YGUARD("yGuard"),
    ALLATORI("Allatori"),
}

@Serializable
enum class ObfuscationLevel(val displayName: String, val description: String) {
    LIGHT("Light", "Rename packages only — minimal risk, minimal protection"),
    STANDARD("Standard", "Rename classes, methods, fields — good balance of protection and safety"),
    AGGRESSIVE("Aggressive", "Full renaming + optimization + repackaging — maximum protection"),
}

@Serializable
enum class ProtectionGoal(val displayName: String) {
    IP_PROTECTION("Intellectual Property Protection"),
    ANTI_REVERSE_ENGINEERING("Anti-Reverse Engineering"),
    STRING_PROTECTION("String Encryption"),
}

@Serializable
enum class ProjectType(val displayName: String) {
    APP("Application"),
    LIBRARY("Library"),
    ANDROID_APP("Android App"),
    ANDROID_LIBRARY("Android Library"),
    PLUGIN("IDE Plugin"),
    UNKNOWN("Unknown"),
}

@Serializable
enum class BuildSystem(val displayName: String) {
    GRADLE("Gradle"),
    MAVEN("Maven"),
    UNKNOWN("Unknown"),
}

@Serializable
enum class RiskCategory(val displayName: String) {
    REFLECTION("Reflection"),
    SERIALIZATION("Serialization"),
    JNI("JNI / Native"),
    DI_FRAMEWORK("Dependency Injection"),
    DYNAMIC_LOADING("Dynamic Loading"),
    ANDROID("Android-specific"),
}

@Serializable
enum class RiskSeverity(val displayName: String, val icon: String) {
    CRITICAL("Critical", "🔴"),
    WARNING("Warning", "🟡"),
    INFO("Info", "🔵"),
}

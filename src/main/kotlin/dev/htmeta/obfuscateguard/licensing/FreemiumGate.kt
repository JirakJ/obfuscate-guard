/*
 * Copyright (c) 2026 Jakub Jirák. All rights reserved.
 * Licensed under the terms of the JetBrains Marketplace EULA.
 */
package dev.htmeta.obfuscateguard.licensing

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.ui.LicensingFacade

@Service(Service.Level.APP)
class FreemiumGate {

    enum class LicenseStatus { FREE, TRIAL, PAID }

    companion object {
        const val PRODUCT_CODE = "POBFUSCATEGUARD"

        const val FEATURE_DEOBFUSCATOR = "deobfuscator"
        const val FEATURE_MAPPING_VAULT = "mapping_vault"
        const val FEATURE_CI_CD_EXPORT = "ci_cd_export"
        const val FEATURE_KEEP_RULES_GENERATOR = "keep_rules_generator"
        const val FEATURE_POLICY_ENGINE = "policy_engine"
        const val FEATURE_AUDIT_TRAIL = "audit_trail"
        const val FEATURE_MULTI_ENGINE = "multi_engine"
        const val FEATURE_CUSTOM_RULES = "custom_rules"

        private val PREMIUM_FEATURES = setOf(
            FEATURE_DEOBFUSCATOR, FEATURE_MAPPING_VAULT, FEATURE_CI_CD_EXPORT,
            FEATURE_KEEP_RULES_GENERATOR, FEATURE_POLICY_ENGINE, FEATURE_AUDIT_TRAIL,
            FEATURE_MULTI_ENGINE, FEATURE_CUSTOM_RULES
        )

        fun getInstance(): FreemiumGate =
            ApplicationManager.getApplication().getService(FreemiumGate::class.java)
    }

    fun getLicenseStatus(): LicenseStatus {
        val facade = LicensingFacade.getInstance() ?: return LicenseStatus.FREE
        val stamp = facade.getConfirmationStamp(PRODUCT_CODE) ?: return LicenseStatus.FREE
        return if (stamp.startsWith("eval:")) LicenseStatus.TRIAL else LicenseStatus.PAID
    }

    fun isPaidFeatureAvailable(featureId: String): Boolean {
        if (featureId !in PREMIUM_FEATURES) return true
        return getLicenseStatus() != LicenseStatus.FREE
    }

    fun isPremiumUser(): Boolean = getLicenseStatus() != LicenseStatus.FREE
}

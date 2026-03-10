/*
 * Copyright (c) 2026 Jakub Jirák. All rights reserved.
 * Licensed under the terms of the JetBrains Marketplace EULA.
 */
package dev.htmeta.obfuscateguard.licensing

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FreemiumGateTest {

    @Test
    fun `product code is POBFUSCATEGUARD`() {
        assertEquals("POBFUSCATEGUARD", FreemiumGate.PRODUCT_CODE)
    }

    @Test
    fun `feature constants are defined`() {
        assertEquals("deobfuscator", FreemiumGate.FEATURE_DEOBFUSCATOR)
        assertEquals("mapping_vault", FreemiumGate.FEATURE_MAPPING_VAULT)
        assertEquals("ci_cd_export", FreemiumGate.FEATURE_CI_CD_EXPORT)
        assertEquals("keep_rules_generator", FreemiumGate.FEATURE_KEEP_RULES_GENERATOR)
        assertEquals("policy_engine", FreemiumGate.FEATURE_POLICY_ENGINE)
        assertEquals("audit_trail", FreemiumGate.FEATURE_AUDIT_TRAIL)
        assertEquals("multi_engine", FreemiumGate.FEATURE_MULTI_ENGINE)
        assertEquals("custom_rules", FreemiumGate.FEATURE_CUSTOM_RULES)
    }

    @Test
    fun `premium features set contains exactly eight features`() {
        val premiumFeatures = setOf(
            FreemiumGate.FEATURE_DEOBFUSCATOR,
            FreemiumGate.FEATURE_MAPPING_VAULT,
            FreemiumGate.FEATURE_CI_CD_EXPORT,
            FreemiumGate.FEATURE_KEEP_RULES_GENERATOR,
            FreemiumGate.FEATURE_POLICY_ENGINE,
            FreemiumGate.FEATURE_AUDIT_TRAIL,
            FreemiumGate.FEATURE_MULTI_ENGINE,
            FreemiumGate.FEATURE_CUSTOM_RULES
        )
        assertEquals(8, premiumFeatures.size)
    }

    @Test
    fun `license status enum has three values`() {
        val values = FreemiumGate.LicenseStatus.values()
        assertEquals(3, values.size)
        assertTrue(values.any { it == FreemiumGate.LicenseStatus.FREE })
        assertTrue(values.any { it == FreemiumGate.LicenseStatus.TRIAL })
        assertTrue(values.any { it == FreemiumGate.LicenseStatus.PAID })
    }
}

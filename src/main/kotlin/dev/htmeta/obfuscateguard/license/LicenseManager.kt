package dev.htmeta.obfuscateguard.license

/**
 * Feature flag service for freemium gating.
 * In the free tier, only basic features are available.
 * Pro/Business tiers unlock advanced features.
 */
object LicenseManager {

    enum class Tier { FREE, PRO, BUSINESS }

    // Current tier — in production, this would check JetBrains Marketplace license
    private var currentTier: Tier = Tier.FREE

    fun getCurrentTier(): Tier = currentTier

    fun isProOrHigher(): Boolean = currentTier != Tier.FREE
    fun isBusiness(): Boolean = currentTier == Tier.BUSINESS

    // Feature checks
    fun canUseAnalyzer(): Boolean = isProOrHigher()
    fun canUseKeepRulesGenerator(): Boolean = isProOrHigher()
    fun canUseDryRun(): Boolean = isProOrHigher()
    fun canUseMappingVault(): Boolean = isProOrHigher()
    fun canUseCiExport(): Boolean = isProOrHigher()
    fun canUsePolicyEngine(): Boolean = isBusiness()
    fun canUseAuditTrail(): Boolean = isBusiness()
    fun canUseSharedProfiles(): Boolean = isBusiness()

    // Free tier features (always available)
    fun canUseWizard(): Boolean = true
    fun canUseRunConfiguration(): Boolean = true
    fun canUseToolWindow(): Boolean = true
    fun canUseProjectDetection(): Boolean = true
}

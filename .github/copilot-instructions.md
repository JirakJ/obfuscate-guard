# ObfuscateGuard — Copilot Instructions

## Project Identity
You are working on **ObfuscateGuard**, a JetBrains IntelliJ Platform plugin (Kotlin) that orchestrates and safeguards code obfuscation for Java/Kotlin projects. It is NOT an obfuscation engine itself — it wraps existing engines (ProGuard/R8, yGuard, Allatori) and adds analysis, governance, and workflow.

## Tech Stack
- **Language:** Kotlin (JVM target 17+)
- **Platform:** IntelliJ Platform SDK (2024.x+)
- **Build:** Gradle with IntelliJ Platform Gradle Plugin 2.x
- **Serialization:** kotlinx.serialization
- **Testing:** JUnit 5 + IntelliJ Test Framework

## Architecture Principles
- All heavy operations run on background threads — **never block EDT**.
- UI only renders state; business logic lives in services.
- Configuration is serialized via a custom `PersistentStateComponent` with schema versioning and migration.
- Every obfuscation "run" produces an **immutable model** (input snapshot + output + mapping).
- Engine adapters implement a common SPI (`ObfuscationEngineAdapter`); adding a new engine must not require modifying existing code.
- Analyzer rules also use an SPI (`AnalyzerRule`); each rule is a small, focused class.

## Coding Standards
- Write **Kotlin** (no Java in new code).
- Small classes, single responsibility. Prefer composition over inheritance.
- No `println` — use IntelliJ `Logger` (`com.intellij.openapi.diagnostic.Logger`).
- No experimental IntelliJ API without a feature flag guard.
- Document public API with **KDoc**. Internal code: comment only when non-obvious.
- Use `@NlsSafe` / `@Nls` annotations for user-facing strings.
- All user-facing strings go through resource bundles (`messages/ObfuscateGuardBundle.properties`).
- Prefer `ReadAction` / `WriteAction` wrappers for PSI access.
- Use `CoroutineScope` from IntelliJ platform services (not `GlobalScope`).

## Security Rules (CRITICAL)
- **Never** send source code, symbols, or mappings outside the device without explicit user opt-in.
- Mapping vault is encrypted (AES-256). Key is derived from machine-specific seed.
- Telemetry is anonymous and minimal (event counts only, no PII, no code).
- Generated configuration must not contain absolute paths, secrets, or tokens.
- When in doubt about a keep rule, generate it but mark as `needsReview = true`.

## Safe Defaults Philosophy
- Always prefer **over-keeping** to **under-keeping**. A missed keep rule breaks the app; an extra one just reduces obfuscation slightly.
- Default obfuscation level is `LIGHT` for libraries, `STANDARD` for apps.
- Dry-run validation is always suggested (never skipped silently).
- Warnings are non-blocking but visible.

## Testing Strategy
- **Unit tests** for analyzer rules and config generators (> 80% coverage target).
- **Integration tests** for engine adapter interactions (> 70% coverage target).
- **Golden tests** for CI templates and generated configurations (every template has one).
- **UI tests** for critical wizard paths.
- Test files go in `src/test/kotlin/` mirroring main source structure.
- Use IntelliJ `BasePlatformTestCase` / `LightPlatformCodeInsightFixtureTestCase` for PSI tests.

## File Organization
```
src/
  main/
    kotlin/dev/htmeta/obfuscateguard/
      core/           # Domain models, interfaces
      analyzer/       # Risk analysis rules
      config/         # Config generation
      engine/         # Engine adapters (proguard/, yguard/, allatori/)
      mapping/        # Mapping management
      ci/             # CI/CD export
      ui/             # Tool window, wizard, settings
      license/        # Licensing and feature flags
      util/           # Shared utilities
    resources/
      META-INF/plugin.xml
      messages/ObfuscateGuardBundle.properties
  test/
    kotlin/dev/htmeta/obfuscateguard/
      # Mirrors main structure
    testData/
      # Sample projects and golden files
```

## Git Conventions
- Branch naming: `feature/FR-XXX-short-description`, `fix/issue-description`, `chore/description`
- Commit messages: conventional commits (`feat:`, `fix:`, `chore:`, `docs:`, `test:`, `refactor:`)
- Always include `Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>` trailer when Copilot assists.

## Common Patterns

### Creating a new Analyzer Rule
```kotlin
class ReflectionUsageRule : AnalyzerRule {
    override val id = "reflection-usage"
    override val category = RiskCategory.REFLECTION

    override fun analyze(psiFile: PsiFile, context: AnalysisContext): List<RiskItem> {
        // Use PsiTreeUtil to find relevant patterns
        // Return RiskItem with location, severity, and suggested keep rule
    }
}
```

### Creating a new Engine Adapter
```kotlin
class ProGuardAdapter : ObfuscationEngineAdapter {
    override val engineType = EngineType.PROGUARD
    override val displayName = "ProGuard/R8"
    // Implement all interface methods
}
```

### Background task execution
```kotlin
// Always use IntelliJ's coroutine scope
project.service<ObfuscateGuardService>().coroutineScope.launch {
    val result = withContext(Dispatchers.Default) {
        // heavy computation
    }
    withContext(Dispatchers.EDT) {
        // update UI
    }
}
```

## What NOT to do
- Do not create God classes or managers with 500+ lines.
- Do not use `SwingUtilities.invokeLater` — use IntelliJ's `ApplicationManager.getApplication().invokeLater`.
- Do not hardcode engine-specific logic in core layer — use the adapter SPI.
- Do not store sensitive data in plain text settings.
- Do not add new dependencies without documenting the reason.
- Do not suppress warnings without a comment explaining why.

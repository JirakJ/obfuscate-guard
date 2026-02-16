# ObfuscateGuard — Implementation Plan

**Verze:** 1.0  
**Datum:** 2026-02-16  
**Status:** Active

---

## Přehled

ObfuscateGuard je IntelliJ Platform plugin (Kotlin), který orchestruje obfuskaci Java/Kotlin projektů. Není to vlastní obfuskátor — je to **workflow, analýza a governance vrstva** nad ProGuard/R8, yGuard a dalšími.

**Positioning:** "Obfuskace bez bolesti — bezpečné defaults, automatické keep rules, mapping management a CI export přímo v IDE."

---

## Phase 0 — Foundation & Architecture

### 0.1 Projekt setup
- [ ] Inicializovat Gradle projekt s IntelliJ Platform Gradle Plugin 2.x
- [ ] Nastavit `plugin.xml` (id: `dev.htmeta.obfuscateguard`, name, vendor, description)
- [ ] Konfigurovat Kotlin 1.9+, JVM target 17
- [ ] Nastavit source structure dle copilot-instructions.md
- [ ] Vytvořit resource bundle `ObfuscateGuardBundle.properties`

### 0.2 CI/CD pipeline
- [ ] GitHub Actions workflow: build, test, verify plugin (runPluginVerifier)
- [ ] Nastavit code signing pro plugin distribution
- [ ] Changelog management (keep-a-changelog formát)
- [ ] Release workflow: build + publish to JetBrains Marketplace (manuální trigger)

### 0.3 Core domain model
- [ ] Definovat `ProjectProfile`, `ObfuscationConfig`, `KeepRule`, `RiskItem`, `MappingRecord`
- [ ] Implementovat `ObfuscationEngineAdapter` SPI (interface + registration via EP)
- [ ] Implementovat `AnalyzerRule` SPI (interface + registration via EP)
- [ ] Settings service s `PersistentStateComponent` a schema versioning

### 0.4 Testovací infrastruktura
- [ ] JUnit 5 + IntelliJ Test Framework setup
- [ ] Sample test projekty v `src/test/testData/` (Gradle Java, Gradle Kotlin, Maven, Android)
- [ ] Golden test framework pro šablony a konfigurace
- [ ] CI matrix: IntelliJ 2024.x a 2025.x

**Checkpoint: Plugin se buildí, loadí v IDE, projde verify, CI zelená.**

---

## Phase 1 — Free Tier MVP

### 1.1 Project detection
- [ ] `ProjectDetector` service: rozpoznání build systému, typu projektu, modulů
- [ ] Detekce existující ProGuard/R8 konfigurace v Android projektech
- [ ] Detekce existujících keep rules v projektu
- [ ] Unit testy pro všechny typy projektů

### 1.2 Configuration wizard
- [ ] Wizard UI (DialogWrapper, 5 kroků)
  - [ ] Krok 1: Project type (auto-detected, editable)
  - [ ] Krok 2: Engine selection (ProGuard/R8 jako default)
  - [ ] Krok 3: Obfuscation level (Light/Standard/Aggressive)
  - [ ] Krok 4: Protection goals (IP / Anti-RE / String)
  - [ ] Krok 5: Review & confirm (preview generated config)
- [ ] Config generator: wizard output → engine-specific konfigurace
- [ ] Persistence: uložení konfigurace do projektu

### 1.3 Run configuration
- [ ] Custom `RunConfiguration` + `RunConfigurationType` + `RunConfigurationFactory`
- [ ] `RunProfileState` spouštějící Gradle/Maven task
- [ ] Real-time log streaming do Run tool window
- [ ] Progress bar s cancel support
- [ ] Clickable file links v logu

### 1.4 Tool window (základní)
- [ ] `ToolWindowFactory` — dockable, default bottom
- [ ] Tab: Overview (status posledního běhu, quick actions)
- [ ] Tab: Settings (odkaz na wizard, manuální config edit)

### 1.5 ProGuard/R8 adapter (MVP)
- [ ] `ProGuardAdapter` implementing `ObfuscationEngineAdapter`
- [ ] Config generation (ProGuard rules syntax)
- [ ] Mapping file parsing
- [ ] Integrace s Gradle `proguard` / `minifyEnabled` task
- [ ] Integrační testy se sample Android projektem

**Checkpoint: Plugin umí nakonfigurovat a spustit ProGuard obfuskaci přes wizard a run config.**

---

## Phase 2 — Pro Tier (Safe Mode + Mapping + CI)

### 2.1 Analyzer engine
- [ ] `AnalyzerService` — orchestrace pravidel, caching, incremental analysis
- [ ] Pravidla:
  - [ ] `ReflectionUsageRule` (Class.forName, Method.invoke, Field.get)
  - [ ] `SerializationRule` (@SerializedName, Serializable, Parcelable, Gson/Jackson)
  - [ ] `JniRule` (native methods, System.loadLibrary)
  - [ ] `DiFrameworkRule` (Spring @Component/@Autowired, Dagger/Hilt @Inject)
  - [ ] `AndroidSpecificRule` (Manifest refs, R class, @Keep, View binding)
  - [ ] `DynamicLoadingRule` (ClassLoader, ServiceLoader)
- [ ] Background analysis s progress indikací
- [ ] Results caching (invalidace při změně souboru)

### 2.2 Keep rules generator
- [ ] Automatické generování keep rules z RiskItems
- [ ] Engine-specific syntax (ProGuard, yGuard)
- [ ] Traceability: rule → source risk
- [ ] `needsReview` flagging pro nejisté rules
- [ ] UI: review panel s accept/reject per rule

### 2.3 Dry-run validace
- [ ] Build s obfuskací do temp directory
- [ ] Spuštění testů proti obfuskované verzi
- [ ] Diff report: passing vs failing testy
- [ ] Non-destructive (nesmí modifikovat zdrojové soubory)

### 2.4 Mapping management
- [ ] `MappingVault` — lokální šifrované úložiště (AES-256)
- [ ] Automatické ukládání po úspěšné obfuskaci (s metadaty)
- [ ] Mapping diff viewer (side-by-side, highlight changes)
- [ ] Export: ProGuard mapping.txt, JSON, Firebase Crashlytics formát
- [ ] Retention policy (max N mappingů, max stáří)

### 2.5 CI/CD export
- [ ] Gradle task generator (standalone, bez IDE)
- [ ] Maven goal generator
- [ ] CI šablony: GitHub Actions, GitLab CI, Jenkins
- [ ] Golden testy pro všechny šablony
- [ ] „Copy to clipboard" UX

### 2.6 Tool window rozšíření
- [ ] Tab: Analysis (risk overview, filtry, batch actions)
- [ ] Tab: Mappings (seznam, diff, export)
- [ ] Tab: CI Export (šablony, generátor)
- [ ] Gutter ikony u rizikových řádků + quick-fix intenty

### 2.7 yGuard adapter
- [ ] `YGuardAdapter` implementing `ObfuscationEngineAdapter`
- [ ] Config generation (yGuard XML syntax)
- [ ] Mapping parsing
- [ ] Integrační testy

**Checkpoint: Plugin má kompletní Pro tier — analýza, keep rules, mappingy, CI export.**

---

## Phase 3 — Business Tier & Monetizace

### 3.1 Licencování
- [ ] JetBrains Marketplace paid plugin integrace
- [ ] Feature flag service (`LicenseManager`)
- [ ] Free / Pro / Business tier gating
- [ ] 14-day trial pro Pro tier
- [ ] Graceful degradation při expiraci

### 3.2 Policy engine (Business)
- [ ] Definice obfuskačních politik (YAML/JSON)
- [ ] Policy validation při konfiguraci (enforce minimum level, required patterns)
- [ ] Sdílené profily přes VCS (`.obfuscate-guard/policy.yaml`)

### 3.3 Audit trail (Business)
- [ ] Logování všech obfuskačních akcí
- [ ] Export audit logu (JSON, CSV)
- [ ] Integrace s IDE event log

### 3.4 Marketplace release
- [ ] Store listing (description, screenshots, feature comparison)
- [ ] EULA / Terms of Service
- [ ] Pricing configuration v Marketplace
- [ ] Beta program (closed) + feedback loop

### 3.5 Analytics
- [ ] Anonymous usage analytics (event counts only)
- [ ] Conversion funnel tracking (install → activate → convert)
- [ ] Crash reporting (opt-in)

**Checkpoint: Plugin je na Marketplace, monetizovaný, s Business tier funkcemi.**

---

## Phase 4 — Growth & Expansion

### 4.1 Multi-engine
- [ ] Allatori adapter
- [ ] Engine comparison view (side-by-side output)
- [ ] Engine migration assistant (ProGuard → Allatori config conversion)

### 4.2 Kotlin Multiplatform
- [ ] KMP project detection a specifická pravidla
- [ ] Per-target obfuskace (JVM, Android, JS)

### 4.3 Advanced features
- [ ] String encryption configuration
- [ ] Flow obfuscation settings
- [ ] Anti-debug configuration
- [ ] Watermarking support

### 4.4 Team collaboration
- [ ] Remote mapping storage (S3/GCS)
- [ ] Shared analyzer rule sets
- [ ] Team dashboard (usage stats, compliance)

### 4.5 Ecosystem
- [ ] Česká lokalizace
- [ ] Documentation site (docs.htmeta.dev)
- [ ] Video tutorials
- [ ] Community analyzer rules marketplace

**Checkpoint: Full-featured product s multi-engine podporou a team features.**

---

## Technické poznámky

### Klíčová rozhodnutí
1. **Kotlin only** — žádný Java kód v nových souborech
2. **SPI-based architecture** — engine adaptery a analyzer rules přes Extension Points
3. **Immutable run model** — každý běh = snapshot vstupu + výstupu
4. **Safe defaults** — raději over-keep než under-keep
5. **No EDT blocking** — vše na background threads

### Rizika a mitigace
| Riziko | Mitigace |
|--------|----------|
| IntelliJ API breaking changes | CI matrix testing, podpora posledních 2 verzí |
| ProGuard/R8 internal changes | Adapter izolace, version-specific handling |
| False positives v analyzátoru | Conservative defaults + `needsReview` flag |
| Performance na velkých projektech | Incremental analysis, caching, cancellation |

---

*Tento plán je živý dokument. Aktualizuj ho při každém checkpointu.*

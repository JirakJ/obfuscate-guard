# ObfuscateGuard — Software Requirements Specification (SRS)

**Verze:** 1.0  
**Datum:** 2026-02-16  
**Autor:** HTMETA.dev  
**Status:** Draft  
**Referenční dokument:** [BRS v1.0](./BRS.md)

---

## 1. Úvod

### 1.1 Účel
Tento dokument definuje funkční a nefunkční požadavky na plugin **ObfuscateGuard** pro IntelliJ-platform IDE. Slouží jako referenční bod pro vývoj, testování a akceptaci.

### 1.2 Rozsah
Plugin poskytuje orchestrační vrstvu pro obfuskaci Java/Kotlin projektů. Neimplementuje vlastní obfuskační engine — integruje se s existujícími nástroji (ProGuard/R8, yGuard) a přidává analýzu rizik, mapping management a CI/CD export.

### 1.3 Definice a zkratky
| Zkratka | Význam |
|---------|--------|
| EDT | Event Dispatch Thread (IntelliJ UI thread) |
| PSI | Program Structure Interface (IntelliJ AST) |
| Keep rule | Pravidlo zabraňující obfuskaci konkrétní třídy/metody |
| Mapping | Soubor mapující obfuskované názvy na originální |
| DI | Dependency Injection |
| JNI | Java Native Interface |

---

## 2. Architektura (high-level)

```
+---------------------------------------------------+
|                  IntelliJ Platform                 |
+---------------------------------------------------+
|  ObfuscateGuard Plugin                            |
|  +---------------------------------------------+ |
|  |  UI Layer (Tool Window, Wizard, Settings)    | |
|  +---------------------------------------------+ |
|  |  Core Layer                                  | |
|  |  +----------+ +----------+ +---------------+| |
|  |  | Analyzer | | Config   | | Mapping       || |
|  |  | Engine   | | Generator| | Manager       || |
|  |  +----------+ +----------+ +---------------+| |
|  |  +----------+ +----------+ +---------------+| |
|  |  | Runner   | | CI Export| | License       || |
|  |  | Engine   | | Engine   | | Manager       || |
|  |  +----------+ +----------+ +---------------+| |
|  +---------------------------------------------+ |
|  |  Integration Layer                           | |
|  |  +----------+ +----------+ +---------------+| |
|  |  | ProGuard | | yGuard   | | Allatori      || |
|  |  | Adapter  | | Adapter  | | Adapter       || |
|  |  +----------+ +----------+ +---------------+| |
|  +---------------------------------------------+ |
+---------------------------------------------------+
```

---

## 3. Funkční požadavky

### 3.1 Projekt a IDE integrace

#### FR-001: Detekce typu projektu
- **Popis:** Automatická detekce build systému (Gradle/Maven), typu projektu (Android app, Java library, Kotlin Multiplatform, multi-module) a existující obfuskační konfigurace.
- **Vstup:** Otevřený projekt v IDE
- **Výstup:** Strukturovaný model projektu (`ProjectProfile`)
- **Akceptační kritéria:**
  - Správně rozpozná Gradle i Maven projekty
  - Detekuje existující ProGuard/R8 konfiguraci v Android projektech
  - Identifikuje multi-module strukturu a závislosti mezi moduly

#### FR-002: Konfigurační wizard
- **Popis:** Průvodce konfigurací obfuskace s inteligentními defaults.
- **Kroky:**
  1. Výběr cíle: Application / Library / Plugin
  2. Výběr obfuskačního engine: ProGuard/R8, yGuard, Allatori
  3. Úroveň obfuskace: Light / Standard / Aggressive
  4. Cíle ochrany: IP Protection / Anti-Reverse-Engineering / String Protection
  5. Review a potvrzení vygenerované konfigurace
- **Akceptační kritéria:**
  - Wizard navrhne konfiguraci na základě typu projektu
  - Každý krok obsahuje vysvětlení dopadu volby
  - Výsledná konfigurace je validní pro zvolený engine

#### FR-003: Run Configuration
- **Popis:** Vlastní IntelliJ Run Configuration pro spuštění obfuskace.
- **Funkce:**
  - Spuštění obfuskace jako Gradle/Maven task
  - Real-time log streaming do Run tool window
  - Progress indikátor s možností cancellation
  - Zobrazení výsledku (success/failure + metriky)
- **Akceptační kritéria:**
  - Nesmí blokovat EDT
  - Log musí obsahovat clickable odkazy na soubory
  - Cancellation musí spolehlivě zastavit proces

### 3.2 Safe-mode analyzátor (klíčový diferenciátor)

#### FR-004: Statická analýza rizik
- **Popis:** PSI-based analýza kódu detekující vzory nekompatibilní s obfuskací.
- **Detekované vzory:**
  - `Class.forName()`, `Method.invoke()` a další reflection
  - Anotace: `@SerializedName`, `@JsonProperty`, `@XmlElement`
  - JNI: `native` metody, `System.loadLibrary()`
  - DI frameworks: Spring (`@Component`, `@Autowired`), Dagger/Hilt (`@Inject`)
  - Serialization: `Serializable`, `Parcelable`, Gson/Jackson/Moshi
  - Android specifické: `AndroidManifest.xml` references, `R` class, `@Keep`
  - Dynamic class loading: `ClassLoader.loadClass()`, `ServiceLoader`
- **Výstup:** Seznam `RiskItem` s lokací, severity (Critical/Warning/Info) a doporučeným keep rule
- **Akceptační kritéria:**
  - False positive rate < 10 %
  - Analýza 50K LOC projektu < 30 sekund
  - Každý risk item má vysvětlení „proč" a odkaz na dokumentaci

#### FR-005: Automatické generování keep rules
- **Popis:** Na základě nalezených rizik vygenerovat odpovídající keep rules.
- **Funkce:**
  - Generování rules pro zvolený engine (ProGuard syntax, yGuard syntax)
  - Traceability: každé rule odkazuje na zdrojový risk item
  - „Needs review" flag pro nejisté rules
  - Batch apply / cherry-pick workflow
- **Akceptační kritéria:**
  - Vygenerované rules jsou syntakticky validní
  - Rules obsahují komentář s odkazem na zdroj rizika
  - User může přijmout/odmítnout jednotlivě

#### FR-006: Dry-run validace
- **Popis:** Automatická validace obfuskované verze.
- **Kroky:**
  1. Build projektu s obfuskací
  2. Spuštění existujících testů proti obfuskované verzi
  3. Porovnání výsledků s neobfuskovanou verzí
  4. Report s nalezanými regresemi
- **Akceptační kritéria:**
  - Dry-run nesmí modifikovat zdrojové soubory
  - Report jasně identifikuje failing testy
  - Proces je přerušitelný

### 3.3 Mapping management

#### FR-007: Uložení mappingů per build
- **Popis:** Automatické ukládání mapping souborů s metadaty.
- **Metadata:**
  - Timestamp
  - Git commit hash (pokud je v git repo)
  - Build variant / flavor
  - Obfuskační engine + verze
  - Konfigurace hash
- **Úložiště:** Lokální šifrovaný vault (`.obfuscate-guard/mappings/`)
- **Akceptační kritéria:**
  - Mapping je uložen automaticky po úspěšné obfuskaci
  - Vault je šifrován AES-256
  - Staré mappingy lze čistit dle retention policy

#### FR-008: Diff mappingů
- **Popis:** Vizuální porovnání dvou mapping souborů.
- **Funkce:**
  - Side-by-side diff view
  - Highlight „velkých skoků" (> X % změněných symbolů mezi verzemi)
  - Export diff jako report
- **Akceptační kritéria:**
  - Diff musí být čitelný i pro 100K+ symbol mappingů
  - Warning při > 30 % change rate

#### FR-009: Export mappingů
- **Popis:** Export mappingů pro crash symbolication / debug.
- **Formáty:** ProGuard mapping.txt, custom JSON, Firebase Crashlytics kompatibilní
- **Cíle:** Lokální soubor, S3/GCS (Business tier), build artifact

### 3.4 CI/CD export

#### FR-010: Generování CI konfigurace
- **Popis:** Export obfuskační konfigurace tak, aby fungovala headless v CI.
- **Výstup:**
  - Gradle task / Maven goal konfigurace
  - ProGuard/R8 rules soubory
  - Build script modifikace (nebo diff patch)
- **Akceptační kritéria:**
  - Vygenerovaná konfigurace funguje bez IDE
  - Deterministický výstup (stejný vstup = stejný výstup)

#### FR-011: CI šablony
- **Popis:** Copy-paste ready šablony pro CI platformy.
- **Podporované platformy:** GitHub Actions, GitLab CI, Jenkins
- **Akceptační kritéria:**
  - Šablony jsou funkční a testované
  - Obsahují komentáře vysvětlující každý krok

### 3.5 Licencování

#### FR-012: Freemium gating
- **Popis:** Feature flags řídící dostupnost funkcí dle licence.
- **Free tier:** FR-001, FR-002, FR-003 (základní)
- **Pro tier:** FR-004, FR-005, FR-006, FR-007, FR-008, FR-009, FR-010, FR-011
- **Business tier:** Policy engine, audit trail, sdílené profily, remote storage

#### FR-013: JetBrains Marketplace integrace
- **Popis:** Integrace s JetBrains paid plugin mechanismem.
- **Funkce:**
  - Licence validace při startu IDE
  - Graceful degradation při expiraci (Free tier funkce zůstanou)
  - Trial period (14 dní Pro)
  - Analytics reporting (anonymní usage data)

---

## 4. Nefunkční požadavky

### NFR-001: Kompatibilita
| Požadavek | Hodnota |
|-----------|---------|
| IntelliJ Platform | Poslední 2 major verze (2025.x, 2024.x) |
| Android Studio | Poslední 2 stable verze |
| Java target | JDK 17+ |
| OS | Windows, macOS, Linux |

### NFR-002: Výkon
| Požadavek | Hodnota |
|-----------|---------|
| Analýza rizik (50K LOC) | < 30 sekund |
| Wizard response time | < 200 ms per step |
| Startup impact | < 500 ms additional IDE startup time |
| Memory overhead | < 100 MB additional heap |
| EDT blocking | Zero — všechny operace na background thread |

### NFR-003: Bezpečnost
- Mapping vault šifrovaný AES-256, klíč derivovaný z machine-specific seed
- Žádné odesílání zdrojového kódu, symbolů ani mappingů bez explicitního opt-in
- Telemetrie je anonymní a minimální (event counts, no PII)
- Konfigurace neobsahuje absolutní cesty ani secrets

### NFR-004: Transparentnost
- Veškeré akce pluginu logovány do IntelliJ log (idea.log)
- Každé keep rule má traceability link na zdroj
- UI jasně komunikuje, co bude provedeno před akcí

### NFR-005: Spolehlivost
- Deterministické generování konfigurace (stejný vstup = stejný výstup)
- Idempotentní operace (opakované spuštění = stejný výsledek)
- Graceful handling chyb (nesmí crashnout IDE)
- Rollback: možnost vrátit konfiguraci do předchozího stavu

### NFR-006: Testovatelnost
| Typ testu | Pokrytí |
|-----------|---------|
| Unit testy (Analyzer, Config Generator) | > 80 % |
| Integrační testy (engine adaptery) | > 70 % |
| Golden testy (šablony, CI export) | 100 % šablon |
| UI testy (wizard flow) | Kritické cesty |

### NFR-007: Lokalizace
- Primární jazyk: angličtina
- I18n ready (resource bundles) pro budoucí rozšíření
- Česká lokalizace jako první non-EN (roadmap)

---

## 5. UX požadavky

### UX-001: Tool Window
- Centrální „ObfuscateGuard" tool window (dockable, default: bottom)
- Tabs: Overview | Analysis | Mappings | CI Export | Settings
- Overview: status posledního běhu, quick actions, varování

### UX-002: Wizard UX
- Max 5 kroků
- Každý krok: nadpis + „explain like I'm busy" popis (1 věta)
- Preview výsledné konfigurace v posledním kroku
- „Back" navigace bez ztráty dat

### UX-003: Fix-it akce
- Gutter ikony u rizikových řádků kódu
- Quick-fix: „Generate keep rule for this class"
- Quick-fix: „Add @Keep annotation"
- Quick-fix: „Switch to light obfuscation for this package"

### UX-004: Notifikace
- Balloon notifikace po dokončení obfuskace (success/failure)
- Warning banner v editoru u souborů s vysokým rizikem
- Non-intrusive — žádné modální dialogy pro informační zprávy

---

## 6. Datový model (klíčové entity)

### ProjectProfile
```
ProjectProfile
  - projectType: APP | LIBRARY | PLUGIN
  - buildSystem: GRADLE | MAVEN
  - modules: List<ModuleInfo>
  - existingConfig: ObfuscationConfig?
  - detectedEngines: List<EngineType>
```

### ObfuscationConfig
```
ObfuscationConfig
  - engine: PROGUARD | R8 | YGUARD | ALLATORI
  - level: LIGHT | STANDARD | AGGRESSIVE
  - goals: Set<ProtectionGoal>
  - keepRules: List<KeepRule>
  - version: Int (for migration)
```

### RiskItem
```
RiskItem
  - id: UUID
  - location: PsiElement reference
  - category: REFLECTION | SERIALIZATION | JNI | DI | DYNAMIC_LOADING | ANDROID
  - severity: CRITICAL | WARNING | INFO
  - description: String
  - suggestedKeepRule: KeepRule?
  - needsReview: Boolean
```

### MappingRecord
```
MappingRecord
  - id: UUID
  - timestamp: Instant
  - gitCommitHash: String?
  - buildVariant: String?
  - engineType: EngineType
  - engineVersion: String
  - configHash: String
  - mappingFile: EncryptedFile
  - stats: MappingStats (classCount, methodCount, fieldCount)
```

---

## 7. API a rozšiřitelnost

### 7.1 Engine Adapter SPI
```kotlin
interface ObfuscationEngineAdapter {
    val engineType: EngineType
    val displayName: String
    fun detectInstallation(project: Project): EngineInstallation?
    fun generateConfig(profile: ProjectProfile, config: ObfuscationConfig): EngineSpecificConfig
    fun parseMapping(mappingFile: VirtualFile): ParsedMapping
    fun run(config: EngineSpecificConfig, listener: RunListener): RunResult
}
```

### 7.2 Analyzer Rule SPI
```kotlin
interface AnalyzerRule {
    val id: String
    val category: RiskCategory
    fun analyze(psiFile: PsiFile, context: AnalysisContext): List<RiskItem>
}
```

Tyto SPI umožní v budoucnu:
- Komunitní přidání nových engine adapterů
- Vlastní analyzer rules (enterprise customers)

---

## 8. Závislosti

| Závislost | Verze | Účel |
|-----------|-------|------|
| IntelliJ Platform SDK | 2024.x+ | Plugin API |
| Kotlin | 1.9+ | Implementační jazyk |
| Gradle IntelliJ Plugin | 2.x | Build a packaging |
| ProGuard Core | 9.x | ProGuard konfigurace parsing |
| kotlinx.serialization | 1.6+ | Konfigurace serializace |
| JUnit 5 + IntelliJ Test Framework | Latest | Testování |

---

## 9. Matice sledovatelnosti (BRS to SRS)

| BRS Cíl | SRS Požadavky |
|---------|---------------|
| G1 (Snížit bariéru) | FR-001, FR-002, FR-003, UX-002 |
| G2 (Eliminovat broken builds) | FR-004, FR-005, FR-006, UX-003 |
| G3 (Týmový governance) | FR-007, FR-008, FR-009, FR-012 |
| G4 (Revenue) | FR-012, FR-013 |

---

*Tento dokument bude aktualizován při každém sprintu na základě implementačního progressu.*

# ObfuscateGuard — Changelog

## [Unreleased]

### Fixed
- Run configuration: the process output is now attached to the Run console (it stayed empty before); missing configuration is reported as a proper execution error.
- Config linter: issue offsets are correct in files with Windows (CRLF) line endings.
- Inline inspection now respects the "Enable ObfuscateGuard" setting, like the gutter icons.
- JNI risk rule also detects Kotlin `external fun` declarations.
- Light level comment in generated configs describes what it actually does (shrink/optimize only, names kept).
- The wizard only offers engines that are implemented (ProGuard/R8); the default level from Settings is pre-selected.

### Removed
- Settings that had no effect (auto-detect, default engine, auto-analyze on open, mapping retention) and the unused mapping-vault service; the description no longer advertises yGuard/Allatori support or a mapping vault.
- Internal business documents (revenue projections, product spec) from `docs/`.

## [2026.8.0] - 2026-11-10

### Changed

- The plugin is now completely free — all features unlocked, no license needed.
- Source code is now open source under Apache-2.0: https://github.com/JirakJ/obfuscate-guard

## [2026.4.1] - 2026-06-10 🔧 Maintenance

### Changed
- `ProGuardParserDefinition.createElement` now returns the public
  `ASTWrapperPsiElement` instead of constructing an internal-package
  `LeafPsiElement`; removed the unused `PsiPlainTextFileImpl` import.

## [2026.4.0] - 2026-06-09 🚀 Feature Release

### Added
- **Code completion in .pro files** — 50+ ProGuard/R8 directives with argument
  hints and inline documentation, backed by a new directive registry.
- **Quick documentation (Ctrl+Q)** — hover docs for every known directive,
  including warnings for dangerous options.
- **Config Linter** — annotates unknown directives (with "did you mean"
  typo suggestions via edit distance), dangerous directives
  (`-dontobfuscate`, `-ignorewarnings`, broad `-dontwarn`), and duplicate
  singleton directives directly in the editor. Includes release-readiness
  checks (missing `-printmapping`, missing `LineNumberTable`).
- **Policy Engine** — team rules in a VCS-shared `obfuscateguard-policy.json`
  (minimum level, required keep attributes, forbidden directives, max
  uncovered critical risks). Dry-Run Validation loads and enforces the policy;
  violations turn the verdict to FAIL.
- **Export Keep Rules action** — writes generated keep rules into
  `proguard-rules.pro` between markers, preserving hand-written content and
  replacing the generated block idempotently on re-export.
- **Persistent Mapping Vault** — mapping records are stored in `index.json`
  and survive IDE restarts; added `deleteMapping`.
- **Screenshot pipeline** — marketplace/README screenshots are rendered from
  the real plugin UI by `ScreenshotGeneratorTest` (opt-in via
  `-PscreenshotsDir`).

### Fixed
- **Stack Trace Deobfuscator never translated method names** — the member
  lookup used original-class keys while stack frames carry obfuscated class
  names. Members are now indexed by `obfuscatedClass.obfuscatedMember`.
- **R8 line-range mapping entries** (`12:34:void run() -> a`) were not parsed;
  the prefix is now stripped.
- **Method-name extraction broke on signatures with dotted argument types**
  (`void handle(java.lang.String)`).
- **Exception-message class replacement could corrupt unrelated identifiers**
  — replacement now matches whole identifiers only.
- **Deobfuscator UX** — mapping file is auto-detected from common build output
  paths, the result can be copied with one click, and the dialog stays open
  for iterative use.
- **Risk analysis froze the UI** when triggered from the tool window — it now
  runs as a background task.
- **Wizard overwrote an existing `proguard-rules.pro` silently** — a `.bak`
  backup is created and the VFS is refreshed.
- **Generate Keep Rules did nothing without prior configuration** — a default
  config is created on demand.
- **CI templates emitted the obsolete `-Pandroid.enableR8=true` flag**
  (removed in AGP 7).

### Technical
- Added IntelliJ Platform test framework + JUnit Vintage for UI-level tests.
- Test suite grew from 53 to 103 tests; all new logic is in pure, fully
  unit-tested objects (`StackTraceDeobfuscator`, `ProGuardDirectives`,
  `ProGuardConfigLinter`, `PolicyEngine`, `KeepRulesExporter`,
  `MappingManager`).

## [2026.3.7] - 2026-03-10 🐛 Bug Fix

### Fixed
- **Cannot open .pro files**: The ProGuard file type was registered using bare
  `FileType` instead of `LanguageFileType`, which prevented IntelliJ from creating
  a PSI tree and opening the file in the text editor. Migrated to a proper
  `ProGuardLanguage` + `LanguageFileType` + `ParserDefinition` architecture so
  `.pro` files now open, edit, and highlight correctly.
- **Missing `@JvmField` on INSTANCE**: The companion object's `INSTANCE` field
  lacked `@JvmField`, causing reflection-based singleton lookup from plugin.xml
  to fail silently.
- **Token types not bound to language**: `IElementType` instances passed `null`
  as the language parameter, breaking platform token-to-language resolution.
- **Pre-existing test failure**: Fixed JUnit 4 imports in `FreemiumGateTest` to
  JUnit 5 (`org.junit.jupiter`).

### Technical
- Created `ProGuardLanguage` extending `Language("ProGuard")`
- Converted `ProGuardFileType` from `FileType` to `LanguageFileType`
- Created `ProGuardParserDefinition` with minimal flat parser
- Created `ProGuardFile` PSI file class
- Registered `lang.parserDefinition` and `lang.syntaxHighlighter` in plugin.xml
- Split monolithic file into `ProGuardFileType.kt`, `ProGuardTokenTypes.kt`,
  `ProGuardLexer.kt`, `ProGuardSyntaxHighlighter.kt`, `ProGuardParserDefinition.kt`

## [2026.3.6] - 2026-03-07 🎉 Initial Release

### Core Features
- Configuration Wizard — Guided setup with auto-detection
- ProGuard Syntax Highlighting — Full editor support for .pro files
- Safe-Mode Risk Analyzer — Detect reflection, serialization, JNI, DI risks
- Inline Inspections and Gutter Icons — Risk warnings in editor
- Dry-Run Validation — Simulate obfuscation
- Stack Trace Deobfuscator — Paste obfuscated crash logs
- Mapping Vault — Encrypted storage with diff viewer
- Keep Rules Generator — Automatic keep rules
- CI/CD Export — GitHub Actions, GitLab CI, Jenkins templates
- Policy Engine — Team-wide obfuscation rules

# ObfuscateGuard — Changelog

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

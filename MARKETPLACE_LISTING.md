# JetBrains Marketplace Listing — ObfuscateGuard

---

## 📝 Plugin Information

| Field       | Value                                  |
|-------------|----------------------------------------|
| **Name**    | ObfuscateGuard                         |
| **Plugin ID** | `dev.htmeta.obfuscateguard`         |
| **Version** | 2026.4.0                               |
| **Category** | Build & Deployment / Security         |
| **Tags**    | Obfuscation, ProGuard, R8, Java Security, Code Protection |

---

## 🎯 Short Description

> Workflow, analysis, and governance layer for Java/Kotlin obfuscation — configuration wizard, risk analyzer, dry-run validation and stack-trace retrace for ProGuard/R8.

---

## 📖 Full Description

### 🛡️ Obfuscation Made Safe — Configure, Validate, and Govern with Confidence

**The Problem:** Obfuscation tools like ProGuard and R8 are powerful but treacherous. Misconfiguration breaks reflection, serialization, JNI, and dependency injection. Teams waste hours debugging obfuscation issues and studying 40-page manuals.

**The Solution:** ObfuscateGuard isn't an obfuscator — it's a **workflow, analysis, and governance layer** over ProGuard/R8. It guides configuration, detects risks before they break your build, and retraces obfuscated stack traces.

---

### ✨ Key Features

All features are free for everyone — no license needed.

- 🧙 **Configuration Wizard** — Set up obfuscation without studying 40-page manuals
- 🔎 **Project Detection** — Automatic detection of your obfuscation tool and setup
- ▶️ **Run Configuration** — Integrated obfuscation run configurations
- 🪟 **Tool Window** — Dedicated ObfuscateGuard panel for orchestration
- 🎨 **Smart .pro Editor** — Syntax highlighting, code completion, and quick documentation (Ctrl+Q) for 50+ ProGuard/R8 directives
- 🧹 **Config Linter** — Unknown directives with typo suggestions, dangerous options, and duplicates flagged in the editor

- 🛡️ **Safe-mode Analyzer** — Automatic detection of risks (reflection, serialization, JNI, DI) and keep rule generation
- ⚠️ **Inline Inspections** — Real-time code inspections for obfuscation hazards
- 🔖 **Gutter Icons** — Visual indicators for classes/methods affected by obfuscation
- 🔧 **Quick-Fix Actions** — One-click fixes for detected obfuscation risks
- 🧪 **Dry-Run Validation** — Verify obfuscation won't break your application before running
- 🗺️ **Stack Trace Deobfuscator** — Translate obfuscated crash logs back to readable code (R8 line ranges, auto-detected mapping)
- 📏 **Keep Rules Generator** — Keep rules from risk analysis with one-click export into proguard-rules.pro
- 📤 **CI/CD Export** — Generate headless obfuscation configs and CI pipeline templates

- 📋 **Policy Engine** — Team-wide rules in a VCS-shared `obfuscateguard-policy.json`, enforced by dry-run validation

---

### 💚 Free & open source

ObfuscateGuard is completely free and open source under the Apache License 2.0: https://github.com/JirakJ/obfuscate-guard

---

### 🎯 Perfect For

- **Android developers** managing ProGuard/R8 configurations
- **Java/Kotlin teams** protecting intellectual property with obfuscation
- **Security engineers** enforcing obfuscation policies across projects
- **DevOps teams** integrating obfuscation into CI/CD pipelines
- **Enterprise organizations** needing audit trails and compliance governance

---

### ⚙️ Requirements

| Requirement        | Value                    |
|--------------------|--------------------------|
| **Minimum IDE Build** | 232 (IntelliJ 2023.2+) |
| **Java Version**   | 17+                      |
| **Platform**       | IntelliJ IDEA (Community or Ultimate) |

---

### 🚀 Quick Start

1. **Install** the plugin from JetBrains Marketplace
2. **Open** a Java/Kotlin project with an obfuscation tool configured
3. **Launch the Configuration Wizard** from the ObfuscateGuard tool window
4. **Review** detected risks and generated keep rules (Pro)
5. **Run a Dry-Run Validation** to verify your configuration before obfuscating (Pro)

---

## 📸 Screenshots & Media

Vygenerované z reálného UI (`docs/screenshots/`, regenerace: `./gradlew test --tests "*ScreenshotGeneratorTest*" -PscreenshotsDir=$(pwd)/docs/screenshots`):

1. **ProGuard editor s lintingem** — `docs/screenshots/pro-editor-linting.png`
2. **Analýza rizik (tool window)** — `docs/screenshots/toolwindow-analysis.png`
3. **Overview (tool window)** — `docs/screenshots/toolwindow-overview.png`

> Upload na Marketplace: Plugin page → Edit → Media (manuálně přes web UI).

---

## 🏷️ Marketplace Categories

| Type          | Category                  |
|---------------|---------------------------|
| **Primary**   | Build & Deployment        |
| **Secondary** | Security                  |

---

## 🔍 Keywords (SEO)

`obfuscation`, `ProGuard`, `R8`, `code protection`, `Java obfuscation`, `Kotlin obfuscation`, `keep rules`, `mapping files`, `deobfuscation`, `stack trace`, `Android ProGuard`, `code security`, `obfuscation wizard`

---

## 📋 Compatibility

| IDE                          | Compatible |
|------------------------------|------------|
| IntelliJ IDEA Community      | ✅          |
| IntelliJ IDEA Ultimate       | ✅          |
| Android Studio               | ✅          |

**Minimum Build:** 232 (IntelliJ Platform 2023.2+)

---

## 📞 Vendor Information

| Field        | Value                        |
|--------------|------------------------------|
| **Vendor**   | Jakub Jirak                  |
| **Email**    | jirak@jakubjirak.com         |
| **Website**  | https://jakubjirak.com       |
| **Support**  | support@jakubjirak.com       |

---

## ✅ Pre-Launch Checklist

- [ ] Plugin icon (40×40 SVG and 80×80 PNG) prepared
- [ ] Screenshots (1280×800 minimum) captured for all key features
- [ ] Short description within 255-character limit
- [ ] Full description reviewed and proofread
- [ ] Plugin signed with JetBrains Marketplace certificate
- [ ] CHANGELOG.md up to date with latest version notes
- [ ] Compatibility tested on minimum build (232)
- [ ] ProGuard/R8 integration tested
- [ ] Support email monitored and responsive

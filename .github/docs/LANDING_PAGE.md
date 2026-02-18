# ObfuscateGuard

## Obfuscation Orchestration for Java/Kotlin — Without the Pain

---

## The Problem

Setting up obfuscation for Java and Kotlin projects is hard. ProGuard manuals are 40+ pages long. One wrong keep rule breaks your app at runtime. Reflection, serialization, and dependency injection create invisible risks that only surface in production. Teams have no way to enforce consistent obfuscation policies.

## The Solution

ObfuscateGuard is not an obfuscator — it is a **workflow, analysis, and governance layer** on top of existing obfuscation engines (ProGuard/R8, yGuard, Allatori) directly inside your JetBrains IDE.

---

## Key Features

### 1. Configuration Wizard
Set up obfuscation without reading 40-page manuals. The wizard auto-detects your build system, project type, and existing obfuscation config, then walks you through setup step by step.

### 2. Safe-Mode Risk Analyzer
Automatically scan your project for reflection, serialization, JNI, and dependency injection patterns that will break after obfuscation. Fix problems before they reach production.

### 3. Inline Inspections and Gutter Icons
See obfuscation risk warnings directly in the editor. Gutter icons mark risky lines like `Class.forName`, native methods, and DI annotations. One-click quick fixes generate the correct keep rules.

### 4. Dry-Run Validation
Simulate obfuscation and get a pass/fail verdict before actually running it. Catch breakages during development, not in your CI pipeline.

### 5. Stack Trace Deobfuscator
Paste an obfuscated crash log and instantly get back the original class names, method names, and line numbers.

### 6. Mapping Vault
Encrypted storage for obfuscation mapping files with a built-in diff viewer. Export mappings for crash symbolication in your crash reporting tool.

### 7. CI/CD Export
Generate headless obfuscation configs and ready-to-use CI pipeline templates for GitHub Actions, GitLab CI, and Jenkins.

### 8. Policy Engine (Business)
Define team-wide obfuscation rules and enforce them across all projects. Full audit trail of every obfuscation action.

---

## How It Works

1. **Install the plugin** — Search for "ObfuscateGuard" in JetBrains Marketplace and install it.
2. **Run the Configuration Wizard** — The wizard detects your project setup and generates the correct obfuscation config.
3. **Analyze and Fix Risks** — The safe-mode analyzer scans your code and highlights risks with inline inspections and quick fixes.
4. **Obfuscate with Confidence** — Run dry-run validation, then execute obfuscation with real-time log streaming and progress tracking.

---

## Pricing

### Free
- Configuration wizard with auto-detection
- Project type and build system detection
- Run configuration with real-time log streaming
- Tool window dashboard
- ProGuard syntax highlighting

### Pro
- Safe-mode risk analyzer
- Inline inspections and gutter icons
- Quick-fix keep rule generation
- Dry-run validation
- Stack trace deobfuscator
- Keep rules generator with traceability
- Mapping vault (encrypted storage, diff viewer, export)
- CI/CD export (GitHub Actions, GitLab CI, Jenkins)

### Business
- Policy engine for team-wide obfuscation rules
- Full audit trail of all obfuscation actions
- Shared VCS-based team configuration profiles

---

## Testimonials

> *"ObfuscateGuard saved us hours of debugging broken builds after obfuscation."*
> — [Your name here], Senior Android Developer

> *"The risk analyzer caught a reflection issue that would have been a production crash."*
> — [Your name here], Tech Lead

> *"Finally, our whole team uses the same obfuscation rules. The policy engine is a game-changer."*
> — [Your name here], Engineering Manager

---

## FAQ

### What obfuscation engines does ObfuscateGuard support?
ObfuscateGuard works with ProGuard, R8, yGuard, and Allatori. It does not replace these tools — it adds a workflow and analysis layer on top of them.

### Does it actually obfuscate my code?
No. ObfuscateGuard orchestrates and validates obfuscation. The actual obfuscation is performed by the engine you choose (ProGuard, R8, etc.).

### Which JetBrains IDEs are supported?
ObfuscateGuard works with IntelliJ IDEA and any JetBrains IDE that supports Java development.

### Can I use it in a team?
Yes. The Business tier includes a policy engine for team-wide rules, an audit trail, and shared configuration profiles stored in version control.

### Is the mapping vault secure?
Yes. Mapping files are stored with encryption. You can export them for use with crash reporting tools like Firebase Crashlytics or Sentry.

---

## Get Started

Install ObfuscateGuard from the [JetBrains Marketplace](https://plugins.jetbrains.com) and run the configuration wizard. Free features are available immediately — no account required.

# ObfuscateGuard — Screenshot Plan

## Screenshots

### 1. Configuration Wizard
**Title:** Step-by-step obfuscation setup
**Capture:** The configuration wizard dialog showing auto-detected build system, project type selection, and obfuscation engine choice (ProGuard/R8/yGuard/Allatori).

### 2. Risk Analysis Results
**Title:** Safe-mode analyzer findings
**Capture:** The tool window showing a list of detected risks (reflection, serialization, JNI, DI) with severity levels, affected files, and line numbers.

### 3. Inline Inspections and Gutter Icons
**Title:** Obfuscation risks highlighted in the editor
**Capture:** A Java file with gutter icons on risky lines (Class.forName, @Inject annotations) and yellow warning highlights with quick-fix popup visible.

### 4. Dry-Run Validation
**Title:** Simulate before you obfuscate
**Capture:** The dry-run results panel showing a pass/fail verdict with a list of potential breakages and suggested fixes.

### 5. Stack Trace Deobfuscator
**Title:** Decode obfuscated crash logs instantly
**Capture:** The deobfuscator dialog with an obfuscated stack trace on the left and the decoded original class/method names on the right.

### 6. Mapping Vault
**Title:** Encrypted mapping file management
**Capture:** The mapping vault view showing a list of stored mapping files with timestamps, the diff viewer comparing two versions, and the export button.

### 7. CI/CD Export
**Title:** Generate CI pipeline templates
**Capture:** The CI/CD export dialog showing template selection (GitHub Actions, GitLab CI, Jenkins) and a preview of the generated YAML/Groovy file.

### 8. ProGuard Syntax Highlighting
**Title:** Full editor support for .pro files
**Capture:** A ProGuard configuration file with syntax highlighting showing colored keywords, comments, and keep rules.

---

## Video Demo Outline (45 seconds)

| Time | Scene | Description |
|------|-------|-------------|
| 0-5s | Intro | Show plugin name and tagline over IDE |
| 5-15s | Wizard | Walk through the configuration wizard: detect project, select engine, generate config |
| 15-25s | Risk Analysis | Run the safe-mode analyzer, show detected risks in the tool window and inline inspections in the editor |
| 25-35s | Dry Run + Fix | Run dry-run validation, show a warning, apply a quick-fix keep rule |
| 35-42s | Deobfuscate | Paste an obfuscated stack trace, show the decoded result |
| 42-45s | CTA | "Install ObfuscateGuard from JetBrains Marketplace" |

# ObfuscateGuard

> JetBrains IntelliJ Platform plugin pro bezpečnou orchestraci obfuskace Java/Kotlin projektů.

**ObfuscateGuard** není obfuskátor — je to **workflow, analýza a governance vrstva** nad existujícími obfuskátory (ProGuard/R8, yGuard, Allatori) přímo v IDE.

## Klíčové funkce

- **Configuration Wizard** — nastavení obfuskace bez studia 40stránkových manuálů
- **Chytrý editor .pro souborů** — syntax highlighting, code completion a rychlá dokumentace (Ctrl+Q) pro 50+ direktiv
- **Config Linter** — neznámé direktivy s návrhem opravy překlepu, nebezpečné volby (`-dontobfuscate`, `-ignorewarnings`, plošný `-dontwarn`), duplicity
- **Safe-mode Analyzer** — automatická detekce rizik (reflection, serialization, JNI, DI) a generování keep rules
- **Export Keep Rules** — zápis vygenerovaných pravidel do `proguard-rules.pro` se zachováním ručně psaného obsahu
- **Dry-run Validation** — ověření, že obfuskace nerozbije aplikaci, včetně vynucení týmové policy
- **Stack Trace Deobfuscator** — retrace crash logů včetně R8 line-range mappingů, auto-detekce mapping souboru
- **Mapping Vault** — perzistentní úložiště mapping souborů s diffem a metadaty buildů
- **CI/CD Export** — šablony pro GitHub Actions, GitLab CI a Jenkins
- **Policy Engine** — týmová pravidla v `obfuscateguard-policy.json` sdílená přes VCS

## Screenshoty

| Editor .pro souborů s lintingem | Analýza rizik |
|---|---|
| ![Editor](docs/screenshots/pro-editor-linting.png) | ![Analýza](docs/screenshots/toolwindow-analysis.png) |

![Overview](docs/screenshots/toolwindow-overview.png)

## Týmová policy

Vytvořte `obfuscateguard-policy.json` v kořeni projektu a commitněte do VCS:

```json
{
  "minLevel": "STANDARD",
  "requireMappingOutput": true,
  "requiredKeepAttributes": ["SourceFile", "LineNumberTable"],
  "forbiddenDirectives": ["-dontobfuscate", "-ignorewarnings"],
  "maxUncoveredCriticalRisks": 0
}
```

Dry-run validace (Tools → ObfuscateGuard → Dry-Run Validation) policy vynucuje a hlásí porušení.

## Vývoj

```bash
./gradlew test            # unit + platform testy
./gradlew buildPlugin     # distribuce do build/distributions
./gradlew runIde          # sandbox IDE s pluginem

# regenerace screenshotů z reálného UI
./gradlew test --tests "*ScreenshotGeneratorTest*" -PscreenshotsDir=$(pwd)/docs/screenshots
```

## Dokumentace

- [Business Requirements (BRS)](docs/BRS.md)
- [Software Requirements (SRS)](docs/SRS.md)
- [Implementation Plan](docs/PLAN.md)

## Tech Stack

- Kotlin | IntelliJ Platform SDK | Gradle

## License

Proprietary — HTMETA.dev

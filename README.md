# ObfuscateGuard

> JetBrains IntelliJ Platform plugin pro bezpečnou orchestraci obfuskace Java/Kotlin projektů.

**ObfuscateGuard** není obfuskátor — je to **workflow, analýza a governance vrstva** nad existujícími obfuskátory (ProGuard/R8, yGuard, Allatori) přímo v IDE.

## Klíčové funkce (plánované)

- **Configuration Wizard** — nastavení obfuskace bez studia 40stránkových manuálů
- **Safe-mode Analyzer** — automatická detekce rizik (reflection, serialization, JNI, DI) a generování keep rules
- **Dry-run Validation** — ověření, že obfuskace nerozbije aplikaci
- **Mapping Management** — šifrovaný vault, diff, export pro crash symbolication
- **CI/CD Export** — generování headless konfigurace a CI šablon
- **Policy Engine** — týmová pravidla pro obfuskaci

## Dokumentace

- [Business Requirements (BRS)](docs/BRS.md)
- [Software Requirements (SRS)](docs/SRS.md)
- [Implementation Plan](docs/PLAN.md)
- [Copilot Instructions](.github/copilot-instructions.md)

## Tech Stack

- Kotlin | IntelliJ Platform SDK | Gradle

## License

Proprietary — HTMETA.dev

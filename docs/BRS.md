# ObfuscateGuard — Business Requirements Specification (BRS)

**Verze:** 1.0  
**Datum:** 2026-02-16  
**Autor:** HTMETA.dev  
**Status:** Draft

---

## 1. Shrnutí produktu

**ObfuscateGuard** je plugin pro IntelliJ-platform IDE, který zjednodušuje, zabezpečuje a standardizuje proces obfuskace Java/Kotlin projektů. Plugin neslouží jako vlastní obfuskátor — místo toho je **orchestrační a governance vrstvou** nad existujícími obfuskátory (ProGuard/R8, yGuard, Allatori), která řeší hlavní bolesti vývojářů:

- Složitá konfigurace a keep rules
- Rozbitá aplikace po obfuskaci (reflection, serialization, frameworks)
- Chybějící auditovatelnost a reprodukovatelnost
- Neexistující týmová pravidla pro obfuskaci

---

## 2. Vize a cíle

### 2.1 Vize
Stát se **de-facto standardem** pro bezpečné používání obfuskace v JetBrains IDE — plugin, bez kterého nikdo nechce obfuskovat.

### 2.2 Strategické cíle
| # | Cíl | Měřitelný indikátor |
|---|-----|---------------------|
| G1 | Snížit bariéru vstupu do obfuskace | Čas od instalace do první úspěšné obfuskace < 10 min |
| G2 | Eliminovat „broken builds" po obfuskaci | Incidenty „obfuskace rozbila build" < 5 % |
| G3 | Umožnit týmový governance | Sdílené profily, policy, audit trail |
| G4 | Generovat recurring revenue | Dosáhnout €80K MRR do 24 měsíců (base scénář) |

---

## 3. Stakeholdeři

| Stakeholder | Potřeba | Priorita |
|------------|---------|----------|
| **Individuální vývojář** (Java/Kotlin/Android) | Rychlé nastavení, bezpečné defaults, žádné rozbité buildy | Highest |
| **Týmový lead / Architekt** | Sdílené profily, policy enforcement, reprodukovatelnost | High |
| **Security / Compliance** | Audit trail, mapping management, deterministický výstup | High |
| **DevOps / Build engineer** | CI/CD integrace, export konfigurace, headless provoz | Medium |
| **Product owner (HTMETA)** | Revenue, aktivace, retence, konverze | Internal |

---

## 4. Scope

### 4.1 In-scope (MVP)
- Podpora **Java/Kotlin** projektů v IntelliJ IDEA, Android Studio
- Integrace s **ProGuard/R8** (primární) a **yGuard** (sekundární)
- Jednotná UI vrstva (tool window, wizard, run configuration)
- **Safe-mode analyzátor**: detekce rizik (reflection, serialization, JNI, DI frameworks), automatické generování keep rules
- **Dry-run validace**: simulovaný build + testy po obfuskaci
- **Mapping management**: uložení per build, diff, export
- **CI/CD export**: Gradle/Maven tasky, GitHub Actions/GitLab CI šablony
- Freemium gating (feature flags) + JetBrains Marketplace licensing

### 4.2 Out-of-scope (MVP)
- Vlastní obfuskační engine
- Multi-language obfuskace (JS/TS, .NET, C/C++)
- Remote/cloud mapping storage (Team plán — roadmap)
- Anti-tamper / runtime protection (roadmap)
- Enterprise SSO/SAML integrace (roadmap)

---

## 5. Business model

### 5.1 Cenová strategie (per user / per year)

| Tier | Cena (EUR/rok) | Funkce |
|------|----------------|--------|
| **Free** | €0 | Základní wizard, lokální obfuskace, log viewer |
| **Pro** | €79–€99 | Safe-mode analyzátor, keep rules AI, mapping vault, CI export, dry-run |
| **Business** | €149–€249 | Policy engine, sdílené profily, audit trail, remote storage, priority support |

### 5.2 Distribuce
- **JetBrains Marketplace** — primární kanál (plugin listing, licensing, billing)
- **Vlastní web** (htmeta.dev) — marketing, docs, enterprise kontakt

### 5.3 Monetizační milníky
| Milestone | Target |
|-----------|--------|
| 1 000 free instalací | M6 |
| 100 platících uživatelů | M9 |
| €10K MRR | M15 |
| €80K MRR | M24 |

---

## 6. Metriky úspěchu (KPIs)

| Metrika | Definice | Cíl (M12) |
|---------|----------|-----------|
| **Aktivace** | % instalací → první úspěšná obfuskace | > 40 % |
| **Free→Paid konverze** | % free userů → Pro/Business | > 5 % |
| **Monthly Active Users** | Unikátní uživatelé za měsíc | > 800 |
| **Renewal rate** | Roční obnovení licence | > 75 % |
| **Support load** | Tikety „broken build" / 100 zákazníků | < 5 |
| **NPS** | Net Promoter Score | > 40 |

---

## 7. Konkurenční analýza

### 7.1 JetBrains ekosystém
| Plugin | Model | Poznámka |
|--------|-------|----------|
| Safely Obfuscate Source | Freemium | Řídce aktualizovaný |
| Taint Bomb auto Java Obfuscator | Free | „One-click" Java obfuskace |
| IntelliGuard 2 | Free | yGuard integrace, starší |

### 7.2 Standalone obfuskátory (benchmark)
| Nástroj | Cena | Typ |
|---------|------|-----|
| ProGuard/R8 | Free / OSS | Bytecode shrinker + obfuskátor |
| Allatori (Smardec) | $290–$4850 | Full-featured, JVM |
| Zelix KlassMaster | $290–$585 | JVM, string encryption |
| DashO (PreEmptive) | Quote-based | Enterprise „app hardening" |

### 7.3 Naše diferenciace
- **IDE-native workflow** (ne standalone CLI)
- **Safe-mode analyzátor** (žádný konkurent nenabízí automatickou detekci rizik)
- **Governance vrstva** (policy, audit, sdílené profily)
- **Mapping management** přímo v IDE

---

## 8. Rizika

| # | Riziko | Pravděpodobnost | Dopad | Mitigace |
|---|--------|----------------|-------|----------|
| R1 | „Commodity" past — jen wrapper, tlak na cenu | Střední | Vysoký | Investovat do Safe-mode a governance jako diferenciátorů |
| R2 | Kompatibilita — změny IntelliJ API mezi verzemi | Vysoká | Střední | Podpora posledních 2 major verzí, CI matrix testing |
| R3 | Právní/etické — zneužití pro malware | Nízká | Vysoký | Jasné ToS, abuse reporting, opatrný marketing |
| R4 | Adopce — vývojáři nemají motivaci obfuskovat | Střední | Vysoký | Edukační content, „time to first obfuscation" < 10 min |
| R5 | ProGuard/R8 nativní IDE integrace od Google | Nízká | Vysoký | Zaměřit se na governance + multi-engine podporu |

---

## 9. Roadmap (high-level)

| Fáze | Popis | Klíčový výstup |
|------|-------|----------------|
| **Phase 0** | Foundation & Architecture | Skeleton plugin, CI/CD, signing |
| **Phase 1** | Free Tier MVP | Wizard, run config, log viewer, ProGuard integrace |
| **Phase 2** | Pro Tier | Safe-mode analyzátor, mapping vault, CI export |
| **Phase 3** | Business Tier | Policy engine, audit trail, sdílené profily |
| **Phase 4** | Growth | Multi-engine, Kotlin Multiplatform, partnerships |

---

## 10. Schvalovací proces

| Role | Odpovědnost |
|------|-------------|
| Product Owner | Schvaluje BRS, prioritizuje features |
| Tech Lead | Validuje technickou proveditelnost |
| UX | Schvaluje wizard flow a UX požadavky |

---

*Tento dokument je živý a bude aktualizován na základě feedbacku a vývoje trhu.*

Udělám to takhle: nejdřív zmapuju konkurenci (přímo v JetBrains Marketplace i mimo IDE), z toho odvodím realistické cenové pásmo. Pak odhadnu velikost trhu a scénáře MRR/ARR (a dám ti je i jako grafy). Nakonec dodám BRS, SRS, „copilot instructions“ a realizační plán.

Konkurence (JetBrains ekosystém)
V Marketplace už „něco“ je, ale je to zatím překvapivě řídké:
	•	Safely Obfuscate Source – plugin je označený jako freemium („některé funkce vyžadují placené předplatné“).  ￼
Pozn.: přesná cena je na JetBrains store stránce, která se v některých regionech/klientech špatně parsuje; veřejné snippet zdroje zmiňují řádově desítky až nižší stovky USD ročně na uživatele, takže ho beru jako horní benchmark pro „specializovaný security“ plugin.  ￼
	•	Taint Bomb auto Java Obfuscator – „one click“ obfuskace Java zdrojáků.  ￼
	•	IntelliGuard 2 – integrace yGuard do IntelliJ (obfuskace bytecode), starší ekosystémově.  ￼

Konkurence (mimo IDE – to je tvůj „skutečný“ benchmark)
Tady se nastavuje očekávání ceny i „hloubky“ ochrany:
	•	Allatori (Smardec): single developer licence $290, site $3750, business $4850, update/support ročně.  ￼
	•	Zelix KlassMaster (Zelix Pty Ltd): standard $585, small developer $290 (a multi-licence).  ￼
	•	„Enterprise“ nástroje typu DashO (PreEmptive) typicky jedou přes quote a prodávají „app hardening“ balík, ne jen rename symbolů.  ￼

Důležitá realita: obfuskace zvyšuje náklady reverse engineeringu, ale není to „bezpečnostní štít“. Jako positioning je fér slibovat „zvýšení nákladů/času útočníka“, ne „neprolomitelnost“.  ￼

Kde se pohybovat s cenou pluginu
JetBrains Store u pluginů běžně používá per-user-per-year (se slevami při obnově), přičemž existují i nízké i střední ceny (v ukázce store stránky jsou pluginy za ~$20/uživatel/rok nebo ~$75/uživatel/rok).  ￼
Marketplace zároveň podporuje freemium model (free core + placené funkce).  ￼

Rozumné cenové pásmo pro „obfuscation“ plugin do JetBrains (podle toho, co chceš dodávat):
	1.	„IDE convenience“ (spouštěč + konfigurace existujících obfuskátorů): €39–€79 / uživatel / rok
	2.	„Pro“ (multi-language, mapping, CI export, guardrails, policy): €79–€149 / uživatel / rok
	3.	„Team/Compliance“ (policy, audit log, pravidla pro PII/strings, SSO/licensing, support SLA): €149–€299 / uživatel / rok

Pozicování proti Allatori/Zelix: ty jsou jednorázové licence řádově stovky USD, ale jsou to plnohodnotné obfuskátory. Ty prodáváš „workflow + bezpečné používání + integraci + governance“ přímo v IDE. Tím se dá obhájit subscription.

Jak velký je trh (TAM/SAM/SOM)
TAM (teoretický): JetBrains uvádí 11.4M konzistentních uživatelů nástrojů.  ￼
Světově je ~20.8M profesionálních vývojářů (2025).  ￼

Pro obfuskaci je relevantní hlavně JVM/Android a “shipovaný” software. Z JetBrains Data Playground (globální prof. devs) máš:
	•	Java: ~1.585M devs použilo za posledních 12 měsíců  ￼
	•	Kotlin: ~336k jako primární jazyk (v tom dashboardu)  ￼
	•	Mobile platforma: ~1.054M (silně koreluje s „potřebuju obfuskovat APK/AAB“)  ￼

SOM (reálně dosažitelný) je vždy mnohem menší, protože:
	•	ne každý používá JetBrains IDE,
	•	ne každý obfuskaci chce/řeší (open-source knihovny, backend bez distribuce, apod.),
	•	část lidí má obfuskaci už v build pipeline a IDE nic nepotřebuje.

Proto dávám scénářový model níže (explicitně je to „working theory“, ne jistota).

MRR/ARR scénáře (graficky)
Vytvořila jsem model ze tří scénářů (konzervativní/základní/agresivní) a tří cenových plánů (59/99/149 € ročně). V modelu vycházím z JetBrains 11.4M uživatelů jako horní hranice a odhaduju, jaká část z nich je tvoje SAM (IntelliJ platform + JVM/Android) a jaká část z toho zaplatí.

Tabulku máš v UI jako „Model ARR/MRR – scénáře a cenové plány“.

Grafy ke stažení:
	•	MRR graf￼
	•	ARR graf￼

Rychlé čtení výsledků (zaokrouhleno):
	•	Konzervativní: ~€0.8k–€2.1k MRR
	•	Základní: ~€8k–€20k MRR
	•	Agresivní: ~€56k–€142k MRR

Tyhle čísla jsou citlivé hlavně na dvě věci: (1) jestli umíš udělat „must-have“ workflow (ne jen tlačítko na existující obfuskátor), (2) enterprise distribuci (týmy, audit, policy).

BRS (Business Requirements Specification)
Cíl produktu
Plugin pro IntelliJ-platform IDE, který zjednoduší a zbezpeční obfuskaci (a ideálně i „hardening“) tak, aby:
	•	vývojářka zvládla nastavení bez studia 40 stránkových konfiguráků,
	•	tým měl repeatable proces (policy, audit, mapping, CI export),
	•	riziko „rozbití“ aplikace obfuskací šlo drasticky dolů (guardrails + validace).

Stakeholdeři
	•	Individuální vývojářky a malé týmy (JVM/Android, desktop, knihovny).
	•	Security/Compliance (vyžadují audit, repeatable proces).
	•	Build/DevOps (chce kompatibilitu s CI a build toolingem).

In-scope (MVP)
	•	Podpora Java/Kotlin projektů v IntelliJ-based IDE.
	•	Integrace s běžnými obfuskátory (např. ProGuard/R8, yGuard – podle směru produktu) a jednotná UI vrstva.
	•	„Safe mode“: analýza projektu, detekce rizik (reflection, serialization, JNI, frameworky), generování keep rules.
	•	Export konfigurace do CI (Gradle/Maven tasky, config soubory).
	•	Mapping management (uložení, diff, možnost použít pro crash symbolication / debug).

Out-of-scope (pro MVP)
	•	Vlastní obfuskátor (to je samostatný produkt).
	•	Plná multi-language obfuskace (JS/TS, .NET) – spíš roadmap.

Business model
	•	Freemium (free: základní workflow + lokální obfuskace; paid: safe-mode analyzátor, policy, mapping vault, CI export, týmové funkce).  ￼
	•	Licence per user per year (standard v JetBrains store).  ￼

Metriky úspěchu
	•	Aktivace: % instalací → první úspěšná obfuskace.
	•	Retence: monthly active users, renewal rate.
	•	Support load: počet incidentů „obfuskace rozbila build“ na 100 zákaznic.
	•	Konverze free→paid.

Rizika
	•	„Commodity“ past: když je to jen wrapper, bude tlak na cenu.
	•	Právní/etické: obfuskace může být zneužitá (malware). Doporučuju jasné ToS + abuse reporting + opatrně s marketingem „uncrackable“.

SRS (Software Requirements Specification)
	1.	Funkční požadavky
Projekt & IDE integrace

	•	FR1: Detekce typu projektu (Gradle/Maven, Android, multi-module).
	•	FR2: Konfigurační wizard (výběr cíle: app/lib/plugin; úroveň: light/standard/aggressive; cíle: IP ochrana / anti-tamper / string protection).
	•	FR3: Spuštění obfuskace lokálně (run configuration) + zobrazení logů.

Safe-mode analyzátor (diferenciátor)
	•	FR4: Statická analýza rizik: reflection usage, annotation scanning, serialization, frameworks (Spring, Jackson, Retrofit, DI), JNI, dynamic classloading.
	•	FR5: Generování keep rules + vysvětlení „proč“ (traceability).
	•	FR6: „Dry run“ validace: zkusit build, spustit testy (pokud existují), a vyhodnotit regresi.

Mapping & reprodukovatelnost
	•	FR7: Uložení mappingů per build (timestamp, git hash, variant).
	•	FR8: Diff mappingů mezi verzemi + varování na „velké skoky“.
	•	FR9: Export mappingů do bezpečného úložiště (lokální vault + volitelně S3/GCS v Team plánu).

CI/CD export
	•	FR10: Vygenerovat konfiguraci do repa (soubor + tasky) tak, aby to jelo bez IDE.
	•	FR11: “Copy-paste ready” snippety do CI (GitHub Actions/Jenkins/GitLab CI – textové šablony).

Licencování a platby
	•	FR12: Podpora freemium gatingu (feature flags).  ￼
	•	FR13: Integrace JetBrains Licensing/paid plugin mechanismu a reporting (Analytics/Sales).  ￼

	2.	Nefunkční požadavky

	•	NFR1: Kompatibilita: IntelliJ platform verze (definovat podporované major verze), Android Studio varianty (pokud cílíš).
	•	NFR2: Výkon: analýza musí být přerušitelná; UI nesmí blokovat EDT; cache výsledků.
	•	NFR3: Bezpečnost: mapping vault šifrovaně; žádné posílání zdrojáků ven bez opt-in.
	•	NFR4: Transparentnost: jasně logovat, co plugin dělá (aby security týmy nebyly alergické).
	•	NFR5: Spolehlivost: deterministické generování konfigurace (stejný vstup → stejný výstup).

	3.	UX požadavky

	•	Jedno centrální „Obfuscation“ tool window: status, poslední běhy, varování, mappingy, export.
	•	Wizard s „explain like I’m busy“: co volba znamená a jaké má dopady.
	•	“Fix-it” tlačítka: „vygeneruj keep rules“, „přidej výjimku pro package“, „přepni na light“.

Speciální „copilot instructions“ (pro vývoj pluginu)
Toto můžeš doslova dát do repa jako COPILOT_INSTRUCTIONS.md:
	•	Jsi senior JetBrains plugin engineer. Piš Kotlin. Nepoužívej experimentální API bez feature flagu.
	•	Všechny operace nad projektem běží mimo EDT; UI jen renderuje stav.
	•	Konfigurace se serializuje přes vlastní settings service + migrace verzí.
	•	Logování přes IntelliJ logger; žádné println.
	•	Každý “run” má immutable model (input snapshot + output).
	•	Testy: unit testy pro analyzátor; integrační testy pro generování configu; golden tests pro šablony.
	•	Bezpečnost: nikdy neodesílej zdrojový kód, symboly ani mapping mimo zařízení bez explicitního opt-in; telemetry je anonymní, minimal.
	•	Kód musí být čitelný: malé třídy, žádná magie; veřejné API dokumentuj KDoc.
	•	Vždy preferuj „safe defaults“: když si nejsi jistý keep pravidlem, navrhni ho, ale označ ho jako „needs review“.

Plán prací (co všechno se musí udělat)
Fáze 0 – produktové rozhodnutí (1–3 dny čisté práce)
	•	Vybrat směr: „wrapper+governance“ (rychle na trh) vs „vlastní obfuskátor“ (dlouhé).
	•	Definovat MVP use-case: Android app? Java library? Obfuskování zdrojáků vs bytecode?

Fáze 1 – technický spike (MVP feasibility)
	•	Spike integrace (Gradle/Maven) a běh obfuskace na sample projektech.
	•	Prototyp tool window + run config.
	•	Ověřit kompatibility matrix (IntelliJ/Android Studio).

Fáze 2 – MVP
	•	Wizard + základní generování configu
	•	Spuštění obfuskace + log viewer
	•	CI export (minimálně Gradle)
	•	Freemium gating připravený (i kdyby byly všechny funkce zatím free)

Fáze 3 – diferenciátor („Safe mode“)
	•	Statická analýza rizik + návrhy keep rules
	•	Dry run + integrace s test runnerem (když existují testy)
	•	Mapping vault + export

Fáze 4 – monetizace a release
	•	Nastavit paid/freemium v Marketplace, připravit store listing, pricing, EULA/ToS
	•	Analytics a sales reporting workflow  ￼
	•	Beta program (closed) + feedback loop, fixy

Fáze 5 – růst
	•	Team plán: policy, audit, sdílené profily, remote storage
	•	Další ekosystémy: Kotlin Multiplatform, server-side frameworks, případně JS/TS (pokud chceš)

Jeden chytrý „trik“ na závěr: v Marketplace je zatím konkurence v obfuskaci relativně malá, ale zároveň už existuje freemium hráč. To je signál, že poptávka existuje, jen je potřeba ji chytit přes “workflow pain” (rozbitá obfuskace, keep rules peklo, audit/policy) – ne přes „přejmenovala jsem proměnné“.

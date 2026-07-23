# Revue — songbook (2026-07-02, mise à jour)

Fork de kawane/songbook (Undertow + Lucene). Gestionnaire de chansons ChordPro, en prod (lié depuis llgmusic.net). Remote = kawane/songbook directement.

## État — le plan de la revue précédente est déjà fait upstream

- La branche `upgrade` a été mergée dans master via PR #44 **côté GitHub**, puis master a reçu 2 commits de plus : `483d970 Use java 21` et `c363040 use 8-jdk21-alpine`. Le projet est donc passé à **Java 21** (plus loin que le Java 17 de la branche upgrade).
- Local synchronisé (2026-07-02) : `master` et `upgrade` fast-forwardés sur origin. La branche `upgrade` est entièrement contenue dans master → elle peut être supprimée.
- Le wrapper Gradle (`gradlew`) a été supprimé upstream : le build passe par Docker (ou un Gradle local, absent sur cette machine).

## Validation locale (2026-07-02) — OK

- `docker build` (gradle:8-jdk21-alpine → temurin:21-jre-alpine) : **BUILD SUCCESSFUL**.
- Conteneur lancé : serveur écoute sur 0.0.0.0:8000, home 200, `/search/` retourne les 2 chansons embarquées, page chanson (Be Bop A Lula) rendue correctement.

## Reste à faire

Rien côté code ni déploiement :

- ~~Rebuild sur pi2~~ — vérifié le 2026-07-02 : la prod tourne déjà sur Temurin 21.0.3 (`docker exec songbook java -version`), donc le pi2 a été rebuildé avec le dernier master. Rien à déployer.
- ~~Supprimer la branche `upgrade` locale~~ — fait (`git branch -d upgrade`).

## Décision à prendre : quel avenir ?

1. **Maintenance minimale** (recommandé court terme) : le service marche, ne toucher qu'en cas de casse.
2. **Upgrade** : ~~finir la branche upgrade~~ fait. Suite éventuelle : moderniser les deps (Undertow/Lucene datent).
3. **Convergence learnsong** : migrer le contenu ChordPro vers learnsong.mesnos.ovh (ABC + leçons + players) pour ne plus maintenir deux stacks (Java vs Deno).

## TODOs hérités du projet

Import/export d'archives, Admin UI, auto-scroll pendant le jeu. À ne faire que si l'option 2 est retenue.

## Analyse fonctionnelle (2026-07-02)

**Ce que fait songbook** (README + code) : un serveur minimaliste pour stocker, chercher et afficher des chansons au format ChordPro (paroles + accords), « pour se libérer des sites pleins d'animations et se concentrer sur la chanson ». Philosophie assumée : rapide, simple, pas de gestion d'utilisateurs — juste une clé admin (cookie) pour éditer.

Fonctionnalités :
- Stockage : 1 fichier ChordPro par chanson (`data/songs/`), pas de BDD. Parseur ChordPro maison (`songbook.chordpro`).
- Recherche plein texte Lucene (titre, artiste, contenu), navigation par artiste.
- CRUD chansons via REST (`/songs/{id}` GET/POST/PUT/DELETE) + éditeur web Ace avec mode ChordPro custom et coloration.
- Négociation de contenu : chaque chanson servie en HTML, texte brut ou `text/song` (ChordPro brut) selon Accept — le site est aussi une API.
- Admin : réindexation Lucene, clé admin auto-générée au premier lancement.
- CSS d'impression dédiée (song.css media screen,print + song-nochords.css) : imprimer les partitions, avec ou sans accords.

**Stack** : Java 21 + Undertow (HTTP) + Lucene (index) + templates HTML maison côté serveur ; frontend Bootstrap 3.3.4, jQuery 1.11.2, RequireJS, Ace (2014-2015).

## Modernisation deps backend — fait (2026-07-02, non commité)

- Lucene 9.9.0 → **10.4.0** ; suppression de `lucene-analyzers-common:8.11.2` (poids mort : StandardAnalyzer est dans lucene-core depuis Lucene 9 — c'était même un doublon de classe sur le classpath).
- Adaptation Lucene 10 dans IndexDatabase : `TopScoreDocCollector.create()` (supprimé en 10) remplacé par `searcher.search(query, n)`, équivalent.
- Undertow 2.3.10 → **2.3.18.Final** (dernière 2.3.x, correctifs CVE).
- JUnit 5.10.1 → **5.12.2** (aucun test dans le projet pour l'instant).
- Validé : build Docker OK, smoke test complet OK (recherche vide/scorée, artistes, pages chanson/artiste, zéro erreur logs).
- Note déploiement : Lucene 10 lit les index Lucene 9 (compat N-1), et `/admin/index/reset` permet de réindexer au besoin.

## Frontend — état des lieux pour le lifting

Constat : Bootstrap 3.3.4 (2015), jQuery 1.11.2 (2014), RequireJS (obsolète), Ace embarqué en vrac. Templates HTML générés en Java par concaténation (`Templates.java`). Pistes par ordre d'effort croissant :
1. **Rafraîchissement cosmétique** : garder la structure, moderniser le CSS (variables, dark mode, meilleure typo des accords), viewport mobile.
2. **Dé-jQuery-fication** : JS vanilla moderne (fetch, modules ES), virer RequireJS ; garder Ace (toujours maintenu) via CDN ou npm.
3. **Refonte** : Bootstrap 5 ou CSS maison léger ; l'occasion d'ajouter les features utilisateur (auto-scroll pendant le jeu, transposition d'accords côté client, mode plein écran/pupitre).

À discuter (brainstorming) : les objectifs d'usage réels — qui s'en sert (llgmusic.net → concerts ? répètes ?), sur quel device (tablette sur pupitre ?), et ce qui manque vraiment au quotidien.

## Lifting frontend — fait (2026-07-05)

Zéro-build conservé (ES modules natifs + CSS servis tels quels). Bootstrap 3 / jQuery / RequireJS / Ace / Glyphicons supprimés. En place :
- **mesnos.style.css v0.1.44 vendorée** (`css/vendor/`) + couche app token-compatible (`css/app.css`) + `css/song.css` réécrite (tous les sélecteurs émis par SongUtils conservés). Dark mode via `data-theme` (convention components.mesnos.ovh), impression forcée en clair (bloc `@media print` final).
- **Monaco 0.52.2 vendoré** (sous-ensemble AMD min/vs, ~4 Mo, `js/vendor/monaco/`) avec langage **songmark** (Monarch porté de vscode-songmark) et thèmes light/dark. Chargé uniquement sur `/edit` et `/new`, par import dynamique. **Fallback textarea sur mobile/tactile** (`pointer: coarse`).
- JS en modules ES : `api.js` (fetch), `search.js`, `view.js` (transposition/plein écran/colonnes portés + **nouveau toggle accords**, qui sert aussi d'« imprimer sans accords » — avant, song-nochords.css n'était référencée nulle part), `edit.js`, `consoleApi.js` (simplifié, textareas).
- Toolbar chanson : barre en bas de l'écran sur mobile (pouces), flottante en haut à droite sinon. SVG inline à la place des glyphicons.
- Scripts de mise à jour des vendors : `tools/update-monaco.sh`, `tools/update-mesnos.sh` + `doc/Update_Vendored_Assets.md`.
- Validé : docker build, placeholders 100% résolus, négociation Accept (song/plain/html), flux admin CRUD + 401, transposition G→G#, toggle accords, dark mode, Monaco + coloration, zéro erreur console. **Reste à vérifier à la main : l'aperçu d'impression** (2 colonnes, sans accords) et l'édition sur un vrai téléphone.

## Manques dans components.mesnos.ovh — ~~à remonter~~ upstreamés en v0.2.0 (2026-07-16)

Les patterns écrits localement ici ont été upstreamés dans la lib (phase 1 du plan écosystème) et publiés en **v0.2.0**. Songbook les consomme désormais depuis le vendor, les doublons locaux ont été supprimés d'`app.css` (cf. `doc/Adopt_Mesnos_0.2.0.md`) :
1. ~~Top nav / app-bar~~ → `.topnav`, `.topnav a`, `.topnav .brand`, `.topnav-search` dans la lib (+ bonus : override `@media (pointer:coarse)` qui passe les liens à `--size-touch-min`).
2. ~~List group~~ → `.item-list`/`.item`/`.item-title`/`.item-sub` dans la lib.
3. ~~Button group~~ → `.btn-group` dans la lib (cible `> button` nu en plus de `> .btn`).
4. ~~Support impression~~ → `@media print` dans la lib : masque `.no-print`/`.topnav`, force noir sur blanc, coupe les ombres.
5. ~~Input de recherche navbar~~ → `.topnav-search input` dans la lib.

Reste local dans `app.css` (spécifique songbook) : `--chord`/`--meta`/`--rule` (+ variantes dark), `html { background }` (correctif fond noir en plein écran — la lib ne style que `body`), le trick de visibilité par rôle, `.song-toolbar`, `#song-edit`, `.page`, `.icon`, et le print de `.song-toolbar`. Les blocs print de `song.css` restent chargés en dernier (tokens forcés clairs, liens masqués).

Phase 2 (plus tard) : remplacer `js/dialog.js` par `<mesnos-dialog>` quand il existera.

## Revue de code + durcissement serveur — fait (2026-07-05)

Revue complète serveur + client. Avis global : projet sain, bien dimensionné pour un usage perso mono-utilisateur. Correctifs appliqués (commit « Server hardening ») :
- **Fuite de reader** : `IndexDatabase.listArtists` n'a jamais fermé son `DirectoryReader` (fuite de handle à chaque /artists). Les 3 méthodes de recherche passent en try-with-resources.
- **Corruption UTF-8** : `ChannelUtil.getStringContents` décodait chaque bloc de 8 Ko séparément → caractères accentués coupés sur une chanson > 8 Ko. Lit tout puis décode une fois.
- **Échappement HTML** : `SongUtils.writeHtml` échappe désormais tout le contenu (titre, métadonnées, paroles, accords) et n'émet un `<a>` que pour les schémas http/https/mailto. Corrige le rendu des chansons contenant `< > &` et ferme un XSS stocké. Sortie text/song inchangée.
- **Clé admin** : 128 bits via `SecureRandom` au lieu de MD5(timestamp de démarrage), prévisible. Cookie de session : `HttpOnly` + `SameSite=Lax` + `Path=/` (rien ne le lit en JS). Même approche cookie conservée.
- **Nettoyage** : suppression du package mort `songbook.chordpro` et du handler CORS inopérant.
- Tests : ajout d'assertions d'échappement HTML. Suite verte (9 tests).

**Sécurité — pistes futures** (non faites, hors périmètre « même approche ») : brancher songbook sur users.mesnos.ovh (JWT access/refresh token) ; la clé transite encore en `?key=` dans l'URL (logs/historique).

## Montée Java 25 / Node 24 + modernisation langage — fait (2026-07-23)

Passage aux dernières LTS stables et adoption des nouveautés du langage Java (Java 21 → 25).

**Runtimes & config de dev :**
- `Dockerfile` : build `gradle:8-jdk21-alpine` → `gradle:9-jdk25-alpine`, runtime `eclipse-temurin:21-jre-alpine` → `:25-jre-alpine`.
- `build.gradle` : ajout d'un bloc `java { toolchain { languageVersion = JavaLanguageVersion.of(25) } }` — la version Java n'était imposée que par l'image Docker, elle est désormais épinglée pour le build local / devcontainer.
- `.devcontainer/devcontainer.json` : feature Java `none` → `25`, ajout de la feature Node `24` (pour les tests vitest).
- `package.json` : ajout de `engines.node >= 24` ; nouveau `.nvmrc` (`24`).
- Suppression des `deploy/docker/{songbook,songbook-dev}/Dockerfile` obsolètes (FROM java:8, songbook-0.4.zip inexistant) ; le Dockerfile racine est le seul à jour.
- Docs : mentions « Java 8 » périmées corrigées en Java 25 (`README.md`, `doc/Install_DIY.md`).

**Modernisation code (refactos syntaxiques, comportement préservé) :**
- **Records** : `IndexEntityType`, `Templates.TemplateCache`, `MimeParser.MimeTypePattern`.
- **Sealed** : `ServerException sealed ... permits SongNotFoundException, MissingArgumentsException` (sous-classes passées `final`).
- **Pattern matching for instanceof** : handler d'exception dans `Server.exceptionHandler`.
- **Switch expressions (forme flèche)** : `SongUtils.escape` + mapping schema.org, `Server.restSong/getSong/deleteSong/searchPage/adminCommand`, les 3 switch `mimeType` d'`IndexDatabase`.
- **Text block** : `Server.createMessage` (JSON via `.formatted`, plus d'échappement de guillemets).
- **`var`** + diamant `<>` + lambda (`MimeParser` : comparateur anonyme → lambda, `List.of` à la place de `Collections.singleton`), idiome `!Files.exists(...)` dans `SongDatabase`.

Validé : `docker build` (Gradle 9 / JDK 25 / Temurin 25) OK, suite d'intégration vitest verte.

**Pistes de modernisation restantes** (option « complète » écartée, plus intrusives) : `Optional` aux frontières (`Server.getHeader`, `SongDatabase.getSongContents`, getters d'env `getWebRoot`/`getDataRoot`/`getSongsPath`/`getHost`) ; extraction des switch `mimeType` dupliqués d'`IndexDatabase` (search/songsByArtist quasi identiques) ; refonte de `SongUtils.writeHtml` (gros bloc de concaténation HTML) en text blocks / builder dédié.

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

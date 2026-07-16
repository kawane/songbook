# Adopter mesnos.style.css v0.2.0 (passation)

Contexte : la phase 1 du plan écosystème
(`components.mesnos.ovh/docs/design/2026-07-16-analyse-besoins-ecosysteme.md`)
a upstreamé dans la lib les patterns écrits localement ici : `.topnav`,
`.item-list`, `.btn-group`, `.form-grid`/`.field-row` et les utilitaires
print (`.no-print`, `.topnav` masqués, fond blanc forcé). Publiée en
**v0.2.0** (vérifier que `https://components.mesnos.ovh/version.json`
annonce bien 0.2.0 avant de commencer — déploiement en cours au 2026-07-16).

Objectif : consommer ces classes depuis la lib vendorée et supprimer les
doublons locaux de `src/dist/web/css/app.css`.

## Étapes

1. **Mettre à jour le vendor** : dans `tools/update-mesnos.sh`, passer
   `VERSION="0.1.44"` → `VERSION="0.2.0"`, puis exécuter le script
   (met à jour `src/dist/web/css/vendor/mesnos.style.css` + `MESNOS_VERSION`).
   Réf : `doc/Update_Vendored_Assets.md`.

2. **Supprimer les doublons dans `src/dist/web/css/app.css`** — pour chaque
   bloc, comparer d'abord avec la version lib (elles ont été upstreamées
   depuis ce fichier, mais la lib cible aussi `> button` nu dans
   `.btn-group`, etc.) puis supprimer le bloc local :
   - `.topnav`, `.topnav a`, `.topnav a:hover`, `.topnav .brand`,
     `.topnav-search`, `.topnav-search input`, `.topnav-search input:focus`
     (~l.42-93) ;
   - `.item-list`, `.item`, `.item + .item`, `.item:hover`, `.item-title`,
     `.item-sub` (~l.95-134) ;
   - `.btn-group` et descendants (~l.136-158).

3. **Réduire le bloc `@media print`** (~l.252) : la lib masque déjà
   `.no-print` et `.topnav` et force fond blanc/texte noir. Ne garder
   localement que `.song-toolbar { display: none !important; }`.
   Ne pas toucher aux blocs print de `song.css` (spécifiques chanson,
   doivent rester en dernier).

4. **Mettre à jour le commentaire d'en-tête d'`app.css`** : il annonce
   combler les manques de la lib (topnav, item list, button groups, print) —
   ce n'est plus vrai après l'étape 2-3.

## Validation

- `npm test` (vitest, cf. `tests/`) et build Docker (`docker build .`).
- Vérifs visuelles : accueil/recherche (topnav + liste), page chanson
  (toolbar mobile en bas), dark mode (bouton 🌓), **aperçu d'impression**
  (2 colonnes, sans accords, toolbar/nav absentes), page signin.
- Ce qui doit rester dans `app.css` : `--chord`/`--meta` (+ variantes dark),
  `.song-toolbar`, utilitaires spécifiques songbook.

Rollback trivial : tout est vendoré, `git revert` suffit.

Étape suivante (hors scope ici, phase 2 du plan) : remplacer
`js/dialog.js` par `<mesnos-dialog>` quand il existera.

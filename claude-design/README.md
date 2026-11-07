# mesnos : brief pour Claude Design

Ce dossier rassemble ce qu'il faut pour travailler le design des sites de **mesnos** :
- ce que fait chaque service, d'après la base de connaissances (`carnet.mesnos.ovh`) ;
- 190 captures des applications réelles, lancées en local depuis leurs dépôts ;
- le système de design actuel : jetons CSS et feuille de styles.

Les captures montrent des **données d'exemple inventées** (chansons, ateliers, comptes). Les
interfaces sont celles des dépôts au 7 novembre 2026.

| Dossier | Contenu |
|---|---|
| `README.md` | ce brief |
| `CAPTURES.md` | l'index des captures, avec une légende par écran |
| `screenshots/<appli>/` | les captures en PNG ; suffixes `-dark` (thème sombre) et `-mobile` (390 px de large) |
| `design-system/` | jetons (`variables.css`, `light.css`, `dark.css`), feuille complète `mesnos.style.css` (v0.6.0), API des web components (`custom-elements.json`) |

---

## 1. mesnos en bref

**mesnos** est un serveur auto-hébergé (un Raspberry Pi) et un ensemble d'outils de musique qui
tournent dessus. C'est un **outil personnel**, pour son propriétaire et Laetitia : apprendre des
chansons pour des ateliers de musique trad (irlandais, fest-noz, bal folk), garder un recueil de
chansons avec accords, réviser avec des lecteurs à boucles. Certains contenus sont partagés par
lien public. Les partager avec un groupe de musiciens est une piste ouverte, pas encore décidée
(voir § 7).

Les principes qui comptent pour le design :
- **chaque service tourne seul** : pas de monolithe ; si un service tombe, les autres perdent une
  fonction, pas tout ;
- **statique d'abord** : le site learnsong est généré, songbook n'a aucune étape de build côté
  navigateur ;
- **une stack simple** : web components Lit et JavaScript sans framework lourd côté navigateur ;
- **une seule bibliothèque d'interface** : un motif qui manque va dans `components`, pas dans la
  feuille d'un site.

## 2. La carte des services

| Service | Rôle | Qui y accède | Langue de l'interface |
|---|---|---|---|
| **learnsong** (site) | Bibliothèque musicale : une page par chanson, lecteurs à boucles, contenus des cours (Moodle, Centre Breton) | tout compte connecté ; pages `share/` publiques | anglais (libellés) + contenu en français |
| **learnsong** (admin) | Édition des chansons, des médias et des sources de cours | administrateurs | français |
| **songbook** | Recueil de chansons au format ChordPro (paroles et accords), sur `songbook.llgmusic.net` | public en lecture ; édition par l'administrateur | anglais |
| **users** | Connexion commune : comptes, session, contrôle d'accès de tous les sites protégés | page de connexion publique | français |
| **components** | Bibliothèque partagée : feuille de styles `mesnos.style.css`, web components (lecteurs à boucles audio, vidéo, YouTube ; grille d'accords ; dialogue commun), et son site de documentation | public (CDN) | anglais |

Hors de ce dossier : le carnet lui-même (site Eleventy), la console d'exploitation
(service-monitoring) et le site vitrine `llgmusic.net`.

### Comment ils s'articulent

```
                       ┌──────────────────────────────┐
                       │ components (CDN, npm)         │
                       │ mesnos.style.css + web comp.  │
                       └───┬──────────┬──────────┬─────┘
          styles, lecteurs │   copie  │   npm    │
                           ▼          ▼          ▼
  navigateur ──► Caddy ──► learnsong   songbook   users
                  │        site+admin  (lecture   (page de
                  │                    publique)   connexion)
                  └── « peut-il passer ? » ──────────►┘
                      (forward_auth avant chaque page protégée)
```

- **Se connecter** : une page protégée renvoie vers la page de connexion de `users`, puis revient
  à la page demandée. Une session dure tant que la page la renouvelle (script `session-keeper`,
  jeton de 15 minutes). Si elle expire, la page affiche un bandeau ; le travail en cours est gardé.
- **Les styles** : chaque site choisit sa version de `components` (0.6.0 aujourd'hui pour
  learnsong et songbook, 0.5.0 pour users). learnsong la charge du CDN, songbook en garde une copie,
  users l'intègre à son bundle (la page de connexion ne doit dépendre d'aucun autre service).
- **Les dialogues** : un seul dialogue pour tous les sites, `<mesnos-dialog>` /
  `confirmDialog()`, sur le `<dialog>` natif.
- **songbook → learnsong** (prévu) : songbook servira de source de chansons à learnsong ; les deux
  restent des outils séparés.

## 3. Les applications, écran par écran

Les légendes détaillées sont dans [`CAPTURES.md`](CAPTURES.md).

### learnsong, le site : réviser une chanson

Le cœur de l'usage. Les chansons sont rangées **par personne → année → atelier → chanson**. Une
page de chanson assemble ce qu'il faut pour la travailler :
- le texte libre (notes, structure AABB, astuces), en Markdown ;
- une **grille d'accords** (composant `<chord-grid>`, notation texte SNHT) ;
- des **lecteurs à boucles** : YouTube, vidéos et audios d'atelier, avec boucle A-B, vitesse,
  transposition, forme d'onde ; les audios sont en mode minimal (une ligne) et se déplient ;
- les PDF (partitions) et liens ;
- le contenu importé des plateformes de cours (Moodle, Centre Breton).

Écrans : `learnsong/01` à `07` (accueil, répertoire, pages de chanson, page publique).

À noter : pas de barre de navigation ni d'identité de site ; l'accueil est une liste à puces ;
l'accès à l'admin est un ⚙️ discret en bas à droite, visible de tous.

### learnsong, l'admin : éditer

Une application à deux colonnes : à gauche, la personne, « Nouvelle chanson », « Sources &
import » et l'arbre des chansons ; à droite, la chanson ouverte (métadonnées, styles en pastilles,
liens, leçons, fichiers, contenu en trois onglets Éditer / Blocs / Aperçu).

Écrans : `learnsong-admin/01` à `12`, dont les dialogues (nouvelle chanson, déplacer, supprimer)
et le bandeau de session expirée.

### songbook : le recueil

Une barre du haut (Songs, Artists, Sign In, recherche, thème), une liste de chansons, et la page
d'une chanson en **police à chasse fixe** : les accords au-dessus des syllabes, le refrain en
retrait. Une barre d'outils en bas : taille du texte, transposition, afficher / masquer les
accords, plein écran. L'administrateur édite dans un éditeur de code (Monaco) avec coloration
ChordPro.

Écrans : `songbook/01` à `11`.

À noter : l'interface est en anglais ; l'accès refusé à l'éditeur est une page brute
« Unauthorized » ; la page d'admin est une liste de boutons.

### users : la porte d'entrée

La page de connexion est l'entrée de tous les sites protégés. Elle reste un formulaire simple
(les gestionnaires de mots de passe doivent la reconnaître). Une fois connecté : profil, mot de
passe, et pour les administrateurs, la gestion des comptes et la configuration du serveur, par
un menu sous le nom de la personne.

Écrans : `users/01` à `17`, du premier lancement à la gestion des comptes.

À noter : la page de connexion ne dit pas vers quel site on va ; le titre est le nom du serveur
(« mesnos ») ; le profil est presque vide.

### components : la bibliothèque

Le site de documentation montre chaque composant en démo, son API, et un **guide de styles** en
22 sections (jetons, boutons, formulaires, listes, tableaux, navigation, messages, popovers,
dialogues, contrôles de lecteur…).

Écrans : `components/01` à `10` ; les 22 sections du guide sont `components/03-styles-*`.

## 4. Le système de design actuel

Les fichiers sont dans `design-system/`.

**Palette** (nommée) :

| Nom | Valeur | Usage principal aujourd'hui |
|---|---|---|
| burnt orange | `#ba5624` | titres h1, avertissements, favicon de songbook |
| olive | `#849324` | titres h2-h4, accent des lecteurs, boutons de l'admin learnsong et de songbook |
| slate blue | `#5d6b89` | accent (bouton principal) en clair, texte secondaire, favicon du site learnsong |
| raisin black | `#202030` | texte principal, fond des blocs de code |
| moonstone | `#78b1b9` | info, grille d'accords |
| redwood `#a76d60`, sage `#8fa998`, cerise `#da4167`, amber `#ffc145`, mint `#bee3db` | | palette secondaire |

**Thème sombre** : fonds `#0f0f0f` / `#1a1a1a` / `#282828`, texte blanc, et un **accent bleu
vif `#3ea6ff`** qui remplace le slate blue (voir les boutons « Se connecter » ou « Créer » en
sombre).

**Typographie** : Inter (texte), une pile monospace système (code, paroles de songbook). Échelle
de 10 à 30 px sur une base de 16 px.

**Espacements et tailles** : base 4 px ; cible tactile minimale 44 px ; boutons de 24 à 48 px ;
rayons 4 / 8 / 12 / 16 px ; conteneurs de 320 à 1280 px.

**Motifs CSS** (sans JavaScript) : `.topnav`, `.item-list`, `.btn-group`, formulaires
(`.form-stack`, `.form-grid`, `.field-row`, `.input-group`, `aria-invalid`), `.table-wrap`,
`.message-*`, `.popover-menu`, `.dialog`, utilitaires d'impression.

**Web components** : `<audio-loop-player>`, `<video-loop-player>`, `<youtube-loop-player>`,
`<chord-grid>`, `<mesnos-dialog>` (+ `confirmDialog()`, `alertDialog()`).

**Le thème** se choisit par `data-theme` sur `<html>` (clair, sombre, ou celui du système), avec
un petit script en tête de page contre le flash.

## 5. Constats à l'œil

Relevés en faisant les captures et dans les notes du carnet. Ce sont des constats, pas des
décisions.

1. **Pas d'identité commune visible.** Chaque site a son en-tête (barre complète dans songbook,
   rien dans learnsong, nom du serveur + menu dans users) et sa couleur de favicon (orange pour
   songbook, slate blue pour learnsong, olive pour son admin).
2. **La couleur d'action change** : slate blue pour le bouton principal en clair, bleu vif en
   sombre, olive pour les boutons de l'admin learnsong, des lecteurs et de la barre de songbook.
3. **Deux langues d'interface** : learnsong (libellés « Songs », « Local Practice Files »,
   lecteurs) et songbook en anglais, l'admin learnsong et users en français.
4. **La navigation entre sites n'existe pas** : on passe de learnsong à son admin par un ⚙️, et
   rien ne mène de learnsong à songbook ni au profil.
5. **L'état de connexion n'est visible nulle part** hors de users.
6. **Les lecteurs sont denses** : une dizaine de petits contrôles sur une ligne, que la page d'une
   chanson empile.
7. **Les états vides et les erreurs** sont peu travaillés : accueil de learnsong en liste à puces,
   « Unauthorized » brut dans songbook, profil presque vide dans users.
8. **Le sombre est quasi noir** (`#0f0f0f`), avec des titres orange et olive conservés.

## 6. Les contraintes à respecter

- **Clair et sombre** pour chaque écran, et le choix du système par défaut.
- **Mobile** : chaque écran doit marcher à 375 px de large (règle des dépôts components et users).
- **Accessibilité** : contraste AA (déjà vérifié par des tests sur les boutons), cibles tactiles
  de 44 px, clavier partout ; les lecteurs ont des raccourcis clavier.
- **Pas de framework lourd ni d'étape de build** : songbook est servi tel quel (modules ES et CSS),
  le site learnsong est statique ; une proposition doit pouvoir s'écrire en HTML + classes CSS de
  `mesnos.style.css` + quelques web components.
- **Tout passe par la bibliothèque** : nouvelles couleurs, nouveaux motifs, nouveaux composants
  vont dans `components` ; les sites ne gardent que ce qui leur est propre.
- **La page de connexion** reste un vrai formulaire HTML (gestionnaires de mots de passe), sans
  ressource chargée d'un autre service.
- **L'impression** : songbook a une mise en page d'impression (la chanson seule, sans barres).

## 7. Ce qui s'annonce

Spécifications et idées du carnet qui toucheront l'interface :
- **La page d'un atelier** (spécifiée en novembre 2026) : ranger les chansons d'un atelier en
  sections choisies (« répertoire », « envisagées »), avec un ordre et une note, sans perdre la
  découverte automatique des nouvelles chansons.
- **Partager avec un groupe** (idée, novembre 2026) : inviter une personne par un lien personnel,
  voir ses appareils, la révoquer ; une page « Ton prénom ? » pour demander l'accès.
- **Dans la bibliothèque** : champ à pastilles (`<mesnos-tag-input>`), bascule de thème
  (`<theme-toggle>`), bouton d'utilisateur connecté (`<mesnos-user-button>`), notifications
  (`<mesnos-toast>`).

## 8. Comment ces captures ont été faites

Les applications ont été construites et lancées en local depuis leurs dépôts, puis capturées avec
Playwright (Chromium) :

| Appli | Dépôt | Version |
|---|---|---|
| components | `mesnos/components.mesnos.ovh` | 0.6.0 (commit `6417df8`) |
| users | `mesnos/users.mesnos.ovh` | commit `1a14465` |
| learnsong (site + admin) | `mesnos/learnsong.mesnos.ovh` | commit `605597d` |
| songbook | `kawane/songbook` | commit `ba722c2` |
| descriptions | `mesnos/carnet.mesnos.ovh` | commit `502f2e5` |

Bureau en 1280 × 800 (pleine page), mobile en 390 × 844. Les chansons, ateliers, médias et
comptes sont des exemples inventés. Les sons sont des tons de synthèse. Les vidéos externes de la
démo vidéo de components n'ont pas pu être chargées, d'où l'état « Lecture impossible ». Les
lecteurs YouTube affichent les vignettes de vraies vidéos publiques.

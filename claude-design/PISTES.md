# Ce qui s’annonce : les pistes de mesnos

Toutes les idées trouvées dans le carnet (`specs/`, `specs/idees.md`, la vision de juin 2026, les revues de juillet) et dans les issues ouvertes (carnet, learnsong, components, users), avec le README de songbook. Rien n’est décidé ici : c’est la carte de ce que l’interface devra pouvoir accueillir un jour.

- **Priorité** : celle que donne le carnet quand il en donne une (classement de `idees.md`, phases des spécifications, issues ouvertes) ; « un jour » pour ce qui n’est pas au programme.
- **Faisabilité** : estimée d’après ce qu’il faut techniquement : « simple » tient dans le site ou le navigateur en quelques jours ; « moyenne » demande un service ou un composant conséquent ; « difficile » demande du calcul lourd, un serveur dédié ou un changement de modèle.
- **Où ça tourne** : au build du site, dans le navigateur, dans un petit service (comme users ou l’admin), en calcul lourd (Python, IA, serveur dédié), ou par un changement de modèle (une décision à écrire).

> Le propriétaire l’a dit en novembre 2026 : learnsong ira sans doute plus loin qu’un site statique. Ce n’est pas le cas aujourd’hui, mais l’ergonomie doit le permettre. La sortie de Deno (avant octobre 2027) est le moment naturel pour choisir.

## 1. Les pistes d’un coup d’œil

Priorité en lignes, faisabilité en colonnes.

| Priorité | Simple | Moyenne | Difficile |
|---|---|---|---|
| **haute** | Page d’un atelier en sections<br>Lecteurs fiables et accessibles<br>Recherche et étiquettes transverses<br>Journal de pratique | Formats de contenu, songbook comme source de learnsong<br>Sortir de Deno : choisir l’architecture de learnsong<br>Éditer depuis le téléphone<br>Capture rapide pendant le cours | — |
| **moyenne** | Les composants qui manquent<br>Interfaces en français<br>Vitesse progressive<br>Liens vers un passage<br>Lecteurs intégrés au téléphone<br>songbook en scène | Console d’exploitation<br>Saisir une grille au téléphone<br>Tables d’arrangement interactives<br>Liens de partage révocables<br>Partager avec un groupe<br>Synchroniser entre appareils<br>Listes de répétition<br>Suivre les leçons<br>Comptes et sécurité<br>Lecteur installable et hors ligne<br>S’enregistrer et se réécouter<br>Boucles suggérées | — |
| **basse** | — | Couper et normaliser dans le navigateur<br>Suites de danse<br>Forme d’onde des longues vidéos | Transcrire en ABC<br>Séparer les pistes |
| **un jour** | — | Pédale pour les boucles<br>Métronome visuel et accordeur<br>Diagrammes d’accords et tablatures<br>« Envoyer au lecteur » depuis le web<br>Packs d’exercices | Espaces de collaboration<br>Accompagnement généré<br>Une association ou une école |

## 2. Les possibilités techniques

Ce qui se passe derrière l’écran, pour savoir ce que l’interface peut promettre. Ces modèles se combinent : un site statique peut avoir un petit service à côté et s’installer comme une application.

| Modèle | Comment | Ce que ça permet | Ce que l’interface doit gérer |
|---|---|---|---|
| **Statique, comme aujourd’hui** | Le site est généré (Lume, bientôt Eleventy) ; les web components et le stockage du navigateur ajoutent l’interactivité. | Recherche par index (Pagefind), journal et listes en local, liens vers un passage, lecteurs enrichis. Pages rapides, hors ligne possible, rien à surveiller. | Dire que journal et listes restent sur cet appareil ; rien ne s’écrit sans passer par l’admin. |
| **Statique + un petit service** | Les pages restent générées ; un service (Go ou Node, sur le Pi, comme users ou l’admin) stocke les données de chaque personne et écrit les chansons. Les composants l’interrogent avec la session de users. | Édition au téléphone, capture rapide, synchronisation, liens de partage, groupes. Si le service tombe, les pages s’affichent quand même (dépendance dégradable). | Un état « en ligne / hors ligne / dégradé » à montrer ; des données à sauvegarder. |
| **Pages rendues par un serveur** | Le serveur fabrique chaque page selon la personne ou le groupe (comme songbook, qui génère ses pages en Java). | Ce qu’on voit dépend de qui on est (groupes fins, commentaires, collaboration) ; plus de reconstruction du site. | Contraire au principe « statique d’abord » : une décision est à écrire ; un service vital de plus. |
| **Application installable (PWA)** | Un service worker garde les pages et les médias ; l’application s’installe sur le téléphone. | Réviser sans réseau (en atelier, en voiture), commandes sur l’écran verrouillé, « Ouvrir avec… ». | Des états de cache à montrer (téléchargé, à mettre à jour) ; de la place disque. |
| **Calcul dans le navigateur** | Web Audio, WebAssembly, WebGPU : tempo, sections, enregistrement, découpe, et même séparation des pistes, sur l’appareil. | Rien ne part sur un serveur ; résultats immédiats sur un bon appareil. | Lent sur les vieux téléphones ; prévoir une progression et un repli. |
| **Calcul lourd sur un serveur** | Python et modèles d’IA (Demucs, Oemer, Basic Pitch) dans Docker : trop lourd pour un Pi 4, le carnet propose une petite machine louée. | Transcription, séparation des pistes, analyse de structure. | Des tâches longues (file d’attente, progression, notification à la fin) ; un coût mensuel. |

## 3. En cours ou prêt à démarrer

Des issues ouvertes ou une spécification écrite : ce qui arrive en premier.

### Page d’un atelier en sections

Priorité **haute** · faisabilité **simple** · au build du site · source : spec page-atelier.md, issue learnsong #8

Ranger les chansons d’un atelier en sections choisies (« répertoire », « envisagées »), dans un ordre et avec une note, sans perdre la découverte automatique des nouvelles chansons.

**Pour l’interface** : Des sections nommées et ordonnées, une note courte par chanson. Plus tard : glisser une chanson d’une section à l’autre dans l’admin, ranger par danse, une section en tableau.

### Lecteurs fiables et accessibles

Priorité **haute** · faisabilité **simple** · dans le navigateur · source : issues components #13 à #22, ux/future-improvements.md

Corriger les dix défauts ouverts des lecteurs (vitesse, boucles inversées, hauteur non appliquée, saisie des temps, plusieurs YouTube, stockage) et alléger leur interface.

**Pour l’interface** : Un libellé à chaque commande, des noms de boucle lisibles, un panneau d’édition sur une seule ligne, plusieurs lecteurs YouTube sur une même page qui démarrent tous, « Zoom sur la boucle » distinct de l’échelle de la forme d’onde.

### Formats de contenu, songbook comme source de learnsong

Priorité **haute** · faisabilité **moyenne** · au build du site · source : sprint 8 (issue carnet #8), décision 0003

Trancher la notation des grilles (SNHT) et des arrangements, et faire lire songbook par learnsong (un fournisseur de chansons avec cache, comme les leçons).

**Pour l’interface** : Un bloc « paroles et accords » dans la page de chanson de learnsong, avec la transposition de songbook ; une origine visible (« depuis songbook »).

### Sortir de Deno : choisir l’architecture de learnsong

Priorité **haute** · faisabilité **moyenne** · changement de modèle · source : décision 0013, issues learnsong #5 et #6, users #4

Avant octobre 2027 : le site passe de Lume à Eleventy, l’admin de Deno à Node ou Go, le frontend de users à Node.

**Pour l’interface** : Rien de visible, mais c’est le bon moment pour décider jusqu’où learnsong devient dynamique (voir « Les possibilités techniques »).

### Les composants qui manquent

Priorité **moyenne** · faisabilité **simple** · dans le navigateur · source : issues components #8 et #11, analyse des besoins (juillet 2026)

Champ à pastilles, bascule de thème, bouton de la personne connectée, notifications, menu mobile.

**Pour l’interface** : Les briques de la coquille commune : identité et menu de compte partout, messages éphémères, navigation au pouce.

### Interfaces en français

Priorité **moyenne** · faisabilité **simple** · au build du site · source : issue components #12

Une version française du site des composants et de l’aide des lecteurs ; learnsong et songbook sont aussi en anglais aujourd’hui. Les libellés passent par un dictionnaire.

**Pour l’interface** : Un choix de langue, des libellés qui tiennent en français (souvent plus longs).

### Console d’exploitation

Priorité **moyenne** · faisabilité **moyenne** · petit service · source : sprint 11 (issue carnet #11), décision 0006

service-monitoring montre d’un coup d’œil l’état de chaque service, les versions déployées, les CI et le jalon en cours.

**Pour l’interface** : Un tableau de bord d’états (ok, dégradé, en panne, avec la raison), dans les styles communs : le même vocabulaire de santé que les sites.

## 4. Sans serveur : le site statique et le navigateur suffisent

Faisable avec l’architecture d’aujourd’hui : un index construit avec le site, des web components, le stockage du navigateur.

### Recherche et étiquettes transverses

Priorité **haute** · faisabilité **simple** · au build du site · source : idees.md n°4, audio-looper.md 2.2

Chercher dans toutes les chansons, filtrer par étiquette (#irish, #gavotte, #difficile), style, atelier, année. Le carnet utilise déjà Pagefind : un index construit avec le site.

**Pour l’interface** : Une recherche présente partout, des filtres en pastilles, une page de résultats commune.

### Journal de pratique

Priorité **haute** · faisabilité **simple** · dans le navigateur · source : idees.md n°2

Noter ce qu’on a travaillé, quand, avec quelles boucles ; le voir en frise. Dans le navigateur d’abord, synchronisé ensuite.

**Pour l’interface** : Un geste « j’ai travaillé ça » sur la page de chanson et dans le lecteur ; une vue frise ; des repères simples (dernière fois, régularité).

### Vitesse progressive

Priorité **moyenne** · faisabilité **simple** · dans le navigateur · source : idees.md n°8

La boucle démarre à 70 % de la vitesse et gagne 5 % à chaque tour (estimé à une demi-journée).

**Pour l’interface** : Un réglage départ, pas et cible dans le lecteur, la vitesse courante bien visible.

### Liens vers un passage

Priorité **moyenne** · faisabilité **simple** · dans le navigateur · source : audio-looper.md 3.4 (API pour pédagogues)

Une adresse qui ouvre une chanson sur une boucle et à une vitesse données : pour s’envoyer un exercice, pour un professeur.

**Pour l’interface** : Un bouton « copier le lien de ce passage » dans le lecteur.

### Lecteurs intégrés au téléphone

Priorité **moyenne** · faisabilité **simple** · dans le navigateur · source : audio-looper.md 1.2 et 2.3

Commandes sur l’écran verrouillé et boutons du casque (Media Session), transitions douces entre les vues (View Transitions).

**Pour l’interface** : Titre et illustration de la chanson sur l’écran verrouillé ; des transitions qui gardent le repère.

### songbook en scène

Priorité **moyenne** · faisabilité **simple** · dans le navigateur · source : README de songbook, revue de juillet 2026

Défilement automatique, réglages d’affichage (taille, colonnes, police), import et export de tout le recueil, collections gardées dans le navigateur.

**Pour l’interface** : Un mode pupitre plein écran, lisible de loin, avec la vitesse de défilement à portée de main.

### Saisir une grille au téléphone

Priorité **moyenne** · faisabilité **moyenne** · dans le navigateur · source : notation-system.md § 8

Taper « C, Am, F, G » et obtenir « | C | Am | F | G | » ; une palette des accords de la tonalité.

**Pour l’interface** : Un clavier d’accords contextuel au-dessus du clavier du téléphone.

### Tables d’arrangement interactives

Priorité **moyenne** · faisabilité **moyenne** · dans le navigateur · source : song-arrangement.md, après le sprint 8

Le déroulé d’un morceau (qui joue quoi, dans quel ordre) en composant : replier par air, couleurs par rôle, « seulement la flûte ».

**Pour l’interface** : Un tableau qui se filtre par instrument et se replie, lisible sur mobile.

## 5. Avec un petit service : learnsong devient plus dynamique

Il faut un service qui écrit, garde des données par personne ou distribue des droits. Le Pi suffit (users et l’admin en sont déjà).

### Éditer depuis le téléphone

Priorité **haute** · faisabilité **moyenne** · petit service · source : idees.md n°1, lesson-sync-dashboard.md

Le premier irritant noté dans le carnet : mettre à jour learnsong sans ordinateur. L’admin et son API existent, pensées pour le bureau.

**Pour l’interface** : Une édition pensée pour le pouce : formulaires courts, envoi de fichiers depuis le téléphone, aperçu immédiat.

### Capture rapide pendant le cours

Priorité **haute** · faisabilité **moyenne** · petit service · source : idees.md n°3

Enregistrer un mémo audio et taper deux notes pendant l’atelier ; ranger plus tard.

**Pour l’interface** : Un bouton « enregistrer » toujours accessible sur mobile, une boîte de brouillons à ranger dans une chanson.

### Liens de partage révocables

Priorité **moyenne** · faisabilité **moyenne** · petit service · source : lesson-sync-dashboard.md (solution B), décision 0016

Aujourd’hui on partage en déplaçant une chanson dans share/. Demain : un lien à jeton par page, avec expiration et révocation.

**Pour l’interface** : Un dialogue « Partager » avec les liens actifs, leur date et un bouton pour les révoquer ; un badge de visibilité sur chaque page (privée, lien, groupe, publique).

### Partager avec un groupe

Priorité **moyenne** · faisabilité **moyenne** · petit service · source : idees.md, novembre 2026

Une invitation personnelle par lien, une session longue par appareil, la révocation ; les groupes dans users, la correspondance groupe ↔ contenus dans learnsong. Sans engagement : la décision 0002 serait à revoir.

**Pour l’interface** : Gérer un groupe et ses invités, voir leurs appareils ; une page « Ton prénom ? » pour demander l’accès ; un invité ne voit que son groupe.

### Synchroniser entre appareils

Priorité **moyenne** · faisabilité **moyenne** · petit service · source : idees.md n°10, audio-looper.md 3.1

Boucles, étiquettes, journal et listes partagés entre le téléphone et l’ordinateur.

**Pour l’interface** : Un état de synchronisation discret (à jour, en attente, hors ligne, conflit).

### Listes de répétition

Priorité **moyenne** · faisabilité **moyenne** · petit service · source : idees.md n°7, README de songbook, issue components #10

Des suites ordonnées de chansons avec leurs boucles préréglées, pour une répétition ou un bal ; les collections de songbook.

**Pour l’interface** : Un constructeur de liste (glisser, réordonner), une lecture enchaînée.

### Suivre les leçons

Priorité **moyenne** · faisabilité **moyenne** · petit service · source : lesson-sync-dashboard.md, roadmap de learnsong (phase 5)

L’état de chaque leçon importée (à jour, ancienne, locale), la synchronisation en tâche de fond, l’alerte des nouvelles leçons ; plus tard Nextcloud et Google Drive comme sources.

**Pour l’interface** : Des états visibles sur les chansons, une notification quand un cours a changé.

### Comptes et sécurité

Priorité **moyenne** · faisabilité **moyenne** · petit service · source : revue users de juillet 2026, décision 0016, issue users #2

Des rôles par site, la connexion par clé d’accès (passkeys), un journal des connexions, songbook qui vérifie lui-même le jeton.

**Pour l’interface** : Un écran « mes appareils et mes clés », une connexion sans mot de passe.

## 6. Ambitieux : à prototyper d’abord

Audio dans le navigateur, application installable, intelligence artificielle : un prototype dira si c’est tenable.

### Lecteur installable et hors ligne

Priorité **moyenne** · faisabilité **moyenne** · dans le navigateur · source : audio-looper.md

Une application web installable : bibliothèque, historique, « Ouvrir avec… », recevoir un lien YouTube partagé, stockage persistant des fichiers.

**Pour l’interface** : Trois vues : accueil (Enregistrer, Ouvrir, Récents), bibliothèque, lecteur en plein écran ; un état hors ligne clair.

### S’enregistrer et se réécouter

Priorité **moyenne** · faisabilité **moyenne** · dans le navigateur · source : in-browser-editing.md (priorité 1), audio-looper.md 1.3

Enregistrer sa propre prise et la boucler aussitôt, sur mobile (AAC ou Opus selon l’appareil).

**Pour l’interface** : Un bouton d’enregistrement dans le lecteur, comparer sa prise et l’original.

### Boucles suggérées

Priorité **moyenne** · faisabilité **moyenne** · dans le navigateur · source : audio-analysis.md (phase 1)

Détecter tempo, tonalité, silences et parties (A, B) dans le navigateur (Essentia.js), pour proposer les boucles.

**Pour l’interface** : Des suggestions « Partie A », « Partie B » à accepter ou corriger, le tempo affiché.

### Couper et normaliser dans le navigateur

Priorité **basse** · faisabilité **moyenne** · dans le navigateur · source : in-browser-editing.md

Isoler une phrase, égaliser le volume, exporter un extrait, sans serveur (WebCodecs, Web Audio).

**Pour l’interface** : Sélectionner sur la forme d’onde, puis « exporter cet extrait ».

### Suites de danse

Priorité **basse** · faisabilité **moyenne** · calcul lourd · source : audio-analysis.md § 3 et § 6

Grouper les airs d’une suite (ton simple, tamm-kreiz, ton double), les enchaîner, homogénéiser leur volume (EBU R128).

**Pour l’interface** : Une « suite » comme objet de la page : ses airs dans l’ordre, une lecture enchaînée.

### Forme d’onde des longues vidéos

Priorité **basse** · faisabilité **moyenne** · dans le navigateur · source : issues components #7 et #20

Aujourd’hui limitée aux 12 premiers Mo de la vidéo : la calculer en entier ou par morceaux.

**Pour l’interface** : Une forme d’onde qui se complète progressivement.

### Transcrire en ABC

Priorité **basse** · faisabilité **difficile** · calcul lourd · source : transcription.md, mesnos/partition-to-abc

Une photo de partition ou un enregistrement deviennent de la notation ABC (prototype : partition-to-abc).

**Pour l’interface** : Un bouton « Transcrire », une tâche longue avec sa progression, un résultat à corriger.

### Séparer les pistes

Priorité **basse** · faisabilité **difficile** · calcul lourd · source : audio-analysis.md § 2, in-browser-editing.md § 2

Mélodie seule, accompagnement seul, ou « moins un » (son instrument retiré) : sur un serveur dédié ou dans le navigateur avec WebGPU.

**Pour l’interface** : Un sélecteur de piste dans le lecteur, une tâche longue avec sa progression.

## 7. Horizon lointain : les idées audacieuses

Pas au programme, parfois farfelues, mais à ne pas rendre impossibles : l’interface doit pouvoir les accueillir un jour.

### Pédale pour les boucles

Priorité **un jour** · faisabilité **moyenne** · dans le navigateur · source : audio-looper.md 3.4

Poser A et B sans lâcher l’instrument, avec une pédale MIDI ou Bluetooth.

**Pour l’interface** : Un écran d’association des pédales, un retour visuel fort quand la boucle change.

### Métronome visuel et accordeur

Priorité **un jour** · faisabilité **moyenne** · dans le navigateur · source : audio-looper.md 3.4

Un métronome calé sur la boucle, un accordeur discret.

**Pour l’interface** : Des outils qui se superposent au lecteur sans le masquer.

### Diagrammes d’accords et tablatures

Priorité **un jour** · faisabilité **moyenne** · dans le navigateur · source : README de songbook

Doigtés guitare et piano à côté des accords, tablatures.

**Pour l’interface** : Un diagramme au toucher d’un accord, un choix d’instrument.

### « Envoyer au lecteur » depuis le web

Priorité **un jour** · faisabilité **moyenne** · dans le navigateur · source : audio-looper.md 3.4

Une extension de navigateur qui envoie l’audio d’une page (YouTube, SoundCloud…) vers le lecteur.

**Pour l’interface** : Arriver dans le lecteur avec le média déjà chargé et une boucle à créer.

### Packs d’exercices

Priorité **un jour** · faisabilité **moyenne** · petit service · source : audio-looper.md 3.4, vision initiale

Des packs (audio, boucles préréglées, notes pédagogiques) préparés par un professeur et partagés.

**Pour l’interface** : Une page de pack, « ajouter à ma bibliothèque ».

### Espaces de collaboration

Priorité **un jour** · faisabilité **difficile** · changement de modèle · source : audio-looper.md 3.2, vision initiale (juin 2026)

Un espace par concert, atelier ou groupe : un fil de médias, des onglets par événement, des commentaires, l’édition à plusieurs.

**Pour l’interface** : Un sélecteur d’espace, un fil d’activité, la frontière visible entre personnel et partagé.

### Accompagnement généré

Priorité **un jour** · faisabilité **difficile** · dans le navigateur · source : notation-system.md (objectif 2)

Jouer la grille SNHT (basse, accords, rythme) pour s’accompagner.

**Pour l’interface** : Un lecteur de grille avec tempo, style et instruments.

### Une association ou une école

Priorité **un jour** · faisabilité **difficile** · changement de modèle · source : audio-looper.md 3.3 et 4, vision initiale

Servir 200 à 400 élèves : stockage, sauvegardes, coûts ; un modèle de dons (HelloAsso, Open Collective), une présence en magasin d’applications.

**Pour l’interface** : Des rôles (élève, professeur, administrateur), des espaces par classe.

## 8. Ce que l’interface doit prévoir dès maintenant

Les places à réserver dans l’ergonomie, pour que ces pistes trouvent leur place sans tout refaire.

- **Une coquille commune**. Un en-tête partagé par tous les sites : la marque, la navigation entre outils, la recherche, la personne connectée et son menu, un indicateur d’état (hors ligne, synchronisation, service dégradé). *Pour : recherche, compte, synchronisation, PWA.*
- **Une page de chanson modulaire**. La page est déjà faite de blocs (texte, grille, lecteurs, leçon). Des blocs viendront : paroles de songbook, journal, arrangement, transcription, pistes séparées, suite. Chaque bloc est une carte qui sait se replier et se déplacer. *Pour : formats, arrangement, transcription, suites.*
- **Un mode pratique**. Plein écran, gros boutons, le lecteur au centre ; le même esprit que le pupitre de songbook. C’est là que viendront vitesse progressive, enregistrement, pédale, métronome. *Pour : lecteur installable, enregistrement, pédale.*
- **La visibilité de chaque contenu**. Un badge (privé, lien, groupe, public) et un seul dialogue « Partager » pour les liens et les groupes. *Pour : liens révocables, groupes, espaces.*
- **Les tâches longues**. Un motif commun : lancer, suivre la progression, être prévenu à la fin, retrouver le résultat. *Pour : transcription, séparation, synchronisation des leçons.*
- **La capture au pouce**. Sur mobile, enregistrer ou noter en un geste depuis n’importe où, et une boîte de brouillons. *Pour : capture rapide, édition au téléphone.*
- **Les collections**. Ateliers en sections, étiquettes, listes de répétition, recueils : une même famille de listes ordonnées, filtrables et réordonnables. *Pour : page d’atelier, recherche, listes.*
- **Les états dégradés et vides**. Hors ligne, service indisponible, rien encore ici, accès refusé : des états dessinés, qui disent quoi faire. *Pour : principe des dépendances dégradables.*

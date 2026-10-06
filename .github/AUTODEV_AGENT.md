# Contrat agent — développement autonome Quantum Deep Calm AI

Ce document définit la frontière entre l'agent de développement et les contrôles GitHub.

## Source de vérité

L'ordre de travail est défini dans `.github/autodev-roadmap.json`.

L'agent prend un seul item `ready` à la fois. Il ne saute pas une priorité pour ajouter une fonction plus spectaculaire ou plus facile.

## Convention de branche et de PR

Pour l'item `QDC-001`, utiliser une branche de la forme :

`autodev/qdc-001-audio-engine`

Le titre de la pull request doit commencer exactement par :

`[AUTODEV QDC-001]`

Le corps de la PR doit contenir `Closes #<issue>` afin que l'issue de travail soit fermée après fusion.

## Obligations de l'agent

Avant d'ouvrir la PR :

1. lire l'item de roadmap et ses critères d'acceptation ;
2. inspecter le code existant avant de modifier l'architecture ;
3. ne pas annoncer comme implémenté ce qui n'est pas réellement branché ;
4. ajouter ou adapter des tests pour le comportement modifié ;
5. exécuter au minimum les tests unitaires concernés et le build Android ;
6. garder le cœur commercial local-first tant qu'un item ne prévoit pas explicitement une dépendance réseau ;
7. ne pas ajouter de TODO, FIXME, HACK ou XXX dans `app/src/main` ;
8. ne pas contourner Lint, les tests, CodeQL, Repo Guard ou l'émulation Android ;
9. ne pas diminuer les contrôles pour faire passer une PR ;
10. ne jamais écrire de secret dans le dépôt.

## Auto-merge autorisé

Une PR `AUTODEV` peut être fusionnée automatiquement seulement lorsque :

- Android developer qualification est verte ;
- CodeQL Java/Kotlin est vert ;
- Repo Guard est vert ;
- aucun autre check terminé n'est en échec ;
- la PR est fusionnable ;
- aucun chemin sensible n'a été modifié.

## Chemins sensibles — auto-merge interdit

Toute modification touchant l'une des zones suivantes doit rester hors auto-merge :

- `.github/workflows/` ;
- `.github/actions/` ;
- `gradle/wrapper/` ;
- `gradle/libs.versions.toml` ;
- `build.gradle.kts` ou `app/build.gradle.kts` ;
- `gradle.properties` ;
- `app/src/main/AndroidManifest.xml` ;
- configuration de sécurité réseau ;
- signature, keystore, release Google Play ou secrets ;
- politique de confidentialité ou déclarations réglementaires lorsqu'elles changent le périmètre de collecte.

Ces changements peuvent être développés automatiquement, mais leur fusion exige une revue explicite car ils modifient la chaîne de confiance, les permissions, les dépendances ou le périmètre réglementaire.

## Interdictions

Le pipeline ne doit jamais :

- pousser directement du code fonctionnel généré par un agent vers `main` ;
- auto-fusionner une PR qui modifie ses propres contrôles de sécurité ;
- masquer un test en échec ;
- remplacer une preuve physique requise par une preuve d'émulateur ;
- publier sur Google Play automatiquement sans la chaîne de release dédiée et ses preuves externes.

## Résultat attendu

Le développement devient une boucle contrôlée :

Roadmap -> issue de travail -> agent -> branche `autodev/*` -> PR -> CI + émulateur + sécurité -> auto-merge si sûre -> item suivant.

La boucle avance automatiquement sur le code courant, mais les frontières de sécurité, permissions, dépendances et publication restent fail-closed.

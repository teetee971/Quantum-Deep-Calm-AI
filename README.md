# Quantum Deep Calm AI

Application Android de méditation et de bien-être, construite en Kotlin avec Jetpack Compose.

Le dépôt suit une règle stricte : aucune fonctionnalité n’est présentée comme opérationnelle tant qu’elle n’est pas réellement implémentée et validée par les contrôles du projet.

## État actuel

Implémenté dans le build Android actuel :

- interface native Jetpack Compose ;
- navigation Accueil / Bibliothèque / Sommeil / Progression / Lecteur ;
- écran d’accueil Quantum Deep Calm avec sessions Calm, Alpha Relaxation, Theta Meditation, Delta Concentration et Schumann ;
- favoris persistants enregistrés localement sur l’appareil ;
- bibliothèque affichant les sessions réellement ajoutées aux favoris ;
- progression persistante basée uniquement sur les démarrages de lecture confirmés par Media3 ;
- date de dernière lecture confirmée ;
- lecteur audio Media3 ;
- ambiance WAV générée localement et lisible hors connexion ;
- lecture, pause et remise au début de la session ;
- tests unitaires et smoke test sur appareil Android géré ;
- test instrumenté de persistance des favoris ;
- génération d’APK debug et d’AAB release par CI ;
- analyse CodeQL, contrôle GitGuardian et politiques de dépôt.

Non encore implémenté dans le produit :

- téléchargements de contenus supplémentaires ;
- programmes Sommeil ;
- objectifs, séries de régularité et historique détaillé ;
- catalogue audio complet avec une piste distincte par session ;
- publication Google Play certifiée sur appareil réel.

L’interface distingue les fonctions opérationnelles des modules encore en préparation afin d’éviter tout faux état fonctionnel.

## Prérequis

- JDK 17 ;
- Android SDK 37 ;
- Gradle Wrapper versionné dans le dépôt.

Le wrapper du dépôt est la référence de build. Il ne faut pas substituer une installation Gradle locale au wrapper pour valider une release.

## Build local

```bash
./gradlew --no-daemon lintDebug lintRelease testDebugUnitTest assembleDebug bundleRelease
```

APK debug :

```text
app/build/outputs/apk/debug/
```

AAB release :

```text
app/build/outputs/bundle/release/
```

## Tests

Tests unitaires :

```bash
./gradlew --no-daemon testDebugUnitTest
```

Smoke test Android géré :

```bash
./gradlew --no-daemon pixel2api35DebugAndroidTest
```

Le smoke test vérifie notamment que le lecteur hors ligne se connecte, démarre effectivement la lecture et alimente l’écran Progression uniquement après confirmation réelle de Media3. Les tests instrumentés valident également la persistance des favoris et des compteurs de progression.

## CI et contrôles

Les workflows GitHub Actions couvrent :

- intégrité du dépôt et dette source interdite ;
- Android Lint ;
- tests unitaires ;
- APK debug ;
- AAB release ;
- smoke test sur appareil géré ;
- CodeQL Java/Kotlin ;
- GitGuardian ;
- contrôle des fichiers interdits ou surdimensionnés ;
- autoloop Android planifiée pour les corrections déterministes sûres.

Les marqueurs `TODO`, `FIXME`, `HACK` et `XXX` sont refusés dans `app/src/main`.

## Sécurité et confidentialité

La configuration Android actuelle :

- désactive les sauvegardes applicatives automatiques ;
- interdit le trafic HTTP en clair ;
- n’intègre aucun secret de signature dans le dépôt ;
- fournit les secrets de signature de release uniquement via GitHub Actions.

Le workflow `Play Release Bundle` exige les quatre secrets de signature Android avant de produire un AAB signé. Leur présence n’est pas supposée : le workflow échoue explicitement s’ils manquent.

## Release Google Play

Le workflow manuel `Play Release Bundle` :

1. valide la présence des secrets de signature ;
2. reconstruit le projet proprement ;
3. exécute Lint et les tests unitaires ;
4. génère l’AAB release ;
5. vérifie sa signature avec `jarsigner` ;
6. publie l’AAB signé comme artefact GitHub Actions.

Un AAB généré par CI ne signifie pas à lui seul que l’application est certifiée « prête production ». La validation finale exige encore les tests réels prévus avant publication.

## Architecture

Voir `docs/ARCHITECTURE.md` pour les règles de séparation entre :

- code Android exécutable ;
- automatisation CI/CD ;
- scripts de maintenance ;
- médias et contenus externes.

## Licence

MIT. Voir `LICENSE`.

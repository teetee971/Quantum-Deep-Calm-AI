# Quantum Deep Calm AI

Application Android de méditation et de bien-être, construite en Kotlin avec Jetpack Compose et conçue pour fonctionner hors ligne.

Le dépôt suit une règle stricte : aucune fonctionnalité n’est présentée comme opérationnelle tant qu’elle n’est pas réellement implémentée et validée par les contrôles du projet.

## État actuel

Implémenté dans le build Android actuel :

- interface native Jetpack Compose ;
- navigation Accueil / Bibliothèque / Sommeil / Progression / Lecteur ;
- cinq sessions réellement sélectionnables : Calm, Alpha Relaxation, Theta Meditation, Delta Concentration et Schumann ;
- un profil audio déterministe distinct pour chaque session, généré localement sans téléchargement ;
- sélection Sommeil composée de sessions réellement jouables et sans promesse d’effet sur le sommeil ;
- favoris persistants enregistrés localement sur l’appareil ;
- bibliothèque affichant et lançant les sessions réellement ajoutées aux favoris ;
- progression persistante basée uniquement sur les démarrages de lecture confirmés par Media3 ;
- mémorisation de la dernière session réellement lue et de sa date ;
- lecteur audio Media3 avec lecture, pause et remise au début ;
- politique de confidentialité accessible dans l’application ;
- aucune permission Internet dans le manifeste Android ;
- tests unitaires et smoke tests sur appareil Android géré ;
- tests instrumentés de persistance des favoris et de la progression ;
- génération d’APK debug et d’AAB release par CI ;
- analyse CodeQL, contrôle GitGuardian et politiques de dépôt.

Non encore prouvé hors du dépôt :

- configuration effective des secrets de signature Google Play ;
- exécution réussie du workflow manuel `Play Release Bundle` avec la vraie clé d’upload ;
- validation de l’AAB signé dans Google Play Console ;
- test physique final sur un téléphone Android réel ;
- accès production Play Console et éventuelles exigences de test fermé liées au type/date du compte développeur ;
- protection/ruleset `main` vérifiée et imposée côté GitHub.

Les objectifs avancés, séries de régularité et analyses détaillées ne font pas partie du cœur commercial actuel et ne sont pas présentés comme disponibles.

## Architecture produit

Le produit commercial actuel est volontairement offline-first :

- pas de compte utilisateur ;
- pas de backend nécessaire au fonctionnement ;
- pas de téléchargement de catalogue ;
- pas d’analytics distant ;
- les fichiers WAV sont générés et contrôlés localement à partir de profils versionnés ;
- les favoris et la progression restent dans le stockage privé de l’application.

Cette architecture réduit les dépendances réseau et la surface de collecte de données. Toute future fonction réseau devra être traitée comme une évolution explicite avec mise à jour des déclarations de confidentialité et Google Play.

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

Le smoke test couvre notamment :

- connexion réelle au lecteur Media3 ;
- démarrage effectif d’une session hors ligne ;
- sélection d’une session de catalogue et affichage du bon contenu dans le lecteur ;
- parcours Sommeil vers une session jouable ;
- favori vers Bibliothèque et disponibilité du bouton de lecture ;
- alimentation de Progression uniquement après confirmation réelle de lecture.

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
- autoloop Android horaire et post-push sur `main` pour les corrections strictement déterministes.

L’autoloop n’applique pas automatiquement les quick-fixes Android Lint aux sources. Lint reste un gate bloquant : une correction source doit compiler et repasser les tests avant fusion.

Les marqueurs `TODO`, `FIXME`, `HACK` et `XXX` sont refusés dans `app/src/main`.

## Sécurité et confidentialité

La configuration Android actuelle :

- désactive les sauvegardes applicatives automatiques ;
- interdit le trafic HTTP en clair ;
- expose le `MediaSessionService` comme requis pour les contrôles média système, tout en refusant dans `onGetSession()` les contrôleurs qui ne sont ni l’application elle-même ni reconnus fiables par Media3 ;
- ne demande pas la permission Internet ;
- n’intègre aucun secret de signature dans le dépôt ;
- fournit les secrets de signature de release uniquement via GitHub Actions et uniquement aux étapes qui en ont besoin.

Le workflow `Play Release Bundle` exige les quatre secrets de signature Android avant de produire un AAB signé. Leur présence n’est pas supposée : le workflow échoue explicitement s’ils manquent.

## Release Google Play

Le workflow manuel `Play Release Bundle` :

1. refuse toute exécution de release signée qui ne provient pas de `main` ;
2. vérifie que le checkout correspond exactement au SHA de la release ;
3. valide la présence des secrets de signature sans les exposer aux autres actions du job ;
4. reconstruit le projet, exécute Lint et les tests unitaires, puis vérifie qu’au moins un test a réellement été exécuté ;
5. exécute le smoke test sur appareil Android géré avant toute signature ;
6. décode temporairement le keystore uniquement après les validations ;
7. génère l’AAB signé et vérifie sa signature avec `jarsigner -verify -strict` ;
8. publie l’AAB signé comme artefact GitHub Actions puis supprime le keystore décodé du runner.

Un AAB généré par CI ne signifie pas à lui seul que l’application est certifiée « prête production ». Le statut production exige également les preuves externes listées plus haut.

## Documentation

Voir `docs/ARCHITECTURE.md` pour les règles de séparation entre code Android, automatisation, scripts et contenus.

## Licence

MIT. Voir `LICENSE`.

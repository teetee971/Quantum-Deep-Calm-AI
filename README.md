# Quantum Deep Calm AI

Application Android native de méditation et bien-être.

## État réellement vérifié

La base Android est en Kotlin + Jetpack Compose. Le lecteur hors ligne fonctionne avec Media3 / ExoPlayer et une ambiance calme générée localement. La lecture, la pause, le redémarrage et la connexion au MediaSession sont couverts par des tests unitaires et un smoke test Android sur émulateur.

Les modules Bibliothèque, Sommeil, Progression, respiration guidée, méditations guidées personnalisées et concentration guidée restent explicitement indiqués comme **en préparation** dans l’interface. Ils ne doivent pas être présentés comme fonctionnels avant leurs propres tests d’acceptation.

## Build reproductible

Le dépôt utilise le Gradle Wrapper 9.6.0 :

```bash
./gradlew lintDebug lintRelease testDebugUnitTest assembleDebug bundleRelease
```

La CI refuse notamment :
- les fichiers simulés ou marqueurs de dette dans `app/src/main` ;
- un `targetSdk` inférieur au minimum Google Play contrôlé par le pipeline ;
- un job de tests unitaires qui exécute zéro test ;
- un build APK/AAB en échec.

## Tests Android

Le smoke test de device managé lance l’application sur un Pixel 2 / API 35, ouvre le lecteur, attend la connexion MediaSession, démarre la lecture et vérifie l’état de lecture.

## Sécurité

Le trafic HTTP en clair est désactivé. Les sauvegardes Android sont désactivées. Le MediaSession refuse les contrôleurs externes non fiables. CodeQL analyse le code Java/Kotlin avec une recompilation forcée pour éviter les faux-verts liés au cache Gradle.

## Google Play

Le workflow `Play Release Bundle` construit un AAB signé uniquement lorsque les vrais secrets de clé d’upload sont configurés :

- `ANDROID_UPLOAD_KEYSTORE_BASE64`
- `ANDROID_UPLOAD_STORE_PASSWORD`
- `ANDROID_UPLOAD_KEY_ALIAS`
- `ANDROID_UPLOAD_KEY_PASSWORD`

Aucune clé n’est stockée dans le dépôt. L’application ne doit être déclarée « prête à publier sur Google Play » qu’après validation du bundle signé et des exigences Play Console.
